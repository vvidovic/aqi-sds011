package hr.vvidovic.aqisds011;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import hr.vvidovic.aqisds011.Sds011Handler;

public class Sds011ViewModel extends ViewModel {
    private Sds011Handler sds011Handler;

    private final MutableLiveData<Boolean> workPeriodic = new MutableLiveData<>();
    private final MutableLiveData<Byte> workPeriodicMinutes = new MutableLiveData<>();

    private final MutableLiveData<Boolean> sensorStarted = new MutableLiveData<>();

    private final MutableLiveData<String> valuePm25 = new MutableLiveData<>();
    private final MutableLiveData<String> valuePm10 = new MutableLiveData<>();
    private final MutableLiveData<String> valueMsg = new MutableLiveData<>();

    public Sds011ViewModel() {
        valuePm25.setValue("PM2.5: --");
        valuePm10.setValue("PM10: --");
        workPeriodic.setValue(Boolean.FALSE);
        workPeriodicMinutes.setValue((byte)0);
    }

    public void setSds011Handler(Sds011Handler sds011Handler) {
        this.sds011Handler = sds011Handler;
    }

    public Sds011Handler getSds011Handler() {
        return sds011Handler;
    }

    public void postValues(float pm25, float pm10) {
        postValuePm25(String.format("%01.2f", pm25));
        postValuePm10(String.format("%01.2f", pm10));
    }

    public void postMsg(String msg) {
        valueMsg.postValue(msg);
    }

    public LiveData<String> getValuePm25() {
        return valuePm25;
    }
    private void postValuePm25(String value) {
        valuePm25.postValue("PM2.5: " + value);
    }

    public LiveData<String> getValuePm10() {
        return valuePm10;
    }
    private void postValuePm10(String value) {
        valuePm10.postValue("PM10: " + value);
    }

    public LiveData<String> getValueMsg() {
        return valueMsg;
    }

    public void postWorkPeriodic(Boolean workPeriodic) {
        this.workPeriodic.postValue(workPeriodic);
        if(workPeriodic) {
            workPeriodicMinutes.setValue((byte)2);
        }
        else {
            workPeriodicMinutes.setValue((byte)0);
        }
        sds011Handler.setWorkPeriodMinutes(workPeriodicMinutes.getValue());
    }
    public boolean isWorkPeriodic() {
        return workPeriodic.getValue();
    }
    public void postSensorStarted(Boolean sensorStarted) {
        this.sensorStarted.postValue(sensorStarted);
        if(sensorStarted) {
            if(sds011Handler.start()) {
                postMsg("Started");
            }
            else {
                postMsg("");
            }
        }
        else {
            if(sds011Handler.stop()) {
                postMsg("Stopped");
            }
            else {
                postMsg("");
            }
        }
    }
}
