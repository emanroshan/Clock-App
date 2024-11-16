package com.example.clock;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StopwatchFragment extends Fragment {

    private TextView stopwatchTextView;
    private Button startStopwatchButton, pauseStopwatchButton, resetStopwatchButton, lapStopwatchButton;
    private RecyclerView lapRecyclerView;
    private LapAdapter lapAdapter;

    private long elapsedTimeInMillis = 0;
    private boolean isRunning = false;

    private Handler handler = new Handler();
    private Runnable updateTimerRunnable;

    private List<String> lapList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stopwatchfrag, container, false);

        stopwatchTextView = view.findViewById(R.id.stopwatchTextView);
        startStopwatchButton = view.findViewById(R.id.startStopwatchButton);
        pauseStopwatchButton = view.findViewById(R.id.pauseStopwatchButton);
        resetStopwatchButton = view.findViewById(R.id.resetStopwatchButton);
        lapStopwatchButton = view.findViewById(R.id.lapStopwatchButton);
        lapRecyclerView = view.findViewById(R.id.lapRecyclerView);

        // Set up RecyclerView with the custom adapter
        lapRecyclerView.setVisibility(View.GONE); // Set to gone initially

        // Initialize the adapter in onCreateView
        lapAdapter = new LapAdapter(lapList);
        lapRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lapRecyclerView.setAdapter(lapAdapter);


        // Set button listeners
        startStopwatchButton.setOnClickListener(v -> startStopwatch());
        pauseStopwatchButton.setOnClickListener(v -> pauseStopwatch());
        resetStopwatchButton.setOnClickListener(v -> resetStopwatch());
        lapStopwatchButton.setOnClickListener(v -> recordLap());

        return view;
    }

    private void startStopwatch() {
        if (!isRunning) {
            isRunning = true;
            startStopwatchButton.setVisibility(View.GONE);
            pauseStopwatchButton.setVisibility(View.VISIBLE);
            resetStopwatchButton.setVisibility(View.VISIBLE);
            lapStopwatchButton.setVisibility(View.VISIBLE);

            // Start the timer
            handler.post(updateTimerRunnable = new Runnable() {
                long startTime = System.currentTimeMillis() - elapsedTimeInMillis;

                @Override
                public void run() {
                    elapsedTimeInMillis = System.currentTimeMillis() - startTime;
                    updateStopwatchTextView();
                    handler.postDelayed(this, 10); // Update every 10ms
                }
            });
        }
    }

    private void pauseStopwatch() {
        if (isRunning) {
            isRunning = false;
            handler.removeCallbacks(updateTimerRunnable);
            startStopwatchButton.setText("Resume");
            startStopwatchButton.setVisibility(View.VISIBLE);
            pauseStopwatchButton.setVisibility(View.GONE);
        }
    }

    private void resetStopwatch() {
        isRunning = false;
        handler.removeCallbacks(updateTimerRunnable);
        elapsedTimeInMillis = 0;
        updateStopwatchTextView();
        lapList.clear();
        lapAdapter.notifyDataSetChanged(); // Clear lap list
        startStopwatchButton.setText("Start");
        startStopwatchButton.setVisibility(View.VISIBLE);
        pauseStopwatchButton.setVisibility(View.GONE);
        resetStopwatchButton.setVisibility(View.GONE);
        lapStopwatchButton.setVisibility(View.GONE);
        lapRecyclerView.setVisibility(View.GONE); // Hide RecyclerView when reset
    }

    private void updateStopwatchTextView() {
        int hours = (int) (elapsedTimeInMillis / 3600000);
        int minutes = (int) ((elapsedTimeInMillis % 3600000) / 60000);
        int seconds = (int) ((elapsedTimeInMillis % 60000) / 1000);
        int milliseconds = (int) ((elapsedTimeInMillis % 1000) / 10);

        String formattedTime = String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds, milliseconds);
        stopwatchTextView.setText(formattedTime);
    }

    private void recordLap() {
        if (isRunning) {
            // Format lap time
            int hours = (int) (elapsedTimeInMillis / 3600000);
            int minutes = (int) ((elapsedTimeInMillis % 3600000) / 60000);
            int seconds = (int) ((elapsedTimeInMillis % 60000) / 1000);
            int milliseconds = (int) ((elapsedTimeInMillis % 1000) / 10);

            String lapTime = String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds, milliseconds);

            // Log lap time for debugging
            Log.d("Stopwatch", "Lap added: " + lapTime);
            lapList.add(lapTime);

            // Notify the adapter that the data has changed
            lapAdapter.notifyDataSetChanged();

            // Make sure RecyclerView is visible
            lapRecyclerView.setVisibility(View.VISIBLE);
        }
    }


}
