package com.frizid.timeline.search;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.adapter.AdapterPost;
import com.frizid.timeline.model.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class TrendingActivity extends AppCompatActivity {

    String type = "";

    //Post
    AdapterPost adapterPost;
    List<Post> posts;
    RecyclerView post;

    //Post
    AdapterPost getAdapterPost;
    List<Post> postList;
    RecyclerView postView;

    private static final int TOTAL_ITEM_EACH_LOAD = 6;
    private int currentPage = 1;
    Button more;
    long initial;

    NightMode sharedPref;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState().equals("night")){
            setTheme(R.style.DarkTheme);
        }else if (sharedPref.loadNightModeState().equals("dim")){
            setTheme(R.style.DimTheme);
        }else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);

        //Back
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        //Search
        findViewById(R.id.search).setOnClickListener(v1 -> startActivity(new Intent(TrendingActivity.this, SearchActivity.class)));

        more = findViewById(R.id.more);

        ParseQuery<Post> postCount = ParseQuery.getQuery(Post.class);
        postCount.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                    initial = posts.size();
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        //Post
        post = findViewById(R.id.post);
        post.setLayoutManager(new LinearLayoutManager(TrendingActivity.this));
        posts = new ArrayList<>();
        trending();
        findViewById(R.id.more).setOnClickListener(view -> {
            more.setText("Loading...");
            loadMoreData();
        });


        //Post
        postView = findViewById(R.id.postView);
        postView.setLayoutManager(new LinearLayoutManager(TrendingActivity.this));
        postList = new ArrayList<>();

        //Type
        findViewById(R.id.music).setOnClickListener(v -> {
            type = "music";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.sports).setOnClickListener(v -> {
            type = "sports";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.memes).setOnClickListener(v -> {
            type = "memes";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.vines).setOnClickListener(v -> {
            type = "vines";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.tv).setOnClickListener(v -> {
            type = "movie";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.animals).setOnClickListener(v -> {
            type = "animals";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.diy).setOnClickListener(v -> {
            type = "diy";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.beauty).setOnClickListener(v -> {
            type = "beauty";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.art).setOnClickListener(v -> {
            type = "art";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.food).setOnClickListener(v -> {
            type = "food";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.style).setOnClickListener(v -> {
            type = "style";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });

        findViewById(R.id.decor).setOnClickListener(v -> {
            type = "decor";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.politics).setOnClickListener(v -> {
            type = "politics";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.trending).setOnClickListener(v -> {
            type = "";
            post.setVisibility(View.VISIBLE);
            postView.setVisibility(View.GONE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        });


    }

    private void loadMoreData() {
        currentPage++;
        trending();
    }

    private void trending() {
        ParseQuery<Post> postq = ParseQuery.getQuery(Post.class);
        postq.setLimit(currentPage*TOTAL_ITEM_EACH_LOAD);
        postq.findInBackground(new FindCallback<Post>() {
            public void done(List<Post> postl1 , ParseException e) {
                if (e == null) {
                    if(!postl1.isEmpty()){
                        posts.clear();
                        for (Post post: postl1){
                            posts.add(post);
                            Collections.shuffle(posts);
                            adapterPost = new AdapterPost(TrendingActivity.this, posts);
                            TrendingActivity.this.post.setAdapter(adapterPost);
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            if (adapterPost.getItemCount() == 0){
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                TrendingActivity.this.post.setVisibility(View.GONE);
                                findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                            }else {
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                TrendingActivity.this.post.setVisibility(View.VISIBLE);
                                findViewById(R.id.nothing).setVisibility(View.GONE);
                                if(adapterPost.getItemCount() >= initial){
                                    more.setVisibility(View.GONE);
                                    currentPage--;
                                    more.setText("Load more");
                                }else {
                                    more.setVisibility(View.VISIBLE);
                                    more.setText("Load more");
                                }
                            }
                        }
                    }
                    else{findViewById(R.id.progressBar).setVisibility(View.GONE);
                        post.setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);}
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    private void getPostsType(){
        ParseQuery<Post> postq = ParseQuery.getQuery(Post.class);
        postq.whereEqualTo("type", type);
        postList.clear();
        postq.findInBackground(new FindCallback<Post>() {
            public void done(List<Post> postl , ParseException e) {
                if (e == null) {
                    if(!postl.isEmpty()){
                        for (Post post: postl){
                            postList.add(post);
                            Collections.shuffle(postList);
                            getAdapterPost = new AdapterPost(TrendingActivity.this, postList);
                            postView.setAdapter(getAdapterPost);
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            if (getAdapterPost.getItemCount() == 0){
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                postView.setVisibility(View.GONE);
                                findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                            }else {
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                postView.setVisibility(View.VISIBLE);
                                findViewById(R.id.nothing).setVisibility(View.GONE);
                            }
                        }
                    }
                    else{findViewById(R.id.progressBar).setVisibility(View.GONE);
                        postView.setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);}

                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }
}