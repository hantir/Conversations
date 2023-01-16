package com.frizid.timeline.group;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.frizid.timeline.model.Group;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.adapter.AdapterUsers;
import com.frizid.timeline.model.Participants;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class GroupMembersActivity extends AppCompatActivity {

    //User
    private RecyclerView users_rv;
    private List<ParseUser> userList;
    private AdapterUsers adapterUsers;

    String id;
    List<String> list;

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

        id  = getIntent().getStringExtra("group");

        //Back
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        //User
        users_rv = findViewById(R.id.list);
        users_rv.setLayoutManager(new LinearLayoutManager(GroupMembersActivity.this));
        userList = new ArrayList<>();
        getMembers();

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

    private void getMembers() {
        ParseQuery<Participants> uq = ParseQuery.getQuery(Participants.class);
        uq.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, id));
        uq.findInBackground(new FindCallback<Participants>() {
            public void done(List<Participants> modelUser, ParseException e) {
                if (e == null) {
                    list.clear();
                    for (Participants id : modelUser) {
                        list.add(id.getUserObj().getObjectId());
                    }
                    getUser();
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    private void getUser() {
        ParseQuery<ParseUser> uq = ParseUser.getQuery();
        uq.whereContainedIn("objectId", list);
        uq.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> modelUsers, ParseException e) {
                if (e == null) {
                    userList.clear();
                    userList.addAll(modelUsers);
                    adapterUsers = new AdapterUsers(GroupMembersActivity.this, userList);
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
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    private void filter(String query) {
        ParseQuery<ParseUser> uq1 = ParseUser.getQuery();
        uq1.whereContainedIn("objectId", list);
        uq1.whereEqualTo("name",query.toLowerCase());

        ParseQuery<ParseUser> uq2 = ParseUser.getQuery();
        uq2.whereContainedIn("objectId", list);
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
                    adapterUsers = new AdapterUsers(GroupMembersActivity.this, userList);
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
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }

        });
    }
}