package tk.heikin.livegpslogger;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import fr.quentinklein.slt.LocationTracker;
import fr.quentinklein.slt.TrackerSettings;

public class LocationService extends Service {

    public static final String LocationServiceName = "tk.heikin.livegpslogger.LocationService";

    private String TAG = "LocationService";

    private Location last = null;

    TrackerSettings settings =
            new TrackerSettings()
                    .setUseGPS(true)
                    .setUseNetwork(true)
                    .setUsePassive(true)
                    .setTimeBetweenUpdates(1000)
                    .setMetersBetweenUpdates(1);

    LocationTracker locationTracker;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Started Service");

        final String trackingId = intent.getStringExtra("trackingId");
        Log.d(TAG, intent.getExtras().toString());

        locationTracker = new LocationTracker(this.getApplicationContext(), settings) {

            @Override
            public void onLocationFound(final Location location) {
                // Do some stuff when a new location has been found.

                new Thread( new Runnable() {
                    @Override
                    public void run() {
                        try {

                            if (location.equals(last)) {
                                return;
                            }

                            last = location;

                            String query = "lat="+location.getLatitude()+"&lon="+location
                                    .getLongitude()+"&acc="+location.getAccuracy()
                                    +"&c="+trackingId+"&time="+location.getTime();

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

            @Override
            public void onTimeout() {
                Log.e(TAG, "Timed out!");
            }
        };

        locationTracker.startListen();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        locationTracker.stopListen();
    }
}
