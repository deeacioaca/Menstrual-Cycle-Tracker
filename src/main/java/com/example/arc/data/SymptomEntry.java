package com.example.arc.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.time.LocalDate;

/** One symptom recorded on one day. The row existing *is* the "on" state. */
@Entity(tableName = "symptom_entry", primaryKeys = {"date", "category_name", "symptom"})
public class SymptomEntry {

    @NonNull
    private LocalDate date = LocalDate.now();

    @NonNull
    private String symptom = "";

    @NonNull
    @ColumnInfo(name = "category_name")
    private String categoryName = "";

    /**
     * Identifies a symptom within a day. The category is part of the identity because the
     * same symptom name can appear under two categories (e.g. "Emotions").
     */
    public static String key(String category, String symptom) {
        return category + "\u0000" + symptom;
    }

    public SymptomEntry() {
    }

    @androidx.room.Ignore
    public SymptomEntry(@NonNull LocalDate date, @NonNull String symptom, @NonNull String categoryName) {
        this.date = date;
        this.symptom = symptom;
        this.categoryName = categoryName;
    }

    @NonNull
    public LocalDate getDate() {
        return date;
    }

    public void setDate(@NonNull LocalDate date) {
        this.date = date;
    }

    @NonNull
    public String getSymptom() {
        return symptom;
    }

    public void setSymptom(@NonNull String symptom) {
        this.symptom = symptom;
    }

    @NonNull
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(@NonNull String categoryName) {
        this.categoryName = categoryName;
    }
}
