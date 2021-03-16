package hr.vvidovic.aqisds011;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import hr.vvidovic.aqisds011.data.Measurement;
import hr.vvidovic.aqisds011.log.AqiLog;

public class Sds011ViewModel extends ViewModel {
    private static final String TAG = Sds011ViewModel.class.getSimpleName();

    public static final byte DEFAULT_WP_MINUTES = (byte)2;
    public static final boolean DEFAULT_WP = false;
    public static final int DEFAULT_WC_CNT = 30;
    public static final boolean DEFAULT_SENSOR_STARTED = false;

    private final MutableLiveData<Measurement> valueMeasurement = new MutableLiveData<>();
    private final MutableLiveData<String> valueMsg = new MutableLiveData<>();
    private final MutableLiveData<String> valueStatus = new MutableLiveData<>();

    private final MutableLiveData<List<Measurement>> history = new MutableLiveData<>();

    private boolean workPeriodic;
    private byte workPeriodMinutes;
    private int workContinuousAverageCount;

    private boolean sensorStarted;

    private final List<Measurement> continuousMeasurementAvgHist = new ArrayList<>();
    private Location location;
    private int locationPriority;

    public Sds011ViewModel() {
        AqiLog.i(TAG, "Sds011ViewModel()");

        valueMeasurement.setValue(new Measurement());
        valueMsg.setValue("");
        valueStatus.setValue("Stopped.");

        List<Measurement> h = new ArrayList<>();
        history.setValue(h);
    }

    public Measurement postMeasurement(Measurement m) {
        AqiLog.i(TAG, "postMeasurement(), periodic: %s", isWorkPeriodic()
                + ", wc avg cnt: " + getWorkContinuousAverageCount()
                + ", cm avg hist size: " + continuousMeasurementAvgHist.size());
        postValueMeasurement(m);

        Measurement newM = null;
        if(!isWorkPeriodic()) {
            continuousMeasurementAvgHist.add(m);
            int cCount = continuousMeasurementAvgHist.size();
            if(cCount >= getWorkContinuousAverageCount()) {
                double pm25sum = 0.0;
                double pm10sum = 0.0;
                for (Measurement cm: continuousMeasurementAvgHist) {
                    pm25sum += cm.pm25;
                    pm10sum += cm.pm10;
                }

                newM = new Measurement((float)pm25sum/cCount, (float)pm10sum/cCount);
                newM.dateTime = m.dateTime;
                newM.locAccuracy = m.locAccuracy;
                newM.locDateTime = m.locDateTime;
                newM.locLatitude = m.locLatitude;
                newM.locLongitude = m.locLongitude;
                continuousMeasurementAvgHist.clear();
            }
        }
        else {
            newM = m;
        }

        if(newM != null) {
            List<Measurement> newHist = history.getValue();

            newHist.add(newM);
            history.postValue(newHist);
        }

        return newM;
    }

    public void postMsg(String msg) {
        AqiLog.i(TAG, "postMsg()");

        valueMsg.postValue(msg);
    }

    public void postStatus(String status) {
        AqiLog.i(TAG, "postStatus()");
        valueStatus.postValue(status);
    }

    public LiveData<Measurement> getValueMeasurement() {
        AqiLog.i(TAG, "getValueMeasurement()");
        return valueMeasurement;
    }
    private void postValueMeasurement(Measurement value) {
        AqiLog.i(TAG, "postValueMeasurement()");
        valueMeasurement.postValue(value);
    }


    public LiveData<String> getValueMsg() {
        AqiLog.i(TAG, "getValueMsg()");
        return valueMsg;
    }

    public LiveData<String> getValueStatus() {
        AqiLog.i(TAG, "getValueStatus()");
        return valueStatus;
    }

    private void configureSensorWorkPeriodic(boolean workPeriodic) {
        AqiLog.i(TAG, "configureSensorWorkPeriodic(%s)", workPeriodic);
        if(workPeriodic) {
            Sds011Handler.instance.setWorkPeriodMinutes(workPeriodMinutes);
        }
        else {
            Sds011Handler.instance.setWorkPeriodMinutes((byte)0);
        }
    }
    public boolean isWorkPeriodic() {
        AqiLog.i(TAG, "isWorkPeriodic(): %s", workPeriodic);
        return workPeriodic;
    }
    public void setWorkPeriodic(boolean workPeriodic) {
        AqiLog.i(TAG, "setWorkPeriodic(%s)", workPeriodic);
        this.workPeriodic = workPeriodic;
        configureSensorWorkPeriodic(workPeriodic);
    }
    public Byte getWorkPeriodMinutes() {
        AqiLog.i(TAG, "getWorkPeriodMinutes(): %s", workPeriodMinutes);
        return workPeriodMinutes;
    }
    public void setWorkPeriodMinutes(byte workPeriodMinutes) {
        AqiLog.i(TAG, "setWorkPeriodMinutes(%s)", workPeriodMinutes);
        this.workPeriodMinutes  = workPeriodMinutes;
    }

    public int getWorkContinuousAverageCount() {
        AqiLog.i(TAG, "getWorkContinuousAverageCount(): %s", workContinuousAverageCount);
        return workContinuousAverageCount;
    }
    public void setWorkContinuousAverageCount(int workContinuousAverageCount) {
        AqiLog.i(TAG, "setWorkContinuousAverageCount(%s)", workContinuousAverageCount);
        this.workContinuousAverageCount = workContinuousAverageCount;
    }

    public void setSensorRunningState(boolean startSensor) {
        AqiLog.i(TAG, "changeSensorState(%s)", startSensor);
        setSensorStarted(startSensor);
        // Update sensor handler workPeriodic value.
        configureSensorWorkPeriodic(workPeriodic);
        if(startSensor) {
            if(Sds011Handler.instance.start()) {
                postMsg("Started");
                LocationHandler.instance.startLocationUpdate();
                if(isWorkPeriodic()) {
                    postStatus("Running in the PERIODIC mode.");
                }
                else {
                    postStatus("Running in the CONTINUOUS mode.");
                }
            }
            else {
                postMsg("");
                postStatus("Sensor is not connected.");
            }
        }
        else {
            LocationHandler.instance.stopLocationUpdate();
            if(Sds011Handler.instance.stop()) {
                postMsg("Stopped");
                postStatus("");
            }
            else {
                postMsg("");
                postStatus("Sensor is not connected.");
            }
        }
    }
    public void setSensorStarted(boolean sensorStarted) {
        AqiLog.i(TAG, "setSensorStarted(%s)", sensorStarted);
        this.sensorStarted = sensorStarted;
    }
    public boolean isSensorStarted() {
        AqiLog.i(TAG, "isSensorStarted(): %s", sensorStarted);
        return sensorStarted;
    }

    public void setHistory(List<Measurement> history) {
        AqiLog.i(TAG, "setHistory()");
        this.history.postValue(history);
    }
    public MutableLiveData<List<Measurement>> getHistory() {
        AqiLog.i(TAG, "getHistory()");
        return history;
    }
    public void setLocation(Location location) {
        AqiLog.i(TAG, "setLocation(%s)", location);
        this.location = location;
    }
    public Location getLocation() {
        AqiLog.i(TAG, "getLocation(): %s", location);
        return this.location;
    }

    public int getLocationPriority() {
        AqiLog.i(TAG, "getLocationPriority(): %s", this.locationPriority);
        return this.locationPriority;
    }
    public void setLocationPriority(int locationPriority) {
        AqiLog.i(TAG, "setLocationPriority(%s)", locationPriority);
        this.locationPriority = locationPriority;
    }
}
