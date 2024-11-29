package com.example.clock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.util.Calendar;
import java.util.List;

public class AlarmClockFragment extends AppCompatActivity {

    private Spinner soundSpinner;
    private Button setAlarmButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarmfrag);  // Use the same layout file for the activity

        soundSpinner = findViewById(R.id.soundSpinner);
        setAlarmButton = findViewById(R.id.setAlarmButton);
        cancelButton = findViewById(R.id.cancelButton);  // Assuming you have a cancel button in the XML layout

        // Set up the spinner with sound options
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Alarm", "Beep", "Bell", "Chime", "Tone"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        soundSpinner.setAdapter(adapter);

        setActiveAlarms();

        // Set up the button listener to set the alarm
        setAlarmButton.setOnClickListener(v -> {
            setAlarm();
            navigateBackToAlarmListActivity();  // Navigate back after setting the alarm
        });

        // Set up the button listener for canceling
        cancelButton.setOnClickListener(v -> navigateBackToAlarmListActivity());  // Navigate back without setting the alarm
    }

    private void setAlarm() {
        // Get the selected sound URI
        Uri soundUri = getSelectedSoundUri();

        // Get the time selected by the user in the TimePicker
        TimePicker timePicker = findViewById(R.id.timePicker);
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        // Create a Calendar instance for the selected time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);   // Set the hour from the TimePicker
        calendar.set(Calendar.MINUTE, minute);      // Set the minute from the TimePicker
        calendar.set(Calendar.SECOND, 0);           // Set seconds to 0 for the alarm
        calendar.set(Calendar.MILLISECOND, 0);      // Set milliseconds to 0 for consistency

        // Store the full timestamp in milliseconds (absolute time)
        long alarmTimeInMillis = calendar.getTimeInMillis();

        // Initialize a variable for alarmId
        int alarmId = (int) alarmTimeInMillis; // Use the alarm time as a unique identifier (can be customized)

        // Store alarm in Room database with the full timestamp (absolute time)
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "alarm_db").build();
            AlarmEntity alarm = new AlarmEntity();
            alarm.time = alarmTimeInMillis-3500;  // Store the full timestamp
            alarm.soundUri = soundUri.toString();
            alarm.id = alarmId;              // Assign the unique alarmId
            db.alarmDao().insertAlarm(alarm);
        }).start();

        // Set the alarm using AlarmManager
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            // Check for exact alarm permissions (Android 12 and higher)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    // Notify the user that exact alarms aren't allowed
                    Toast.makeText(this, "Exact alarms are not allowed. Please enable the permission in settings.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent);  // Launch the settings activity for exact alarm permission
                } else {
                    // Set the exact alarm with the unique alarmId
                    setExactAlarm(alarmManager, soundUri, alarmTimeInMillis, alarmId);
                }
            } else {
                // For Android versions below 12, proceed with setting the alarm directly
                setExactAlarm(alarmManager, soundUri, alarmTimeInMillis, alarmId);
            }
        }
    }

    private void navigateBackToAlarmListActivity() {
        // Navigate back to the AlarmListActivity after the button click
        Intent intent = new Intent(AlarmClockFragment.this, AlarmListActivity.class);
        startActivity(intent);
        finish();  // Optionally call finish to remove this activity from the stack
    }


    private void setActiveAlarms() {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabaseSingleton.getInstance(this);
                List<AlarmEntity> alarms = db.alarmDao().getAllAlarms(); // Fetch all alarms

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                long currentTime = System.currentTimeMillis();

                if (alarmManager != null) {
                    for (AlarmEntity alarm : alarms) {
                        Log.d("AlarmClockFragment", "Loaded alarm ID: " + alarm.id + ", Time: " + alarm.time);

                        if (alarm.isActive && alarm.time > currentTime) {
                            // Set the alarm only if it's active and time is in the future
                            setExactAlarm(alarmManager, Uri.parse(alarm.soundUri), alarm.time, alarm.id);
                        } else {
                            Log.d("AlarmClockFragment", "Skipping alarm ID: " + alarm.id + " as it's in the past or inactive.");
                        }
                    }
                } else {
                    Log.e("AlarmClockFragment", "AlarmManager is null.");
                }
            } catch (Exception e) {
                Log.e("AlarmClockFragment", "Error in setActiveAlarms: ", e);
            }
        }).start();
    }





    private void setExactAlarm(AlarmManager alarmManager, Uri soundUri, long alarmTimeInMillis, int alarmId) {
        try {
            long currentTime = System.currentTimeMillis();

            // If the alarm time has passed for today, set it for the next day
            if (alarmTimeInMillis <= currentTime) {
                alarmTimeInMillis += 86400000; // Add one day (in milliseconds)
            }

            // Save alarm details in SharedPreferences (optional, for debugging or user reference)
            SharedPreferences prefs = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("alarmTime_" + alarmId, alarmTimeInMillis);
            editor.putString("soundUri_" + alarmId, soundUri.toString());
            editor.apply();

            // Create the intent for the AlarmReceiver
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("ALARM_SOUND_URI", soundUri.toString());
            intent.putExtra("ALARM_ID", alarmId);

            // Create a unique PendingIntent for each alarm
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    alarmId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Schedule the alarm
            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeInMillis,
                        pendingIntent
                );
            }

            // Show a Toast on the main thread
            runOnUiThread(() ->
                    Toast.makeText(this, "Alarm set successfully!", Toast.LENGTH_SHORT).show()
            );

            Log.d("AlarmClockFragment", "Alarm set with ID: " + alarmId + " at " + alarmTimeInMillis);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }




    private Uri getSelectedSoundUri() {
        String selectedSound = soundSpinner.getSelectedItem().toString();

        // Define the mapping from sound names to URIs
        switch (selectedSound) {
            case "Alarm":
                return Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm);
            case "Beep":
                return Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.beep);
            case "Bell":
                return Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bell);
            case "Chime":
                return Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.chime);
            case "Tone":
                return Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tone);
            default:
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); // Fallback sound
        }
    }
}