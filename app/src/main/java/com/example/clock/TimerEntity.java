package com.example.clock;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "timer_table")
public class TimerEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "timeRemainingInMillis")
    private long timeRemainingInMillis;

    @ColumnInfo(name = "soundResourceId")
    private int soundResourceId;

    @ColumnInfo(name = "isActive")
    private boolean isActive;

    // Getter for id
    public int getId() {
        return id;
    }

    // Setter for id
    public void setId(int id) {
        this.id = id;
    }

    // Getter for timeRemainingInMillis
    public long getTimeRemainingInMillis() {
        return timeRemainingInMillis;
    }

    // Setter for timeRemainingInMillis
    public void setTimeRemainingInMillis(long timeRemainingInMillis) {
        this.timeRemainingInMillis = timeRemainingInMillis;
    }

    // Getter for soundResourceId
    public int getSoundResourceId() {
        return soundResourceId;
    }

    // Setter for soundResourceId
    public void setSoundResourceId(int soundResourceId) {
        this.soundResourceId = soundResourceId;
    }

    // Getter for isActive
    public boolean isActive() {
        return isActive;
    }

    // Setter for isActive
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}


