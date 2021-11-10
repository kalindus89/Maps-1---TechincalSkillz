package com.techincalskillz.mitch_2017_easy_learn_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.techincalskillz.R;

import java.util.List;

public class BatchLocationActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    LinearLayout requestLocations, mStartService, mStopService, mStartIntentService, mStopIntentService;
    TextView outputText;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_location);

        requestLocations = findViewById(R.id.requestLocations);
        mStartService = findViewById(R.id.mStartService);
        mStopService = findViewById(R.id.mStopService);
        mStartIntentService = findViewById(R.id.mStartIntentService);
        mStopIntentService = findViewById(R.id.mStopIntentService);
        outputText = findViewById(R.id.outputText);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(BatchLocationActivity.this);

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }

                List<Location> locations = locationResult.getLocations(); //  get set of locations in given time period(here 15 seconds. check below method) and set to list
                LocationResultHelper locationResultHelper = new LocationResultHelper(BatchLocationActivity.this, locations);
                locationResultHelper.showNotification();
                locationResultHelper.saveLastLocationResults();

                outputText.setText(locationResultHelper.getLocationResultText());
                //  Log.d("aaaaaaa1",locationResult.getLastLocation().getLatitude() + " Longitude: " + locationResult.getLastLocation().getLongitude());
                //   Log.d("aaaaaaa list size ", String.valueOf(locations.size()));


            }
        };

        requestLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // used to get multiple(batch) locations in time intervals
                requestBatchLocationUpdate();

            }
        });

        mStartIntentService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requestBatchLocationUpdateForIntentService();

            }
        });

        mStopIntentService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (fusedLocationProviderClient != null) {
                    fusedLocationProviderClient.removeLocationUpdates(getPendingIntent());
                }
            }
        });

        mStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Receive Location Updates in Background Service When app is not Running

                Intent intent = new Intent(BatchLocationActivity.this, MyBackgroundLocationService.class);
                ContextCompat.startForegroundService(BatchLocationActivity.this, intent);
                Toast.makeText(BatchLocationActivity.this, "Service Started", Toast.LENGTH_SHORT).show();
            }
        });

        mStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(BatchLocationActivity.this, MyBackgroundLocationService.class);
                stopService(intent);
                Toast.makeText(BatchLocationActivity.this, "Service Stopped", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(BatchLocationActivity.this, MyBackgroundLocationIntentService.class);
        intent.setAction(MyBackgroundLocationIntentService.ACTION_PROCESS_UPDATES);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return PendingIntent.getForegroundService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }else{
            return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        }


    }

    private void requestBatchLocationUpdate() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // location update time
        locationRequest.setFastestInterval(3000); // location update time

        locationRequest.setMaxWaitTime(15 * 1000); // can save battery usage. here update every 15s


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);

    }

    private void requestBatchLocationUpdateForIntentService() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // location update time
        locationRequest.setFastestInterval(3000); // location update time

        locationRequest.setMaxWaitTime(15 * 1000); // can save battery usage. here update every 15s


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());

    }


    public void closeBackgroundMethods() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // closeBackgroundMethods();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //   closeBackgroundMethods();
    }

    @Override
    protected void onResume() {
        super.onResume();
        outputText.setText(LocationResultHelper.getLastSavedLocationResults(this));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // closeBackgroundMethods();

        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this); // start below method

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(LocationResultHelper.KEY_LOCATION_RESULTS)) {
            outputText.setText(LocationResultHelper.getLastSavedLocationResults(this));

        }
    }
}