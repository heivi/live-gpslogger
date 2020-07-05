package tk.heikin.livegpslogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private LocationService locationService;
    private Intent serviceIntent;
    private String TAG = "MainActivity";
    private BroadcastReceiver receiver = null;
    private boolean registered = false;
    private String serverS = "";
    private String trackingidS = "";

    TextView status = null;
    TextView buffered = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra("status");
                int points = intent.getIntExtra("points", 0);
                int bufferedPoints = intent.getIntExtra("bufferedPoints", 0);
                // do something here.

                buffered.setText(bufferedPoints+ " points buffered");
                status.setText(s+", points sent: "+points);
                Log.d(TAG, "Status: " + s);
            }
        };

        status = (TextView) findViewById(R.id.textView4);
        buffered = (TextView) findViewById(R.id.bufferText);

        final Button button = (Button) findViewById(R.id.button);
        final Button button2 = (Button) findViewById(R.id.button2);
        final TextView server = (TextView) findViewById(R.id.textView3);

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            TextView versionTV = (TextView) findViewById(R.id.versionText);
            Date buildDate = BuildConfig.buildTime;
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            versionTV.setText("Version " + version + " (Built " + df.format(buildDate) + ")");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String serverTextTmp = sharedPreferences.getString("pref_server", "http://gps.virekunnas" +
                ".fi/");
        if (!serverTextTmp.endsWith("/")) {
            serverTextTmp = serverTextTmp+"/";
        }
        final String serverText = serverTextTmp;
        serverS = serverTextTmp;

        server.setText("Server: "+serverText);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                button.setClickable(false);
                button2.setClickable(true);

                // Perform action on click
                //startService(new Intent();
                serviceIntent = new Intent(v.getContext().getApplicationContext(), LocationService.class);
                EditText trackingID = (EditText) findViewById(R.id.trackingID);
                //TextView textView = (TextView) findViewById(R.id.textView);
                serviceIntent.putExtra("trackingId", trackingID.getText().toString());
                trackingidS = trackingID.getText().toString();
                serviceIntent.putExtra("server", serverText);
                startService(serviceIntent);
                TextView textView2 = (TextView) findViewById(R.id.textView2);
                textView2.setText("Running as "+trackingID.getText());
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                button.setClickable(true);
                button2.setClickable(false);
                // Perform action on click
                if (serviceIntent != null) {
                    stopService(serviceIntent);
                    TextView textView2 = (TextView) findViewById(R.id.textView2);
                    textView2.setText("Not running");
                }
                status.setText("Not running");
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "Activity paused!");
    }

    @Override
    public void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // send to server
                try {
                    //URL url = new URL("http://gps.heikin.tk/logger/save.php?"+query);
                    URL url = new URL(serverS + "save.php?c=" + trackingidS +
                            "&time="+System.currentTimeMillis()/1000L+"&screen=on");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
                        //Log.d(TAG, "Broadcast: " + sendStatus("OK: " + location.getTime()));
                        //sendStatus("OK", true);
                    } else {
                        Log.e(TAG, "Error sending!");
                        //Log.d(TAG, "Broadcast: " + sendStatus("Error: " + location.getTime()));
                        //sendStatus("Error sending!", true);
                    }

                    connection.disconnect();
                } catch (UnknownHostException e) {
                    //sendStatus("Internet error!", false);
                } catch (ProtocolException e) {

                } catch (IOException e) {

                }
            }
        }).start();


        /*
        final Button button = (Button) findViewById(R.id.button);
        final Button button2 = (Button) findViewById(R.id.button2);
        final TextView server = (TextView) findViewById(R.id.textView3);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String serverTextTmp = sharedPreferences.getString("pref_server", "https://gps.virekunnas" +
                ".fi/");
        if (!serverTextTmp.endsWith("/")) {
            serverTextTmp = serverTextTmp+"/";
        }
        final String serverText = serverTextTmp;

        server.setText("Server: "+serverText);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                button.setClickable(false);
                button2.setClickable(true);

                // Perform action on click
                //startService(new Intent();
                serviceIntent = new Intent(v.getContext().getApplicationContext(), LocationService.class);
                EditText trackingID = (EditText) findViewById(R.id.trackingID);
                //TextView textView = (TextView) findViewById(R.id.textView);
                serviceIntent.putExtra("trackingId", trackingID.getText().toString());
                serviceIntent.putExtra("server", serverText);
                startService(serviceIntent);
                TextView textView2 = (TextView) findViewById(R.id.textView2);
                textView2.setText("Running as " + trackingID.getText());
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                button.setClickable(true);
                button2.setClickable(false);
                // Perform action on click
                if (serviceIntent != null) {
                    stopService(serviceIntent);
                    TextView textView2 = (TextView) findViewById(R.id.textView2);
                    textView2.setText("Not running");
                }
                status.setText("Not running");
            }
        });
        */
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!registered) {
            LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver
                    (receiver, new IntentFilter("location.status"));
            registered = true;
            Log.d(TAG, "registered");
        }
    }

    @Override
    protected void onStop() {
        if (isFinishing() && registered) {
            LocalBroadcastManager.getInstance(this.getApplicationContext()).unregisterReceiver(receiver);
            registered = false;
            Log.d(TAG, "unregistered");
        }
        super.onStop();
    }
}
