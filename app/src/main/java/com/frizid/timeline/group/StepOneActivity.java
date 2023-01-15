package com.frizid.timeline.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.model.Group;
import com.frizid.timeline.model.Participants;

import timber.log.Timber;

public class StepOneActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_step_one);

        //back
        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        //EditText
        EditText name = findViewById(R.id.name);
        EditText groupname = findViewById(R.id.groupname);
        EditText link = findViewById(R.id.link);
        EditText details = findViewById(R.id.details);
        ParseUser currenuser = ParseUser.getCurrentUser();

        //Next
        findViewById(R.id.next).setOnClickListener(v -> {

            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

            if (name.getText().toString().isEmpty() || groupname.getText().toString().isEmpty()){
                Snackbar.make(v,"Name & username should not be empty", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }else {
                //checkUsername
                ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
                query.whereEqualTo("groupname", groupname.getText().toString());
                query.orderByAscending("groupname");
                query.countInBackground(new CountCallback() {
                    public void done(int count, ParseException e) {
                        if (e == null) {
                            if (count>0){
                                Snackbar.make(v,"Groupname already exist, try with new one", Snackbar.LENGTH_LONG).show();
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                            }else {
                                Group groups = new Group();
                                groups.setGName(""+name.getText().toString());
                                groups.setGroupname(""+groupname.getText().toString());
                                groups.setGBio(""+details.getText().toString());
                                groups.setGLink(""+link.getText().toString());
                                groups.saveInBackground(new SaveCallback() {
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Participants partspnts = new Participants();
                                            partspnts.setRole("creator");
                                            partspnts.setUserObj(currenuser);
                                            partspnts.setGroupObj(groups);
                                            partspnts.saveInBackground();
                                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                                            Intent intent = new Intent(getApplicationContext(), StepTwoActivity.class);
                                            intent.putExtra("group", groups.getObjectId());
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });
                            }
                        } else {
                            Timber.d("Error: %s", e.getMessage());
                        }
                    }
                });
            }
        });
    }
}