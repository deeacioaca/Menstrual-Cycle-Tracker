package com.example.arc.util;

import android.util.Patterns;

public final class Validation {

    public static final int MIN_PASSWORD_LENGTH = 5;

    private Validation() {
    }

    public static boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
