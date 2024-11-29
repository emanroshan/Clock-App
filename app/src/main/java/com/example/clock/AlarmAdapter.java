package com.example.clock;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private List<AlarmEntity> alarms;
    private final AlarmAdapterCallback callback;

    // Constructor accepts a callback instead of relying on the context
    public AlarmAdapter(List<AlarmEntity> alarms, AlarmAdapterCallback callback) {
        this.alarms = alarms != null ? alarms : new ArrayList<>();
        this.callback = callback;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmEntity alarm = alarms.get(position);

        // Format the time without AM/PM suffix for individual items
        String formattedTime = formatTime(alarm.time);

        // Set the formatted time in the TextView
        holder.alarmTime.setText(formattedTime);

        // Set switch state
        holder.alarmSwitch.setOnCheckedChangeListener(null); // Clear previous listener
        holder.alarmSwitch.setChecked(alarm.isActive);

        // Add a new listener for switch state changes
        holder.alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (callback != null) {
                new Thread(() -> {
                    callback.onAlarmStatusChanged(alarm.id, isChecked);
                    alarm.isActive = isChecked; // Update local object
                }).start();
            }
        });

        // Handle delete button click
        holder.deleteButton.setOnClickListener(v -> {
            if (callback != null) {
                new Thread(() -> callback.onAlarmDeleted(alarm.id, position)).start();
            }
        });

        // Show AM/PM section header if necessary (only once for each section)
        if (position == 0 || isNewSection(position)) {
            String sectionTitle = (alarm.time < 43200000) ? "AM" : "PM"; // AM/PM section title
            holder.alarmTime.setText(formattedTime + " " + sectionTitle); // Append AM/PM only here
        }
    }

    private boolean isNewSection(int position) {
        // Check if the current alarm belongs to a different section (AM/PM)
        return position > 0 && (alarms.get(position).time < 43200000) != (alarms.get(position - 1).time < 43200000);
    }




    @Override
    public int getItemCount() {
        return alarms.size();
    }

    public void setAlarms(List<AlarmEntity> alarms) {
        this.alarms = alarms != null ? alarms : new ArrayList<>();  // Update the alarm list
        notifyDataSetChanged();  // Notify the adapter to refresh the list
    }



    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView alarmTime;
        Switch alarmSwitch;
        ImageView deleteButton;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            alarmTime = itemView.findViewById(R.id.timeTextView); // Corrected ID
            alarmSwitch = itemView.findViewById(R.id.alarmSwitch);
            deleteButton = itemView.findViewById(R.id.deleteButton); // Added delete button
        }
    }

    private String formatTime(long timeInMillis) {
        if (timeInMillis <= 0) return "Invalid Time"; // Safeguard for invalid time
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // Adjusted to 12-hour format
        return sdf.format(new Date(timeInMillis));
    }

    // Callback interface
    public interface AlarmAdapterCallback {
        void onAlarmDeleted(int alarmId, int position);

        void onAlarmStatusChanged(int alarmId, boolean isActive);
    }
}
