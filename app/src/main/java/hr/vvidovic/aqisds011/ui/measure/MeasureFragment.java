package hr.vvidovic.aqisds011.ui.measure;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import hr.vvidovic.aqisds011.R;
import hr.vvidovic.aqisds011.Sds011Handler;

public class MeasureFragment extends Fragment {

    private MeasureViewModel measureViewModel;
    private Sds011Handler sds011Handler;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sds011Handler = new Sds011Handler(getActivity(), measureViewModel);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        measureViewModel =
                new ViewModelProvider(this).get(MeasureViewModel.class);
        View root = inflater.inflate(R.layout.fragment_measure, container, false);

        final TextView textViewPm25 = root.findViewById(R.id.text_measure_pm25);
        measureViewModel.getValuePm25().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textViewPm25.setText(s);
            }
        });

        final TextView textViewPm10 = root.findViewById(R.id.text_measure_pm10);
        measureViewModel.getValuePm10().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textViewPm10.setText(s);
            }
        });

        final TextView textViewMsg = root.findViewById(R.id.text_measure_msg);
        measureViewModel.getValueMsg().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textViewMsg.setText(s);
            }
        });

        Button btnStart = root.findViewById(R.id.button_measure_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                measureViewModel.postMsg("Starting...");
                    if(sds011Handler.start()) {
                        measureViewModel.postMsg("Started");
                    }
                    else {
                        measureViewModel.postMsg("");
                    }
            }
        });

        Button btnStop = root.findViewById(R.id.button_measure_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                measureViewModel.postMsg("Stopping...");
                boolean stopped = sds011Handler.stop();
                if(stopped) {
                    measureViewModel.postMsg("Stopped");
                }
                else {
                    measureViewModel.postMsg("");
                }
            }
        });

        return root;
    }
}