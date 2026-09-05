package com.example.arc.data;

import android.content.Context;

import com.example.arc.cycle.CycleCalculator;
import com.example.arc.cycle.CycleInsights;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Single entry point to Arc's stored data. Every method here touches the database and
 * must be called from {@link ArcDatabase#io()}.
 */
public class CycleRepository {

    private final ArcDatabase database;

    public CycleRepository(Context context) {
        this.database = ArcDatabase.getInstance(context);
    }

    // --- period days ------------------------------------------------------

    public List<PeriodDay> periodDays() {
        return database.periodDayDao().getAll();
    }

    public Set<LocalDate> periodDates() {
        return CycleCalculator.toDateSet(periodDays());
    }

    public PeriodDay periodDay(LocalDate date) {
        return database.periodDayDao().findByDate(date);
    }

    public boolean isPeriodDay(LocalDate date) {
        return periodDay(date) != null;
    }

    public void setPeriodDay(LocalDate date, int flow) {
        database.periodDayDao().upsert(new PeriodDay(date, flow));
    }

    public void clearPeriodDay(LocalDate date) {
        database.periodDayDao().deleteByDate(date);
    }

    /** Marks or unmarks {@code date}; returns the state it ended up in. */
    public boolean togglePeriodDay(LocalDate date) {
        if (isPeriodDay(date)) {
            clearPeriodDay(date);
            return false;
        }
        setPeriodDay(date, PeriodDay.FLOW_MEDIUM);
        return true;
    }

    // --- symptoms ---------------------------------------------------------

    /** Keys of the symptoms logged on {@code date}, as {@link SymptomEntry#key}. */
    public Set<String> symptomsOn(LocalDate date) {
        Set<String> symptoms = new HashSet<>();
        for (SymptomEntry entry : database.symptomEntryDao().getForDate(date)) {
            symptoms.add(SymptomEntry.key(entry.getCategoryName(), entry.getSymptom()));
        }
        return symptoms;
    }

    public Set<LocalDate> datesWithSymptoms() {
        return new HashSet<>(database.symptomEntryDao().getDatesWithSymptoms());
    }

    public void setSymptom(LocalDate date, String category, String symptom, boolean on) {
        if (on) {
            database.symptomEntryDao().insert(new SymptomEntry(date, symptom, category));
        } else {
            database.symptomEntryDao().delete(date, category, symptom);
        }
    }

    // --- derived ----------------------------------------------------------

    public CycleInsights insights() {
        return CycleCalculator.analyse(periodDays());
    }
}
