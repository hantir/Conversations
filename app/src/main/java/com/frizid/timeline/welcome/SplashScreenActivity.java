package com.frizid.timeline.welcome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.frizid.timeline.Check;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.emailAuth.LoginActivity;

@SuppressWarnings("ALL")
public class SplashScreenActivity extends AppCompatActivity {

    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState().equals("night")){
            setTheme(R.style.SplashScreenDark);
        }else if (sharedPref.loadNightModeState().equals("dim")){
            setTheme(R.style.SplashScreenDim);
        }else setTheme(R.style.SplashScreen);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        SharedPreferences settings=getSharedPreferences("prefs",0);
        boolean firstRun= settings.getBoolean("firstRun",false);
        if(!firstRun) {
            SharedPreferences.Editor editor= settings.edit();
            editor.putBoolean("firstRun",true);
            editor.apply();
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class );
                startActivity(intent);
                finish();
            },3000);
        } else {
            new Handler().postDelayed(() -> {
               Intent intent = new Intent(getApplicationContext(), Check.class );
                startActivity(intent);
                finish();
            },3000);
        }

    }
}