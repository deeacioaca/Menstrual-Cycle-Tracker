package com.example.arc.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.arc.R;

/** Landing screen: sign up, or continue to sign in. */
public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        findViewById(R.id.register_button).setOnClickListener(
                v -> startActivity(new Intent(this, RegisterActivity.class)));

        findViewById(R.id.login_button).setOnClickListener(
                v -> startActivity(new Intent(this, LoginActivity.class)));
    }
}
