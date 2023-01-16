package com.frizid.timeline.menu;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.frizid.timeline.emailAuth.LoginActivity;
import com.parse.ParseUser;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.SharedMode;
import com.frizid.timeline.profile.EditProfileActivity;
import com.frizid.timeline.reel.ReelActivity;
import com.frizid.timeline.search.LocationActivity;
import com.frizid.timeline.search.SearchActivity;

import java.util.Locale;
import java.util.Objects;

@SuppressWarnings("ALL")
public class MenuActivity extends AppCompatActivity {

    NightMode sharedPref;
    SharedMode sharedMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState().equals("night")){
            setTheme(R.style.DarkTheme);
        }else if (sharedPref.loadNightModeState().equals("dim")){
            setTheme(R.style.DimTheme);
        }else setTheme(R.style.AppTheme);
        sharedMode = new SharedMode(this);
        if (!sharedMode.loadNightModeState().isEmpty()){
            setApplicationLocale(sharedMode.loadNightModeState());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.reel).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, ReelActivity.class)));

        findViewById(R.id.group).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, TermsActivity.class)));

        findViewById(R.id.saved).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, SavedActivity.class)));

        findViewById(R.id.near).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, LocationActivity.class)));

        findViewById(R.id.camera).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, TranslationActivity.class)));

        findViewById(R.id.editImage).setOnClickListener(v -> pickImage());

        findViewById(R.id.editVideo).setOnClickListener(v -> pickVideo());

        findViewById(R.id.search).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, SearchActivity.class)));

        findViewById(R.id.verify).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, VerificationActivity.class)));

        findViewById(R.id.logOut).setOnClickListener(v -> {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("Please, wait a moment.");
            dialog.setMessage("Logging out...");
            dialog.show();
            ParseUser.logOutInBackground(e -> {
                if (e == null) {
                    Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                else
                    Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        });

        findViewById(R.id.email).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, EditEmailActivity.class)));

        findViewById(R.id.pass).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, EditPasswordActivity.class)));

        findViewById(R.id.profile).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, EditProfileActivity.class)));

        findViewById(R.id.phone).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, EditNumberActivity.class)));

        findViewById(R.id.policy).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, PrivacyActivity.class)));


        if (ParseUser.getCurrentUser().get("phone").toString().isEmpty()){
            findViewById(R.id.email).setVisibility(View.VISIBLE);
            findViewById(R.id.pass).setVisibility(View.VISIBLE);
            findViewById(R.id.phone).setVisibility(View.GONE);
        }else {
            findViewById(R.id.email).setVisibility(View.GONE);
            findViewById(R.id.pass).setVisibility(View.GONE);
            findViewById(R.id.phone).setVisibility(View.VISIBLE);
        }

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch aSwitch = findViewById(R.id.nightSwitch);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch dimSwitch = findViewById(R.id.dimSwitch);

        if (sharedPref.loadNightModeState().equals("night")){
            aSwitch.setChecked(true);
            dimSwitch.setChecked(false);
        }
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                sharedPref.setNightModeState("night");
                dimSwitch.setChecked(false);
            }else {
                if (dimSwitch.isChecked()){
                    sharedPref.setNightModeState("dim");
                    dimSwitch.setChecked(true);
                    aSwitch.setChecked(false);
                }else {
                    sharedPref.setNightModeState("day");
                    dimSwitch.setChecked(false);
                    aSwitch.setChecked(false);
                }

            }
            restartApp();
        });

        if (sharedPref.loadNightModeState().equals("dim")){
            dimSwitch.setChecked(true);
            aSwitch.setChecked(false);
        }
        dimSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                sharedPref.setNightModeState("dim");
                aSwitch.setChecked(false);
            }else {
                if (aSwitch.isChecked()){
                    sharedPref.setNightModeState("night");
                    dimSwitch.setChecked(false);
                    aSwitch.setChecked(true);
                }else {
                    sharedPref.setNightModeState("day");
                    dimSwitch.setChecked(false);
                    aSwitch.setChecked(false);
                }

            }
            restartApp();
        });

        findViewById(R.id.invite).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, InviteActivity.class)));

    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, 1);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK && requestCode == 1 && data != null){
            Uri video_uri = Objects.requireNonNull(data).getData();
            Intent intent = new Intent(MenuActivity.this, MenuActivity.class);
            intent.putExtra("uri", video_uri.toString());
            startActivity(intent);
        }
        if (resultCode == RESULT_OK && requestCode == 2 && data != null){
            Uri dp_uri = Objects.requireNonNull(data).getData();
            Intent intent = new Intent(MenuActivity.this, MenuActivity.class);
            intent.putExtra("uri", dp_uri.toString());
            startActivity(intent);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void restartApp() {
        Intent i = getBaseContext().getPackageManager().
                getLaunchIntentForPackage(getBaseContext().getPackageName());
        Objects.requireNonNull(i).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void setApplicationLocale(String locale) {
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(new Locale(locale.toLowerCase()));
        } else {
            config.locale = new Locale(locale.toLowerCase());
        }
        resources.updateConfiguration(config, dm);
    }

}