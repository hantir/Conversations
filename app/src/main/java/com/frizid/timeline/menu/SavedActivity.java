package com.frizid.timeline.menu;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.adapter.AdapterPost;
import com.frizid.timeline.adapter.AdapterReelView;
import com.frizid.timeline.model.Post;
import com.frizid.timeline.model.Reel;
import com.frizid.timeline.model.Saves;
import com.frizid.timeline.model.SavesReel;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class SavedActivity extends AppCompatActivity {

    //Reel
    RecyclerView reelView;
    AdapterReelView adapterReelView;
    List<Reel> reel;
    List<String> myReelSaves;

    //Post
    AdapterPost adapterPost;
    List<Post> posts;
    RecyclerView post;
    List<String> mySaves;

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
        setContentView(R.layout.activity_saved);

        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        //Reel
        reelView = findViewById(R.id.reel);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        reelView.setLayoutManager(gridLayoutManager);
        reel = new ArrayList<>();

        //Post
        post = findViewById(R.id.posts);
        post.setLayoutManager(new LinearLayoutManager(SavedActivity.this));
        posts = new ArrayList<>();
        mySaved();

        //TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tabLayout.getSelectedTabPosition() == 1) {
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    myReelSaved();
                    reelView.setVisibility(View.VISIBLE);
                    post.setVisibility(View.GONE);
                } else if (tabLayout.getSelectedTabPosition() == 0) {
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    post.setVisibility(View.VISIBLE);
                    reelView.setVisibility(View.GONE);
                    mySaved();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void myReelSaved() {
        myReelSaves = new ArrayList<>();
        ParseQuery<SavesReel> uq = ParseQuery.getQuery(SavesReel.class);
        uq.whereEqualTo("userObj",ParseUser.getCurrentUser());
        uq.findInBackground(new FindCallback<SavesReel>() {
            public void done(List<SavesReel> srList, ParseException e) {
                if (e == null) {
                    if (srList.size() > 0) {
                        for (SavesReel srid :srList)
                        {
                            myReelSaves.add(srid.getObjectId());
                        }
                    }
                    getReel();
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    private void mySaved() {
        mySaves = new ArrayList<>();
        ParseQuery<Saves> uq = ParseQuery.getQuery(Saves.class);
        uq.whereEqualTo("userObj",ParseUser.getCurrentUser());
        uq.findInBackground(new FindCallback<Saves>() {
            public void done(List<Saves> sList, ParseException e) {
                if (e == null) {
                    if (!sList.isEmpty()) {
                        for (Saves sid :sList)
                        {
                            mySaves.add(sid.getObjectId());
                        }
                    }
                    getAllPost();
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }


    private void getReel() {
        ParseQuery<Reel> pq = ParseQuery.getQuery(Reel.class);
        pq.whereContainedIn("objectId", myReelSaves);
        pq.findInBackground(new FindCallback<Reel>() {
            public void done(List<Reel> pList, ParseException e) {
                if (e == null) {
                    reel.clear();
                    if (!pList.isEmpty()) {
                        reel.addAll(pList);
                        adapterReelView = new AdapterReelView(reel);
                        reelView.setAdapter(adapterReelView);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        reelView.setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    } else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        reelView.setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
            });
    }

    private void getAllPost() {
        ParseQuery<Post> pq = ParseQuery.getQuery(Post.class);
        pq.whereContainedIn("objectId", mySaves);
        pq.findInBackground(new FindCallback<Post>() {
            public void done(List<Post> pList, ParseException e) {
                if (e == null) {
                    posts.clear();
                    if (!pList.isEmpty()) {
                        posts.addAll(pList);
                        adapterPost = new AdapterPost(SavedActivity.this, posts);
                        SavedActivity.this.post.setAdapter(adapterPost);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        SavedActivity.this.post.setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    } else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        SavedActivity.this.post.setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });

    }
}