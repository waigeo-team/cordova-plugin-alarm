package org.waigeo.cordova.alarm;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Date;
import java.text.SimpleDateFormat;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import android.os.Environment;
import android.Manifest;
import android.os.Build;

public class Alarm extends CordovaPlugin {

    private static final String TAG = "Alarm";

    private String viewUrl = null;
    private String musicUrl = null;

    @Override
    public boolean execute(String action, JSONArray data, final CallbackContext callbackContext) {
        try {
            JSONObject options = data.getJSONObject(0);
            if (action.equals("add")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date alarmDate = sdf.parse((String) options.get("date"));
                if (options.has("viewUrl")) this.viewUrl = (String) options.get("viewUrl");
                if (options.has("musicUrl")) this.musicUrl = (String) options.get("musicUrl");
                addAlarm(alarmDate);
                callbackContext.success("added for: " + alarmDate.toString());
                return true;
            }

            if (action.equals("remove")) {
                Intent intent = new Intent(this.cordova.getActivity(), AlarmReceiver.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this.cordova.getActivity(), 0, intent, 0);
                AlarmManager alarmManager = (AlarmManager) (this.cordova.getActivity().getSystemService(Context.ALARM_SERVICE));
                alarmManager.cancel(pendingIntent);
                callbackContext.success("removed");
                return true;
            }

            if (action.equals("stop")) {
                LockScreenActivity lockScreenActivity = (LockScreenActivity) this.cordova.getActivity();
                lockScreenActivity.finish();
                startAppInBackground();
                callbackContext.success("true");
                this.viewUrl = null;
                this.musicUrl = null;
                return true;
            }

            if (action.equals("snooze")) {
                int snoozeMinutes = 10;
                if (options.has("snoozeMinutes")) this.snoozeMinutes = (int) options.get("snoozeMinutes");
                Date nextReminder = new Date((new Date()).getTime() + 60000 * snoozeMinutes);
                addAlarm(nextReminder);
                LockScreenActivity lockScreenActivity = (LockScreenActivity) this.cordova.getActivity();
                lockScreenActivity.finish();
                startAppInBackground();
                callbackContext.success("true");
                return true;
            }

            if (action.equals("isFromAlarmTrigger")) {
                if (this.cordova.getActivity().getClass() == LockScreenActivity.class) {
                    callbackContext.success("true");
                }else{
                    callbackContext.success("false");
                }
                return true;
            }

            return false;

        } catch (Exception e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
            return false;
        }
    }

    /**
     * On lance l'application normalement après le déverouillage
     */
    private void startAppInBackground(){
        Intent intent = new Intent();
        intent.setAction("org.waigeo.cordova.alarm.ALARM");
        intent.setPackage(this.cordova.getActivity().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.cordova.getActivity().startActivity(intent);
    }

    private void addAlarm(Date time) {

        Intent intent = new Intent(this.cordova.getActivity(), AlarmReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("viewUrl", this.viewUrl);
        intent.putExtra("musicUrl", this.musicUrl);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.cordova.getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        AlarmManager alarmManager = (AlarmManager) (this.cordova.getActivity().getSystemService(Context.ALARM_SERVICE));
        alarmManager.cancel(pendingIntent);
        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(time.getTime(), pendingIntent);
        alarmManager.setAlarmClock(clockInfo, pendingIntent);
    }
}
