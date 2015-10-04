package com.thecacophonytrust.cacophonometer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.thecacophonytrust.cacophonometer.recording.RecordingArray;
import com.thecacophonytrust.cacophonometer.recording.RecordingManager;
import com.thecacophonytrust.cacophonometer.util.LoadData;
import com.thecacophonytrust.cacophonometer.recording.PlayRecording;
import com.thecacophonytrust.cacophonometer.R;
import com.thecacophonytrust.cacophonometer.recording.Recording;
import com.thecacophonytrust.cacophonometer.recording.RecordingUploadManager;
import com.thecacophonytrust.cacophonometer.rules.Rule;
import com.thecacophonytrust.cacophonometer.rules.RulesArray;
import com.thecacophonytrust.cacophonometer.recording.RecordingDataObject;
import com.thecacophonytrust.cacophonometer.util.Update;

public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = "MainActivity.java";

    @Override
    public void onBackPressed() {
        //TODO make exit if pressed twice in a row
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "MainActivity created.");
        init();

        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            openSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "Resuming main activity");
        Update.now();
        updateText();   //Updates the text displayed on the screen for MainActivity
        displayNextRecordingStatus();
        super.onResume();
    }

    /**
     * Displays a Toast of the recording status (time until recording, device is recording, no recording found).
     */
    private void displayNextRecordingStatus(){
        String text;
        if (RecordingManager.isRecordingNow())
            text = "Device is recording now.";
        else if (RulesArray.getNextRule() == null)
            text = "No rules found for recording";
        else {
            //TODO this could be done better... but works for now.
            long timeToNextRecording = (RecordingManager.getRecordingStartTime() - System.currentTimeMillis())/1000;
            int hours = (int) (timeToNextRecording / 3600);
            int minutes = (int) (timeToNextRecording % 3600) / 60;
            int seconds = (int) timeToNextRecording % 60;
            text = String.format("Next recording starts in %02d:%02d:%02d.", hours, minutes, seconds);
        }
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Updates the text that is displayed.
     */
    private void updateText() {
        Rule nextRule = RulesArray.getNextRule();
        RecordingDataObject lastRDO = Recording.getLastRecordingDataObject();
        TextView infoTextView = (TextView) findViewById(R.id.info_text);
        TextView lastRecordingTextView = (TextView) findViewById(R.id.last_recording_text);

        if (nextRule == null){
            infoTextView.setText("No rule found for next recording");
        } else {
            String time = nextRule.getTimeAsString();
            String name = nextRule.getName();
            infoTextView.setText(String.format("Next recording at %s for rule '%s'", time, name));
        }

        if (lastRDO == null){
            lastRecordingTextView.setText("Last recording not found");
        } else {
            lastRecordingTextView.setText("Last recording from rule '"+lastRDO.getRuleName()+"'");
        }
    }

    /**
     * Opens the Settings activity
     */
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Opens the Rules activity
     */
    public void openRules(View v) {
        Intent intent = new Intent(this, RulesMenuActivity.class);
        startActivity(intent);
    }

    /**
     * Plays the last recording that was recorded in this session.
     * @param v
     */
    public void playLastRecording(View v) {
        RecordingDataObject rdo = Recording.getLastRecordingDataObject();
        if (rdo == null) {
            Toast.makeText(getApplicationContext(), "No recording found from this session", Toast.LENGTH_SHORT).show();
        } else {
            PlayRecording.play(rdo, getApplicationContext());
        }
    }

    public void init() {
        Log.i(LOG_TAG, "Initializing Cacophonometer.");
        RulesArray.clear();
        RecordingArray.clear();
        LoadData.loadSettings();
        LoadData.loadRules();
        LoadData.loadRecordings();
        RecordingManager.init(getApplicationContext());
    }
}