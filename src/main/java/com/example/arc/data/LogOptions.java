package com.example.arc.data;

import java.util.Arrays;
import java.util.List;

/** The chips offered on the Log screen, grouped the way the design presents them. */
public final class LogOptions {

    public static final String GROUP_MOOD = "Mood";
    public static final String GROUP_SYMPTOM = "Symptom";

    public static final List<String> MOODS = Arrays.asList(
            "Calm", "Low", "Irritable", "Energetic", "Anxious", "Content");

    public static final List<String> SYMPTOMS = Arrays.asList(
            "Cramps", "Headache", "Bloating", "Tender breasts",
            "Acne", "Back ache", "Nausea", "Poor sleep");

    private LogOptions() {
    }
}
