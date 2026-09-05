package com.example.arc.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.arc.R;
import com.example.arc.data.ArcDatabase;
import com.example.arc.data.CycleRepository;
import com.example.arc.data.LogOptions;
import com.example.arc.data.PeriodDay;
import com.example.arc.data.SymptomEntry;
import com.example.arc.ui.widget.ChipGroupBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/** Flow, mood and symptoms for one day. Every tap writes straight through. */
public class LogFragment extends Fragment {

    private static final DateTimeFormatter SUBTITLE = DateTimeFormatter.ofPattern("EEEE, d MMMM");

    private CycleRepository repository;
    private View root;
    private LocalDate date;
    private final View[] flowOptions = new View[4];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_log, container, false);
        repository = new CycleRepository(requireContext());

        date = LocalDate.now();
        if (getActivity() instanceof MainActivity) {
            LocalDate handed = ((MainActivity) getActivity()).consumeLogDate();
            if (handed != null) {
                date = handed;
            }
        }

        ((TextView) root.findViewById(R.id.logTitle)).setText(
                date.equals(LocalDate.now()) ? getString(R.string.log_title_today)
                        : getString(R.string.log_title_other));
        ((TextView) root.findViewById(R.id.logSubtitle)).setText(date.format(SUBTITLE));

        flowOptions[0] = root.findViewById(R.id.flowNone);
        flowOptions[1] = root.findViewById(R.id.flowLight);
        flowOptions[2] = root.findViewById(R.id.flowMedium);
        flowOptions[3] = root.findViewById(R.id.flowHeavy);
        for (int i = 0; i < flowOptions.length; i++) {
            final int flow = i;
            flowOptions[i].setOnClickListener(v -> setFlow(flow));
        }
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ArcDatabase.io().execute(() -> {
            PeriodDay day = repository.periodDay(date);
            Set<String> active = repository.symptomsOn(date);
            int flow = day == null ? 0 : day.getFlow();
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    highlightFlow(flow);
                    buildChips(active);
                });
            }
        });
    }

    private void buildChips(Set<String> active) {
        ChipGroupBuilder.fill(
                root.findViewById(R.id.moodChips), LogOptions.MOODS, active,
                LogOptions.GROUP_MOOD, this::toggle);
        ChipGroupBuilder.fill(
                root.findViewById(R.id.symptomChips), LogOptions.SYMPTOMS, active,
                LogOptions.GROUP_SYMPTOM, this::toggle);
    }

    private void toggle(String group, String label, boolean on) {
        ArcDatabase.io().execute(() -> repository.setSymptom(date, group, label, on));
    }

    private void setFlow(int flow) {
        highlightFlow(flow);
        ArcDatabase.io().execute(() -> {
            if (flow == 0) {
                repository.clearPeriodDay(date);
            } else {
                repository.setPeriodDay(date, flow);
            }
        });
    }

    private void highlightFlow(int flow) {
        for (int i = 0; i < flowOptions.length; i++) {
            flowOptions[i].setSelected(i == flow);
        }
    }

    /** Key helper so the chip builder and repository agree on identity. */
    public static String key(String group, String label) {
        return SymptomEntry.key(group, label);
    }
}
