package com.frizid.timeline.menu;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseUser;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.model.Verification;

public class VerificationActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_verification);

        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        EditText name = findViewById(R.id.name);
        EditText username = findViewById(R.id.username);
        EditText known = findViewById(R.id.known);
        EditText id = findViewById(R.id.id);

        findViewById(R.id.send).setOnClickListener(v -> {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            if (name.getText().toString().isEmpty()) { findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(v, "Enter name", Snackbar.LENGTH_SHORT).show();
            }else if (username.getText().toString().isEmpty()) {findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(v, "Enter username", Snackbar.LENGTH_SHORT).show();
            }else if (known.getText().toString().isEmpty()) {findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(v, "Enter known as", Snackbar.LENGTH_SHORT).show();
            } else if (id.getText().toString().isEmpty()) {findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(v, "Enter photo id link", Snackbar.LENGTH_SHORT).show();
            }else {
                Verification vq = new Verification();
                vq.setName(name.getText().toString());
                vq.setUsername(username.getText().toString());
                vq.setUserObj(ParseUser.getCurrentUser());
                vq.setLink(id.getText().toString());
                vq.setKnown(known.getText().toString());
                vq.saveInBackground();
                Snackbar.make(v, "Request Sent", Snackbar.LENGTH_LONG).show();
                name.setText("");
                username.setText("");
                known.setText("");
                id.setText("");
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });

    }
}