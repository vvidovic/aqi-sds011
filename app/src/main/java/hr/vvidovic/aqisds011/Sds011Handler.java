package hr.vvidovic.aqisds011;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.time.LocalDateTime;
import java.util.Arrays;

import hr.vvidovic.aqisds011.data.AppDatabase;
import hr.vvidovic.aqisds011.data.Measurement;
import hr.vvidovic.aqisds011.log.AqiLog;

public class Sds011Handler {
    private static final String TAG = Sds011Handler.class.getSimpleName();

    public static final Sds011Handler instance = new Sds011Handler();

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    // SDS011 identifiers
    private static final int PRODUCT_ID = 29987;
    private static final int VENDOR_ID = 6790;

    private static final byte CMD_MODE = 2;
    private static final byte CMD_QUERY_DATA = 4;
    private static final byte CMD_DEVICE_ID = 5;
    private static final byte CMD_SLEEP = 6;
    private static final byte CMD_FIRMWARE = 7;
    private static final byte CMD_WORKING_PERIOD = 8;

    private static final byte MODE_ACTIVE = 0;
    private static final byte MODE_QUERY = 1;

    private static final byte SLEEP_YES = 0;
    private static final byte SLEEP_NOT = 1;

    private static final byte MODE_GET = 0;
    private static final byte MODE_SET = 1;

    private static final byte PERIOD_CONTINUOUS = 0;

    private UsbManager usbManager;
    private UsbSerialDevice serial;

    private AppDatabase db;
    private Sds011ViewModel model;

    // 0：continuous(default) - report each second
    // 1-30minute：【work  30  seconds and sleep n*60-30 seconds】- report after 30 seconds working
    private byte workPeriodMinutes = 0;
    private byte workPeriodReportedMinutes = 0;

    public void init(Activity activity, Sds011ViewModel model, AppDatabase db) {
        AqiLog.i(TAG, "init()");
        this.model = model;
        this.db = db;
        initUsb(activity);
    }

    public boolean start() {
        AqiLog.i(TAG, "start()");
        if(serial != null) {
            serial.write(constructCommand(CMD_SLEEP, MODE_SET, SLEEP_NOT));

            return true;
        }
        return false;
    }

    public boolean stop() {
        AqiLog.i(TAG, "stop()");
        if(serial != null) {
            serial.write(constructCommand(CMD_SLEEP, MODE_SET, SLEEP_YES));
            return true;
        }
        return false;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void initUsb(Activity activity) {
        AqiLog.d(TAG, "initUsb()");
        usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);

        UsbDevice device = activity.getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
        AqiLog.d(TAG, "device: %s", device);
        if (device != null) {
            // Device was attached, no need to ask for permissions
            connect(device);
        }
        else {
            // List all devices, find DSS011 by product id and ask for permission
            PendingIntent permissionIntent = PendingIntent.getBroadcast(activity, 0,
                    new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.registerReceiver(usbReceiver, filter, Context.RECEIVER_EXPORTED);
            }
            else {
                activity.registerReceiver(usbReceiver, filter);
            }

            for (UsbDevice usbDevice : usbManager.getDeviceList().values()) {
                if (usbDevice.getProductId() == PRODUCT_ID && usbDevice.getVendorId() == VENDOR_ID) {
                    usbManager.requestPermission(usbDevice, permissionIntent);
                    break;
                }
            }
        }
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            AqiLog.i(TAG, "BroadcastReceiver() onReceive(), intent: %s", intent);
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            connect(device);
                        }
                    }
                    else {
                        AqiLog.d(TAG, "permission denied for device %s", device);
                    }
                }
            }
        }
    };

    private UsbSerialInterface.UsbReadCallback serialRespCallback = new UsbSerialInterface.UsbReadCallback() {

        @Override
        public void onReceivedData(final byte[] b)
        {
            AqiLog.i(TAG, "onReceivedData()");
            if (b.length == 10) {
                if(b[1] == (byte)0xc0) {
                    model.postDebugMsg("Measuring, wp: " + workPeriodReportedMinutes + " - " + LocalDateTime.now().toString());
                    float pm25 = (256 * Math.abs(b[3]) + Math.abs(b[2])) / 10.0f;
                    float pm10 = (256 * Math.abs(b[5]) + Math.abs(b[4])) / 10.0f;

                    Measurement m = new Measurement(pm25, pm10, model.getLocation());
                    Measurement mToSave = model.postMeasurement(m);

                    // For continuous measurements - don't save each measurement
                    if(mToSave != null) {
                        db.measurementDao().insert(mToSave);
                    }
                }
                // Sleep response
                else if(b[1] == (byte)0xc5 && b[2] == (byte)6) {
                    switch(b[4]) {
                        case 0:
                            model.postDebugMsg("Sleeping...");
                            break;
                        case 1:
                            model.postDebugMsg("Awake...");
                            serial.write(constructCommand(CMD_WORKING_PERIOD, (byte)1, workPeriodMinutes));
                            break;
                        default:
                            model.postDebugMsg("err,b[4]!=(0,1): " + Arrays.toString(b));
                    }
                }
                // Set working period response
                else if(b[1] == (byte)0xc5 && b[2] == (byte)8) {
                    workPeriodReportedMinutes = b[4];
                    model.postDebugMsg("working period: " + (int)workPeriodReportedMinutes);
                }
                // Set data reporting mode response
                else if(b[1] == (byte)0xc5 && b[2] == (byte)2) {
                    model.postDebugMsg("reporting mode: " + (int)b[4]);
                }
            }
        }
    };


    /*
    SDS011 USB related code copied from: https://github.com/milosevic81/sds011_android
    https://github.com/felHR85/UsbSerial
    https://dev.to/minkovsky/working-on-my-iot-air-quality-monitoring-setup-40a5
    https://www.banggood.com/Geekcreit-Nova-PM-Sensor-SDS011-High-Precision-Laser-PM2_5-Air-Quality-Detection-Sensor-Module-Tester-p-1144246.html?utm_source=google&utm_medium=cpc_ods&utm_campaign=nancy-197s-sdsrm-bag-all-m-content&utm_content=nancy&gclid=CjwKCAjw0vTtBRBREiwA3URt7qq2SrHrKZjl5-T8WsEyMzuyt6P0df34Mdc5w4K-pcUH1BDTgAPctBoC2MIQAvD_BwE&cur_warehouse=CN
    Communication protocol:
    https://cdn.sparkfun.com/assets/parts/1/2/2/7/5/Laser_Dust_Sensor_Control_Protocol_V1.3.pdf
    Serial communication protocol: 9600 8N1. (Rate of 9600, data bits 8, parity none, stop bits 1)
    Serial report communication cycle: 1+0.5 seconds
    Data frame (10 bytes): message header + order+ data(6 bytes) + checksum + message trailer
    Bytes |	Name	             |  Content
    ------|----------------------|----------------
      0	  | message header	     |  AA
      1	  | order	             |  C0
      2	  | data 1	             |  PM2.5 low byte
      3	  | data 2	             |  PM2.5 high byte
      4	  | data 3	             |  PM10 low byte
      5	  | data 4	             |  PM10 high byte
      6	  | data 5	             |  0(reserved)
      7	  | data 6	             |  0(reserved)
      8	  | checksum	         |  checksum
      9	  | message trailer	     |  AB
    Checksum: data 1 + data 2 + ...+ data 6
    PM2.5 data content: PM2.5 (ug/m3) = ((PM2.5 high byte*256 ) + PM2.5 low byte)/10
    PM10 data content: PM10 (ug/m3) = ((PM10 high byte*256 ) + PM10 low byte)/10
    */
    public void connect(UsbDevice device) {
        AqiLog.i(TAG, "connect(), sensor started: %s", model.isSensorStarted());

        try {
            UsbDeviceConnection usbConnection = usbManager.openDevice(device);
            serial = UsbSerialDevice.createUsbSerialDevice(device, usbConnection);

            // Serial communication protocol: 9600 8N1. (Rate of 9600, data bits 8, parity none, stop bits 1)
            serial.open();
            serial.setBaudRate(9600);
            serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
            serial.setParity(UsbSerialInterface.PARITY_NONE);
            serial.setStopBits(UsbSerialInterface.STOP_BITS_1);
            serial.setFlowControl(UsbSerialInterface. FLOW_CONTROL_OFF);


            serial.read(serialRespCallback);
            if(!model.isSensorStarted()) {
                serial.write(constructCommand(CMD_MODE, MODE_SET, MODE_ACTIVE));
                serial.write(constructCommand(CMD_WORKING_PERIOD, (byte)1, workPeriodMinutes));
                serial.write(constructCommand(CMD_SLEEP, MODE_SET, SLEEP_YES));
            }
        } catch (Exception e) {
            model.postDebugMsg("Error connecting to SDS011: " + e.getLocalizedMessage());
        }
    }

    private byte[] constructCommand(final byte cmd, final byte cmd2, final byte cmd3) {
        AqiLog.i(TAG, "constructCommand()");
        final byte[] command = new byte[19];
        command[0] = (byte)0xaa;  // head
        command[1] = (byte)0xb4;  // command 1
        command[2] = cmd;   // data byte 1
        command[3] = cmd2;  // data byte 2
        command[4] = cmd3;  // data byte 3

        command[15] = (byte)0xff;  // data byte 14 (device id byte 1)
        command[16] = (byte)0xff;  // data byte 15 (device id byte 2)

        int checksum = (cmd + cmd2 + cmd3 - 2) % 256;
        command[17] = (byte)checksum;  // checksum
        command[18] = (byte)0xab;  // tail

        return command;

    }

    public void setWorkPeriodMinutes(byte workPeriodMinutes) {
        AqiLog.i(TAG, "setWorkPeriodMinutes()");
        this.workPeriodMinutes = workPeriodMinutes;
    }
}
