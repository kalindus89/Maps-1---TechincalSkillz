package com.techincalskillz;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class LocationResultHelper {

    public static final String KEY_LOCATION_RESULTS = "key-result-location";
    private Context mContext;
    private List<Location> mLocationsList;

    public LocationResultHelper(Context mContext, List<Location> mLocationsList) {
        this.mContext = mContext;
        this.mLocationsList = mLocationsList;
    }

    public String getLocationResultText() {

        if (mLocationsList.isEmpty()) {
            return "Location not received";
        } else {
            StringBuilder sb = new StringBuilder();
            for (Location location : mLocationsList) {
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


    private CharSequence getLocationResultTitle() {
        String results = mContext.getResources().getQuantityString
                (R.plurals.num_location_reported, mLocationsList.size(), mLocationsList.size()); // return the size of mLocationList with text
        return results + " : " + DateFormat.getDateTimeInstance().format(new Date());
    }

    public void showNotification() {

        Intent notificationIntent = new Intent(mContext, MapFragmentActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MapFragmentActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = null;

        notificationBuilder =
                new NotificationCompat.Builder(mContext, App.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_add_location_24) // small icon on top
                        .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.logo_app_pink)) // must be image(png/jpg)
                        .setColor(Color.parseColor("#FF4081"))// color of small iocn
                        .setContentTitle(getLocationResultTitle())
                        .setContentText(getLocationResultText())
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setContentIntent(pendingIntent);

        getNotificationManager().notify(0 /* ID of notification */, notificationBuilder.build());

    }

    private NotificationManager getNotificationManager() {

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager;
    }


    public void saveLastLocationResults() {

        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(KEY_LOCATION_RESULTS, getLocationResultTitle() + "\n" + getLocationResultText())
                .apply();
    }

    public static  String getLastSavedLocationResults(Context context) {

        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_RESULTS, "Values Loading...");
    }

}
