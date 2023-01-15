package com.frizid.timeline.notifications;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.adapter.AdapterNotification;
import com.frizid.timeline.model.Ads;
import com.frizid.timeline.model.Notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class NotificationScreen extends AppCompatActivity {

    private ArrayList<Notification> notifications;
    private AdapterNotification adapterNotification;
    RecyclerView recyclerView;

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
        setContentView(R.layout.activity_notification_screen);

        MobileAds.initialize(getApplicationContext(), initializationStatus -> {
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);

        //Notification
        ParseUser.getCurrentUser().put("count",0);
        ParseUser.getCurrentUser().saveInBackground();

        ParseQuery<Ads> qdq = ParseQuery.getQuery(Ads.class);
        qdq.whereEqualTo("type","on");
        qdq.getFirstInBackground(new GetCallback<Ads>() {
            public void done(Ads peobj, ParseException e) {
                if (e == null) {
                    if(peobj.isDataAvailable())
                    {mAdView.setVisibility(View.VISIBLE);
                    }else {
                        mAdView.setVisibility(View.GONE);
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        //User
        recyclerView = findViewById(R.id.notify);
        recyclerView.setLayoutManager(new LinearLayoutManager(NotificationScreen.this));
        notifications = new ArrayList<>();

        findViewById(R.id.back).setOnClickListener(v -> {
            ParseUser.getCurrentUser().put("count",0);
            ParseUser.getCurrentUser().saveInBackground();
            onBackPressed();
        });
        getAllNotifications();
    }

    private void getAllNotifications() {
        notifications.clear();
        ParseQuery<Notification> pq = ParseQuery.getQuery(Notification.class);
        pq.whereEqualTo("userObj", ParseUser.getCurrentUser());
        pq.findInBackground(new FindCallback<Notification>() {
            public void done(List<Notification> uList, ParseException e) {
                if (e == null) {

                    notifications.addAll(uList);

                    Collections.reverse(notifications);
                    adapterNotification = new AdapterNotification(NotificationScreen.this, notifications);
                    recyclerView.setAdapter(adapterNotification);
                    if (adapterNotification.getItemCount() == 0){
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.notify).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.notify).setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

}