package com.frizid.timeline.story;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.R;
import com.frizid.timeline.model.High;
import com.frizid.timeline.model.HighView;
import com.frizid.timeline.model.Story;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class HighViewActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    //Declare
    String userId;
    long pressTime = 0L;
    final long limit = 500L;

    //Id
    StoriesProgressView storiesProgressView;
    ImageView sImage;
    VideoView sVideo;
    TextView name,time,seen;
    CircleImageView dp;
    String storyId;



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
        storyId = getIntent().getStringExtra("storyId");

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
        findViewById(R.id.time).setVisibility(View.GONE);
        findViewById(R.id.dot).setVisibility(View.GONE);

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
            findViewById(R.id.highlight).setVisibility(View.VISIBLE);
        }

        //Todo
        findViewById(R.id.seen_layout).setVisibility(View.GONE);
        findViewById(R.id.sendMessage).setVisibility(View.GONE);
        findViewById(R.id.imageView2).setVisibility(View.GONE);

        //Highlight
        findViewById(R.id.highlight).setOnClickListener(v -> {

            String[] userIds = {userId, ParseUser.getCurrentUser().getObjectId()};
            ParseQuery<High> hq = ParseQuery.getQuery(High.class);
            hq.whereContainedIn("userObj", Arrays.asList(userIds));
            hq.whereEqualTo("storyObj",ParseObject.createWithoutData(Story.class,storyId));
            hq.findInBackground(new FindCallback<High>() {
                public void done(List<High> highList, ParseException e) {
                    if (e == null) {
                        for (High ho:highList){
                        if (ho.getObjectId().equals(ParseUser.getCurrentUser())) {
                            ho.deleteInBackground();
                            Snackbar.make(v, "Removed from HighLight", Snackbar.LENGTH_LONG).show();
                        } else {
                            ParseQuery<High> hq1 = ParseQuery.getQuery(High.class);
                            hq1.whereEqualTo("userObj", ParseObject.createWithoutData(ParseUser.class,userId));
                            hq1.whereEqualTo("storyObj", ParseObject.createWithoutData(Story.class,storyId));
                            hq1.getFirstInBackground(new GetCallback<High>() {
                                public void done(High hobj, ParseException e) {
                                    if (e == null) {
                                        High hqs = new High();
                                        hqs.setFile( hobj.getFile());
                                        hqs.setStoryObj( ParseObject.createWithoutData(Story.class,storyId));
                                        hqs.setType(hobj.getType());
                                        hqs.setUserObj(ParseUser.getCurrentUser());
                                        hqs.saveInBackground();
                                        Snackbar.make(v, "Added to HighLight", Snackbar.LENGTH_LONG).show();
                                    } else {
                                        Log.d("High", "Error: " + e.getMessage());
                                    }
                                }
                            });
                        }
                    }
                }
                    else {
                        Log.d("High", "Error: " + e.getMessage());
                    }}
            });

        });

    }

    private void getUserDetails(String userId) {
        ParseQuery<ParseUser> uq = ParseQuery.getQuery(ParseUser.class);
        uq.getInBackground(userId, new GetCallback<ParseUser>() {
            public void done(ParseUser uo, ParseException e) {
                if (e == null) {
                    try{
                        Picasso.get().load(uo.getParseFile("photo").getUrl()).into(dp);
                    }catch(NullPointerException ignored){
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    @Override
    public void onNext() {
     finish();
    }

    @Override
    public void onPrev() {
        finish();
    }


    private void getStories(String userId) {
        ParseQuery<High> hvq1 = ParseQuery.getQuery(High.class);
        hvq1.whereEqualTo("userObj", ParseObject.createWithoutData(ParseUser.class,userId));
        hvq1.whereEqualTo("storyObj", ParseObject.createWithoutData(Story.class,storyId));
        hvq1.getFirstInBackground(new GetCallback<High>() {
            public void done(High hobj, ParseException e) {
                if (e == null) {
                    storiesProgressView.setStoriesCount(1);
                    storiesProgressView.setStoryDuration(8000);
                    storiesProgressView.setStoriesListener(HighViewActivity.this);
                    storiesProgressView.startStories(0);
                    addView();
                    seenNumber();

                    if (hobj.getType().equals("image")){
                        sImage.setVisibility(View.VISIBLE);
                        sVideo.setVisibility(View.GONE);
                        Glide.with(HighViewActivity.this).load(hobj.getFile().getUrl()).into(sImage);

                    }else if (hobj.getType().equals("video")){

                        sImage.setVisibility(View.GONE);
                        sVideo.setVisibility(View.VISIBLE);
                        sVideo.setVideoPath(hobj.getFile().getUrl());
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
                    Log.d("High", "Error: " + e.getMessage());
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

    private void addView(){
        HighView hvqs = new HighView();
        hvqs.setStoryObj( ParseObject.createWithoutData(Story.class, storyId));
        hvqs.setUserObj(ParseUser.getCurrentUser());
        hvqs.saveInBackground();
    }
    private void seenNumber(){
        ParseQuery<HighView> hvq1 = ParseQuery.getQuery(HighView.class);
        hvq1.whereEqualTo("storyObj", ParseObject.createWithoutData(Story.class,storyId));
        hvq1.findInBackground(new FindCallback<HighView>() {
            public void done(List<HighView> hvobjList, ParseException e) {
                if (e == null) {
                    seen.setText(""+hvobjList.size());
                } else {
                    Log.d("HighView", "Error: " + e.getMessage());
                }
            }
        });
    }

}