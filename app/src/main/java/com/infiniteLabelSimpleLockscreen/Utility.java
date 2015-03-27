package com.infiniteLabelSimpleLockscreen;

import android.app.Activity;
import android.hardware.SensorEvent;
import android.os.Build;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Utility extends Activity {
    private static String HOST = "";
    private static int PORT = 8888;
    private static Socket socket = null;
    private static PrintWriter out = null;
    private static String deviceID = "";
    public static String complexGestureString = "0,1,2,3,4,5,6,7,8";

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
        deviceID = getUniquePsuedoID();
        ParseQuery<ParseObject> query;
        query = ParseQuery.getQuery("Parameters");
        query.getInBackground("G9vWUL34Sa", new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    HOST = object.getString("IP_Address");
                    System.out.println(HOST);
                } else {
                    // something went wrong
                }
            }
        });
        query = ParseQuery.getQuery("ComplexGesture");
        query.whereEqualTo("deviceID", deviceID);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    // No result
                } else {
                    complexGestureString = object.getString("GestureString");
                }
            }
        });
    }

    public static void socketWrite(final String action, final int deltaX, final int deltaY, final SensorEvent accEvent) {
        parseWrite(action);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    System.out.println(action);
                    socket = new Socket(HOST, PORT);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    out.write(action + ", ");
                    out.write(deviceID + ", ");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    out.write(dateFormat.format(new Date()) + ", ");
                    out.write(Float.toString(accEvent.values[0]) + ", " + Float.toString(accEvent.values[1]) + ", " + Float.toString(accEvent.values[2]) + ", ");
                    out.write(String.valueOf(deltaX) + ", " + String.valueOf(deltaY));
                    out.flush();
                    out.close();
                    socket.close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void parseWrite(final String action) {
        ParseObject parseObj = new ParseObject("InfiniteLabel");
        parseObj.put("deviceID", deviceID);
        parseObj.put("action", action);
        parseObj.saveInBackground();
    }

    public static String gestureNumToString(int gesture) {
        switch(gesture) {
            case 0:
                return "SwipeRight";
            case 1:
                return "SwipeUp";
            case 2:
                return "SwipeLeft";
            case 3:
                return "SwipeDown";
            default:
                return "";
        }
    }
}
