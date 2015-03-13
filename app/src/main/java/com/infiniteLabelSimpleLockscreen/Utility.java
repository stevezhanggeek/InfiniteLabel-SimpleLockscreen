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


public class Utility {
    private static String HOST = "";
    private static int PORT = 8888;
    private static Socket socket = null;
    private static PrintWriter out = null;
    private static String deviceID = "";

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
        new HttpAsyncTask().execute("http://www.xiaoyizhang.me/Research/infiniteLabel.json");
        deviceID = Utility.getUniquePsuedoID();
    }

    public static void setServerIP(String ip) {
        HOST = ip;
        System.out.println(HOST);
    }

    public static void socketWrite(final String action, final int deltaX, final int deltaY, final SensorEvent accEvent) {
        ParseObject parseObj = new ParseObject("InfiniteLabel");
        parseObj.put("deviceID", deviceID);
        parseObj.put("action", action);
        parseObj.saveInBackground();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    System.out.println("InfiniteLabel-" + action);
                    socket = new Socket(HOST, PORT);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    out.write(action + ", ");
                    out.write(Float.toString(accEvent.values[0]) + ", " + Float.toString(accEvent.values[1]) + ", " + Float.toString(accEvent.values[2]) + ", ");
                    out.write(String.valueOf(deltaX) + ", " + String.valueOf(deltaY));
                    out.flush();
                    out.close();
                    socket.close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static String gestureNumToString(int gesture) {
        switch(gesture) {
            case 0:
                return "RIGHT";
            case 1:
                return "UP";
            case 2:
                return "LEFT";
            case 3:
                return "DOWN";
            default:
                return "";
        }
    }
}
