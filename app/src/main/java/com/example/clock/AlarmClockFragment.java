package com.example.clock;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class AlarmClockFragment extends Fragment {

    private Spinner soundSpinner;
    private Button setAlarmButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alarmfrag, container, false);

        soundSpinner = view.findViewById(R.id.soundSpinner);
        setAlarmButton = view.findViewById(R.id.setAlarmButton);

        // Set up the spinner with sound options
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                new String[]{"Beep", "Sound 1", "Sound 2", "Sound 3", "Sound 4", "Sound 5"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        soundSpinner.setAdapter(adapter);

        // Set up the button listener to set the alarm
        setAlarmButton.setOnClickListener(v -> setAlarm());
        return view;
    }

    private void setAlarm() {
        // Get the selected sound from the Spinner
        Uri soundUri = getSelectedSoundUri();

        // Set the alarm time (e.g., 10 seconds from now)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 10); // Set alarm for 10 seconds later

        // Create an intent to trigger the AlarmReceiver
        Intent alarmIntent = new Intent(requireContext(), AlarmReceiver.class);
        alarmIntent.putExtra("ALARM_SOUND_URI", soundUri);

        // Set the alarm using AlarmManager
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private Uri getSelectedSoundUri() {
        String selectedSound = soundSpinner.getSelectedItem().toString();

        // Define the mapping from sound names to URIs
        switch (selectedSound) {
            case "Beep":
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); // Default beep sound
            case "Sound 1":
                return Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.alarm);
            case "Sound 2":
                return Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.bell);
            case "Sound 3":
                return Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.beep);
            case "Sound 4":
                return Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.chime);
            case "Sound 5":
                return Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.tone);
            default:
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); // Fallback sound
        }
    }
}
