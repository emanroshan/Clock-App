package com.example.clock;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarms")
public class AlarmEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public long time; // Alarm time in milliseconds
    public String soundUri; // Alarm sound URI
    public boolean isActive = true;
}
