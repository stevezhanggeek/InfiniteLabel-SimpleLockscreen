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

public class NaturalActivity extends Activity implements SensorEventListener {
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

    @Override
    protected void onResume() {
        super.onResume();
        Utility.initParameters();
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

        // Enable Local Datastore.
        //Parse.enableLocalDatastore(this);
        Parse.initialize(this, "9DNSMkDuMcOv0Mi918JSe1CfMlkBPQ9UJVp8ksQB", "Qo53m1lBF7kXzbdP0OJ8bbL1OH6AuJnZbFRyOI4K");

        glowPad.setOnTriggerListener(new GlowPadView.OnTriggerListener() {
            @Override
            public void onGrabbed(View v, int handle) {
                System.out.println("Start Touch");
            }

            @Override
            public void onReleased(View v, int handle) {
            }

            @Override
            public void onTrigger(View v, int target) {
                switch(target) {
                    case 1:
                        Utility.socketWrite("SwipeUp", 0, 0, accEvent);
                        glowPad.reset(true);
                        v.setVisibility(View.GONE);
                        finish();
                        break;
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
                final TextView txt = (TextView) findViewById(R.id.textView);
                String sleepiness_description = "";
                switch(target) {
                    case 1:
                        sleepiness_description = "Unlock";
                        break;
                    default:
                        sleepiness_description = "Please Swipe Up to Unlock";
                        break;
                }
                txt.setText(sleepiness_description);
            }
        });
    }
}
