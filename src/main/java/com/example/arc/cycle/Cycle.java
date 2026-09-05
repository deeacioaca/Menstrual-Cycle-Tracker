package com.example.arc.cycle;

import java.time.LocalDate;

/** One observed menstrual cycle: when bleeding started, how long it bled, how long the cycle ran. */
public class Cycle {

    private final LocalDate startDate;
    private final int periodLength;
    private final int cycleLength;

    public Cycle(LocalDate startDate, int periodLength, int cycleLength) {
        this.startDate = startDate;
        this.periodLength = periodLength;
        this.cycleLength = cycleLength;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public int getPeriodLength() {
        return periodLength;
    }

    /** Days until the next cycle started, or 0 when this is the cycle still in progress. */
    public int getCycleLength() {
        return cycleLength;
    }

    public boolean isComplete() {
        return cycleLength > 0;
    }
}
