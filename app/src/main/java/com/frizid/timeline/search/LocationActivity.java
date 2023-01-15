package com.frizid.timeline.search;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.adapter.AdapterUsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

@SuppressWarnings("ALL")
public class LocationActivity extends AppCompatActivity {

    //User
    AdapterUsers adapterUsers;
    List<ParseUser> userList;
    RecyclerView users_rv;

    private static final int LOCATION_PICK_CODE = 1009;
    TextView location;

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
        setContentView(R.layout.activity_location);

        //Back
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        //Search
        location = findViewById(R.id.location);
        findViewById(R.id.editText).setOnClickListener(v -> {
            Intent intent = new PlaceAutocomplete.IntentBuilder()
                    .accessToken("sk.eyJ1Ijoic3BhY2VzdGVyIiwiYSI6ImNrbmg2djJmdzJpZGQyd2xjeTk3a2twNTQifQ.iIiTRT_GwIYwFMsCWP5XGA")
                    .placeOptions(PlaceOptions.builder()
                            .backgroundColor(Color.parseColor("#ffffff"))
                            .build(PlaceOptions.MODE_CARDS))
                    .build(this);
            startActivityForResult(intent, LOCATION_PICK_CODE);
        });

        //User
        users_rv = findViewById(R.id.users);
        users_rv.setLayoutManager(new LinearLayoutManager(LocationActivity.this));
        userList = new ArrayList<>();

        if (location.getText().toString().isEmpty()){
            findViewById(R.id.set).setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.GONE);
        }else {
            getAllUsers();
            findViewById(R.id.set).setVisibility(View.GONE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        }

    }

    private void getAllUsers() {

        ParseQuery<ParseUser> userq = ParseUser.getQuery();
        userq.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        userq.whereEqualTo("location", location.getText().toString().toLowerCase());
        userq.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> modelUserList, ParseException e) {
                if (e == null) {
                    if (modelUserList.size() > 0) {
                        userList.addAll(modelUserList);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });

                    Collections.reverse(userList);
                    adapterUsers = new AdapterUsers(LocationActivity.this, userList);
                    users_rv.setAdapter(adapterUsers);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    if (adapterUsers.getItemCount() == 0){
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.users).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.users).setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Location
        if (resultCode == Activity.RESULT_OK && requestCode == LOCATION_PICK_CODE && data != null) {
            CarmenFeature feature = PlaceAutocomplete.getPlace(data);
            location.setText(feature.text());

            if (location.getText().toString().isEmpty()){
                findViewById(R.id.set).setVisibility(View.VISIBLE);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }else {
                getAllUsers();
                findViewById(R.id.set).setVisibility(View.GONE);
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            }

        }
    }
}