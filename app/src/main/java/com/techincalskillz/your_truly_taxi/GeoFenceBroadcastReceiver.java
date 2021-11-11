package com.techincalskillz.your_truly_taxi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeoFenceBroadcastReceiver extends BroadcastReceiver {

    //This call call when geo fence is triggered

    @Override
    public void onReceive(Context context, Intent intent) {

       // Toast.makeText(context, "Geo fence triggered", Toast.LENGTH_LONG).show();

        NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d("TAG", "onReceive: Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences(); // help to identify which geofence user is in

        for (Geofence geofence : geofenceList) {
            Log.d("TAG", "onReceive: " + geofence.getRequestId()); // we pass the request id
        }

        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_ENTER", "GEOFENCE TRANSITION ENTER",YourTrulyActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_DWELL", "GEOFENCE TRANSITION DWELL",YourTrulyActivity.class);

                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_EXIT", "GEOFENCE TRANSITION EXIT",YourTrulyActivity.class);

                break;
        }

    }

}