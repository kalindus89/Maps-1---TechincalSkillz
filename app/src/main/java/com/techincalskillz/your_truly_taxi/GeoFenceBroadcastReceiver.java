package com.techincalskillz.your_truly_taxi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class GeoFenceBroadcastReceiver extends BroadcastReceiver {

    //This call call when geo fence is triggered

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        Log.d("TAG", "GeoFenceBroadcastReceiver: " );

        Toast.makeText(context, "Geo fence triggered", Toast.LENGTH_LONG).show();

    }

}