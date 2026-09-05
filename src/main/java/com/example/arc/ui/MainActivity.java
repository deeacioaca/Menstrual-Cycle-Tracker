package com.example.arc.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.arc.R;

import java.time.LocalDate;

/** The tabbed shell: Today, Calendar, Log, Insights. */
public class MainActivity extends AppCompatActivity {

    private static final String STATE_TAB = "tab";

    private final int[] tabIds = {R.id.tab_today, R.id.tab_calendar, R.id.tab_log, R.id.tab_insights};
    private int current = -1;

    /** Day the Calendar tab handed to the Log tab, if any. */
    private LocalDate pendingLogDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int i = 0; i < tabIds.length; i++) {
            final int index = i;
            findViewById(tabIds[i]).setOnClickListener(v -> select(index));
        }
        select(savedInstanceState == null ? 0 : savedInstanceState.getInt(STATE_TAB, 0));
    }

    @Override
    protected void onSaveInstanceState(@androidx.annotation.NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TAB, current);
    }

    /** Opens the Log tab for a particular day, used by the calendar's "Log this day". */
    public void openLogFor(LocalDate date) {
        pendingLogDate = date;
        select(2);
    }

    public LocalDate consumeLogDate() {
        LocalDate d = pendingLogDate;
        pendingLogDate = null;
        return d;
    }

    private void select(int index) {
        if (index == current) {
            return;
        }
        current = index;

        Fragment fragment;
        switch (index) {
            case 1:
                fragment = new CalendarFragment();
                break;
            case 2:
                fragment = new LogFragment();
                break;
            case 3:
                fragment = new InsightsFragment();
                break;
            default:
                fragment = new TodayFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .commit();

        for (int i = 0; i < tabIds.length; i++) {
            View tab = findViewById(tabIds[i]);
            boolean on = i == index;
            tab.findViewWithTag("icon").setSelected(on);
            TextView label = tab.findViewWithTag("label");
            label.setSelected(on);
            label.setTextColor(ContextCompat.getColor(this,
                    on ? R.color.arc_ink : R.color.arc_tab_idle));
        }
    }
}
