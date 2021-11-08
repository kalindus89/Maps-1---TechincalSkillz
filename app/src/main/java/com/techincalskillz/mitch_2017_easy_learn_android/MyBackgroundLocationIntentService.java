package com.techincalskillz.mitch_2017_easy_learn_android;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationResult;
import com.techincalskillz.App;
import com.techincalskillz.R;

import java.util.List;


public class MyBackgroundLocationIntentService extends IntentService {

    public static final String ACTION_PROCESS_UPDATES = "com.techincalskillz"+".PROCESS_UPDATES";

    public MyBackgroundLocationIntentService() {
        super("MyBackgroundLocationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d("aaaaaa ", "onHandleIntent: ");

        startForeground(1001,getNotification());
            if(intent!=null){
                if(ACTION_PROCESS_UPDATES.equals(intent.getAction())){

                    LocationResult locationResult= LocationResult.extractResult(intent);
                    if(locationResult!=null) {
                        List<Location> locations = locationResult.getLocations(); //  get set of locations in given time period(here 15 seconds. check below method) and set to list
                        LocationResultHelper locationResultHelper = new LocationResultHelper(getApplicationContext(), locations);
                        locationResultHelper.showNotification();
                        locationResultHelper.saveLastLocationResults();

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Location Received", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
            }

    }

    private Notification getNotification() {

        NotificationCompat.Builder notificationBuilder = null;

        notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), App.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_add_location_24) // small icon on top
                        .setContentTitle("Location Notification")
                        .setContentText("Location Intent Service is running in the background")
                        .setAutoCancel(true);

        return notificationBuilder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopForeground(false);
        Log.d("aaaaaa", "onDestroy: ");
    }
}