package com.example.clock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the sound URI from the intent
        Uri soundUri = intent.getParcelableExtra("ALARM_SOUND_URI");

        if (soundUri != null) {
            // Play the alarm sound
            Ringtone ringtone = RingtoneManager.getRingtone(context, soundUri);
            ringtone.play();
        } else {
            // Default sound if no URI is provided
            Ringtone ringtone = RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
            ringtone.play();
        }

        // Display a Toast message to indicate alarm is triggered
        Toast.makeText(context, "Alarm Triggered!", Toast.LENGTH_SHORT).show();
    }
}
