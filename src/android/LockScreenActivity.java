package org.waigeo.cordova.alarm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileDescriptor;

import java.util.Collections;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.content.Intent;

import org.apache.cordova.*;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;

public class LockScreenActivity extends CordovaActivity {

    private static final String TAG = "LockScreenActivity";
    private float lastVolumeValue = 0.1f;
    private MediaPlayer mediaPlayer;
    private LinearLayout layout;
    private SystemWebView webView;
    private Handler volumeIncrease;
    private String viewUrl;
    private String musicUrl;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent thisIntent = getIntent();
        this.viewUrl = thisIntent.getStringExtra("viewUrl");
        this.musicUrl = thisIntent.getStringExtra("musicUrl");
        
        showOnLockScreen();

        Log.d(TAG, "Adding webview container");
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        setContentView(layout);

        Log.d(TAG, "Starting WebApp");
        super.init();
        if (this.viewUrl != null) {
            loadUrl(this.viewUrl);
        } else {
            loadUrl(launchUrl);
        }

        Log.d(TAG, "Notify with sound and vibrator");
        startVibrator();
        startRingTone();

        this.preferences.set("alarmFired", true);
    }

    @Override
    protected CordovaWebView makeWebView() {
        webView = new SystemWebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        layout.addView(webView);
        return new CordovaWebViewImpl(new SystemWebViewEngine(webView, this.preferences));
    }

    @Override
    protected void createViews() {
        appView.getView().requestFocusFromTouch();
    }

    @Override
    public void onDestroy() {

        Log.d(TAG, "LockScreenActivity onDestroy");
        this.viewUrl = null;
        this.musicUrl = null;

        if(volumeIncrease != null) volumeIncrease.removeCallbacksAndMessages(null);
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        layout.removeView(webView);
        webView.removeAllViews();
        webView.destroy();

        super.onDestroy();
    }

    private void showOnLockScreen() {
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
    }

    private void startVibrator() {
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(2000);
    }

    private void startRingTone() {
        if (this.musicUrl != null) {
            Log.d(TAG, "Starting music (" + this.musicUrl + ")");
            try {
                AssetFileDescriptor afd = getAssets().openFd(this.musicUrl);
                if(afd == null) return;
                Log.d(TAG, "afd.toString()" + afd.toString());

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(lastVolumeValue, lastVolumeValue);
                mediaPlayer.prepare();
                // descriptor.close();
                mediaPlayer.start();
            } catch (Exception e) {
                Log.e(TAG, "Issue on the music player");
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Music set to default");
            Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            mediaPlayer = MediaPlayer.create(this, defaultRingtoneUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(lastVolumeValue, lastVolumeValue);
            mediaPlayer.start();
        }

        scheduleVolumeIncrease();
    }

    private void scheduleVolumeIncrease() {
        volumeIncrease = new Handler();
        volumeIncrease.postDelayed(new Runnable() {
            public void run() {

                lastVolumeValue = Math.min(lastVolumeValue + 0.1f, 1f);
                if(mediaPlayer != null) mediaPlayer.setVolume(lastVolumeValue, lastVolumeValue);

                scheduleVolumeIncrease();
            }
        }, 4000);
    }
}
