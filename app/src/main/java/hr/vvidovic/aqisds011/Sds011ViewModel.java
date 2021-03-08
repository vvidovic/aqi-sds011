package hr.vvidovic.aqisds011;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import hr.vvidovic.aqisds011.data.Measurement;

public class Sds011ViewModel extends ViewModel {
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
        Log.i(getClass().getSimpleName(), "Sds011ViewModel()");

        valueMeasurement.setValue(new Measurement());
        valueMsg.setValue("");
        valueStatus.setValue("Stopped.");

        List<Measurement> h = new ArrayList<>();
        history.setValue(h);
    }

    public Measurement postMeasurement(Measurement m) {
        Log.i(getClass().getSimpleName(), "postMeasurement(), periodic: " + isWorkPeriodic()
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
        Log.i(getClass().getSimpleName(), "postMsg()");
        valueMsg.postValue(msg);
    }

    public void postStatus(String status) {
        Log.i(getClass().getSimpleName(), "postStatus()");
        valueStatus.postValue(status);
    }

    public LiveData<Measurement> getValueMeasurement() {
        Log.i(getClass().getSimpleName(), "getValueMeasurement()");
        return valueMeasurement;
    }
    private void postValueMeasurement(Measurement value) {
        Log.i(getClass().getSimpleName(), "postValueMeasurement()");
        valueMeasurement.postValue(value);
    }


    public LiveData<String> getValueMsg() {
        Log.i(getClass().getSimpleName(), "getValueMsg()");
        return valueMsg;
    }

    public LiveData<String> getValueStatus() {
        Log.i(getClass().getSimpleName(), "getValueStatus()");
        return valueStatus;
    }

    private void configureSensorWorkPeriodic(boolean workPeriodic) {
        Log.i(getClass().getSimpleName(), "configureSensorWorkPeriodic(" + workPeriodic + ")");
        if(workPeriodic) {
            Sds011Handler.instance.setWorkPeriodMinutes(workPeriodMinutes);
        }
        else {
            Sds011Handler.instance.setWorkPeriodMinutes((byte)0);
        }
    }
    public boolean isWorkPeriodic() {
        Log.i(getClass().getSimpleName(), "isWorkPeriodic(): " + workPeriodic);
        return workPeriodic;
    }
    public void setWorkPeriodic(boolean workPeriodic) {
        Log.i(getClass().getSimpleName(), "setWorkPeriodic(" + workPeriodic + ")");
        this.workPeriodic = workPeriodic;
        configureSensorWorkPeriodic(workPeriodic);
    }
    public Byte getWorkPeriodMinutes() {
        Log.i(getClass().getSimpleName(), "getWorkPeriodMinutes(): " + workPeriodMinutes);
        return workPeriodMinutes;
    }
    public void setWorkPeriodMinutes(byte workPeriodMinutes) {
        Log.i(getClass().getSimpleName(), "setWorkPeriodMinutes(" + workPeriodMinutes +")");
        this.workPeriodMinutes  = workPeriodMinutes;
    }

    public int getWorkContinuousAverageCount() {
        Log.i(getClass().getSimpleName(), "getWorkContinuousAverageCount(): " + workContinuousAverageCount);
        return workContinuousAverageCount;
    }
    public void setWorkContinuousAverageCount(int workContinuousAverageCount) {
        Log.i(getClass().getSimpleName(), "setWorkContinuousAverageCount(" + workContinuousAverageCount + ")");
        this.workContinuousAverageCount = workContinuousAverageCount;
    }

    public void setSensorRunningState(boolean startSensor) {
        Log.i(getClass().getSimpleName(), "changeSensorState(" + startSensor + ")");
        setSensorStarted(startSensor);
        // Update sensor handler workPeriodic value.
        configureSensorWorkPeriodic(workPeriodic);
        if(startSensor) {
            if(Sds011Handler.instance.start()) {
                postMsg("Started");
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
        Log.i(getClass().getSimpleName(), "setSensorStarted(" + sensorStarted + ")");
        this.sensorStarted = sensorStarted;
    }
    public boolean isSensorStarted() {
        Log.i(getClass().getSimpleName(), "isSensorStarted(): " + sensorStarted);
        return sensorStarted;
    }

    public void setHistory(List<Measurement> history) {
        Log.i(getClass().getSimpleName(), "setHistory()");
        this.history.postValue(history);
    }
    public MutableLiveData<List<Measurement>> getHistory() {
        Log.i(getClass().getSimpleName(), "getHistory()");
        return history;
    }
    public void setLocation(Location location) {
        Log.i(getClass().getSimpleName(), "setLocation(" + location + ")");
        this.location = location;
    }
    public Location getLocation() {
        Log.i(getClass().getSimpleName(), "getLocation(): " + location);
        return this.location;
    }

    public int getLocationPriority() {
        Log.i(getClass().getSimpleName(), "getLocationPriority(): " + this.locationPriority);
        return this.locationPriority;
    }
    public void setLocationPriority(int locationPriority) {
        Log.i(getClass().getSimpleName(), "setLocationPriority(" + locationPriority + ")");
        this.locationPriority = locationPriority;
    }
}
