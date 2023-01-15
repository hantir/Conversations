package com.frizid.timeline.story;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.App;
import com.frizid.timeline.R;
import com.frizid.timeline.model.High;
import com.frizid.timeline.model.Story;
import com.frizid.timeline.model.StoryView;
import com.frizid.timeline.who.ViewedActivity;
import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class StoryViewActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    //Declare
    String userId;
    int counter = 0;
    long pressTime = 0L;
    final long limit = 500L;

    //Id
    StoriesProgressView storiesProgressView;
    ImageView sImage;
    VideoView sVideo;
    TextView name,time,seen;
    CircleImageView dp;

    //List
    List<String> layouts;
    List<String> storyids;


    private final View.OnTouchListener onTouchListener = new View.OnTouchListener(){
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_story_view);

        //HisId
        userId = getIntent().getStringExtra("userId");

        //ID
        View reverse =  findViewById(R.id.reverse);
        View skip =  findViewById(R.id.skip);
        storiesProgressView =  findViewById(R.id.stories);
        sImage = findViewById(R.id.image);
        sVideo = findViewById(R.id.video);
        time = findViewById(R.id.time);
        name = findViewById(R.id.name);
        dp = findViewById(R.id.dp);
        seen = findViewById(R.id.seen);

        //Get
        getStories(userId);
        getUserDetails(userId);

        //View
        reverse.setOnClickListener(v -> storiesProgressView.reverse());
        reverse.setOnTouchListener(onTouchListener);

        skip.setOnClickListener(v -> storiesProgressView.skip());
        skip.setOnTouchListener(onTouchListener);

        //Me
        if (userId.equals(Objects.requireNonNull(ParseUser.getCurrentUser()))){
            findViewById(R.id.seen_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.delete_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.highlight).setVisibility(View.VISIBLE);
            findViewById(R.id.message).setVisibility(View.GONE);
        }else {
            findViewById(R.id.message).setVisibility(View.VISIBLE);
        }

        //view
        findViewById(R.id.seen_layout).setOnClickListener(v -> {
            Intent intent = new Intent(StoryViewActivity.this, ViewedActivity.class);
            intent.putExtra("userId",userId);
            intent.putExtra("storyId",storyids.get(counter));
            startActivity(intent);
        });

        //Delete
        findViewById(R.id.delete_layout).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Delete");
            builder.setMessage("Are you sure ?");
            builder.setPositiveButton("Delete",
                    (dialog, which) -> {
                        Toast.makeText(StoryViewActivity.this, "Please wait deleting", Toast.LENGTH_SHORT).show();
                        ParseQuery<Story> sq = ParseQuery.getQuery(Story.class);
                        sq.getInBackground(storyids.get(counter), new GetCallback<Story>() {
                            public void done(Story object, ParseException e) {
                                if (e == null) {
                                    object.deleteInBackground();
                                    finish();
                                    onBackPressed();
                                    Toast.makeText(StoryViewActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Timber.d("Error: %s", e.getMessage());
                                }
                            }
                        });
                    });
            builder.setNegativeButton("No", (dialog, which) -> {
                dialog.dismiss();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        //Edit
        EditText sendMessage = findViewById(R.id.sendMessage);
        sendMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0){
                    storiesProgressView.resume();
                }else {
                    storiesProgressView.pause();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        findViewById(R.id.imageView2).setOnClickListener(v -> {
            if (sendMessage.getText().toString().isEmpty()){
                Snackbar.make(v, "Type a message", Snackbar.LENGTH_SHORT).show();
            }else {
                try {
                    /*HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    hashMap.put("receiver", userid);
                    hashMap.put("msg",  sendMessage.getText().toString());
                    hashMap.put("isSeen", false);
                    hashMap.put("timestamp", storyids.get(counter));
                    hashMap.put("type", "story");
                    FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);*/
                    Snackbar.make(v, "Message sent", Snackbar.LENGTH_SHORT).show();
                    sendMessage.setText("");
                    storiesProgressView.resume();
                }catch (Exception e){
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });

        //Highlight
        findViewById(R.id.highlight).setOnClickListener(v -> {

            ParseQuery<High> hq = ParseQuery.getQuery(High.class);
            hq.whereEqualTo("userObj",ParseUser.getCurrentUser());
            hq.whereEqualTo("storyObj",ParseObject.createWithoutData(Story.class, storyids.get(counter)));
            hq.getFirstInBackground(new GetCallback<High>() {
                public void done(High hList, ParseException e) {
                    if (e == null) {
                        if (hList.isDataAvailable()){
                            hList.deleteInBackground();
                            Snackbar.make(v, "Removed from HighLight", Snackbar.LENGTH_LONG).show();
                        } else {
                            Story sq = ParseObject.createWithoutData(Story.class, storyids.get(counter));
                            High hq = new High();
                            hq.setFile(sq.getParseFile("imageObj"));
                            hq.setStoryObj(ParseObject.createWithoutData(Story.class, storyids.get(counter)));
                            hq.setType(sq.getType());
                            hq.setUserObj(ParseUser.getCurrentUser());
                            Snackbar.make(v, "Added to HighLight", Snackbar.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Timber.d("Error: %s", e.getMessage());
                    }}
            });
        });
    }

    private void getUserDetails(String userId) {
        ParseQuery<ParseUser> uq = ParseUser.getQuery();
        uq.getInBackground(userId, new GetCallback<ParseUser>() {
            public void done(ParseUser object, ParseException e) {
                if (e == null) {
                    try {
                        ParseFile photoFile = object.getParseFile("photo");
                        if (photoFile.isDataAvailable()) {
                            Picasso.get().load(photoFile.getUrl()).into(dp);
                        }
                    }catch(NullPointerException ignored){
                    }
                    name.setText(Objects.requireNonNull(object.getUsername()).toString());
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    @Override
    public void onNext() {
        //Display
        ParseQuery<Story> sq = ParseQuery.getQuery(Story.class);
        sq.getInBackground(storyids.get(++counter), new GetCallback<Story>() {
            public void done(Story object, ParseException e) {
                if (e == null) {
                    long lastTime = object.getCreatedAt().getTime();
                    time.setText(App.getTimeAgo(lastTime));
                    ParseFile storyFile = object.getParseFile("imageObj");
                        if (object.get("type").toString().equals("image")){
                            sImage.setVisibility(View.VISIBLE);
                            sVideo.setVisibility(View.GONE);
                            Glide.with(getApplicationContext()).load(storyFile.getUrl()).into(sImage);

                        }else if (object.get("type").toString().equals("video")){

                            sImage.setVisibility(View.GONE);
                            sVideo.setVisibility(View.VISIBLE);
                            sVideo.setVideoPath(storyFile.getUrl());
                            sVideo.setOnPreparedListener(mp -> {
                                sVideo.start();
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                            });
                            storiesProgressView.pause();
                            sVideo.setOnCompletionListener(mp -> {

                                sVideo.pause();
                                storiesProgressView.skip();
                            });
                        }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
        addView(storyids.get(counter));
        seenNumber(storyids.get(counter));
    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
        //Display
        ParseQuery<Story> sq = ParseQuery.getQuery(Story.class);
        sq.getInBackground(storyids.get(--counter), new GetCallback<Story>() {
            public void done(Story object, ParseException e) {
                if (e == null) {
                    long lastTime = object.getCreatedAt().getTime();
                    time.setText(App.getTimeAgo(lastTime));
                    ParseFile storyFile = object.getParseFile("imageObj");
                    if (object.get("type").toString().equals("image")){
                        sImage.setVisibility(View.VISIBLE);
                        sVideo.setVisibility(View.GONE);
                        Glide.with(getApplicationContext()).load(storyFile.getUrl()).into(sImage);

                    }else if (object.get("type").toString().equals("video")){

                        sImage.setVisibility(View.GONE);
                        sVideo.setVisibility(View.VISIBLE);
                        sVideo.setVideoPath(storyFile.getUrl());
                        sVideo.setOnPreparedListener(mp -> {
                            sVideo.start();
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                        });
                        storiesProgressView.pause();
                        sVideo.setOnCompletionListener(mp -> {

                            sVideo.pause();
                            storiesProgressView.skip();
                        });
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
        seenNumber(storyids.get(counter));
    }


    private void getStories(String userId) {
        layouts = new ArrayList<>();
        storyids = new ArrayList<>();
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        ParseQuery<Story> sq = ParseQuery.getQuery(Story.class);
        sq.whereEqualTo("userObj", ParseObject.createWithoutData(ParseUser.class, userId));
        sq.whereGreaterThan("createdAt", cal.getTime());
        sq.setLimit(100);
        sq.orderByDescending("createdAt");
        sq.findInBackground(new FindCallback<Story>() {
            public void done(List<Story> sobjList, ParseException e) {
                if (e == null) {
                    layouts.clear();
                    storyids.clear();
                    for (Story story : sobjList){
                        layouts.add(story.getImageObj().getUrl());
                        storyids.add(story.getObjectId());
                    }
                    storiesProgressView.setStoriesCount(layouts.size());
                    storiesProgressView.setStoryDuration(8000);
                    storiesProgressView.setStoriesListener(StoryViewActivity.this);
                    storiesProgressView.startStories(counter);
                    addView(storyids.get(counter));
                    seenNumber(storyids.get(counter));

                    //Display
                    ParseQuery<Story> sq = ParseQuery.getQuery(Story.class);
                    sq.getInBackground(storyids.get(counter), new GetCallback<Story>() {
                        public void done(Story object, ParseException e) {
                            if (e == null) {
                                long lastTime = object.getCreatedAt().getTime();
                                time.setText(App.getTimeAgo(lastTime));
                                ParseFile storyFile = object.getParseFile("imageObj");
                                if (object.get("type").toString().equals("image")){
                                    sImage.setVisibility(View.VISIBLE);
                                    sVideo.setVisibility(View.GONE);
                                    Glide.with(getApplicationContext()).load(storyFile.getUrl()).into(sImage);
                                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                                }else if (object.get("type").toString().equals("video")){

                                    sImage.setVisibility(View.GONE);
                                    sVideo.setVisibility(View.VISIBLE);
                                    sVideo.setVideoPath(storyFile.getUrl());
                                    sVideo.setOnPreparedListener(mp -> {
                                        sVideo.start();
                                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                                    });
                                    storiesProgressView.pause();
                                    sVideo.setOnCompletionListener(mp -> {

                                        sVideo.pause();
                                        storiesProgressView.skip();
                                    });
                                }
                            } else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    private void addView(String storyId){
        StoryView storyView = new StoryView();
        storyView.setUserObj(ParseObject.createWithoutData(ParseUser.class, userId));
        storyView.setStoryObj( ParseObject.createWithoutData(Story.class, storyId));
        storyView.setUserObj(ParseUser.getCurrentUser());
        storyView.saveInBackground();
    }
    private void seenNumber(String storyId){
        ParseQuery<StoryView> svq = ParseQuery.getQuery(StoryView.class);
        svq.whereEqualTo("userObj", ParseObject.createWithoutData(ParseUser.class, userId));
        svq.whereEqualTo("storyObj", ParseObject.createWithoutData(Story.class, storyId));
        svq.findInBackground(new FindCallback<StoryView>() {
            public void done(List<StoryView> svobjList, ParseException e) {
                if (e == null) {
                    seen.setText(""+svobjList.size());
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

}