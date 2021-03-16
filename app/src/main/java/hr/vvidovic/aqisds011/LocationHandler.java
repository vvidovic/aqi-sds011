package hr.vvidovic.aqisds011;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationHandler {
    public static final int LOCATION_DISABLED = LocationRequest.PRIORITY_NO_POWER;
    public static LocationHandler instance = new LocationHandler();
    private static long SEC = 1000L;

    private Activity activity;
    private Sds011ViewModel model;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationUpdate = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.i(getClass().getSimpleName(),
                    "onLocationResult(), locationResult: " + locationResult);
            if(locationResult != null) {
                model.setLocation(locationResult.getLastLocation());
                model.postMsg("new location: " + locationResult.getLastLocation());
            }
        }
    };


    public void init(Activity activity, Sds011ViewModel model) {
        Log.i(getClass().getSimpleName(), "init()");
        this.activity = activity;
        this.model = model;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    // We want to receive location updates dependant on how often we save measurements (sm):
    // - sm <= 10 sec: 10 sec
    // - 10 < sm <= 50 sec: 10 + (sm - 10) / 2
    // - 50 < sm <= 140 sec: 30 + (sm - 30) / 3
    // - 140 < sm: 60 sec
    long calculateLocationUpdateInterval(final long saveMeasurementIntervalMillis) {
        final long locationUpdateMillis;
        if(saveMeasurementIntervalMillis <= 10 * SEC) {
            locationUpdateMillis = 10 * SEC;
        }
        else if(saveMeasurementIntervalMillis <= 50 * SEC) {
            locationUpdateMillis = 10 * SEC + (saveMeasurementIntervalMillis - 10 * SEC) / 2;
        }
        else if(saveMeasurementIntervalMillis <= 140 * SEC) {
            locationUpdateMillis = 30 * SEC + (saveMeasurementIntervalMillis - 50 * SEC) / 3;
        }
        else {
            locationUpdateMillis = 60 * SEC;
        }

        return locationUpdateMillis;
    }

    public boolean updateLocationRequestSettings(final int locationPriority) {
        Log.i(getClass().getSimpleName(), "setLocationPriority()");
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(locationPriority);
        // How often we save measurements:
        // - in the continuous mode sensor sends measurement each second:
        //   number of measurements * seconds
        // - in the periodic mode sensor sends measurement every n minutes:
        //   number of minutes
        final long saveMeasurementIntervalMillis;
        if(model.isWorkPeriodic()) {
            saveMeasurementIntervalMillis = model.getWorkPeriodMinutes() * 60 * SEC;
        }
        else {
            saveMeasurementIntervalMillis = model.getWorkContinuousAverageCount() * SEC;
        }
        final long locationUpdateMillis =
                calculateLocationUpdateInterval(saveMeasurementIntervalMillis);

        locationRequest.setInterval(locationUpdateMillis);
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(activity);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        long startTime = System.currentTimeMillis();
        while (!task.isComplete() && System.currentTimeMillis() - startTime < 2000) {
            Log.i(getClass().getSimpleName(), "setLocationPriority(), waiting task to complete...");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Log.i(getClass().getSimpleName(),
                "setLocationPriority(), task - complete: " +
                        task.isComplete() + ", success: " + task.isSuccessful());

        if (!task.isComplete()) {
            return false;
        }

        return task.isSuccessful();
    }

    public void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationUpdate, Looper.getMainLooper());
    }

    public void stopLocationUpdate() {
        fusedLocationClient.removeLocationUpdates(locationUpdate);
    }

    public void updateLocationLastLocation() {
        Log.i(getClass().getSimpleName(), "updateLocationLastLocation()");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        for (String perm: permissions) {
            if (ActivityCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                Log.e(getClass().getSimpleName(), "Permission '" + perm + "' doesn't exist.");
                activity.requestPermissions(new String[]{ perm }, AqiRequest.CODE_PERMISSION.val());
            }
            else {
                Log.i(getClass().getSimpleName(), "Permission '" + perm + "' granted.");
            }
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            return null;
        }

        Task<Location> locationTask = fusedLocationClient.getLastLocation();
        locationTask.addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.i(getClass().getSimpleName(), "onSuccess(), location: " + location);
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            model.setLocation(location);
                        }
                    }
                }).addOnCompleteListener(activity, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Log.i(getClass().getSimpleName(), "onComplete(), task: " + task);
            }
        }).addOnFailureListener(activity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(getClass().getSimpleName(), "onFailure(), e: " + e);
            }
        }).addOnCanceledListener(activity, new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.i(getClass().getSimpleName(), "onCanceled()");
            }
        });
    }
}
