package com.example.arc.cycle;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/** Everything the UI needs to describe the user's cycle, computed once. */
public class CycleInsights {

    private final List<Cycle> cycles;
    private final int averageCycleLength;
    private final int averagePeriodLength;
    private final LocalDate currentCycleStart;
    private final LocalDate predictedNextStart;
    private final boolean estimated;

    CycleInsights(List<Cycle> cycles, int averageCycleLength, int averagePeriodLength,
                  LocalDate currentCycleStart, LocalDate predictedNextStart, boolean estimated) {
        this.cycles = cycles;
        this.averageCycleLength = averageCycleLength;
        this.averagePeriodLength = averagePeriodLength;
        this.currentCycleStart = currentCycleStart;
        this.predictedNextStart = predictedNextStart;
        this.estimated = estimated;
    }

    /** Newest first. */
    public List<Cycle> getCycles() {
        return Collections.unmodifiableList(cycles);
    }

    public int getAverageCycleLength() {
        return averageCycleLength;
    }

    public int getAveragePeriodLength() {
        return averagePeriodLength;
    }

    public LocalDate getCurrentCycleStart() {
        return currentCycleStart;
    }

    public LocalDate getPredictedNextStart() {
        return predictedNextStart;
    }

    /** True while there is too little history for the averages to mean much. */
    public boolean isEstimated() {
        return estimated;
    }

    public boolean hasData() {
        return currentCycleStart != null;
    }

    /** 1-based day of the cycle in progress, or 0 when nothing has been logged. */
    public int cycleDayOn(LocalDate date) {
        if (currentCycleStart == null || date.isBefore(currentCycleStart)) {
            return 0;
        }
        return (int) (date.toEpochDay() - currentCycleStart.toEpochDay()) + 1;
    }

    /** Negative when the predicted date has already passed. */
    public long daysUntilNextPeriod(LocalDate from) {
        if (predictedNextStart == null) {
            return Long.MIN_VALUE;
        }
        return predictedNextStart.toEpochDay() - from.toEpochDay();
    }

    /** First fertile day, as a 1-based day of the current cycle. */
    public int getFertileStartDay() {
        return ovulationDay() - 5;
    }

    /** Last fertile day, as a 1-based day of the current cycle. */
    public int getFertileEndDay() {
        return ovulationDay() + 1;
    }

    /** Estimated ovulation, as a 1-based day of the current cycle. */
    public int ovulationDay() {
        LocalDate ovulation = getPredictedOvulation();
        if (ovulation == null || currentCycleStart == null) {
            return averageCycleLength - 14;
        }
        return (int) (ovulation.toEpochDay() - currentCycleStart.toEpochDay()) + 1;
    }

    /** Which phase a 1-based cycle day falls in. */
    public Phase phaseOn(int cycleDay) {
        if (cycleDay <= averagePeriodLength) {
            return Phase.MENSTRUAL;
        }
        if (cycleDay < getFertileStartDay()) {
            return Phase.FOLLICULAR;
        }
        if (cycleDay <= getFertileEndDay()) {
            return Phase.FERTILE;
        }
        return Phase.LUTEAL;
    }

    /** Ovulation is estimated as 14 days before the next period starts. */
    public LocalDate getPredictedOvulation() {
        return predictedNextStart == null ? null : predictedNextStart.minusDays(14);
    }
}
