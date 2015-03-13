package com.infiniteLabelSimpleLockscreen;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseObject;

import net.frakbot.glowpadbackport.GlowPadView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PromptActivity extends Activity implements SensorEventListener {
    private int[] gestureList = {0,1,3,2,0,2,2,1};
    private int indexCurrentGesture = 0;

    // Sensor
    SensorManager mSensorManager;
    Sensor mGyroSensor, mAccSensor;
    static SensorEvent gyroEvent, accEvent;

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nothing...
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accEvent = event;
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroEvent = event;
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.main);

        Parse.initialize(this, "9DNSMkDuMcOv0Mi918JSe1CfMlkBPQ9UJVp8ksQB", "Qo53m1lBF7kXzbdP0OJ8bbL1OH6AuJnZbFRyOI4K");
        Utility.initParameters();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccSensor, SensorManager.SENSOR_DELAY_NORMAL);

        if(getIntent()!=null&&getIntent().hasExtra("kill")&&getIntent().getExtras().getInt("kill")==1){
            finish();
        }
        // initialize receiver
        startService(new Intent(this, MyService.class));

        // Use user desktop wallpaper in lockscreen
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        final Drawable wallpaperDrawable = wallpaperManager.getFastDrawable();
        final GlowPadView glowPad = (GlowPadView) findViewById(R.id.incomingCallWidget);
        RelativeLayout ll = (RelativeLayout) findViewById(R.id.main);
        ll.setBackground(wallpaperDrawable);

        final TextView txt = (TextView) findViewById(R.id.textView);
        txt.setText("First, please swipe " + Utility.gestureNumToString(gestureList[0]));
        glowPad.changeUnlockPosition(gestureList[0]);

        glowPad.setOnTriggerListener(new GlowPadView.OnTriggerListener() {
            String sleepiness_description = "";
            @Override
            public void onGrabbed(View v, int handle) {
                glowPad.changeUnlockPosition(gestureList[indexCurrentGesture]);
            }

            @Override
            public void onReleased(View v, int handle) {
                if (indexCurrentGesture < gestureList.length) {
                    glowPad.changeUnlockPosition(gestureList[indexCurrentGesture]);
                }
            }

            @Override
            public void onTrigger(View v, int target) {
                if (target == gestureList[indexCurrentGesture]) {
                    Utility.socketWrite(Utility.gestureNumToString(target), 0, 0, accEvent);
                    glowPad.reset(true);
                    indexCurrentGesture++;
                }

                if (indexCurrentGesture == gestureList.length) {
                    v.setVisibility(View.GONE);
                    finish();
                } else {
                    sleepiness_description = "Next, please swipe " +  Utility.gestureNumToString(gestureList[indexCurrentGesture]);
                    txt.setText(sleepiness_description);
                }
            }

            @Override
            public void onGrabbedStateChange(View v, int handle) {
            }

            @Override
            public void onFinishFinalAnimation() {
                // Do nothing
            }

            @Override
            public void onMovedOnTarget(int target) {
                if (target == gestureList[indexCurrentGesture]) {
                    sleepiness_description = "Correct";
                } else {
                    sleepiness_description = "Wrong gesture, please swipe " + Utility.gestureNumToString(gestureList[indexCurrentGesture]);
                }
                txt.setText(sleepiness_description);
            }
        });
    }
}
