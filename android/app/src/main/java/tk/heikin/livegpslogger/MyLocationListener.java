package tk.heikin.livegpslogger;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by heikki on 8.9.2015.
 */
public class MyLocationListener implements LocationListener {

    private static String TAG = "MyLocationListener";

    private String trackingId = "none";

    private String server = "https://gps.virekunnas.fi/";

    private Context context = null;

    private static LocalBroadcastManager broadcaster = null;

    private static int pointsFound = 0;
    public static int pointsBuffered = 0;

    private Location last = null;

    public static List<URL> buffer = Collections.synchronizedList(new ArrayList<URL>());

    public static boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    public MyLocationListener(String trackingIdin, String serverIn, Context contextin) {
        trackingId = trackingIdin;
        context = contextin;
        server = serverIn;
        Log.d(TAG, "Tracking ID: "+trackingId);
        broadcaster = LocalBroadcastManager.getInstance(context.getApplicationContext());
        sendStatus("Waiting for GPS...", false, false);
    }

    public static boolean sendStatus(String message, boolean increment, boolean buffered) {
        Intent intent = new Intent("location.status");
        if(message != null) {
            intent.putExtra("status", message);
        }
        if (increment) {
            intent.putExtra("points", ++pointsFound);
        } else {
            intent.putExtra("points", pointsFound);
        }
        if (buffered) {
            intent.putExtra("bufferedPoints", ++pointsBuffered);
        } else {
            intent.putExtra("bufferedPoints", pointsBuffered);
        }
        Log.d(TAG, "Status: " + message);
        return broadcaster.sendBroadcast(intent);
    }

    public void onLocationChanged(final Location location) {

        //Toast toast = Toast.makeText(context, "Got new location", Toast.LENGTH_SHORT);
        //toast.show();

        new Thread( new Runnable() {
            @Override
            public void run() {
                try {

                    if (last != null && location.getTime() <= (last.getTime()+1000)) {
                        Log.d(TAG, "Old timestamp: "+last.getTime()+", new: "+location.getTime());
                        return;
                    } else if (last != null) {
                        Log.d(TAG, "Old: "+last.getTime()+", new: "+location.getTime());
                    } else {
                        Log.d(TAG, "Last null");
                    }

                    last = location;

                    // send time in seconds instead of milliseconds
                    String query = "lat="+location.getLatitude()+"&lon="+location
                            .getLongitude()+"&acc="+location.getAccuracy()
                            +"&c="+trackingId+"&time="+(location.getTime()/1000);

                    Log.v(TAG, "Query: "+query);
                    URL url = new URL(server + "save.php?" + query);

                    try {

                        if (!isOnline(context)) {
                            buffer.add(url);
                            sendStatus("No network", false, true);
                        } else {

                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            //connection.setRequestProperty("Cookie", cookie);
                            Log.d(TAG, connection.toString());
                            //Set to POST
                            connection.setDoOutput(true);
                            connection.setRequestMethod("POST");
                            connection.setReadTimeout(10000);

                            if (connection.getResponseMessage().equals("OK")) {
                                Log.d(TAG, "Sent POST");
                                //Log.d(TAG, "Broadcast: " + sendStatus("OK: " + location.getTime()));
                                sendStatus("OK", true, false);
                            } else {
                                Log.e(TAG, "Error sending!");
                                //Log.d(TAG, "Broadcast: " + sendStatus("Error: " + location.getTime()));
                                sendStatus("Error sending!", false, false);
                            }

                            connection.disconnect();
                        }
                    } catch (UnknownHostException e) {
                        buffer.add(url);
                        sendStatus("Internet error!", false, false);
                    } catch (ConnectException e) {
                        buffer.add(url);
                        sendStatus("Internet error!", false, false);
                    }


                    //Log.d(TAG, "Sent POST");

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Log.e(TAG, e.toString());
                }
            }
        }).start();
    }

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
        //Toast.makeText(context,"GPS Disabled", Toast.LENGTH_LONG).show();
        Log.d(TAG, "Provider disabled");
    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
        //Toast.makeText(context,"GPS enabled", Toast.LENGTH_LONG).show();
        Log.d(TAG, "Provider enabled");
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
        Log.d(TAG, "Status changed: "+status);
    }

}
