package com.frizid.timeline.menu;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;

import java.util.Objects;

public class EditEmailActivity extends AppCompatActivity {

    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState().equals("night")){
            setTheme(R.style.DarkTheme);
        }else if (sharedPref.loadNightModeState().equals("dim")){
            setTheme(R.style.DimTheme);
        }else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_email);

        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        EditText email = findViewById(R.id.email);
        EditText pass = findViewById(R.id.pass);

        findViewById(R.id.login).setOnClickListener(v -> {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            if (pass.getText().toString().isEmpty()){  findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(v, "Enter your password", Snackbar.LENGTH_SHORT).show();
            }else if (email.getText().toString().isEmpty()){  findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(v, "Enter your new email", Snackbar.LENGTH_SHORT).show();
            }else {
                ParseUser.getCurrentUser().setEmail(email.getText().toString());
                ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            Snackbar.make(v, "Email Changed", Snackbar.LENGTH_SHORT).show();
                            pass.setText("");
                            email.setText("");
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                        } else {
                            Snackbar.make(v, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_SHORT).show();
                        }
                    }});
            }
        });

    }
}