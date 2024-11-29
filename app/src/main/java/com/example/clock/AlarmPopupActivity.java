package com.example.clock;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AlarmPopupActivity extends AppCompatActivity {

    private TextView alarmTimeText;
    private TextView alarmDateText;
    private TextView alarmLabelText;
    private Ringtone ringtone;  // Variable to hold the ringtone instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_popup);

        // Initialize the TextViews
        alarmTimeText = findViewById(R.id.alarmTimeText);
        alarmDateText = findViewById(R.id.alarmDateText);
        alarmLabelText = findViewById(R.id.alarmLabelText);

        // Set dynamic time and date (you can pass these values via the Intent if needed)
        String alarmTime = getCurrentTime();
        String alarmDate = getCurrentDate();

        alarmTimeText.setText(alarmTime);
        alarmDateText.setText(alarmDate);
        alarmLabelText.setText("Alarm");

        // Close Button
        ImageButton closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            // Stop the ringtone and close the alarm screen
            stopRingtone();
            finish();
        });

        // Snooze Button
        Button snoozeButton = findViewById(R.id.snoozeButton);
        snoozeButton.setOnClickListener(v -> {
            // Stop the ringtone and snooze the alarm
            stopRingtone();
            snoozeAlarm();
            finish();  // Close the alarm screen after snooze
        });

        // Start the ringtone
        Uri soundUri = getSelectedSoundUri();
        startRingtone(soundUri);
    }

    // Method to start the ringtone
    private void startRingtone(Uri soundUri) {
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), soundUri);
        ringtone.setLooping(true);  // Make sure the ringtone loops
        ringtone.play();
    }

    // Method to stop the ringtone (and any active sound playing)
    private void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();  // Stop the ringtone
        }
    }

    // Method to snooze the alarm (sets it for 5 minutes later)
    private void snoozeAlarm() {
        // Check if permission is granted
        boolean permissionGranted = canScheduleExactAlarms();
        Log.d("AlarmPopupActivity", "Exact alarm permission granted: " + permissionGranted);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !permissionGranted) {
            // Show a message to the user that permission is required
            Toast.makeText(this, "Permission to schedule exact alarms is required.", Toast.LENGTH_SHORT).show();

            // Open system settings to allow the user to grant permission
            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
            return;
        }

        // Get the snooze time (e.g., 5 minute later)
        long snoozeTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);

        // Get the sound URI (for simplicity, we assume it's the default sound, but it could be passed)
        Uri soundUri = getSelectedSoundUri();

        // Create an intent for the AlarmReceiver
        Intent snoozeIntent = new Intent(this, AlarmReceiver.class);
        snoozeIntent.putExtra("ALARM_SOUND_URI", soundUri.toString());

        // Use a unique alarm ID (same as the original alarm ID or generate a new one)
        int alarmId = (int) snoozeTime;  // Using snooze time as unique ID for simplicity

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                alarmId,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Get the AlarmManager system service
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            try {
                // Set the snooze alarm
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        snoozeTime,
                        pendingIntent
                );
                Toast.makeText(this, "Snoozed for 5 minute", Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) {
                // Handle exception when permission is not granted
                Toast.makeText(this, "Permission denied for scheduling exact alarms", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // Android 12 (API 31) and above
            // Check if permission is granted to schedule exact alarms
            try {
                AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                if (appOpsManager != null) {
                    int mode = appOpsManager.checkOpNoThrow(
                            "android:scheduled_exact_alarm",
                            android.os.Process.myUid(),
                            getPackageName()
                    );
                    return mode == AppOpsManager.MODE_ALLOWED;
                }
            } catch (Exception e) {
                Log.e("AlarmPopupActivity", "Error checking exact alarm permission", e);
            }
        }
        return true; // For older versions (API < 31), no need to check as exact alarms are allowed
    }

    // Helper method to get the selected sound URI (you can extend this to support custom sounds)
    private Uri getSelectedSoundUri() {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);  // Using default alarm sound for simplicity
    }

    // Helper method to get the current time in a formatted string
    private String getCurrentTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return timeFormat.format(new Date());
    }

    // Helper method to get the current date in a formatted string
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
        return dateFormat.format(new Date());
    }
}
