package hr.vvidovic.aqisds011.ui.measure;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import hr.vvidovic.aqisds011.R;

public class MeasureFragment extends Fragment {

    private MeasureViewModel measureViewModel;

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
        return root;
    }
}