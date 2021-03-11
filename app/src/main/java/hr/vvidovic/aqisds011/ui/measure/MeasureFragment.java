package hr.vvidovic.aqisds011.ui.measure;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.time.Instant;
import java.util.List;

import hr.vvidovic.aqisds011.R;
import hr.vvidovic.aqisds011.Sds011ViewModel;
import hr.vvidovic.aqisds011.data.Measurement;

public class MeasureFragment extends Fragment {

    private Sds011ViewModel model;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.i(getClass().getSimpleName(), "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(getClass().getSimpleName(), "onCreateView()");
        model = new ViewModelProvider(requireActivity()).get(Sds011ViewModel.class);
        View root = inflater.inflate(R.layout.fragment_measure, container, false);

        final TextView textViewStatus = root.findViewById(R.id.text_measure_status);
        model.getValueStatus().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textViewStatus.setText(s);
            }
        });

        final TextView textViewPm25 = root.findViewById(R.id.text_measure_pm25);
        final TextView textViewPm25Aqi = root.findViewById(R.id.text_measure_pm25_aqi);
        final TextView textViewPm10 = root.findViewById(R.id.text_measure_pm10);
        final TextView textViewPm10Aqi = root.findViewById(R.id.text_measure_pm10_aqi);
        model.getValueMeasurement().observe(getViewLifecycleOwner(), new Observer<Measurement>() {
            @Override
            public void onChanged(@Nullable Measurement m) {
                textViewPm25.setText(String.format("%01.2f", m.pm25));
                textViewPm25Aqi.setText(String.format("%d", m.aqiPm25()));
                textViewPm25.setBackgroundColor(ContextCompat.getColor(getContext(), m.aqiPm25Category().color));
                textViewPm25Aqi.setBackgroundColor(ContextCompat.getColor(getContext(), m.aqiPm25Category().color));

                textViewPm10.setText(String.format("%01.2f", m.pm10));
                textViewPm10Aqi.setText(String.format("%d", m.aqiPm10()));
                textViewPm10.setBackgroundColor(ContextCompat.getColor(getContext(), m.aqiPm10Category().color));
                textViewPm10Aqi.setBackgroundColor(ContextCompat.getColor(getContext(), m.aqiPm10Category().color));
            }
        });


        final TextView textViewMsg = root.findViewById(R.id.text_measure_msg);
        model.getValueMsg().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textViewMsg.setText(s);
            }
        });

        ToggleButton tbMode = root.findViewById(R.id.toggle_measure_mode);
        tbMode.setChecked(model.isWorkPeriodic());
        tbMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setWorkPeriodic(isChecked);
                final List<Measurement> history = model.getHistory().getValue();
                final String lastMeasurement;
                if(history.isEmpty()) {
                    lastMeasurement = "no history";
                }
                else {
                    lastMeasurement = Instant.ofEpochMilli(history.get(history.size() -1).dateTime).toString();
                }
                model.postMsg("last date-time: " + lastMeasurement);
            }
        });

        Button btnStart = root.findViewById(R.id.button_measure_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tbMode.setEnabled(false);
                model.postMsg("Starting...");
                model.setSensorRunningState(true);
                updateButtonsEnabled(root, true);
            }
        });

        Button btnStop = root.findViewById(R.id.button_measure_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tbMode.setEnabled(true);
                model.postMsg("Stopping...");
                model.setSensorRunningState(false);
                updateButtonsEnabled(root, false);
            }
        });

        updateButtonsEnabled(root, model.isSensorStarted());

        return root;
    }

    private void updateButtonsEnabled(View root, boolean started) {
        Log.i(getClass().getSimpleName(), "updateButtonsEnabled()");
        ToggleButton tbMode = root.findViewById(R.id.toggle_measure_mode);
        Button btnStart = root.findViewById(R.id.button_measure_start);
        Button btnStop = root.findViewById(R.id.button_measure_stop);

        tbMode.setEnabled(!started);
        btnStart.setEnabled(!started);
        btnStop.setEnabled(started);
    }
}