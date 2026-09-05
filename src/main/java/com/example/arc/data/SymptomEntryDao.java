package com.example.arc.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface SymptomEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SymptomEntry entry);

    @Query("DELETE FROM symptom_entry "
            + "WHERE date = :date AND category_name = :category AND symptom = :symptom")
    void delete(LocalDate date, String category, String symptom);

    @Query("SELECT * FROM symptom_entry WHERE date = :date")
    List<SymptomEntry> getForDate(LocalDate date);

    @Query("SELECT DISTINCT date FROM symptom_entry")
    List<LocalDate> getDatesWithSymptoms();

    @Query("SELECT * FROM symptom_entry ORDER BY date DESC")
    List<SymptomEntry> getAll();
}
