package com.example.clock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlarmListActivity extends Fragment implements AlarmAdapter.AlarmAdapterCallback {

    private RecyclerView alarmRecyclerView;
    private AlarmAdapter alarmAdapter;
    private List<AlarmEntity> alarmList = new ArrayList<>();

    private ImageView addAlarmButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_alarm_list, container, false);

        // Initialize RecyclerView
        alarmRecyclerView = view.findViewById(R.id.alarmRecyclerView);
        alarmAdapter = new AlarmAdapter(alarmList, this); // Pass the callback

        alarmRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        alarmRecyclerView.setAdapter(alarmAdapter);

        // Set the click listener for the icon
        ImageView addAlarmButton = view.findViewById(R.id.addAlarmButton);
        addAlarmButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AlarmClockFragment.class);
            startActivity(intent);
        });

        loadAlarms();

        return view;
    }


    private String formatTime(long timeInMillis) {
        if (timeInMillis <= 0) return "Invalid Time"; // Safeguard for invalid time
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // 12-hour format
        return sdf.format(new Date(timeInMillis));
    }


    private void loadAlarms() {
        new Thread(() -> {
            AppDatabase db = AppDatabaseSingleton.getInstance(getContext());
            List<AlarmEntity> alarms = db.alarmDao().getAllAlarms(); // Fetch alarms

            // Separate alarms into AM and PM lists
            List<AlarmEntity> amAlarms = new ArrayList<>();
            List<AlarmEntity> pmAlarms = new ArrayList<>();

            // Split alarms based on AM/PM
            for (AlarmEntity alarm : alarms) {
                String timeString = formatTime(alarm.time); // Get the formatted time string
                if (timeString.endsWith("AM")) {
                    amAlarms.add(alarm); // Add to AM list if time ends with "AM"
                } else {
                    pmAlarms.add(alarm); // Add to PM list if time ends with "PM"
                }
            }

            // Sort both lists by time string (AM first)
            amAlarms.sort((a1, a2) -> formatTime(a1.time).compareTo(formatTime(a2.time)));
            pmAlarms.sort((a1, a2) -> formatTime(a1.time).compareTo(formatTime(a2.time)));

            // Log sorted alarms for verification
            Log.d("SortedAlarms", "AM Alarms: " + amAlarms.size() + " PM Alarms: " + pmAlarms.size());

            // Update the adapter with both AM and PM alarms
            requireActivity().runOnUiThread(() -> {
                List<AlarmEntity> allSortedAlarms = new ArrayList<>();
                allSortedAlarms.addAll(amAlarms);
                allSortedAlarms.addAll(pmAlarms);
                alarmAdapter.setAlarms(allSortedAlarms); // Combine the two lists and pass them to the adapter
            });
        }).start();
    }





    @Override
    public void onAlarmDeleted(int alarmId, int position) {
        new Thread(() -> {
            try {
                // Delete the alarm from the database
                AppDatabase db = AppDatabaseSingleton.getInstance(getContext());
                db.alarmDao().deleteAlarm(alarmId);  // Delete by ID

                // After deletion, reload alarms and update the adapter
                requireActivity().runOnUiThread(() -> loadAlarms());  // Reload alarms from DB and refresh UI
            } catch (Exception e) {
                Log.e("AlarmListActivity", "Error deleting alarm", e);
                // Handle any errors (e.g., show a toast)
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error deleting alarm", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    @Override
    public void onAlarmStatusChanged(int alarmId, boolean isActive) {
        AppDatabase db = AppDatabaseSingleton.getInstance(getContext());
        db.alarmDao().updateAlarmStatus(alarmId, isActive); // Update in DB
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check for existing alarms using stored PendingIntent
        PendingIntent pendingIntent = getStoredPendingIntent(requireContext());
        if (pendingIntent != null) {
            Log.d("AlarmListActivity", "Stored Alarm PendingIntent exists!");
        } else {
            Log.d("AlarmListActivity", "No Stored PendingIntent found.");
        }

        // Set all active alarms on launch
        setActiveAlarms();
    }

    private void setActiveAlarms() {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabaseSingleton.getInstance(requireContext());
                List<AlarmEntity> alarms = db.alarmDao().getAllAlarms(); // Fetch all alarms

                AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                long currentTime = System.currentTimeMillis();

                if (alarmManager != null) {
                    for (AlarmEntity alarm : alarms) {
                        Log.d("AlarmListActivity", "Loaded alarm ID: " + alarm.id + ", Time: " + alarm.time);

                        if (alarm.isActive && alarm.time > currentTime) {
                            // Set only active alarms with a future time
                            setExactAlarm(alarmManager, Uri.parse(alarm.soundUri), alarm.time, alarm.id);
                        } else {
                            Log.d("AlarmListActivity", "Skipping alarm ID: " + alarm.id + " as it's inactive or in the past.");
                        }
                    }
                } else {
                    Log.e("AlarmListActivity", "AlarmManager is null.");
                }
            } catch (Exception e) {
                Log.e("AlarmListActivity", "Error in setActiveAlarms: ", e);
            }
        }).start();
    }

    private void setExactAlarm(AlarmManager alarmManager, Uri soundUri, long alarmTimeInMillis, int alarmId) {
        try {
            long currentTime = System.currentTimeMillis();

            // Adjust alarm time if it has already passed
            if (alarmTimeInMillis <= currentTime) {
                alarmTimeInMillis += 86400000; // Add one day (24 hours in milliseconds)
            }

            // Save alarm details in SharedPreferences for debugging
            SharedPreferences prefs = requireContext().getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("alarmTime_" + alarmId, alarmTimeInMillis);
            editor.putString("soundUri_" + alarmId, soundUri.toString());
            editor.apply();

            // Create an intent for the AlarmReceiver
            Intent intent = new Intent(requireContext(), AlarmReceiver.class);
            intent.putExtra("ALARM_SOUND_URI", soundUri.toString());
            intent.putExtra("ALARM_ID", alarmId);

            // Create a unique PendingIntent for each alarm
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    requireContext(),
                    alarmId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Schedule the alarm
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTimeInMillis,
                    pendingIntent
            );

            Log.d("AlarmListActivity", "Alarm set with ID: " + alarmId + " at " + alarmTimeInMillis);

        } catch (SecurityException e) {
            Log.e("AlarmListActivity", "Error setting alarm: ", e);
        }
    }

    private PendingIntent getStoredPendingIntent(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
        long alarmTime = prefs.getLong("alarmTime", -1);
        String soundUriString = prefs.getString("soundUri", null);

        if (alarmTime != -1 && soundUriString != null) {
            Uri soundUri = Uri.parse(soundUriString);

            // Create the Intent and PendingIntent
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            alarmIntent.putExtra("ALARM_SOUND_URI", soundUri.toString());
            return PendingIntent.getBroadcast(
                    context,
                    0,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        }
        return null;
    }


}

