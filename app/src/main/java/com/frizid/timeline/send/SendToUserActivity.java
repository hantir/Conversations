package com.frizid.timeline.send;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class SendToUserActivity extends AppCompatActivity {

    private static String type;
    private static String uri;
    public static String getType() {
        return type;
    }
    public static String getUri() {
        return uri;
    }
    public SendToUserActivity(){

    }

    //User
    //AdapterSendUsers adapterUsers;
    List<ParseUser> userList;
    RecyclerView users_rv;

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
        //setContentView(R.layout.activity_create_chat);

        //Strings
         type =getIntent().getStringExtra("type");
         uri =getIntent().getStringExtra("uri");

        //User
        users_rv = findViewById(R.id.users);
        users_rv.setLayoutManager(new LinearLayoutManager(SendToUserActivity.this));
        userList = new ArrayList<>();
        getAllUsers();

        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        EditText editText = findViewById(R.id.editText);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filter(editText.getText().toString());
                return true;
            }
            return false;
        });

        
    }

    private void filter(final String query) {
        ParseQuery<ParseUser> uq1 = ParseQuery.getQuery("Users");
        uq1.whereEqualTo("name",query.toLowerCase());

        ParseQuery<ParseUser> uq2 = ParseQuery.getQuery("Users");
        uq2.whereEqualTo("username",query.toLowerCase());

        List<ParseQuery<ParseUser>> queries = new ArrayList<ParseQuery<ParseUser>>();
        queries.add(uq1);
        queries.add(uq2);

        ParseQuery<ParseUser> mainQuery = ParseQuery.or(queries);
        mainQuery.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> modelUsers, ParseException e) {
                if (e == null) {
                    userList.addAll(modelUsers);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    //adapterUsers = new AdapterSendUsers(SendToUserActivity.this, userList);
                    //adapterUsers.notifyDataSetChanged();
                    //users_rv.setAdapter(adapterUsers);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    /*if (adapterUsers.getItemCount() == 0){
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.users).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }else {*/
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.users).setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    //}
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }

        });
    }


    private void getAllUsers() {
        ParseQuery<ParseUser> hvq1 = ParseQuery.getQuery(ParseUser.class);
        hvq1.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        hvq1.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> uobjList, ParseException e) {
                if (e == null) {
                    userList.addAll(uobjList);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    //adapterUsers = new AdapterSendUsers(SendToUserActivity.this, userList);
                    //users_rv.setAdapter(adapterUsers);
                    /*if (adapterUsers.getItemCount() == 0){
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.users).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }else {*/
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.users).setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    //}
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

}