package com.example.clock;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TimerDao {
    @Insert
    void insertTimer(TimerEntity timer);

    @Update
    void updateTimer(TimerEntity timer);

    @Query("SELECT * FROM timer_table WHERE isActive = 1")
    List<TimerEntity> getActiveTimers();

    @Query("DELETE FROM timer_table WHERE id = :timerId")
    void deleteTimerById(int timerId);
}
