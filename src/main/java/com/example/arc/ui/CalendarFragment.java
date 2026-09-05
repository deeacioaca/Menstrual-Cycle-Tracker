package com.example.arc.ui;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.arc.R;
import com.example.arc.cycle.CycleCalculator;
import com.example.arc.cycle.CycleInsights;
import com.example.arc.cycle.DayKind;
import com.example.arc.data.ArcDatabase;
import com.example.arc.data.CycleRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;

/** Month grid, legend, and a card describing the selected day. */
public class CalendarFragment extends Fragment {

    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MMMM");
    private static final DateTimeFormatter SELECTED = DateTimeFormatter.ofPattern("d MMMM");

    private CycleRepository repository;
    private View root;
    private GridLayout grid;

    private YearMonth month = YearMonth.now();
    private LocalDate selected = LocalDate.now();

    private Set<LocalDate> periods = Collections.emptySet();
    private Set<LocalDate> symptoms = Collections.emptySet();
    private CycleInsights insights;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_calendar, container, false);
        repository = new CycleRepository(requireContext());
        grid = root.findViewById(R.id.monthGrid);

        root.findViewById(R.id.previousMonth).setOnClickListener(v -> {
            month = month.minusMonths(1);
            renderAll();
        });
        root.findViewById(R.id.nextMonth).setOnClickListener(v -> {
            month = month.plusMonths(1);
            renderAll();
        });
        root.findViewById(R.id.logThisDay).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openLogFor(selected);
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ArcDatabase.io().execute(() -> {
            Set<LocalDate> p = repository.periodDates();
            Set<LocalDate> s = repository.datesWithSymptoms();
            CycleInsights i = repository.insights();
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    periods = p;
                    symptoms = s;
                    insights = i;
                    renderAll();
                });
            }
        });
    }

    private void renderAll() {
        ((TextView) root.findViewById(R.id.monthLabel)).setText(month.atDay(1).format(MONTH));
        ((TextView) root.findViewById(R.id.yearLabel)).setText(String.valueOf(month.getYear()));
        buildGrid();
        renderSelected();
    }

    private void buildGrid() {
        grid.removeAllViews();
        LocalDate first = month.atDay(1);
        int lead = first.getDayOfWeek().getValue() % 7; // Sunday-first, matching the design
        int cells = ((lead + month.lengthOfMonth() + 6) / 7) * 7;

        int cellHeight = (int) (46 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < cells; i++) {
            int dayOfMonth = i - lead + 1;
            View cell = buildCell(dayOfMonth < 1 || dayOfMonth > month.lengthOfMonth()
                    ? null : month.atDay(dayOfMonth));

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = cellHeight;
            lp.columnSpec = GridLayout.spec(i % 7, 1, 1f);
            lp.rowSpec = GridLayout.spec(i / 7);
            lp.bottomMargin = (int) (4 * getResources().getDisplayMetrics().density);
            int gap = (int) (1 * getResources().getDisplayMetrics().density);
            lp.leftMargin = gap;
            lp.rightMargin = gap;
            cell.setLayoutParams(lp);
            grid.addView(cell);
        }
    }

    private View buildCell(@Nullable LocalDate date) {
        LinearLayout cell = new LinearLayout(requireContext());
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        if (date == null) {
            return cell;
        }

        boolean isSelected = date.equals(selected);
        DayKind kind = insights == null
                ? (periods.contains(date) ? DayKind.PERIOD : DayKind.NONE)
                : CycleCalculator.classify(date, periods, insights);

        // A tint over a transparent shape paints nothing, so set the solid colour itself.
        cell.setBackgroundResource(R.drawable.bg_day_cell);
        GradientDrawable shape = (GradientDrawable) cell.getBackground().mutate();
        int fill;
        if (isSelected) {
            fill = color(R.color.arc_ink);
        } else if (kind == DayKind.PERIOD) {
            fill = color(R.color.arc_chip_on);
        } else if (kind == DayKind.PREDICTED_PERIOD) {
            fill = color(R.color.arc_predicted_cell);
        } else if (kind == DayKind.FERTILE || kind == DayKind.OVULATION) {
            fill = color(R.color.arc_fertile_cell);
        } else {
            fill = 0x00000000;
        }
        shape.setColor(fill);
        // Today keeps a ring when it is not the selected day.
        boolean isToday = date.equals(LocalDate.now());
        shape.setStroke(isToday && !isSelected
                        ? (int) (1 * getResources().getDisplayMetrics().density) : 0,
                color(R.color.arc_border_strong));

        TextView number = new TextView(requireContext());
        number.setText(String.valueOf(date.getDayOfMonth()));
        number.setTextSize(14.5f);
        number.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.hanken_regular),
                isToday ? Typeface.BOLD : Typeface.NORMAL);
        number.setTextColor(color(isSelected ? R.color.arc_paper : R.color.arc_text));
        cell.addView(number);

        View dot = new View(requireContext());
        int d = (int) (4 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(d, d);
        dp.topMargin = (int) (5 * getResources().getDisplayMetrics().density);
        dot.setLayoutParams(dp);
        if (symptoms.contains(date)) {
            dot.setBackgroundResource(isSelected ? R.drawable.dot_ink : R.drawable.dot_period);
        } else if (kind == DayKind.PERIOD) {
            dot.setBackgroundResource(R.drawable.dot_period);
        } else if (kind == DayKind.PREDICTED_PERIOD) {
            dot.setBackgroundResource(R.drawable.dot_predicted);
        } else if (kind == DayKind.FERTILE || kind == DayKind.OVULATION) {
            dot.setBackgroundResource(R.drawable.dot_fertile);
        } else {
            dot.setVisibility(View.INVISIBLE);
        }
        cell.addView(dot);

        cell.setOnClickListener(v -> {
            selected = date;
            buildGrid();
            renderSelected();
        });
        return cell;
    }

    private void renderSelected() {
        ((TextView) root.findViewById(R.id.selectedLabel))
                .setText(selected.format(SELECTED).toUpperCase());

        TextView note = root.findViewById(R.id.selectedNote);
        DayKind kind = insights == null
                ? DayKind.NONE
                : CycleCalculator.classify(selected, periods, insights);
        boolean logged = symptoms.contains(selected);

        if (periods.contains(selected)) {
            note.setText(R.string.calendar_note_period);
        } else if (kind == DayKind.PREDICTED_PERIOD) {
            note.setText(R.string.calendar_note_predicted);
        } else if (kind == DayKind.OVULATION) {
            note.setText(R.string.calendar_note_ovulation);
        } else if (kind == DayKind.FERTILE) {
            note.setText(R.string.calendar_note_fertile);
        } else if (logged) {
            note.setText(R.string.calendar_note_logged);
        } else {
            note.setText(R.string.calendar_note_empty);
        }
    }

    private int color(int res) {
        return ContextCompat.getColor(requireContext(), res);
    }
}
