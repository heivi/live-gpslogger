package tk.heikin.livegpslogger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private LocationService locationService;
    private Intent serviceIntent;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.button);
        final Button button2 = (Button) findViewById(R.id.button2);

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
                startService(serviceIntent);
                TextView textView2 = (TextView) findViewById(R.id.textView2);
                textView2.setText("Running!: "+trackingID.getText());
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "Activity paused!");
    }
}
