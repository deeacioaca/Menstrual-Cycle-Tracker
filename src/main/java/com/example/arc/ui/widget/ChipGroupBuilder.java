package com.example.arc.ui.widget;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.arc.R;
import com.example.arc.data.SymptomEntry;

import java.util.List;
import java.util.Set;

/** Fills a wrapping row with pill chips that write through on tap. */
public final class ChipGroupBuilder {

    /** Raised when a chip is switched on or off. */
    public interface OnToggle {
        void onToggle(String group, String label, boolean on);
    }

    private ChipGroupBuilder() {
    }

    public static void fill(ViewGroup container, List<String> labels, Set<String> active,
                            String group, OnToggle listener) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        for (String label : labels) {
            TextView chip = (TextView) inflater.inflate(R.layout.item_chip, container, false);
            chip.setText(label);
            chip.setSelected(active.contains(SymptomEntry.key(group, label)));
            chip.setOnClickListener(v -> {
                boolean on = !v.isSelected();
                v.setSelected(on);
                listener.onToggle(group, label, on);
            });
            container.addView(chip);
        }
    }
}
