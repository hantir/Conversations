package com.frizid.timeline.post;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.adapter.AdapterCommentReply;
import com.frizid.timeline.model.Comment;
import com.frizid.timeline.model.CommentReply;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ReplyActivity extends AppCompatActivity {

    String commentId;

    List<CommentReply> modelCommentReplies;
    AdapterCommentReply adapterCommentReply;
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
        setContentView(R.layout.activity_reply);

        recyclerView = findViewById(R.id.chat_rv);

        commentId = getIntent().getStringExtra("cId");

        modelCommentReplies = new ArrayList<>();

        //Header
        findViewById(R.id.back).setOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        loadComments();

        //Send
        EditText editText = findViewById(R.id.editText);
        findViewById(R.id.message_send).setOnClickListener(v -> {

            if (editText.getText().toString().isEmpty()){
                Snackbar.make(v, "Type a comment", Snackbar.LENGTH_SHORT).show();
            }else {
                CommentReply replyc = new CommentReply();
                replyc.setUserObj(ParseUser.getCurrentUser());
                replyc.setComment(editText.getText().toString());
                replyc.setCommentObj( ParseObject.createWithoutData(Comment.class, commentId));
                replyc.saveInBackground(new SaveCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            Snackbar.make(v, "Comment sent", Snackbar.LENGTH_SHORT).show();
                            onBackPressed();
                            editText.setText("");
                        }
                        else {
                            Timber.d("Error: %s", e.getMessage());
                        }}
                });

            }

        });

    }

    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        modelCommentReplies = new ArrayList<>();
        ParseQuery<CommentReply> creplyq = ParseQuery.getQuery(CommentReply.class);
        creplyq.whereEqualTo("commentObj", ParseObject.createWithoutData(Comment.class, commentId));
        modelCommentReplies.clear();
        creplyq.findInBackground(new FindCallback<CommentReply>() {
            public void done(List<CommentReply> replyl , ParseException e) {
                if (e == null) {
                    if(replyl.size()>0){
                        for (CommentReply reply: replyl){
                            modelCommentReplies.add(reply);
                            adapterCommentReply = new AdapterCommentReply(ReplyActivity.this, modelCommentReplies);
                            recyclerView.setAdapter(adapterCommentReply);
                            adapterCommentReply.notifyDataSetChanged();
                        }
                    }

                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }


}