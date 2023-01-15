package com.frizid.timeline;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.frizid.timeline.emailAuth.LoginActivity;
import com.parse.ParseUser;

public class Check extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null){
            startActivity(new Intent(Check.this, LoginActivity.class));
        }else {
            startActivity(new Intent(Check.this, MainActivity.class));
        }
        finish();
    }

}