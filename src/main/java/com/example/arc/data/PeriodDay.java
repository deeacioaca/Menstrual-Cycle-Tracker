package com.example.arc.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

/**
 * A single day the user marked as bleeding. Cycles are derived by grouping
 * consecutive rows, so correcting a mistake never corrupts the history.
 */
@Entity(tableName = "period_day")
public class PeriodDay {

    public static final int FLOW_LIGHT = 1;
    public static final int FLOW_MEDIUM = 2;
    public static final int FLOW_HEAVY = 3;

    @PrimaryKey
    @NonNull
    private LocalDate date = LocalDate.now();

    private int flow = FLOW_MEDIUM;

    public PeriodDay() {
    }

    @androidx.room.Ignore
    public PeriodDay(@NonNull LocalDate date, int flow) {
        this.date = date;
        this.flow = flow;
    }

    @NonNull
    public LocalDate getDate() {
        return date;
    }

    public void setDate(@NonNull LocalDate date) {
        this.date = date;
    }

    public int getFlow() {
        return flow;
    }

    public void setFlow(int flow) {
        this.flow = flow;
    }
}
