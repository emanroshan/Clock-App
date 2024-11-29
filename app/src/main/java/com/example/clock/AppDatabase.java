package com.example.clock;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {AlarmEntity.class, TimerEntity.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AlarmDao alarmDao();
    public abstract TimerDao timerDao();

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Make sure the column names match what's in TimerEntity
            database.execSQL("CREATE TABLE IF NOT EXISTS `timer_table` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`timeRemainingInMillis` INTEGER NOT NULL, " +
                    "`soundResourceId` INTEGER NOT NULL, " +
                    "`isActive` INTEGER NOT NULL);");
        }
    };

}
