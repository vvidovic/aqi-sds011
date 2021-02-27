package hr.vvidovic.aqisds011;

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

    private Sds011Handler sds011Handler;

    private final MutableLiveData<Boolean> workPeriodic = new MutableLiveData<>();
    private final MutableLiveData<Byte> workPeriodMinutes = new MutableLiveData<>();
    private final MutableLiveData<Integer> workContinuousAverageCount = new MutableLiveData<>();

    private final MutableLiveData<Boolean> sensorStarted = new MutableLiveData<>();

    private final MutableLiveData<Measurement> valueMeasurement = new MutableLiveData<>();
    private final MutableLiveData<String> valueMsg = new MutableLiveData<>();
    private final MutableLiveData<String> valueStatus = new MutableLiveData<>();

    private final MutableLiveData<List<Measurement>> history = new MutableLiveData<>();

    private final List<Measurement> continuousMeasurementAvgHist = new ArrayList<>();


    public Sds011ViewModel() {
        Log.i(getClass().getSimpleName(), "Sds011ViewModel()");
//        workPeriodic.setValue(DEFAULT_WP);
//        workPeriodMinutes.setValue(DEFAULT_WP_MINUTES);
//        workContinuousAverageCount.setValue(DEFAULT_WC_CNT);
//        sensorStarted.setValue(Boolean.FALSE);

        valueMeasurement.setValue(new Measurement());
        valueMsg.setValue("");
        valueStatus.setValue("Stopped.");

        List<Measurement> h = new ArrayList<>();
        history.setValue(h);
    }

    public void setSds011Handler(Sds011Handler sds011Handler) {
        Log.i(getClass().getSimpleName(), "setSds011Handler()");
        this.sds011Handler = sds011Handler;
    }

    public Sds011Handler getSds011Handler() {
        Log.i(getClass().getSimpleName(), "getSds011Handler()");
        return sds011Handler;
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

    public void postWorkPeriodic(Boolean workPeriodic) {
        Log.i(getClass().getSimpleName(), "postWorkPeriodic()");
        this.workPeriodic.postValue(workPeriodic);
        configureSensorWorkPeriodic(workPeriodic);
    }
    private void configureSensorWorkPeriodic(boolean workPeriodic) {
        Log.i(getClass().getSimpleName(), "configureSensorWorkPeriodic()");
        if(workPeriodic) {
            sds011Handler.setWorkPeriodMinutes(workPeriodMinutes.getValue());
        }
        else {
            sds011Handler.setWorkPeriodMinutes((byte)0);
        }
    }
    public boolean isWorkPeriodic() {
        Log.i(getClass().getSimpleName(), "isWorkPeriodic()");
        return workPeriodic.getValue();
    }
    public void setWorkPeriodic(boolean periodic) {
        Log.i(getClass().getSimpleName(), "setWorkPeriodic()");
        workPeriodic.setValue(periodic);
    }
    public Byte getWorkPeriodMinutes() {
        Log.i(getClass().getSimpleName(), "getWorkPeriodMinutes()");
        return workPeriodMinutes.getValue();
    }
    public void setWorkPeriodMinutes(Byte minutes) {
        Log.i(getClass().getSimpleName(), "setWorkPeriodMinutes()");
        workPeriodMinutes.setValue(minutes);
    }

    public Integer getWorkContinuousAverageCount() {
        Log.i(getClass().getSimpleName(), "getWorkContinuousAverageCount()");
        return workContinuousAverageCount.getValue();
    }
    public void setWorkContinuousAverageCount(Integer count) {
        Log.i(getClass().getSimpleName(), "setWorkContinuousAverageCount()");
        workContinuousAverageCount.setValue(count);
    }

    public void postSensorStarted(Boolean started) {
        Log.i(getClass().getSimpleName(), "postSensorStarted()");
        sensorStarted.postValue(started);
        // Update sensor handler workPeriodic value.
        configureSensorWorkPeriodic(workPeriodic.getValue());
        if(started) {
            if(sds011Handler.start()) {
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
            if(sds011Handler.stop()) {
                postMsg("Stopped");
                postStatus("");
            }
            else {
                postMsg("");
                postStatus("Sensor is not connected.");
            }
        }
    }
    public void setSensorStarted(Boolean started) {
        Log.i(getClass().getSimpleName(), "setSensorStarted()");
        sensorStarted.setValue(started);
    }
    public boolean isSensorStarted() {
        Log.i(getClass().getSimpleName(), "isSensorStarted()");
        return sensorStarted.getValue();
    }

    public void setHistory(List<Measurement> history) {
        Log.i(getClass().getSimpleName(), "setHistory()");
        this.history.postValue(history);
    }
    public MutableLiveData<List<Measurement>> getHistory() {
        Log.i(getClass().getSimpleName(), "getHistory()");
        return history;
    }
}
