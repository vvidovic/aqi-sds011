package hr.vvidovic.aqisds011.ui.history;

import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HistoryViewModel extends ViewModel {

    private MutableLiveData<String[][]> mHistoryData;

    public HistoryViewModel() {
        mHistoryData = new MutableLiveData<>();

        String[][] model  = {
                {"1", "here01", "10.0", "20.0"},
                {"2", "there1", "11.0", "21.0"},
                {"3", "there2", "12.0", "22.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
                {"4", "there3", "13.0", "23.0"},
        };

        mHistoryData.setValue(model);
    }

    public LiveData<String[][]> getHistory() {
        return mHistoryData;
    }
}