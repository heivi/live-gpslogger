package tk.heikin.livegpslogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static String TAG = "NetworkChangeReceiver";

    public NetworkChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Network changed");

        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifi.isAvailable() || mobile.isAvailable()) && MyLocationListener.isOnline(context)) {

            Log.d(TAG, "Network available!");

            new Thread( new Runnable() {
                @Override
                public void run() {
                    if (MyLocationListener.buffer.size() == 0) {
                        return;
                    }
                    // copy urls to local buffer
                    List<URL> localbuffer = new ArrayList<URL>(MyLocationListener.buffer.size());
                    for (URL url : MyLocationListener.buffer) {
                        localbuffer.add(url);
                    }
                    MyLocationListener.buffer.clear();
                    for (int i = 0; i < localbuffer.size(); i++) {
                        Log.d(TAG, "Sending from buffer "+i);
                        HttpURLConnection connection = null;
                        try {
                            connection = (HttpURLConnection) localbuffer.get(i).openConnection();
                        } catch (IOException e) {
                            MyLocationListener.pointsBuffered--;
                            e.printStackTrace();
                        }
                        //connection.setRequestProperty("Cookie", cookie);
                        Log.d(TAG, connection.toString());
                        //Set to POST
                        connection.setDoOutput(true);
                        try {
                            connection.setRequestMethod("POST");
                        } catch (ProtocolException e) {
                            MyLocationListener.pointsBuffered--;
                            e.printStackTrace();
                        }
                        connection.setReadTimeout(10000);

                        //Log.d(TAG, connection.getResponseMessage());

                        try {
                            if (connection.getResponseMessage().equals("OK")) {
                                Log.d(TAG, "Sent POST");
                                //Log.d(TAG, "Broadcast: " + sendStatus("OK: " + location.getTime()));
                                MyLocationListener.pointsBuffered--;
                                MyLocationListener.sendStatus("OK", true, false);
                            } else {
                                Log.e(TAG, "Error sending!");
                                //Log.d(TAG, "Broadcast: " + sendStatus("Error: " + location.getTime()));
                                MyLocationListener.pointsBuffered--;
                                MyLocationListener.sendStatus("Error sending!", false, false);
                            }
                        } catch (IOException e) {
                            MyLocationListener.pointsBuffered--;
                            e.printStackTrace();
                        }

                        connection.disconnect();

                    }
                }
            }).start();

        }
    }
}