package com.example.arc.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {PeriodDay.class, SymptomEntry.class},
        version = 3,
        exportSchema = false)
@TypeConverters(Converters.class)
public abstract class ArcDatabase extends RoomDatabase {

    private static final String NAME = "arc-database";

    private static volatile ArcDatabase instance;
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();

    public abstract PeriodDayDao periodDayDao();

    public abstract SymptomEntryDao symptomEntryDao();

    public static ArcDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (ArcDatabase.class) {
                if (instance == null) {
                    // Application context only - an Activity here would leak for the
                    // lifetime of the process.
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(), ArcDatabase.class, NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }

    /** Shared background executor for database work. */
    public static ExecutorService io() {
        return IO;
    }
}
