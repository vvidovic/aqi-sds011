package hr.vvidovic.aqisds011.ui.measure;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MeasureViewModel extends ViewModel {

    private MutableLiveData<String> mValuePm25;
    private MutableLiveData<String> mValuePm10;

    public MeasureViewModel() {
        mValuePm25 = new MutableLiveData<>();
        mValuePm10 = new MutableLiveData<>();
        mValuePm25.setValue("PM2.5 -- µg/m3");
        mValuePm10.setValue("PM10 -- µg/m3");
    }

    public LiveData<String> getValuePm25() {
        return mValuePm25;
    }
    public LiveData<String> getValuePm10() {
        return mValuePm10;
    }
}