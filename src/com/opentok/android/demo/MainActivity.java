package com.opentok.android.demo;

import com.opentok.android.demo.controlbar.ControlBarActivity;
import com.opentok.android.demo.helloworld.HelloWorldActivity;

import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Main demo app for getting started with the OpenTok Android SDK.
 * It contains:
 * - a basic hello-world activity
 * - a basic hello-world activity with control bar with stream name and action buttons to switch camera and audio mute
 */
public class MainActivity extends Activity {

    private static final String LOGTAG = "demo-opentok-sdk";
    private WakeLock wakeLock;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
       
        final ListView listActivities = (ListView) findViewById(R.id.listview);
        String[] activityNames = {
            getString(R.string.helloworld),
            getString(R.string.controlbar)
        };
        
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_1, activityNames);
        listActivities.setAdapter(adapter);
              
        listActivities.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                // these positions are hard-coded to some example activities, they match
                // the array contents of activityNames above.
                if (0 == position) {
                    startHelloWorldApp();
                } else if (1 == position) {
                    startControlBarApp();
                } else {
                    Log.wtf(LOGTAG, "unknown item clicked?");
                }
            }
        });
       
        // Disable screen dimming
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Full Wake Lock");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onStop() {

        super.onStop();

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }

    }
    @Override
    public void onResume() {

        super.onResume();

        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {

        super.onPause();

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    /**
     * Starts the Hello-World demo app. See HelloWorldActivity.java
     */
    public void startHelloWorldApp() {

        Log.i(LOGTAG, "starting hello-world app");

        Intent intent = new Intent(MainActivity.this, HelloWorldActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Starts the Control Bar demo app. See ControlBarActivity.java
     */
    public void startControlBarApp() {

        Log.i(LOGTAG, "starting control bar app");

        Intent intent = new Intent(MainActivity.this, ControlBarActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}