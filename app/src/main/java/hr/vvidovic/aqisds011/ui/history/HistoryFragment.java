package hr.vvidovic.aqisds011.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import hr.vvidovic.aqisds011.R;

public class HistoryFragment extends Fragment {

    private HistoryViewModel historyViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        historyViewModel =
                new ViewModelProvider(this).get(HistoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        final TableLayout tl = root.findViewById(R.id.table_history);

        LiveData<String[][]> historyData  = historyViewModel.getHistory();
        for (String[] row: historyData.getValue()) {
            TableRow tr = new TableRow(root.getContext());
            for (String val: row) {
                tr.addView(createCell(root, val));
            }
            tl.addView(tr);
        }

        return root;
    }

    private TextView createCell(View root, Object val) {
        TextView tv = new TextView(root.getContext());
        tv.setPadding(10, 10, 10, 10);
        tv.setText(val.toString());
        System.out.println("val: " + val.toString());
        return tv;
    }
}