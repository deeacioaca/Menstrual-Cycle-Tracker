package com.example.arc.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.arc.R;
import com.example.arc.cycle.CycleInsights;
import com.example.arc.cycle.Phase;
import com.example.arc.data.ArcDatabase;
import com.example.arc.data.CycleRepository;
import com.example.arc.ui.widget.CycleRingView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** The ring, the phase timeline, and what to expect. */
public class TodayFragment extends Fragment {

    private static final DateTimeFormatter TODAY_FORMAT = DateTimeFormatter.ofPattern("EEEE, d MMM");
    private static final DateTimeFormatter NEXT_FORMAT = DateTimeFormatter.ofPattern("d MMM");

    private CycleRepository repository;
    private View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_today, container, false);
        repository = new CycleRepository(requireContext());
        ((TextView) root.findViewById(R.id.dateLabel))
                .setText(LocalDate.now().format(TODAY_FORMAT).toUpperCase());
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
        if (root == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        CycleRingView ring = root.findViewById(R.id.ring);
        TextView dayView = root.findViewById(R.id.cycleDay);
        TextView phaseView = root.findViewById(R.id.phaseName);
        TextView pillView = root.findViewById(R.id.periodPill);
        TextView expect = root.findViewById(R.id.expectBody);
        TextView nextValue = root.findViewById(R.id.nextPeriodValue);
        TextView nextNote = root.findViewById(R.id.nextPeriodNote);
        TextView lenValue = root.findViewById(R.id.cycleLengthValue);
        TextView lenNote = root.findViewById(R.id.cycleLengthNote);
        View timeline = root.findViewById(R.id.timeline);

        if (!insights.hasData()) {
            dayView.setText("—");
            phaseView.setText(R.string.today_no_data_phase);
            pillView.setText(R.string.today_no_data_pill);
            expect.setText(R.string.today_no_data_body);
            nextValue.setText("—");
            nextNote.setText(R.string.today_awaiting);
            lenValue.setText(getString(R.string.today_days, insights.getAverageCycleLength()));
            lenNote.setText(R.string.today_default_estimate);
            timeline.setVisibility(View.GONE);
            ring.setCycle(0, insights.getAverageCycleLength(), 0, 0, -1);
            return;
        }

        int day = insights.cycleDayOn(today);
        int length = insights.getAverageCycleLength();
        ring.setCycle(day, length, insights.getAveragePeriodLength(),
                insights.getFertileStartDay(), insights.getFertileEndDay());

        dayView.setText(String.valueOf(day));
        phaseView.setText(getString(R.string.today_phase, phaseLabel(insights.phaseOn(day))));

        long days = insights.daysUntilNextPeriod(today);
        pillView.setText(days > 1 ? getString(R.string.today_period_in, days)
                : days == 1 ? getString(R.string.today_period_tomorrow)
                : days == 0 ? getString(R.string.today_period_today)
                : getString(R.string.today_period_late, -days));

        expect.setText(expectationFor(insights.phaseOn(day)));
        nextValue.setText(insights.getPredictedNextStart().format(NEXT_FORMAT));
        nextNote.setText(insights.isEstimated()
                ? getString(R.string.today_estimate_rough)
                : getString(R.string.today_estimate_tight));
        lenValue.setText(getString(R.string.today_days, length));
        lenNote.setText(insights.isEstimated()
                ? getString(R.string.today_few_cycles)
                : getString(R.string.today_regular));

        timeline.setVisibility(View.VISIBLE);
        layOutTimeline(insights, day);
    }

    /** The four phase bands, weighted by this user's own averages. */
    private void layOutTimeline(CycleInsights insights, int day) {
        int period = insights.getAveragePeriodLength();
        int fertileStart = insights.getFertileStartDay();
        int fertileEnd = insights.getFertileEndDay();
        int length = insights.getAverageCycleLength();

        int follicular = Math.max(fertileStart - period - 1, 1);
        int fertile = Math.max(fertileEnd - fertileStart + 1, 1);
        int luteal = Math.max(length - fertileEnd, 1);

        weight(R.id.bandPeriod, period);
        weight(R.id.bandFollicular, follicular);
        weight(R.id.bandFertile, fertile);
        weight(R.id.bandLuteal, luteal);
        weight(R.id.labelPeriod, period);
        weight(R.id.labelFollicular, follicular);
        weight(R.id.labelFertile, fertile);
        weight(R.id.labelLuteal, luteal);

        // Mark the phase you are actually in, rather than assuming it is the last band.
        Phase now = insights.phaseOn(day);
        markPhase(R.id.labelPeriod, now == Phase.MENSTRUAL);
        markPhase(R.id.labelFollicular, now == Phase.FOLLICULAR);
        markPhase(R.id.labelFertile, now == Phase.FERTILE);
        markPhase(R.id.labelLuteal, now == Phase.LUTEAL);
    }

    /** The current phase is inked in; the rest stay muted. */
    private void markPhase(int id, boolean current) {
        TextView label = root.findViewById(id);
        label.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(),
                current ? R.color.arc_ink : R.color.arc_label));
        label.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(requireContext(),
                current ? R.font.hanken_medium : R.font.hanken_light));
    }

    private void weight(int id, float w) {
        View v = root.findViewById(id);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.weight = w;
        v.setLayoutParams(lp);
    }

    private String phaseLabel(Phase phase) {
        switch (phase) {
            case MENSTRUAL:
                return getString(R.string.phase_menstrual);
            case FOLLICULAR:
                return getString(R.string.phase_follicular);
            case FERTILE:
                return getString(R.string.phase_fertile);
            default:
                return getString(R.string.phase_luteal);
        }
    }

    private String expectationFor(Phase phase) {
        switch (phase) {
            case MENSTRUAL:
                return getString(R.string.expect_menstrual);
            case FOLLICULAR:
                return getString(R.string.expect_follicular);
            case FERTILE:
                return getString(R.string.expect_fertile);
            default:
                return getString(R.string.expect_luteal);
        }
    }
}
