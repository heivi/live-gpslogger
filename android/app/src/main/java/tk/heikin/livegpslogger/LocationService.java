package tk.heikin.livegpslogger;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationService extends Service {

    public static final String LocationServiceName = "tk.heikin.livegpslogger.LocationService";

    private String trackingId = "none";

    private String server = "https://gps.virekunnas.fi/";

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

        if (sdk <= Build.VERSION_CODES.HONEYCOMB_MR2) {

        } else {
            if (sdk <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                noti = new Notification.Builder(this.getApplicationContext())
                        .setContentTitle("Location tracking on background")
                        .getNotification();
            } else if (sdk < Build.VERSION_CODES.O) {
                noti = new Notification.Builder(this.getApplicationContext())
                        .setContentTitle("Location tracking on background")
                        .build();
            } else {
                String NOTIFICATION_CHANNEL_ID = "tk.heikin.kepardigps";
                String channelName = "KeparDI GPS";
                NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
                chan.setLightColor(Color.BLUE);
                chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                assert manager != null;
                manager.createNotificationChannel(chan);

                Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);
                noti = notificationBuilder
                        .setOngoing(true)
                        .setContentTitle("KeparDI GPS is running in background")
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build();
            }

            this.startForeground(notifyId, noti);
        }

        pm = (PowerManager) this.getApplicationContext().getSystemService(Context.POWER_SERVICE);

        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Location tracking");
        wl.acquire(240*60*1000L /*240 minutes*/);

        Log.d(TAG, "Wake lock acquired");

        trackingId = intent.getStringExtra("trackingId");
        server = intent.getStringExtra("server");
        Log.d(TAG, intent.getExtras().toString());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener(trackingId, server, this.getApplicationContext());

        // This method is used to get updated location.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e(TAG, "No permission for GPS");
        }

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(locationListener);

        if (wl != null) {
            wl.release();
            wl = null;
            Log.d(TAG, "Wake lock released");
        }

    }
}
