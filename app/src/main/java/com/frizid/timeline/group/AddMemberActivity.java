package com.frizid.timeline.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.frizid.timeline.model.Group;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.adapter.AdapterParticipants;
import com.frizid.timeline.model.Participants;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class AddMemberActivity extends AppCompatActivity {

    //User
    AdapterParticipants adapterUsers;
    List<ParseUser> userList;
    RecyclerView users_rv;
    String id,myGroupRole;

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
        setContentView(R.layout.activity_add_member);

        id = getIntent().getStringExtra("group");

        findViewById(R.id.imageView).setOnClickListener(v -> {
            //Next activity
            Intent intent = new Intent(AddMemberActivity.this, GroupProfileActivity.class);
            intent.putExtra("group", id);
            intent.putExtra("type", "create");
            startActivity(intent);
            finish();
        });

        EditText editText = findViewById(R.id.editText);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filter(editText.getText().toString());
                return true;
            }
            return false;
        });

        //User
        users_rv = findViewById(R.id.users);
        users_rv.setLayoutManager(new LinearLayoutManager(AddMemberActivity.this));
        userList = new ArrayList<>();

        ParseQuery<Participants> gp = ParseQuery.getQuery(Participants.class);
        gp.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, id));
        gp.whereEqualTo("userObj", ParseUser.getCurrentUser());
        gp.getFirstInBackground(new GetCallback<Participants>() {
            public void done(Participants gpList, ParseException e) {
                if (e == null) {
                    if (gpList.isDeleted) {
                        myGroupRole = ""+gpList.get("role");
                        getAllUsers();
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });

    }

    private void filter(final String query) {
        ParseQuery<ParseUser> uq1 = ParseUser.getQuery();
        uq1.whereNotEqualTo("objectId",ParseUser.getCurrentUser());
        uq1.whereEqualTo("name",query.toLowerCase());

        ParseQuery<ParseUser> uq2 = ParseUser.getQuery();
        uq2.whereNotEqualTo("objectId",ParseUser.getCurrentUser());
        uq2.whereEqualTo("username",query.toLowerCase());

        List<ParseQuery<ParseUser>> queries = new ArrayList<ParseQuery<ParseUser>>();
        queries.add(uq1);
        queries.add(uq2);

        ParseQuery<ParseUser> mainQuery = ParseQuery.or(queries);
        mainQuery.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> modelUsers, ParseException e) {
                if (e == null) {
                    userList.clear();
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    userList.addAll(modelUsers);
                    adapterUsers = new AdapterParticipants(AddMemberActivity.this, userList,""+id,""+myGroupRole);
                    adapterUsers.notifyDataSetChanged();
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
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }

        });
    }

    private void getAllUsers() {
        ParseQuery<ParseUser> uq = ParseUser.getQuery();
        uq.whereNotEqualTo("objectId",ParseUser.getCurrentUser());
        uq.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> modelUsers, ParseException e) {
                if (e == null) {
                    userList.clear();
                    userList.addAll(modelUsers);
                    adapterUsers = new AdapterParticipants(AddMemberActivity.this, userList,""+id,""+myGroupRole);
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
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

}