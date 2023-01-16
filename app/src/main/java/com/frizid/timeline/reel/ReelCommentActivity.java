package com.frizid.timeline.reel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.frizid.timeline.model.Comment;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.frizid.timeline.App;
import com.frizid.timeline.R;
import com.frizid.timeline.adapter.AdapterCommentReel;
import com.frizid.timeline.model.Notification;
import com.frizid.timeline.model.Reel;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

@SuppressWarnings("ALL")
public class ReelCommentActivity extends AppCompatActivity {

    String position;
    String reelId;
    String type;

    //Comments
    List<Comment> commentsList;
    AdapterCommentReel adapterComments;
    RecyclerView recyclerView;

    private RequestQueue requestQueue;
    private boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_party_chat);

        requestQueue = Volley.newRequestQueue(ReelCommentActivity.this);

        recyclerView = findViewById(R.id.chat_rv);

        position = getIntent().getStringExtra("item");

        reelId = getIntent().getStringExtra("id");

        type = getIntent().getStringExtra("type");

        findViewById(R.id.imageView).setOnClickListener(v -> {
            if (type.equals("view")){

                Intent intent3 = new Intent(ReelCommentActivity.this, ViewReelActivity.class);
                intent3.putExtra("id", reelId);
                startActivity(intent3);
                finish();

            }else {
                Intent intent = new Intent(ReelCommentActivity.this, ReelActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("type", type);
                startActivity(intent);
                finish();
            }
        });

        //Send
        EditText editText = findViewById(R.id.editText);
        findViewById(R.id.message_send).setOnClickListener(v -> {

            if (editText.getText().toString().isEmpty()){
                Snackbar.make(v, "Type a comment", Snackbar.LENGTH_SHORT).show();
            }else {
                Comment reelcq = new Comment();
                reelcq.setUserObj(ParseUser.getCurrentUser());
                reelcq.setComment(editText.getText().toString());
                reelcq. setReelObj( ParseObject.createWithoutData(Reel.class, reelId));
                reelcq.saveInBackground(new SaveCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            Snackbar.make(v, "Comment sent", Snackbar.LENGTH_SHORT).show();
                            onBackPressed();
                            addToHisNotification(getIntent().getStringExtra("his"), "Commented on your reel");
                            notify = true;
                            if (notify){
                                App.sendNotification(getIntent().getStringExtra("his"), ParseUser.getCurrentUser().getUsername(), "Commented on your reel");
                            }
                            notify = false;
                            editText.setText("");
                        }
                        else {
                            Timber.d("Error: %s", e.getMessage());
                        }}
                });
            }

        });

        commentsList = new ArrayList<>();
        loadComments();

    }

    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        commentsList = new ArrayList<>();
        ParseQuery<Comment> reelcq = ParseQuery.getQuery(Comment.class);
        reelcq.whereEqualTo("reelObj", ParseObject.createWithoutData(Reel.class, reelId));
        reelcq.findInBackground(new FindCallback<Comment>() {
            public void done(List<Comment> modelComments , ParseException e) {
                if (e == null) {
                    commentsList.addAll(modelComments);
                    adapterComments = new AdapterCommentReel(getApplicationContext(), commentsList);
                    recyclerView.setAdapter(adapterComments);
                    adapterComments.notifyDataSetChanged();
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (type.equals("view")){

            Intent intent3 = new Intent(ReelCommentActivity.this, ViewReelActivity.class);
            intent3.putExtra("id", reelId);
            startActivity(intent3);
            finish();

        }else {
            Intent intent = new Intent(ReelCommentActivity.this, ReelActivity.class);
            intent.putExtra("position", position);
            intent.putExtra("type", type);
            startActivity(intent);
            finish();
        }
    }

    private void addToHisNotification(String hisUid, String message){
        Notification notif = new Notification();
        notif.setReelObj(ParseObject.createWithoutData(Reel.class, reelId));
        notif.setType("reel");
        notif.setPUserObj( ParseObject.createWithoutData(ParseUser.class, hisUid));
        notif.setNotification( message);
        notif.setSUserObj( ParseUser.getCurrentUser());
        notif.saveInBackground();
    }
}