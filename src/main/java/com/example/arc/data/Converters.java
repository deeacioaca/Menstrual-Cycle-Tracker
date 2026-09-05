package com.example.arc.data;

import androidx.room.TypeConverter;

import java.time.LocalDate;

public final class Converters {

    private Converters() {
    }

    @TypeConverter
    public static Long fromDate(LocalDate date) {
        return date == null ? null : date.toEpochDay();
    }

    @TypeConverter
    public static LocalDate toDate(Long epochDay) {
        return epochDay == null ? null : LocalDate.ofEpochDay(epochDay);
    }
}
