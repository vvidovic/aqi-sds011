package hr.vvidovic.aqisds011;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import hr.vvidovic.aqisds011.data.Measurement;

public class Sds011ViewModel extends ViewModel {
    public static final byte DEFAULT_WP_MINUTES = (byte)2;
    public static final boolean DEFAULT_WP = false;

    private Sds011Handler sds011Handler;

    private final MutableLiveData<Boolean> workPeriodic = new MutableLiveData<>();
    private final MutableLiveData<Byte> workPeriodMinutes = new MutableLiveData<>();

    private final MutableLiveData<Boolean> sensorStarted = new MutableLiveData<>();

    private final MutableLiveData<Measurement> valueMeasurement = new MutableLiveData<>();
    private final MutableLiveData<String> valueMsg = new MutableLiveData<>();
    private final MutableLiveData<String> valueStatus = new MutableLiveData<>();

    private final MutableLiveData<List<Measurement>> history = new MutableLiveData<>();

    public Sds011ViewModel() {
        valueMeasurement.setValue(new Measurement());
        sensorStarted.setValue(Boolean.FALSE);
//        workPeriodic.setValue(Boolean.FALSE);
//        workPeriodicMinutes.setValue((byte)2);
        valueStatus.setValue("Stopped.");

        List<Measurement> h = new ArrayList<>();
        history.setValue(h);
    }

    public void setSds011Handler(Sds011Handler sds011Handler) {
        this.sds011Handler = sds011Handler;
    }

    public Sds011Handler getSds011Handler() {
        return sds011Handler;
    }

    public void postMeasurement(Measurement m) {
        postValueMeasurement(m);

        List<Measurement> newHist = history.getValue();

        newHist.add(m);
        history.postValue(newHist);
    }

    public void postMsg(String msg) {
        valueMsg.postValue(msg);
    }

    public void postStatus(String status) {
        valueStatus.postValue(status);
    }

    public LiveData<Measurement> getValueMeasurement() {
        return valueMeasurement;
    }
    private void postValueMeasurement(Measurement value) {
        valueMeasurement.postValue(value);
    }


    public LiveData<String> getValueMsg() {
        return valueMsg;
    }

    public LiveData<String> getValueStatus() { return valueStatus; }

    public void postWorkPeriodic(Boolean workPeriodic) {
        this.workPeriodic.postValue(workPeriodic);
        if(workPeriodic) {
            sds011Handler.setWorkPeriodMinutes(workPeriodMinutes.getValue());
        }
        else {
            sds011Handler.setWorkPeriodMinutes((byte)0);
        }
    }
    public boolean isWorkPeriodic() {
        return workPeriodic.getValue();
    }
    public void setWorkPeriodic(boolean periodic) {
        workPeriodic.setValue(periodic);
    }
    public Byte getWorkPeriodMinutes() {
        return workPeriodMinutes.getValue();
    }
    public void setWorkPeriodMinutes(Byte minutes) {
        workPeriodMinutes.setValue(minutes);
    }

    public void postSensorStarted(Boolean sensorStarted) {
        this.sensorStarted.postValue(sensorStarted);
        if(sensorStarted) {
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
    public boolean isSensorStarted() {
        return sensorStarted.getValue();
    }

    public void addToHistory(Measurement measurement) {
        List<Measurement> newHistory = history.getValue();
        newHistory.add(measurement);
        history.postValue(newHistory);
    }
    public void setHistory(List<Measurement> history) {
        this.history.postValue(history);
    }
    public List<Measurement> getHistory() {
        return history.getValue();
    }
}
