package hr.vvidovic.aqisds011.ui.measure;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MeasureViewModel extends ViewModel {

    private MutableLiveData<String> mValuePm25;
    private MutableLiveData<String> mValuePm10;
    private MutableLiveData<String> mValueMsg;

    public MeasureViewModel() {
        mValuePm25 = new MutableLiveData<>();
        mValuePm10 = new MutableLiveData<>();
        mValueMsg = new MutableLiveData<>();
        mValuePm25.setValue("PM2.5: --");
        mValuePm10.setValue("PM10: --");
    }

    public void postValues(float pm25, float pm10) {
        postValuePm25(String.format("%01.2f", pm25));
        postValuePm10(String.format("%01.2f", pm10));
    }

    public void postMsg(String msg) {
        mValueMsg.postValue(msg);
    }

    public LiveData<String> getValuePm25() {
        return mValuePm25;
    }
    private void postValuePm25(String value) {
        mValuePm25.postValue("PM2.5: " + value);
    }

    public LiveData<String> getValuePm10() {
        return mValuePm10;
    }
    private void postValuePm10(String value) {
        mValuePm10.postValue("PM10: " + value);
    }

    public LiveData<String> getValueMsg() {
        return mValueMsg;
    }
}