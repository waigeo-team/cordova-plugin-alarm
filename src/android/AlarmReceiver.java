package org.waigeo.cordova.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent pIntent) {
        Log.v(TAG, "Alarm received");

        Intent intent = new Intent(context, LockScreenActivity.class);
        intent.putExtra("viewUrl", pIntent.getStringExtra("viewUrl"));
        intent.putExtra("musicUrl", pIntent.getStringExtra("musicUrl"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        context.startActivity(intent);
    }
}
