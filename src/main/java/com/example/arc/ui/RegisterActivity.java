package com.example.arc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arc.R;
import com.example.arc.util.Validation;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameField;
    private EditText emailField;
    private EditText passwordField;
    private CompoundButton termsSwitch;
    private CompoundButton privacySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameField = findViewById(R.id.etName);
        emailField = findViewById(R.id.etEmail);
        passwordField = findViewById(R.id.etPassword);
        termsSwitch = findViewById(R.id.switchTermsOfService);
        privacySwitch = findViewById(R.id.switchPrivacyPolicy);

        findViewById(R.id.back_register).setOnClickListener(v -> finish());

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            if (isValid()) {
                continueToLogin();
            }
        });
    }

    private void continueToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_EMAIL, text(emailField));
        intent.putExtra(LoginActivity.EXTRA_PASSWORD, text(passwordField));
        startActivity(intent);
    }

    private boolean isValid() {
        if (text(nameField).isEmpty()) {
            return fail(R.string.error_name_required);
        }
        if (text(emailField).isEmpty()) {
            return fail(R.string.error_email_required);
        }
        if (!Validation.isEmailValid(text(emailField))) {
            return fail(R.string.error_email_invalid);
        }
        if (text(passwordField).isEmpty()) {
            return fail(R.string.error_password_required);
        }
        if (text(passwordField).length() < Validation.MIN_PASSWORD_LENGTH) {
            return fail(R.string.error_password_too_short);
        }
        if (!termsSwitch.isChecked()) {
            return fail(R.string.error_terms_required);
        }
        if (!privacySwitch.isChecked()) {
            return fail(R.string.error_privacy_required);
        }
        return true;
    }

    private String text(EditText field) {
        return field.getText().toString();
    }

    /** Shows {@code message} and reports the field as invalid. */
    private boolean fail(@StringRes int message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        return false;
    }
}
