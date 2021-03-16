package hr.vvidovic.aqisds011;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import hr.vvidovic.aqisds011.data.AppDatabase;
import hr.vvidovic.aqisds011.data.Measurement;
import hr.vvidovic.aqisds011.log.AqiLog;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Sds011ViewModel model;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AqiLog.i(TAG, "onCreate(), savedInstanceState: %s", savedInstanceState);

        model = new ViewModelProvider(this).get(Sds011ViewModel.class);

        // Read / init settings.
        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);

        model.setWorkPeriodic(
                prefs.getBoolean(getString(R.string.settings_work_periodic_key),
                        Sds011ViewModel.DEFAULT_WP));
        model.setWorkPeriodMinutes(
                (byte) prefs.getInt(getString(R.string.settings_work_period_key),
                        Sds011ViewModel.DEFAULT_WP_MINUTES));

        model.setWorkContinuousAverageCount(
                prefs.getInt(getString(R.string.settings_work_continuous_avg_cnt_key),
                        Sds011ViewModel.DEFAULT_WC_CNT));

        model.setSensorStarted(
                prefs.getBoolean(getString(R.string.settings_sensor_started_key),
                        Sds011ViewModel.DEFAULT_SENSOR_STARTED));

        model.setLocationPriority(
                prefs.getInt(getString(R.string.settings_location_priority_key),
                        LocationHandler.LOCATION_DISABLED));

        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_history)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "aqi-sds011")
                .allowMainThreadQueries().build();
        List<Measurement> history = db.measurementDao().getAll();
        model.setHistory(history);
        Sds011Handler.instance.init(this, model, db);

        LocationHandler.instance.init(this, model);
        AqiLog.i(TAG, "Before checking location...");
        LocationHandler.instance.updateLocationRequestSettings(model.getLocationPriority());
        LocationHandler.instance.updateLocationLastLocation();



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AqiLog.i(TAG, "onDestroy()");

        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(getString(R.string.settings_work_periodic_key), model.isWorkPeriodic());
        editor.putBoolean(getString(R.string.settings_sensor_started_key), model.isSensorStarted());

        editor.commit();
    }
}