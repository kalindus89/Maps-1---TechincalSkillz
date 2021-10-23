package com.techincalskillz;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class MyBackgroundLocationService extends Service {
    private FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallBack;

    public MyBackgroundLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }

                List<Location> locations = locationResult.getLocations(); //  get set of locations in given time period(here 15 seconds. check below method) and set to list
                LocationResultHelper locationResultHelper = new LocationResultHelper(getApplicationContext(), locations);
                locationResultHelper.showNotification();
                locationResultHelper.saveLastLocationResults();

                //  Log.d("aaaaaaa1",locationResult.getLastLocation().getLatitude() + " Longitude: " + locationResult.getLastLocation().getLongitude());
            }
        };

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("aaaaaa ", "onStartCommand: ");

        getLocationUpdates();

        startForeground(1001, getNotification());

        return START_STICKY; // where the service is left "started" and will later be restarted by the system.
    }

    private Notification getNotification() {

        NotificationCompat.Builder notificationBuilder = null;

        notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), App.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_add_location_24) // small icon on top
                        .setContentTitle("Location Notification")
                        .setContentText("Location Service is running in the background")
                        .setAutoCancel(true);

        return notificationBuilder.build();
    }

    private void getLocationUpdates() {

        // from requestBatchLocationUpdate() in BatchLocationActivity.class
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // location update time
        locationRequest.setFastestInterval(3000); // location update time

        locationRequest.setMaxWaitTime(15 * 1000); // can save battery usage. here update every 15s


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            stopSelf(); //stop service if permission is not granted
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.myLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("aaaaaa", "onDestroy: ");
        stopForeground(true);
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        }
    }
}