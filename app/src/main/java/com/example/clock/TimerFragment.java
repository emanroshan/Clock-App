package com.example.clock;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

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
    private int selectedSound = R.raw.beep; // Default sound

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
        int hours = numberPickerHours.getValue();
        int minutes = numberPickerMinutes.getValue();
        int seconds = numberPickerSeconds.getValue();

        timeRemainingInMillis = (hours * 3600 + minutes * 60 + seconds) * 1000;

        if (timeRemainingInMillis > 0) {
            // Get selected sound
            int selectedPosition = soundSelectorSpinner.getSelectedItemPosition();
            selectedSound = soundResources[selectedPosition];

            startCountDown(timeRemainingInMillis);

            // Update UI
            startTimerButton.setVisibility(View.GONE);
            pauseResumeTimerButton.setVisibility(View.VISIBLE);
            stopTimerButton.setVisibility(View.VISIBLE);
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
        new AlertDialog.Builder(requireContext())
                .setTitle("Timer Finished")
                .setMessage("The timer has finished!")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Stop the sound and reset the timer
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                    resetTimerUI();
                })
                .show();
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
