package com.techincalskillz;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import java.util.List;

public class LocationResultHelper {

    private Context mContext;
    private List<Location> mLocationsList;

    public LocationResultHelper(Context mContext, List<Location> mLocationsList) {
        this.mContext = mContext;
        this.mLocationsList = mLocationsList;
    }

    public String getLocationText(){

        if(mLocationsList.isEmpty()){
            return "Location not received";
        }
        else {
            StringBuilder sb = new StringBuilder();
            for (Location location:mLocationsList){
                sb.append("(");
                sb.append(location.getLatitude());
                sb.append(", ");
                sb.append(location.getLongitude());
                sb.append(")\n");
              //  sb.append("");
            }
            return sb.toString();
        }
    }

    public void showNotification(){

        Intent notificationIntent = new Intent(mContext, MapFragmentActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MapFragmentActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0 ,PendingIntent.FLAG_ONE_SHOT);




    }
}
