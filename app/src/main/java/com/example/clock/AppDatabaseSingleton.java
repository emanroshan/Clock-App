package com.example.clock;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class AppDatabaseSingleton {

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Alter the table to add a new column
            database.execSQL("ALTER TABLE alarms ADD COLUMN isActive INTEGER NOT NULL DEFAULT 0");
        }
    };

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


    private static AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "alarm_db")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Register both migrations
                            .build();
                }
            }
        }
        return instance;
    }
}
