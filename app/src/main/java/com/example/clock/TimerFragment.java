package com.example.clock;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class TimerFragment extends Fragment {

    private NumberPicker numberPickerHours, numberPickerMinutes, numberPickerSeconds;
    private TextView timerTextView;
    private Button startTimerButton, pauseResumeTimerButton, stopTimerButton;
    private Spinner soundSelectorSpinner;

    private CountDownTimer countDownTimer;
    private long timeRemainingInMillis = 0;
    private boolean isPaused = false;

    private MediaPlayer mediaPlayer;

    private int[] soundResources = {
            R.raw.beep,
            R.raw.bell,
            R.raw.chime,
            R.raw.alarm,
            R.raw.tone
    };
    private int selectedSound = R.raw.tone;

    // Default sound

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.timerfrag, container, false);

        numberPickerHours = view.findViewById(R.id.numberPickerHours);
        numberPickerMinutes = view.findViewById(R.id.numberPickerMinutes);
        numberPickerSeconds = view.findViewById(R.id.numberPickerSeconds);
        timerTextView = view.findViewById(R.id.timerTextView);
        startTimerButton = view.findViewById(R.id.startTimerButton);
        pauseResumeTimerButton = view.findViewById(R.id.pauseResumeTimerButton);
        stopTimerButton = view.findViewById(R.id.stopTimerButton);
        soundSelectorSpinner = view.findViewById(R.id.soundSelectorSpinner);


        numberPickerHours.setTextColor(Color.WHITE);
        numberPickerMinutes.setTextColor(Color.WHITE);
        numberPickerSeconds.setTextColor(Color.WHITE);

        // Set up NumberPickers
        numberPickerHours.setMinValue(0);
        numberPickerHours.setMaxValue(23);
        numberPickerMinutes.setMinValue(0);
        numberPickerMinutes.setMaxValue(59);
        numberPickerSeconds.setMinValue(0);
        numberPickerSeconds.setMaxValue(59);

        // Button listeners
        startTimerButton.setOnClickListener(v -> startTimer());
        pauseResumeTimerButton.setOnClickListener(v -> pauseResumeTimer());
        stopTimerButton.setOnClickListener(v -> stopTimer());

        // Set default sound
        soundSelectorSpinner.setSelection(0);

        return view;
    }

    private void startTimer() {
        // Get the time from the NumberPickers
        int hours = numberPickerHours.getValue();
        int minutes = numberPickerMinutes.getValue();
        int seconds = numberPickerSeconds.getValue();

        // Convert time to milliseconds
        timeRemainingInMillis = (hours * 3600 + minutes * 60 + seconds) * 1000;

        if (timeRemainingInMillis > 0) {
            // Get the selected sound resource
            int selectedPosition = soundSelectorSpinner.getSelectedItemPosition();
            selectedSound = soundResources[selectedPosition];

            // Start the countdown timer
            startCountDown(timeRemainingInMillis);

            // Set the alarm when the timer finishes
            setAlarmForTimerFinish(timeRemainingInMillis);

            // Update UI to reflect the active timer state
            startTimerButton.setVisibility(View.GONE);
            pauseResumeTimerButton.setVisibility(View.VISIBLE);
            stopTimerButton.setVisibility(View.VISIBLE);
        }
    }


    private void setAlarmForTimerFinish(long timeRemainingInMillis) {
        // Get the AlarmManager system service
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            // Check if we have permission to schedule exact alarms
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // For Android 12 and above, check if the app can schedule exact alarms
                if (!alarmManager.canScheduleExactAlarms()) {
                    // Notify the user that they need to grant permission
                    Toast.makeText(getActivity(), "Please enable exact alarm permission", Toast.LENGTH_LONG).show();

                    // Open the settings page to allow the user to enable the permission
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent);
                    return;
                }
            }

            // Create an intent to trigger the TimerReceiver when the timer finishes
            Intent intent = new Intent(getActivity(), TimerReceiver.class);
            intent.putExtra("SOUND_RESOURCE_ID", selectedSound);

            // Create a PendingIntent to be triggered by AlarmManager
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    getActivity(),
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Set the alarm to trigger at the appropriate time
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + timeRemainingInMillis,
                    pendingIntent
            );
        }
    }



    private void startCountDown(long millis) {
        countDownTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemainingInMillis = millisUntilFinished;
                updateTimerTextView();
            }

            @Override
            public void onFinish() {
                playAlarmSound();
                showAlertDialog();
            }
        }.start();
    }

    private void updateTimerTextView() {
        int hours = (int) (timeRemainingInMillis / 3600000);
        int minutes = (int) ((timeRemainingInMillis % 3600000) / 60000);
        int seconds = (int) ((timeRemainingInMillis % 60000) / 1000);

        timerTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void pauseResumeTimer() {
        if (isPaused) {
            startCountDown(timeRemainingInMillis);
            pauseResumeTimerButton.setText("Pause");
        } else {
            countDownTimer.cancel();
            pauseResumeTimerButton.setText("Resume");
        }
        isPaused = !isPaused;
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        resetTimerUI();
    }

    private void resetTimerUI() {
        startTimerButton.setVisibility(View.VISIBLE);
        pauseResumeTimerButton.setVisibility(View.GONE);
        stopTimerButton.setVisibility(View.GONE);
        isPaused = false;
        timerTextView.setText("00:00:00");
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void playAlarmSound() {
        mediaPlayer = MediaPlayer.create(getContext(), selectedSound);
        mediaPlayer.setLooping(true); // Set sound to loop
        mediaPlayer.start();
    }

    private void showAlertDialog() {
        // Inflate the custom layout
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.timerpopup, null);

        // Find views in the custom layout
        TextView dialogTimerTextView = dialogView.findViewById(R.id.dialogTimerTextView);
        Button dismissButton = dialogView.findViewById(R.id.dismissButton);
        Button restartButton = dialogView.findViewById(R.id.restartButton);

        // Create the AlertDialog
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false) // Prevent dismissal by tapping outside
                .create();

        // Start a handler to update the timer in the dialog
        Handler dialogHandler = new Handler();
        Runnable dialogRunnable = new Runnable() {
            @Override
            public void run() {
                timeRemainingInMillis -= 1000; // Decrement timer by 1 second

                // Update the timer text in the dialog
                int hours = (int) (timeRemainingInMillis / 3600000);
                int minutes = (int) ((timeRemainingInMillis % 3600000) / 60000);
                int seconds = (int) ((timeRemainingInMillis % 60000) / 1000);

                String timerText = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                dialogTimerTextView.setText(timerText);

                // Keep updating if the dialog is visible
                if (alertDialog.isShowing()) {
                    dialogHandler.postDelayed(this, 1000);
                }
            }
        };

        // Start the timer update
        dialogHandler.post(dialogRunnable);

        // Handle the Dismiss button
        dismissButton.setOnClickListener(v -> {
            dialogHandler.removeCallbacks(dialogRunnable); // Stop updating the timer
            alertDialog.dismiss();
            resetTimerUI();
        });

        // Handle the Restart button
        restartButton.setOnClickListener(v -> {
            dialogHandler.removeCallbacks(dialogRunnable); // Stop updating the timer
            alertDialog.dismiss();
            resetTimerUI();
            startTimer(); // Restart the timer
        });

        // Show the AlertDialog
        alertDialog.show();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
