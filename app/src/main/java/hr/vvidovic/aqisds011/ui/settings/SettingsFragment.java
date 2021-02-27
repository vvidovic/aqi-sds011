package hr.vvidovic.aqisds011.ui.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import hr.vvidovic.aqisds011.R;
import hr.vvidovic.aqisds011.Sds011ViewModel;

public class SettingsFragment extends Fragment {

    private Sds011ViewModel model;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        model = new ViewModelProvider(requireActivity()).get(Sds011ViewModel.class);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        final EditText editWorkPeriod = root.findViewById(R.id.edit_settings_work_period_minutes);
        editWorkPeriod.setText(model.getWorkPeriodMinutes().toString());

        final EditText editWorkContAvgCnt = root.findViewById(R.id.edit_settings_work_continuous_avg_cnt);
        editWorkContAvgCnt.setText(model.getWorkContinuousAverageCount().toString());

        final Button buttonSave = root.findViewById(R.id.button_settings_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable minutesStr = editWorkPeriod.getText();
                Editable avgCountStr = editWorkContAvgCnt.getText();
                try {
                    Byte minutestByte = Byte.valueOf(minutesStr.toString());
                    if(minutestByte < 0 || minutestByte > 30) {
                        throw new NumberFormatException("0 <= minutes <= 30.");
                    }

                    Integer avgCount = Integer.valueOf(avgCountStr.toString());
                    if(avgCount < 1) {
                        throw new NumberFormatException("1 <= number of averaging measurements.");
                    }

                    model.setWorkPeriodMinutes(minutestByte);
                    model.setWorkContinuousAverageCount(avgCount);

                    SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt(getString(R.string.settings_work_period_key), minutestByte);
                    editor.putInt(getString(R.string.settings_work_continuous_avg_cnt), avgCount);
                    editor.commit();

                    new AlertDialog.Builder(getContext())
                            .setTitle("Saved")
                            .setMessage("Settings saved.")
                            .setIcon(R.drawable.ic_baseline_info_24)
                            .setPositiveButton("OK", null)
                            .show();
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
}