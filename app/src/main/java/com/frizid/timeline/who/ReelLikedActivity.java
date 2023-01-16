package com.frizid.timeline.who;

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
import com.frizid.timeline.model.Reel;
import com.frizid.timeline.model.ReelLike;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

@SuppressWarnings("ALL")
public class ReelLikedActivity extends AppCompatActivity {

    //User
    String reelId;
    private RecyclerView users_rv;
    private List<ParseUser> userList;
    private AdapterUsers adapterUsers;

    List<String> followingList;

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
        setContentView(R.layout.activity_who);

        reelId = getIntent().getStringExtra("reelId");

        //Back
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

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

        //User
        users_rv = findViewById(R.id.list);
        users_rv.setLayoutManager(new LinearLayoutManager(ReelLikedActivity.this));
        userList = new ArrayList<>();

        ParseQuery<ReelLike> rqd = ParseQuery.getQuery(ReelLike.class);
        rqd.whereEqualTo("reelObj", ParseObject.createWithoutData(Reel.class, reelId));
        rqd.findInBackground(new FindCallback<ReelLike>() {
            public void done(List<ReelLike> rlList, ParseException e) {
                if (e == null) {
                    followingList.clear();
                    for (ReelLike ruid : rlList ){
                        followingList.add(ruid.getUserObj().toString());
                    }
                    getAllUsers();
                }else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

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

    private void filter(String toString) {
        ParseQuery<ParseUser> gpn = ParseUser.getQuery();
        gpn.whereContainedIn("objectId", followingList);
        gpn.whereEqualTo("name", toString.toLowerCase());

        ParseQuery<ParseUser> gpun = ParseUser.getQuery();
        gpun.whereContainedIn("objectId", followingList);
        gpun.whereEqualTo("username", toString.toLowerCase());

        List<ParseQuery<ParseUser>> queries = new ArrayList<ParseQuery<ParseUser>>();
        queries.add(gpn);
        queries.add(gpun);

        ParseQuery<ParseUser> aQuery = ParseQuery.or(queries);
        aQuery.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> uList, ParseException e) {
                userList.clear();
                if (e == null) {
                    if (!uList.isEmpty()) {
                        userList.addAll(uList);
                    }
                    adapterUsers = new AdapterUsers(ReelLikedActivity.this, userList);
                    users_rv.setAdapter(adapterUsers);
                    adapterUsers.notifyDataSetChanged();
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
                else {
                    Timber.d("Error: %s", e.getMessage());
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
                    userList.clear();
                    if (!uList.isEmpty()) {
                        userList.addAll(uList);
                    }
                    adapterUsers = new AdapterUsers(ReelLikedActivity.this, userList);
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
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });

    }
}