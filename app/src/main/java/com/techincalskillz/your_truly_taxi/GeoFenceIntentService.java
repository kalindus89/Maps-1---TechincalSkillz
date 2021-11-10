package com.techincalskillz.your_truly_taxi;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GeoFenceIntentService extends IntentService {


    public GeoFenceIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        System.out.println("TAG "+"aaaaaaaaaaa");
        Log.d("TAG", "GeoFenceBroadcastReceiver: " );

        Toast.makeText(this, "Geo fence triggered", Toast.LENGTH_LONG).show();
    }
}