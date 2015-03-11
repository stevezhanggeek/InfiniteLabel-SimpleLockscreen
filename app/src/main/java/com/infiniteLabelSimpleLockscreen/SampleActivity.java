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
import java.util.UUID;

class HttpAsyncTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... urls) {
        return GET(urls[0]);
    }

    private String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null) result = convertInputStreamToString(inputStream);
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        JSONObject init_parameters = null;
        try {
            init_parameters = new JSONObject(result);
            String server_ip = init_parameters.getString("server_ip");
            SampleActivity.setServerIP(server_ip);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }
}

public class SampleActivity extends Activity implements SensorEventListener {
    private static String HOST = "";
    private static int PORT = 8888;
    private static Socket socket = null;
    private static PrintWriter out = null;
    private static String deviceID = "";

    // Sensor
    SensorManager mSensorManager;
    Sensor mGyroSensor, mAccSensor;
    static SensorEvent gyroEvent, accEvent;

    /**
     * Return pseudo unique ID
     * @return ID
     */
    public static String getUniquePsuedoID() {
        String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);
        String serial = null;
        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            serial = "serial"; // some value
        }
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

    public static void initParameters() {
        new HttpAsyncTask().execute("http://xiaoyizhang.me/Research/infiniteLabel.json");
    }

    public static void setServerIP(String ip){
        HOST = ip;
        System.out.println(HOST);
    }

    public static void socketWrite(final String action, final int deltaX, final int deltaY){
        ParseObject parseObj = new ParseObject("InfiniteLabel");
        parseObj.put("deviceID", deviceID);
        parseObj.put("action", action);
        parseObj.saveInBackground();

        new Thread(new Runnable()
        {
            @Override
            public void run() {
                initParameters();
                // TODO Auto-generated method stub
                try
                {
                    System.out.println("InfiniteLabel-" + action);
                    System.out.println("Acc[0]: " + Float.toString(accEvent.values[0]));
                    System.out.println("Delta X, Y: " + String.valueOf(deltaX) + ", " + String.valueOf(deltaY));

                    socket = new Socket(HOST, PORT);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    out.write(action+", ");
                    out.write(Float.toString(accEvent.values[0])+", "+Float.toString(accEvent.values[1])+", "+Float.toString(accEvent.values[2])+", ");
                    out.write(String.valueOf(deltaX)+", "+String.valueOf(deltaY));
                    out.flush();
                    out.close();
                    socket.close();
                }
                catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

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

        initParameters();

        deviceID = getUniquePsuedoID();

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
                        socketWrite("SwipeUp", 0, 0);
                        glowPad.reset(true);
//                        v.setVisibility(View.GONE);
//                        finish();
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
