package hr.vvidovic.aqisds011;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import hr.vvidovic.aqisds011.data.AppDatabase;
import hr.vvidovic.aqisds011.data.Measurement;

public class MainActivity extends AppCompatActivity {

    private Sds011ViewModel model;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        model = new ViewModelProvider(this).get(Sds011ViewModel.class);
        List<Measurement> history = db.measurementDao().getAll();
        model.setHistory(history);
        Sds011Handler sds011Handler = new Sds011Handler(this, model, db);
        model.setSds011Handler(sds011Handler);

        // Read / init settings.
        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if(!prefs.contains(getString(R.string.settings_work_period_key))) {
            editor.putInt(
                    getString(R.string.settings_work_period_key), Sds011ViewModel.DEFAULT_WP_MINUTES);
            editor.apply();
            model.setWorkPeriodMinutes(Sds011ViewModel.DEFAULT_WP_MINUTES);
        }
        else {
            model.setWorkPeriodMinutes(
                    (byte)prefs.getInt(getString(R.string.settings_work_period_key),
                            Sds011ViewModel.DEFAULT_WP_MINUTES));
        }

        if(!prefs.contains(getString(R.string.settings_work_periodic_key))) {
            editor.putBoolean(
                    getString(R.string.settings_work_periodic_key), Sds011ViewModel.DEFAULT_WP);
            editor.apply();
            model.setWorkPeriodic(Sds011ViewModel.DEFAULT_WP);
        }
        else {
            model.setWorkPeriodic(
                    prefs.getBoolean(getString(R.string.settings_work_periodic_key),
                            Sds011ViewModel.DEFAULT_WP));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(
                getString(R.string.settings_work_periodic_key), model.isWorkPeriodic());
        editor.apply();
    }
}