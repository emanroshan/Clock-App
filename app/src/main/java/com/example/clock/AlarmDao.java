package com.example.clock;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AlarmDao {
    @Insert
    void insertAlarm(AlarmEntity alarm);

    @Query("SELECT * FROM alarms ORDER BY time ASC")
    List<AlarmEntity> getAllAlarms();

    @Query("UPDATE alarms SET isActive = :isActive WHERE id = :alarmId")
    void updateAlarmStatus(int alarmId, boolean isActive);

    @Query("DELETE FROM alarms WHERE id = :alarmId")
    void deleteAlarm(int alarmId);
}

