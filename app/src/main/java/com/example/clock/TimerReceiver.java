package com.example.clock;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import androidx.core.app.NotificationCompat;

public class TimerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int soundResourceId = intent.getIntExtra("SOUND_RESOURCE_ID", R.raw.beep);

        // Play the sound when the timer finishes
        MediaPlayer mediaPlayer = MediaPlayer.create(context, soundResourceId);
        mediaPlayer.setLooping(true);  // Loop the alarm sound
        mediaPlayer.start();

        // Show a notification or pop-up
        showTimerFinishedNotification(context);
    }

    private void showTimerFinishedNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "TIMER_CHANNEL")
                .setSmallIcon(R.drawable.timer)
                .setContentTitle("Timer Finished")
                .setContentText("The timer has completed!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(1001, builder.build());
        }
    }
}

