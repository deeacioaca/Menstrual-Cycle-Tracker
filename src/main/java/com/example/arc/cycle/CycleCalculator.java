package com.example.arc.cycle;

import com.example.arc.data.PeriodDay;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Derives cycles, averages and predictions from the raw logged period days.
 *
 * <p>Pure functions over plain data - no Android or Room types - so the maths can be
 * reasoned about (and tested) on its own.
 */
public final class CycleCalculator {

    /** Used until the user has logged enough to have a real average. */
    public static final int DEFAULT_CYCLE_LENGTH = 28;
    public static final int DEFAULT_PERIOD_LENGTH = 5;

    /** Luteal phase length; ovulation is estimated this many days before the next period. */
    private static final int LUTEAL_PHASE_DAYS = 14;
    /** Sperm survive to roughly five days before ovulation, the egg about a day after. */
    private static final int FERTILE_DAYS_BEFORE_OVULATION = 5;
    private static final int FERTILE_DAYS_AFTER_OVULATION = 1;

    private CycleCalculator() {
    }

    public static CycleInsights analyse(List<PeriodDay> periodDays) {
        List<LocalDate> dates = new ArrayList<>();
        for (PeriodDay day : periodDays) {
            dates.add(day.getDate());
        }
        Collections.sort(dates);

        List<List<LocalDate>> runs = groupConsecutive(dates);
        if (runs.isEmpty()) {
            return new CycleInsights(Collections.emptyList(), DEFAULT_CYCLE_LENGTH,
                    DEFAULT_PERIOD_LENGTH, null, null, true);
        }

        // Oldest first while we measure gaps between starts.
        List<Cycle> cycles = new ArrayList<>();
        for (int i = 0; i < runs.size(); i++) {
            LocalDate start = runs.get(i).get(0);
            int periodLength = runs.get(i).size();
            int cycleLength = 0;
            if (i + 1 < runs.size()) {
                LocalDate nextStart = runs.get(i + 1).get(0);
                cycleLength = (int) (nextStart.toEpochDay() - start.toEpochDay());
            }
            cycles.add(new Cycle(start, periodLength, cycleLength));
        }

        int averageCycleLength = averageCompletedCycleLength(cycles);
        int averagePeriodLength = averagePeriodLength(cycles);
        LocalDate currentStart = cycles.get(cycles.size() - 1).getStartDate();
        LocalDate predictedNext = currentStart.plusDays(averageCycleLength);

        // One logged period gives a start date but no measured gap yet.
        boolean estimated = countCompleted(cycles) < 2;

        Collections.reverse(cycles);
        return new CycleInsights(cycles, averageCycleLength, averagePeriodLength,
                currentStart, predictedNext, estimated);
    }

    /**
     * Classifies a day for the calendar. {@code loggedPeriodDates} wins over any prediction,
     * because something the user recorded outranks something we guessed.
     */
    public static DayKind classify(LocalDate date, Set<LocalDate> loggedPeriodDates,
                                   CycleInsights insights) {
        if (loggedPeriodDates.contains(date)) {
            return DayKind.PERIOD;
        }
        LocalDate nextStart = insights.getPredictedNextStart();
        if (nextStart == null) {
            return DayKind.NONE;
        }
        if (!date.isBefore(nextStart)
                && date.isBefore(nextStart.plusDays(insights.getAveragePeriodLength()))) {
            return DayKind.PREDICTED_PERIOD;
        }
        LocalDate ovulation = nextStart.minusDays(LUTEAL_PHASE_DAYS);
        if (date.equals(ovulation)) {
            return DayKind.OVULATION;
        }
        if (!date.isBefore(ovulation.minusDays(FERTILE_DAYS_BEFORE_OVULATION))
                && !date.isAfter(ovulation.plusDays(FERTILE_DAYS_AFTER_OVULATION))) {
            return DayKind.FERTILE;
        }
        return DayKind.NONE;
    }

    public static Set<LocalDate> toDateSet(List<PeriodDay> periodDays) {
        Set<LocalDate> dates = new HashSet<>();
        for (PeriodDay day : periodDays) {
            dates.add(day.getDate());
        }
        return dates;
    }

    /** Splits sorted dates into runs of calendar-consecutive days. */
    private static List<List<LocalDate>> groupConsecutive(List<LocalDate> sortedDates) {
        List<List<LocalDate>> runs = new ArrayList<>();
        List<LocalDate> current = null;
        LocalDate previous = null;
        for (LocalDate date : sortedDates) {
            if (previous != null && date.equals(previous)) {
                continue;
            }
            if (previous == null || date.toEpochDay() - previous.toEpochDay() > 1) {
                current = new ArrayList<>();
                runs.add(current);
            }
            current.add(date);
            previous = date;
        }
        return runs;
    }

    private static int countCompleted(List<Cycle> cycles) {
        int n = 0;
        for (Cycle cycle : cycles) {
            if (cycle.isComplete()) {
                n++;
            }
        }
        return n;
    }

    private static int averageCompletedCycleLength(List<Cycle> cycles) {
        int total = 0;
        int n = 0;
        for (Cycle cycle : cycles) {
            if (cycle.isComplete()) {
                total += cycle.getCycleLength();
                n++;
            }
        }
        return n == 0 ? DEFAULT_CYCLE_LENGTH : Math.round((float) total / n);
    }

    private static int averagePeriodLength(List<Cycle> cycles) {
        // The newest run may still be in progress, so it would drag the average down.
        int total = 0;
        int n = 0;
        for (Cycle cycle : cycles) {
            if (cycle.isComplete()) {
                total += cycle.getPeriodLength();
                n++;
            }
        }
        if (n == 0) {
            return cycles.isEmpty()
                    ? DEFAULT_PERIOD_LENGTH
                    : Math.max(cycles.get(cycles.size() - 1).getPeriodLength(), 1);
        }
        return Math.round((float) total / n);
    }
}
