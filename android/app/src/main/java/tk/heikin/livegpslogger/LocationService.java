package tk.heikin.livegpslogger;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationService extends Service {

    public static final String LocationServiceName = "tk.heikin.livegpslogger.LocationService";

    private String trackingId = "none";

    private String TAG = "LocationService";

    private int notifyId = 65432356;

    private Location last = null;

    private LocationManager locationManager = null;
    private LocationListener locationListener = null;

    private PowerManager pm = null;
    private PowerManager.WakeLock wl = null;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    @TargetApi(16)
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Started Service");
        Notification noti;

        int sdk = Build.VERSION.SDK_INT;

        if (sdk <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            noti = new Notification.Builder(this.getApplicationContext())
                    .setContentTitle("Location tracking on background")
                    .getNotification();
        } else {
            noti = new Notification.Builder(this.getApplicationContext())
                    .setContentTitle("Location tracking on background")
                    .build();
        }

        this.startForeground(notifyId, noti);

        pm = (PowerManager) this.getApplicationContext().getSystemService(Context.POWER_SERVICE);

        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Location tracking");
        wl.acquire();

        Log.d(TAG, "Wake lock acquired");

        trackingId = intent.getStringExtra("trackingId");
        Log.d(TAG, intent.getExtras().toString());

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener(trackingId, this.getApplicationContext());

        // This method is used to get updated location.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0,
                locationListener);
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
        //        locationListener);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "Destroying service!");
        this.stopForeground(true);

        locationManager.removeUpdates(locationListener);

        if (wl != null) {
            wl.release();
            wl = null;
            Log.d(TAG, "Wake lock released");
        }

    }
}
