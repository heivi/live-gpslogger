package tk.heikin.livegpslogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

public class MainActivity extends AppCompatActivity {

    private LocationService locationService;
    private Intent serviceIntent;
    private String TAG = "MainActivity";
    private BroadcastReceiver receiver = null;
    private boolean registered = false;

    TextView status = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra("status");
                int points = intent.getIntExtra("points", 0);
                // do something here.

                status.setText(s+", points found: "+points);
                Log.d(TAG, "Status: " + s);
            }
        };

        status = (TextView) findViewById(R.id.textView4);

        final Button button = (Button) findViewById(R.id.button);
        final Button button2 = (Button) findViewById(R.id.button2);
        final TextView server = (TextView) findViewById(R.id.textView3);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String serverTextTmp = sharedPreferences.getString("pref_server", "http://gps.virekunnas" +
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
        /*final Button button = (Button) findViewById(R.id.button);
        final Button button2 = (Button) findViewById(R.id.button2);
        final TextView server = (TextView) findViewById(R.id.textView3);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String serverTextTmp = sharedPreferences.getString("pref_server", "http://gps.virekunnas" +
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
        });*/
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
