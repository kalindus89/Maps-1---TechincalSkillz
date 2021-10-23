package com.techincalskillz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class BatchLocationActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    LinearLayout requestLocations;
    TextView outputText;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_location);

        requestLocations = findViewById(R.id.requestLocations);
        outputText = findViewById(R.id.outputText);


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(BatchLocationActivity.this);

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }

                List<Location> locations=locationResult.getLocations(); //  get set of locations in given time period(here 15 seconds. check below method) and set to list
                LocationResultHelper locationResultHelper = new LocationResultHelper(BatchLocationActivity.this,locations);
                locationResultHelper.showNotification();
                locationResultHelper.saveLastLocationResults();



                outputText.setText(locationResultHelper.getLocationResultText());
                //  Log.d("aaaaaaa1",locationResult.getLastLocation().getLatitude() + " Longitude: " + locationResult.getLastLocation().getLongitude());
                  Log.d("aaaaaaa list size ", String.valueOf(locations.size()));


            }
        };

        requestLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // used to get multiple(batch) locations in time intervals
                requestBatchLocationUpdate();

            }
        });


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
        if(key.equals(LocationResultHelper.KEY_LOCATION_RESULTS)){
            outputText.setText(LocationResultHelper.getLastSavedLocationResults(this));

        }
    }
}