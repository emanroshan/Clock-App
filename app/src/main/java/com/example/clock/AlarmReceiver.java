package com.example.clock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static Ringtone ringtone;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReciever", "Alarm Recieved!");
        Intent popupIntent = new Intent(context, AlarmPopupActivity.class);
        popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // To start the Activity from the receiver
        context.startActivity(popupIntent);



        // Display a Toast message
        Toast.makeText(context, "Alarm Triggered!", Toast.LENGTH_SHORT).show();

        // Show a notification
        showNotification(context);

    }

    // Show a notification with an option to stop the alarm
    private void showNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "alarm_channel",
                    "Alarm Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Build notification
        Notification notification = new NotificationCompat.Builder(context, "alarm_channel")
                .setContentTitle("Alarm Triggered")
                .setContentText("Time to wake up!")
                .setSmallIcon(R.drawable.alarm_icon)
                .setAutoCancel(true)
                .build();

        // Show the notification
        notificationManager.notify(1, notification);
    }

    // Stop the ringtone after a delay (e.g., 30 seconds)
    private void stopRingtoneAfterDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> stopRingtone(), 30000);
    }

    // Stop the ringtone if it's playing
    public static void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }
}

