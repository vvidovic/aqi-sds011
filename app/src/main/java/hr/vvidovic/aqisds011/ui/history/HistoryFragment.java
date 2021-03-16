package hr.vvidovic.aqisds011.ui.history;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import hr.vvidovic.aqisds011.R;
import hr.vvidovic.aqisds011.AqiRequest;
import hr.vvidovic.aqisds011.Sds011ViewModel;
import hr.vvidovic.aqisds011.data.AppDatabase;
import hr.vvidovic.aqisds011.data.Measurement;
import hr.vvidovic.aqisds011.log.AqiLog;
import hr.vvidovic.aqisds011.ui.measure.MeasureFragment;

public class HistoryFragment extends Fragment {
    private static final String TAG = HistoryFragment.class.getSimpleName();

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Sds011ViewModel model;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        model = new ViewModelProvider(requireActivity()).get(Sds011ViewModel.class);
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        refreshTable(root);

        model.getHistory().observe(getViewLifecycleOwner(), new Observer<List<Measurement>>() {
            @Override
            public void onChanged(@Nullable List<Measurement> m) {
                refreshTable(root);
            }
        });

        final AppDatabase db = Room.databaseBuilder(getContext(), AppDatabase.class, "aqi-sds011")
                .allowMainThreadQueries().build();

        final Button buttonClear = root.findViewById(R.id.button_history_clear);
        final Button buttonExport = root.findViewById(R.id.button_history_export);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete history")
                        .setMessage("Are you sure you want to delete all history data?")
                        .setIcon(R.drawable.ic_baseline_error_24)
                        .setPositiveButton("OK", (dialog, which) -> {
                            db.measurementDao().deleteAll();
                            model.getHistory().getValue().clear();
                            model.setHistory(model.getHistory().getValue());

                            Toast.makeText(getContext(), R.string.msg_history_delete_success, Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("NO", null)
                        .show();
            }
        });
        buttonExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askUserToSaveExport();
            }
        });

        return root;
    }

    // Export data happens after user select the destination.
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == AqiRequest.CREATE_FILE.val() && resultCode == Activity.RESULT_OK) {
            ContentResolver contentResolver = getContext().getContentResolver();
            try {
                OutputStream os = contentResolver.openOutputStream(data.getData());
                BufferedOutputStream bos = new BufferedOutputStream(os);
                StringBuilder sbHead = new StringBuilder();
                sbHead
                        .append("id").append(',')
                        .append("dateTime").append(',')
                        .append("pm25").append(',')
                        .append("pm10").append(',')
                        .append("locDateTime").append(',')
                        .append("locLatitude").append(',')
                        .append("locLongitude").append(',')
                        .append("locAccuracy").append('\n');
                bos.write(sbHead.toString().getBytes());

                for (Measurement m: model.getHistory().getValue()) {
                    StringBuilder sbRow = new StringBuilder();
                    sbRow
                            .append(m.id).append(',')
                            .append(m.dateTime).append(',')
                            .append(m.pm25).append(',')
                            .append(m.pm10).append(',')
                            .append(m.locDateTime).append(',')
                            .append(m.locLatitude).append(',')
                            .append(m.locLongitude).append(',')
                            .append(m.locAccuracy).append('\n');
                    bos.write(sbRow.toString().getBytes());
                }
                bos.flush();
                bos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void askUserToSaveExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        intent.putExtra(Intent.EXTRA_TITLE, String.format("aqi_sds011_%s.csv", dateTime));

        startActivityForResult(intent, AqiRequest.CREATE_FILE.val());
    }

    private void refreshTable(View root) {
        final TableLayout tl = root.findViewById(R.id.table_history);
        tl.removeAllViews();

        // Add header
        tl.addView(createRowHeader(root,"date-time",
                "AQI", "PM2.5",
                "AQI", "PM10",
                "map"));
        // Add data
        for (Measurement m: model.getHistory().getValue()) {
            tl.addView(createRow(root, m));
            AqiLog.d(TAG, m.toString());
        }

    }

    private TableRow createRowHeader(View root,
                                     String dateTime,
                                     String pm25aqi, String pm25,
                                     String pm10aqi, String pm10,
                                     String location) {
        TableRow tr = new TableRow(root.getContext());
        tr.addView(createCell(root, dateTime, 0, Gravity.CENTER));
        tr.addView(createCell(root, pm25aqi, 0, Gravity.CENTER));
        tr.addView(createCell(root, pm25, 0, Gravity.CENTER));
        tr.addView(createCell(root, pm10aqi, 0, Gravity.CENTER));
        tr.addView(createCell(root, pm10, 0, Gravity.CENTER));
        tr.addView(createCell(root, location, 0, Gravity.CENTER));

        return tr;
    }

    private TableRow createRow(View root,
                               String dateTime,
                               String pm25aqi, String pm25, Measurement.Category pm25Category,
                               String pm10aqi, String pm10, Measurement.Category pm10Category,
                               boolean location, double latitude, double longitude,
                               int gravity) {
        TableRow tr = new TableRow(root.getContext());
        tr.addView(createCell(root, dateTime,0, gravity));
        tr.addView(createCell(root, pm25aqi, ContextCompat.getColor(getContext(), pm25Category.color), gravity));
        tr.addView(createCell(root, pm25, ContextCompat.getColor(getContext(), pm25Category.color), gravity));
        tr.addView(createCell(root, pm10aqi, ContextCompat.getColor(getContext(), pm10Category.color), gravity));
        tr.addView(createCell(root, pm10, ContextCompat.getColor(getContext(), pm10Category.color), gravity));

        if(location) {
            tr.addView(createMapsLocation(root, latitude, longitude));
        }
        return tr;
    }

    private TableRow createRow(View root, Measurement m) {
        LocalDateTime dt =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(m.dateTime), ZoneId.systemDefault());
        return createRow(root,
                dateTimeFormatter.format(dt),
                String.format("%d", m.aqiPm25()),
                String.format("%01.2f", m.pm25),
                m.aqiPm25Category(),
                String.format("%d", m.aqiPm10()),
                String.format("%01.2f", m.pm10),
                m.aqiPm10Category(),
                m.hasLocation(),
                m.locLatitude,
                m.locLongitude,
                Gravity.RIGHT);
    }

    private TextView createCell(View root, Object val, int color, int gravity) {
        TextView tv = new TextView(root.getContext());
        tv.setPadding(10, 10, 10, 10);
        tv.setText(val.toString());
        tv.setGravity(gravity);
        tv.setBackgroundColor(color);
        return tv;
    }

    private View createMapsLocation(View root, double latitude, double longitude) {
        TextView tv = new TextView(root.getContext());
        tv.setPadding(10, 10, 10, 10);
        tv.setText("show");
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(ContextCompat.getColor(getContext(), R.color.map_link));

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AqiLog.e(TAG, "%s/%s", latitude, longitude);

                Uri mapsIntentUri = Uri.parse(
                        String.format("geo:0,0?q=%s,%s(%s)",
                                latitude, longitude, "Last+location"));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapsIntentUri);
//                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        return tv;
    }
}