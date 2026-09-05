package com.example.arc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arc.R;
import com.example.arc.util.Validation;

public class LoginActivity extends AppCompatActivity {

    /** Prefill values handed over by {@link RegisterActivity}. */
    public static final String EXTRA_EMAIL = "email";
    public static final String EXTRA_PASSWORD = "password";

    private EditText emailField;
    private EditText passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField = findViewById(R.id.etEmailLogin);
        passwordField = findViewById(R.id.etPasswordLogin);

        emailField.setText(getIntent().getStringExtra(EXTRA_EMAIL));
        passwordField.setText(getIntent().getStringExtra(EXTRA_PASSWORD));

        findViewById(R.id.back_login).setOnClickListener(v -> finish());

        findViewById(R.id.button).setOnClickListener(v -> {
            if (isValid()) {
                startActivity(new Intent(this, MainActivity.class));
            }
        });
    }

    private boolean isValid() {
        if (text(emailField).isEmpty()) {
            return fail(R.string.error_email_required);
        }
        if (!Validation.isEmailValid(text(emailField))) {
            return fail(R.string.error_email_invalid);
        }
        if (text(passwordField).isEmpty()) {
            return fail(R.string.error_password_required);
        }
        return true;
    }

    private String text(EditText field) {
        return field.getText().toString();
    }

    private boolean fail(@StringRes int message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        return false;
    }
}
