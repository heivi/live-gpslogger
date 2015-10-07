package tk.heikin.livegpslogger;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by heikki on 8.9.2015.
 */
public class MyLocationListener implements LocationListener {

    private String TAG = "MyLocationListener";

    private String trackingId = "none";

    private Context context = null;

    private Location last = null;

    public MyLocationListener(String trackingIdin, Context contextin) {
        trackingId = trackingIdin;
        context = contextin;
        Log.d(TAG, "Tracking ID: "+trackingId);
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

                    URL url = new URL("http://gps.heikin.tk/logger/save.php?"+query);
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    //connection.setRequestProperty("Cookie", cookie);
                    Log.d(TAG, connection.toString());
                    //Set to POST
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setReadTimeout(10000);
                    /*Writer writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(query);
                    writer.flush();
                    writer.close();*/

                    //Log.d(TAG, connection.getResponseMessage());

                    if (connection.getResponseMessage().equals("OK")) {
                        Log.d(TAG, "Sent POST");
                    } else {
                        Log.e(TAG, "Error sending!");
                    }

                    connection.disconnect();

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
