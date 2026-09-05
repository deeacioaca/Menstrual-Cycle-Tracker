package com.example.arc.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface PeriodDayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(PeriodDay day);

    @Query("DELETE FROM period_day WHERE date = :date")
    void deleteByDate(LocalDate date);

    @Query("SELECT * FROM period_day WHERE date = :date")
    PeriodDay findByDate(LocalDate date);

    @Query("SELECT * FROM period_day ORDER BY date ASC")
    List<PeriodDay> getAll();
}
