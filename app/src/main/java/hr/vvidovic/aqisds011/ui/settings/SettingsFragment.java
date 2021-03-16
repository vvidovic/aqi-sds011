package hr.vvidovic.aqisds011.ui.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.LocationRequest;

import hr.vvidovic.aqisds011.LocationHandler;
import hr.vvidovic.aqisds011.R;
import hr.vvidovic.aqisds011.Sds011ViewModel;
import hr.vvidovic.aqisds011.log.AqiLog;
import hr.vvidovic.aqisds011.ui.measure.MeasureFragment;

public class SettingsFragment extends Fragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private Sds011ViewModel model;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AqiLog.i(TAG, "onCreateView(), savedInstanceState: " + savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(Sds011ViewModel.class);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        Spinner spinner = root.findViewById(R.id.spinner_settings_location);
        String[] locationOptions = getResources().getStringArray(R.array.settings_location_selection);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(getContext(), R.layout.spinner_item, locationOptions);
        spinner.setAdapter(adapter);
        spinner.setSelection(getLocationSelection());

        final EditText editWorkPeriod = root.findViewById(R.id.edit_settings_work_period_minutes);
        editWorkPeriod.setText(model.getWorkPeriodMinutes().toString());

        final EditText editWorkContAvgCnt = root.findViewById(R.id.edit_settings_work_continuous_avg_cnt);
        editWorkContAvgCnt.setText(String.valueOf(model.getWorkContinuousAverageCount()));

        final Button buttonSave = root.findViewById(R.id.button_settings_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable minutesStr = editWorkPeriod.getText();
                Editable avgCountStr = editWorkContAvgCnt.getText();
                try {
                    byte minutestByte = Byte.valueOf(minutesStr.toString());
                    if(minutestByte < 0 || minutestByte > 30) {
                        throw new NumberFormatException("0 <= minutes <= 30.");
                    }

                    int avgCount = Integer.valueOf(avgCountStr.toString());
                    if(avgCount < 1) {
                        throw new NumberFormatException("1 <= number of averaging measurements.");
                    }

                    model.setWorkPeriodMinutes(minutestByte);
                    model.setWorkContinuousAverageCount(avgCount);
                    int locationPriority = getLocationPriority(spinner); // uses model
                    model.setLocationPriority(locationPriority);

                    SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt(getString(R.string.settings_work_period_key), minutestByte);
                    editor.putInt(getString(R.string.settings_work_continuous_avg_cnt_key), avgCount);
                    editor.putInt(getString(R.string.settings_location_priority_key), locationPriority);
                    editor.commit();

                    Toast.makeText(getContext(), R.string.msg_settings_save_success, Toast.LENGTH_SHORT).show();
                }
                catch (NumberFormatException e) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Error")
                            .setMessage("Error saving: " + e.getLocalizedMessage())
                            .setIcon(R.drawable.ic_baseline_error_24)
                            .setPositiveButton("OK", null)
                            .show();
                }
            }
        });

        return root;
    }

    private int getLocationSelection() {
        switch (model.getLocationPriority()) {
            case LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY:
                return 1;
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                return 2;
            default:
                return 0;
        }
    }

    private int getLocationPriority(Spinner spinner) {
        AqiLog.i(TAG, "getLocationPriority()");

        String loc = spinner.getSelectedItem().toString();

        final boolean locationSetOk;
        final int locationPriority;
        if(loc.equals(getString(R.string.settings_location_high_accuracy))) {
            locationPriority = LocationRequest.PRIORITY_HIGH_ACCURACY;
            locationSetOk = LocationHandler.instance.updateLocationRequestSettings(locationPriority);
        }
        else if(loc.equals(getString(R.string.settings_location_low_power))) {
            locationPriority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
            locationSetOk = LocationHandler.instance.updateLocationRequestSettings(locationPriority);
        }
        else {
            locationPriority = LocationHandler.LOCATION_DISABLED;
            locationSetOk = true;
        }
        AqiLog.i(TAG, "locationSetOk: " + locationSetOk);

        if(locationPriority != LocationHandler.LOCATION_DISABLED) {
            LocationHandler.instance.updateLocationLastLocation();
        }

        if(!locationSetOk) {
            spinner.setSelection(0);

            new AlertDialog.Builder(getContext())
                    .setTitle("Error")
                    .setMessage("Can't change location settings.")
                    .setIcon(R.drawable.ic_baseline_error_24)
                    .setPositiveButton("OK", null)
                    .show();
        }

        return locationPriority;
    }
}