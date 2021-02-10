package hr.vvidovic.aqisds011.ui.measure;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import hr.vvidovic.aqisds011.R;
import hr.vvidovic.aqisds011.Sds011ViewModel;

public class MeasureFragment extends Fragment {

    private Sds011ViewModel model;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        model = new ViewModelProvider(requireActivity()).get(Sds011ViewModel.class);
        View root = inflater.inflate(R.layout.fragment_measure, container, false);

        final TextView textViewPm25 = root.findViewById(R.id.text_measure_pm25);
        model.getValuePm25().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textViewPm25.setText(s);
            }
        });

        final TextView textViewPm10 = root.findViewById(R.id.text_measure_pm10);
        model.getValuePm10().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textViewPm10.setText(s);
            }
        });

        final TextView textViewMsg = root.findViewById(R.id.text_measure_msg);
        model.getValueMsg().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textViewMsg.setText(s);
            }
        });

        Button btnStart = root.findViewById(R.id.button_measure_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.postMsg("Starting...");
                model.postSensorStarted(true);
            }
        });

        Button btnStop = root.findViewById(R.id.button_measure_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.postMsg("Stopping...");
                model.postSensorStarted(false);
            }
        });

        ToggleButton tbMode = root.findViewById(R.id.toggle_measure_mode);
        tbMode.setChecked(model.isWorkPeriodic());
        tbMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.postWorkPeriodic(isChecked);
            }
        });

        return root;
    }
}