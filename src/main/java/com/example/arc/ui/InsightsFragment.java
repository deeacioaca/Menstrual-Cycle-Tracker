package com.example.arc.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.arc.R;
import com.example.arc.cycle.Cycle;
import com.example.arc.cycle.CycleInsights;
import com.example.arc.data.ArcDatabase;
import com.example.arc.data.CycleRepository;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Cycle length history, averages, and what the numbers say. */
public class InsightsFragment extends Fragment {

    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MMM");
    private static final int MAX_BARS = 6;

    private CycleRepository repository;
    private View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_insights, container, false);
        repository = new CycleRepository(requireContext());
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ArcDatabase.io().execute(() -> {
            CycleInsights insights = repository.insights();
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> render(insights));
            }
        });
    }

    private void render(CycleInsights insights) {
        List<Cycle> complete = new ArrayList<>();
        for (Cycle c : insights.getCycles()) {
            if (c.isComplete()) {
                complete.add(c);
            }
        }
        Collections.reverse(complete); // oldest first, left to right
        if (complete.size() > MAX_BARS) {
            complete = complete.subList(complete.size() - MAX_BARS, complete.size());
        }

        buildChart(complete);

        TextView summary = root.findViewById(R.id.chartSummary);
        TextView periodValue = root.findViewById(R.id.avgPeriodValue);
        TextView variationValue = root.findViewById(R.id.variationValue);
        TextView patternBody = root.findViewById(R.id.patternBody);
        View empty = root.findViewById(R.id.insightsEmpty);
        View chartCard = root.findViewById(R.id.chartCard);

        boolean hasData = !complete.isEmpty();
        empty.setVisibility(hasData ? View.GONE : View.VISIBLE);
        chartCard.setVisibility(hasData ? View.VISIBLE : View.GONE);

        periodValue.setText(getString(R.string.today_days, insights.getAveragePeriodLength()));

        int variation = variationOf(complete);
        variationValue.setText(variation < 0 ? "—" : getString(R.string.insights_plus_minus, variation));

        if (!hasData) {
            patternBody.setText(R.string.insights_pattern_none);
            return;
        }
        summary.setText(variation <= 2
                ? getString(R.string.insights_summary_regular)
                : getString(R.string.insights_summary_variable, variation));
        patternBody.setText(variation <= 2
                ? getString(R.string.insights_pattern_confident)
                : getString(R.string.insights_pattern_spread));
    }

    private void buildChart(List<Cycle> cycles) {
        LinearLayout chart = root.findViewById(R.id.chart);
        chart.removeAllViews();
        if (cycles.isEmpty()) {
            return;
        }
        float density = getResources().getDisplayMetrics().density;
        int shortest = Integer.MAX_VALUE;
        for (Cycle c : cycles) {
            shortest = Math.min(shortest, c.getCycleLength());
        }

        for (int i = 0; i < cycles.size(); i++) {
            Cycle cycle = cycles.get(i);
            boolean latest = i == cycles.size() - 1;

            LinearLayout column = new LinearLayout(requireContext());
            column.setOrientation(LinearLayout.VERTICAL);
            column.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams cp =
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            cp.setMarginEnd((int) (10 * density));
            column.setLayoutParams(cp);

            TextView value = new TextView(requireContext());
            value.setText(String.valueOf(cycle.getCycleLength()));
            value.setTextSize(11.5f);
            value.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.hanken_regular));
            value.setTextColor(ContextCompat.getColor(requireContext(), R.color.arc_label));
            column.addView(value);

            View bar = new View(requireContext());
            // Scale from the shortest cycle so small differences stay visible.
            int barDp = 30 + Math.max(cycle.getCycleLength() - shortest, 0) * 16;
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, (int) (Math.min(barDp, 110) * density));
            bp.topMargin = (int) (9 * density);
            bar.setLayoutParams(bp);
            bar.setBackgroundResource(latest ? R.drawable.bg_bar_latest : R.drawable.bg_bar);
            column.addView(bar);

            TextView month = new TextView(requireContext());
            month.setText(cycle.getStartDate().format(MONTH));
            month.setTextSize(10.5f);
            month.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.hanken_light));
            month.setTextColor(ContextCompat.getColor(requireContext(), R.color.arc_faint));
            LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mp.topMargin = (int) (9 * density);
            month.setLayoutParams(mp);
            column.addView(month);

            chart.addView(column);
        }
    }

    /** Half the spread of observed cycle lengths, or -1 when there is nothing to compare. */
    private int variationOf(List<Cycle> cycles) {
        if (cycles.size() < 2) {
            return -1;
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Cycle c : cycles) {
            min = Math.min(min, c.getCycleLength());
            max = Math.max(max, c.getCycleLength());
        }
        return (int) Math.ceil((max - min) / 2.0);
    }
}
