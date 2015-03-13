package com.infiniteLabelSimpleLockscreen;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.Parse;

import net.frakbot.glowpadbackport.GlowPadView;

public class LockscreenActivity extends Activity implements SensorEventListener {
    private int[] gestureList = {1,3};
    private int indexCurrentGesture = 0;
    private boolean isPromptSession = false;

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
        Intent myIntent = getIntent();
        String naturalOrPrompt = myIntent.getStringExtra("naturalOrPrompt");
        if (naturalOrPrompt != null && naturalOrPrompt.equals("prompt")) {
            int[] tempGestureList = {0,1,3,2,0,2,2,1};
            gestureList = tempGestureList;
            isPromptSession = true;
            Utility.parseWrite("Start Prompt Session");
        }

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
                    if (isPromptSession) {
                        Utility.parseWrite("Finish Prompt Session");
                    }
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