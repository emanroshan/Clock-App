package com.example.clock;

import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DigitalClockFragment extends Fragment {
    private TextView digitalClock;
    private Switch timeFormatSwitch;
    private Handler handler;
    private Runnable clockRunnable;
    private boolean is24HourFormat = true; // Default to 24-hour format// Default to 24-hour format

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.digitalfrag, container, false);

        digitalClock = view.findViewById(R.id.digitalClock);
        timeFormatSwitch = view.findViewById(R.id.timeFormatSwitch);

        handler = new Handler();

        // Toggle between 12-hour and 24-hour formats
        timeFormatSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            is24HourFormat = isChecked;
        });

        // Update the clock every second
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                updateClock();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(clockRunnable);
        return view;
    }

    private void updateClock() {
        String format = is24HourFormat ? "HH:mm:ss" : "hh:mm:ss a";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

        // Get the current time and add 3500 milliseconds (3.5 seconds)
        long currentTimeMillis = System.currentTimeMillis() + 3500; // Increase time by 3500 ms
        String currentTime = sdf.format(new Date(currentTimeMillis));

        if (!is24HourFormat) {
            // Format AM/PM to be smaller in size
            String[] parts = currentTime.split(" ");
            if (parts.length == 2) {
                SpannableString spannable = new SpannableString(parts[0] + " " + parts[1]);
                spannable.setSpan(new RelativeSizeSpan(0.6f), parts[0].length() + 1, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                digitalClock.setText(spannable);
            } else {
                digitalClock.setText(currentTime);
            }
        } else {
            digitalClock.setText(currentTime); // Display 24-hour format
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(clockRunnable); // Stop updating the clock
    }
}
