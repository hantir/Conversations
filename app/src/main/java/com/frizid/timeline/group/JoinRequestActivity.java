package com.frizid.timeline.group;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.frizid.timeline.model.Group;
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
import com.frizid.timeline.adapter.AdapterUsersJoin;
import com.frizid.timeline.model.Ads;
import com.frizid.timeline.model.Request;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class JoinRequestActivity extends AppCompatActivity {


    //User
    private RecyclerView users_rv;
    private List<ParseUser> userList;
    private AdapterUsersJoin adapterUsersJoin;
    List<String> requestList;

    private static String group;
    public static String getGroup() {
        return group;
    }
    public JoinRequestActivity(){
    }

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

        group = getIntent().getStringExtra("group");

        MobileAds.initialize(getApplicationContext(), initializationStatus -> {
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);

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

        //Back
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        //User
        users_rv = findViewById(R.id.list);
        users_rv.setLayoutManager(new LinearLayoutManager(JoinRequestActivity.this));
        userList = new ArrayList<>();
        getRequest();
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
        ParseQuery<ParseUser> uq1 = ParseUser.getQuery();
        uq1.whereContainedIn("objectId", requestList);
        uq1.whereEqualTo("name",query.toLowerCase());

        ParseQuery<ParseUser> uq2 = ParseUser.getQuery();
        uq2.whereContainedIn("objectId", requestList);
        uq2.whereEqualTo("username",query.toLowerCase());

        List<ParseQuery<ParseUser>> queries = new ArrayList<ParseQuery<ParseUser>>();
        queries.add(uq1);
        queries.add(uq2);

        ParseQuery<ParseUser> mainQuery = ParseQuery.or(queries);
        mainQuery.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> modelUsers, ParseException e) {
                if (e == null) {
                    userList.clear();
                    userList.addAll(modelUsers);
                    adapterUsersJoin = new AdapterUsersJoin(JoinRequestActivity.this, userList);
                    users_rv.setAdapter(adapterUsersJoin);
                    if (adapterUsersJoin.getItemCount() == 0){
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        users_rv.setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        users_rv.setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }

        });
    }

    private void getAllUsers() {
        ParseQuery<ParseUser> uq = ParseUser.getQuery();
        uq.whereContainedIn("objectId", requestList);
        uq.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> modelUsers, ParseException e) {
                if (e == null) {
                    userList.clear();
                    userList.addAll(modelUsers);
                    adapterUsersJoin = new AdapterUsersJoin(JoinRequestActivity.this, userList);
                    users_rv.setAdapter(adapterUsersJoin);
                    if (adapterUsersJoin.getItemCount() == 0){
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        users_rv.setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        users_rv.setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    private void getRequest(){
        requestList = new ArrayList<>();

        ParseQuery<Request> rp = ParseQuery.getQuery(Request.class);
        rp.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, group));
        rp.findInBackground(new FindCallback<Request>() {
            public void done(List<Request> gpList, ParseException e) {
                if (e == null) {
                    requestList.clear();
                    if (!gpList.isEmpty()) {
                        for(Request gpl: gpList ) {
                            requestList.add(gpl.getUserObj().getObjectId());
                        }
                    }
                    getAllUsers();
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });

    }

}