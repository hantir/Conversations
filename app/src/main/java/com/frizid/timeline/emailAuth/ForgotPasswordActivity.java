package com.frizid.timeline.emailAuth;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.frizid.timeline.R;

import java.util.Objects;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        //Back
        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        //EditText
        EditText email = findViewById(R.id.email);

        //Button
        findViewById(R.id.send).setOnClickListener(v -> {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            String mEmail = email.getText().toString().trim();

            if (mEmail.isEmpty()){
                Snackbar.make(v, "Enter your email", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }else {
                ParseUser.requestPasswordResetInBackground(mEmail, new RequestPasswordResetCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            Snackbar.make(v, "Password reset link sent on your email", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(v, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG).show();
                        }
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                    }
                });
            }

        });

    }
}