package com.techincalskillz.your_truly_taxi;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;
import com.techincalskillz.mitch_2017_easy_learn_android.BatchLocationActivity;

public class GeoFenceHelper extends ContextWrapper {

    private static final String tag="GeofenceHelper";
    PendingIntent pendingIntent;

    public GeoFenceHelper(Context base) {
        super(base);
    }

    public GeofencingRequest getGeofencingRequest(Geofence geofence){

        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER) // Trigger as soon as enter to geo fence
                .build();
    }

    public Geofence getGeofence(String id, LatLng latLng, float radius, int transitionTypes){
        return new Geofence.Builder()
                .setCircularRegion(latLng.latitude,latLng.longitude,radius)
                .setRequestId(id)
                .setTransitionTypes(transitionTypes) // 3 types. enter, dwell(inside), exist
                .setLoiteringDelay(5000) // here, every 5 seconds update the status.(enter, dwell(inside), exist)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    public PendingIntent getPendingIntent(){

        if(pendingIntent!=null){

            return pendingIntent;
        }

       Intent intent= new Intent(this,GeoFenceBroadcastReceiver.class);

        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public String getErrorString(Exception e){
        if(e instanceof ApiException){
            ApiException apiException = (ApiException) e;

            switch (apiException.getStatusCode()){
                case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                    return "GEO_FENCE_NOT_AVAILABLE";
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                    return "GEOFENCE_TOO_MANY_GEOFENCES";
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "GEOFENCE_TOO_MANY_PENDING_INTENTS";
            }

        }
        return e.getLocalizedMessage();
    }


}
