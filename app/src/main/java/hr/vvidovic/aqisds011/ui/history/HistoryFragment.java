package hr.vvidovic.aqisds011.ui.history;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.time.Instant;
import java.time.LocalDateTime;

import hr.vvidovic.aqisds011.R;
import hr.vvidovic.aqisds011.Sds011ViewModel;
import hr.vvidovic.aqisds011.data.Measurement;

public class HistoryFragment extends Fragment {

    private Sds011ViewModel model;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        model = new ViewModelProvider(requireActivity()).get(Sds011ViewModel.class);
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        final TableLayout tl = root.findViewById(R.id.table_history);

        // Add header
        tl.addView(createRowHeader(root,"date-time", "AQI", "PM2.5", "AQI", "PM10"));
        // Add data
        for (Measurement m: model.getHistory()) {
            tl.addView(createRow(root, m));
        }

        final Button buttonClear = root.findViewById(R.id.button_history_clear);
        final Button buttonExport = root.findViewById(R.id.button_history_export);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("TODO")
                        .setMessage("Not yet implemented")
                        .setIcon(R.drawable.ic_baseline_info_24)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
        buttonExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("TODO")
                        .setMessage("Not yet implemented")
                        .setIcon(R.drawable.ic_baseline_info_24)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });

        return root;
    }

    private TableRow createRowHeader(View root, String dateTime, String pm25aqi, String pm25, String pm10aqi, String pm10) {
        TableRow tr = new TableRow(root.getContext());
        tr.addView(createCell(root, dateTime, Gravity.CENTER));
        tr.addView(createCell(root, pm25aqi, Gravity.CENTER));
        tr.addView(createCell(root, pm25, Gravity.CENTER));
        tr.addView(createCell(root, pm10aqi, Gravity.CENTER));
        tr.addView(createCell(root, pm10, Gravity.CENTER));

        return tr;
    }

    private TableRow createRow(View root, String dateTime, String pm25aqi, String pm25, String pm10aqi, String pm10, int gravity) {
        TableRow tr = new TableRow(root.getContext());
        tr.addView(createCell(root, dateTime, gravity));
        tr.addView(createCell(root, pm25aqi, gravity));
        tr.addView(createCell(root, pm25, gravity));
        tr.addView(createCell(root, pm10aqi, gravity));
        tr.addView(createCell(root, pm10, gravity));

        return tr;
    }

    private TableRow createRow(View root, Measurement m) {
        return createRow(root,
                Instant.ofEpochMilli(m.dateTime).toString(),
                String.format("%d", m.aqiPm25()),
                String.format("%01.2f", m.pm25),
                String.format("%d", m.aqiPm10()),
                String.format("%01.2f", m.pm10),
                Gravity.RIGHT);
    }

    private TextView createCell(View root, Object val, int gravity) {
        TextView tv = new TextView(root.getContext());
        tv.setPadding(10, 10, 10, 10);
        tv.setText(val.toString());
        tv.setGravity(gravity);
        return tv;
    }
}