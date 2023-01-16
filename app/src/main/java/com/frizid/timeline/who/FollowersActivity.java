package com.frizid.timeline.who;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.adapter.AdapterUsers;
import com.frizid.timeline.model.Ads;
import com.frizid.timeline.model.Follow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class FollowersActivity extends AppCompatActivity {

    //User
    String userId;
    private RecyclerView users_rv;
    private List<ParseUser> userList;
    private AdapterUsers adapterUsers;

    //Follow
    List<String> followingList = new ArrayList<>();

    NightMode sharedPref;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState().equals("night")){
            setTheme(R.style.DarkTheme);
        }else if (sharedPref.loadNightModeState().equals("dim")){
            setTheme(R.style.DimTheme);
        }else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who);

        userId = getIntent().getStringExtra("Id");

        MobileAds.initialize(getApplicationContext(), initializationStatus -> {
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);

        ParseQuery<Ads> qdq = ParseQuery.getQuery(Ads.class);
        qdq.whereEqualTo("type","on");
        qdq.getFirstInBackground(new GetCallback<Ads>() {
            public void done(Ads adsobj, ParseException e) {
                if (e == null) {
                    if(adsobj.isDataAvailable())
                    {
                        mAdView.setVisibility(View.VISIBLE);
                    }else {
                        mAdView.setVisibility(View.GONE);
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        //Back
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        //User
        users_rv = findViewById(R.id.list);
        users_rv.setLayoutManager(new LinearLayoutManager(FollowersActivity.this));
        userList = new ArrayList<>();
        checkFollowing();
        //EdiText
        EditText editText = findViewById(R.id.editText);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filter(editText.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void filter(String query) {
        ParseQuery<ParseUser> gpn = ParseUser.getQuery();
        gpn.whereContainedIn("objectId", followingList);
        gpn.whereEqualTo("name", query.toLowerCase());

        ParseQuery<ParseUser> gpun = ParseUser.getQuery();
        gpun.whereContainedIn("objectId", followingList);
        gpun.whereEqualTo("username", query.toLowerCase());

        List<ParseQuery<ParseUser>> queries = new ArrayList<ParseQuery<ParseUser>>();
        queries.add(gpn);
        queries.add(gpun);

        ParseQuery<ParseUser> aQuery = ParseQuery.or(queries);
        aQuery.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> uList, ParseException e) {
                userList.clear();
                if (e == null) {
                    if (uList.size()>0) {
                        userList.addAll(uList);
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }
                adapterUsers = new AdapterUsers(FollowersActivity.this, userList);
                users_rv.setAdapter(adapterUsers);
                if (adapterUsers.getItemCount() == 0){
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    users_rv.setVisibility(View.GONE);
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }else {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    users_rv.setVisibility(View.VISIBLE);
                    findViewById(R.id.nothing).setVisibility(View.GONE);
                }
            }
        });
    }

    private void getAllUsers() {
        ParseQuery<ParseUser> gp = ParseUser.getQuery();
        gp.whereContainedIn("objectId", followingList);
        gp.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> uList, ParseException e) {
                if (e == null) {
                    if (uList.size() > 0) {
                        adapterUsers = new AdapterUsers(FollowersActivity.this, uList);
                        users_rv.setAdapter(adapterUsers);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        users_rv.setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    }else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        users_rv.setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });
    }

    private void checkFollowing() {
        ParseQuery<Follow> flwr = ParseQuery.getQuery(Follow.class);
        flwr.whereEqualTo("toObj",ParseObject.createWithoutData(ParseUser.class, userId));
        flwr.whereNotEqualTo("fromObj",ParseObject.createWithoutData(ParseUser.class, userId));
        flwr.findInBackground(new FindCallback<Follow>() {
            public void done(List<Follow> flList, ParseException e) {
                if (e == null) {
                    if (flList.size() > 0) {
                        followingList.clear();
                        for (Follow flobj : flList) {
                            followingList.add(flobj.getFromObj().getObjectId());
                        }
                        getAllUsers();
                    } else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        users_rv.setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }
}