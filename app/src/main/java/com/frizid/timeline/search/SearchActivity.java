package com.frizid.timeline.search;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.adapter.AdapterGroups;
import com.frizid.timeline.adapter.AdapterPost;
import com.frizid.timeline.adapter.AdapterUsers;
import com.frizid.timeline.model.Group;
import com.frizid.timeline.model.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

@SuppressWarnings("ALL")
public class SearchActivity extends AppCompatActivity {

    //User
    AdapterUsers adapterUsers;
    List<ParseUser> userList;
    RecyclerView users_rv;

    //Group
    AdapterGroups adapterGroups;
    List<Group> modelGroups;
    RecyclerView groups;

    //Post
    AdapterPost adapterPost;
    List<Post> modelPosts;
    RecyclerView post;

    //Other
    private static final int TOTAL_ITEM_EACH_LOAD = 6;
    private int currentPage = 1;
    Button more,moreUsers;
    long initial;
    String type = "user";

    NightMode sharedPref;

    long initialUsers;
    private static final int TOTAL_ITEM_EACH_LOAD_users = 10;
    private int currentPageUsers = 1;

    long startTime;
    long endTime;

    @Override
    protected void onStart() {
        super.onStart();
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onBackPressed() {
        endTime = System.currentTimeMillis();
        long timeSpend = endTime - startTime;
        /*if (timeSpend > 200000){
            addMoney();
        }*/
        super.onBackPressed();
    }

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
        setContentView(R.layout.activity_search);

        //Back
        findViewById(R.id.back).setOnClickListener(v -> {
                onBackPressed();
        });

        more = findViewById(R.id.more);
        moreUsers = findViewById(R.id.moreUsers);

        //User
        users_rv = findViewById(R.id.users);
        users_rv.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        userList = new ArrayList<>();
        getAllUsers();

        //Post
        post = findViewById(R.id.post);
        post.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        modelPosts = new ArrayList<>();

        //Group
        groups = findViewById(R.id.groups);
        groups.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        modelGroups = new ArrayList<>();

        //EdiText
        EditText editText = findViewById(R.id.editText);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                switch (type) {
                    case "user":
                        filterUser(editText.getText().toString());
                        break;
                    case "post":
                        filterPost(editText.getText().toString());
                        break;
                    case "group":
                        filterGroup(editText.getText().toString());
                        break;
                }
                return true;
            }
            return false;
        });

        //TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tabLayout.getSelectedTabPosition() == 0) {
                    getAllUsers();
                    type = "user";
                    users_rv.setVisibility(View.VISIBLE);
                    post.setVisibility(View.GONE);
                    groups.setVisibility(View.GONE);
                    more.setVisibility(View.GONE);
                    moreUsers.setVisibility(View.VISIBLE);

                } else if (tabLayout.getSelectedTabPosition() == 1) {
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    users_rv.setVisibility(View.GONE);
                    groups.setVisibility(View.GONE);
                    post.setVisibility(View.VISIBLE);
                    type = "post";
                    moreUsers.setVisibility(View.GONE);
                    more.setVisibility(View.VISIBLE);
                    getAllPost();
                } else if (tabLayout.getSelectedTabPosition() == 2) {
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

                    users_rv.setVisibility(View.GONE);
                    moreUsers.setVisibility(View.GONE);
                    groups.setVisibility(View.VISIBLE);
                    post.setVisibility(View.GONE);
                    more.setVisibility(View.GONE);
                    type = "group";
                    getAllGroup();

                } else if (tabLayout.getSelectedTabPosition() == 4) {
                    startActivity(new Intent(SearchActivity.this, LocationActivity.class));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        ParseQuery<Post> pq = ParseQuery.getQuery(Post.class);
        pq.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                int i = 0;
                if (e == null) {
                    initial = i;
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        ParseQuery<ParseUser> puq = ParseQuery.getQuery(ParseUser.class);
        puq.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                int i = 0;
                if (e == null) {
                    initialUsers = i;
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        findViewById(R.id.more).setOnClickListener(view -> {
            more.setText("Loading...");
            loadMoreData();
        });

        findViewById(R.id.moreUsers).setOnClickListener(v -> loadMoreUsers());

        //Tag
        if (getIntent().hasExtra("hashtag")){
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            users_rv.setVisibility(View.GONE);
            groups.setVisibility(View.GONE);
            post.setVisibility(View.VISIBLE);
            type = "post";
            Objects.requireNonNull(tabLayout.getTabAt(1)).select();
            more.setVisibility(View.GONE);
            moreUsers.setVisibility(View.GONE);
            filterPost(getIntent().getStringExtra("hashtag"));
            editText.setText(getIntent().getStringExtra("hashtag"));
        }

    }

    private void filterPost(String query) {
        ParseQuery<Post> pq1 = ParseQuery.getQuery(Post.class);
        pq1.whereEqualTo("text",query.toLowerCase());

        ParseQuery<Post> pq2 = ParseQuery.getQuery(Post.class);
        pq2.whereEqualTo("type",query.toLowerCase());

        List<ParseQuery<Post>> queries = new ArrayList<ParseQuery<Post>>();
        queries.add(pq1);
        queries.add(pq2);

        ParseQuery<Post> mainQueryp = ParseQuery.or(queries);
        mainQueryp.setLimit(currentPage*TOTAL_ITEM_EACH_LOAD);
        mainQueryp.findInBackground(new FindCallback<Post>() {
            public void done(List<Post> postsList, ParseException e) {
                    if (e == null) {
                        modelPosts.clear();
                        modelPosts.addAll(postsList);
                        Collections.reverse(modelPosts);
                        adapterPost = new AdapterPost(SearchActivity.this, modelPosts);
                        SearchActivity.this.post.setAdapter(adapterPost);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        if (!postsList.isEmpty()) {
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            SearchActivity.this.post.setVisibility(View.VISIBLE);
                            findViewById(R.id.nothing).setVisibility(View.GONE);
                            more.setVisibility(View.GONE);

                        } else {
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            SearchActivity.this.post.setVisibility(View.GONE);
                            findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                        }
                    } else {
                        Timber.d("Error: %s", e.getMessage());
                    }
                }
            });
    }

    private void filterGroup(String query) {
        ParseQuery<Group> pgq1 = ParseQuery.getQuery(Group.class);
        pgq1.whereEqualTo("gName",query.toLowerCase());

        ParseQuery<Group> pgq2 = ParseQuery.getQuery(Group.class);
        pgq2.whereEqualTo("gUsername",query.toLowerCase());

        List<ParseQuery<Group>> queries = new ArrayList<ParseQuery<Group>>();
        queries.add(pgq1);
        queries.add(pgq2);

        ParseQuery<Group> mainQueryg = ParseQuery.or(queries);
        mainQueryg.findInBackground(new FindCallback<Group>() {
            public void done(List<Group> modelGroupsList, ParseException e) {
                if (e == null) {
                    modelGroups.clear();
                    modelGroups.addAll(modelGroupsList);
                    Collections.reverse(modelGroups);
                    adapterGroups = new AdapterGroups(SearchActivity.this, modelGroups);
                    groups.setAdapter(adapterGroups);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    if (!modelGroups.isEmpty()) {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.groups).setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);

                    } else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.groups).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    private void filterUser(String query) {
        ParseQuery<ParseUser> puq1 = ParseUser.getQuery();
        puq1.whereEqualTo("name",query.toLowerCase());

        ParseQuery<ParseUser> puq2 = ParseUser.getQuery();
        puq2.whereEqualTo("username",query.toLowerCase());

        List<ParseQuery<ParseUser>> queries = new ArrayList<ParseQuery<ParseUser>>();
        queries.add(puq1);
        queries.add(puq2);

        ParseQuery<ParseUser> mainQueryu = ParseQuery.or(queries);
        mainQueryu.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> modelUsers, ParseException e) {
                if (e == null) {
                    userList.clear();
                    userList.addAll(modelUsers);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    Collections.reverse(userList);
                    adapterUsers = new AdapterUsers(SearchActivity.this, userList);
                    users_rv.setAdapter(adapterUsers);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    if (modelUsers.size() > 0) {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.moreUsers).setVisibility(View.VISIBLE);
                        findViewById(R.id.users).setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                        moreUsers.setVisibility(View.GONE);

                    } else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.users).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                        moreUsers.setVisibility(View.GONE);
                        findViewById(R.id.moreUsers).setVisibility(View.GONE);
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    private void loadMoreData() {
        currentPage++;
        getAllPost();
    }

    private void loadMoreUsers() {
        currentPageUsers++;
        getAllUsers();
    }

    private void getAllPost() {
        ParseQuery<Post> pq = ParseQuery.getQuery(Post.class);
        pq.setLimit(currentPage*TOTAL_ITEM_EACH_LOAD);
        pq.findInBackground(new FindCallback<Post>() {
            public void done(List<Post> pList, ParseException e) {
                if (e == null) {
                    modelPosts.clear();
                    modelPosts.addAll(pList);
                    Collections.reverse(modelPosts);
                    adapterPost = new AdapterPost(SearchActivity.this, modelPosts);
                    SearchActivity.this.post.setAdapter(adapterPost);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    if (modelPosts.size() > 0) {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.more).setVisibility(View.VISIBLE);
                        SearchActivity.this.post.setVisibility(View.VISIBLE);
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
                    else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        SearchActivity.this.post.setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                        findViewById(R.id.more).setVisibility(View.GONE);
                    }

                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });
    }

    private void getAllGroup() {
        ParseQuery<Group> gq = ParseQuery.getQuery(Group.class);
        gq.findInBackground(new FindCallback<Group>() {
            public void done(List<Group> gList, ParseException e) {
                if (e == null) {
                    modelGroups.clear();
                    modelGroups.addAll(gList);
                    Collections.reverse(modelGroups);
                    adapterGroups = new AdapterGroups(SearchActivity.this, modelGroups);
                    groups.setAdapter(adapterGroups);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    if (modelGroups.size() > 0) {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.groups).setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);}
                    else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.groups).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);}

                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });
    }

    private void getAllUsers() {
        ParseQuery<ParseUser> gq = ParseUser.getQuery();
        gq.whereNotEqualTo("objectId",ParseUser.getCurrentUser().getObjectId());
        gq.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> uList, ParseException e) {
                if (e == null) {
                    userList.clear();
                    userList.addAll(uList);
                    Collections.reverse(userList);
                    adapterUsers = new AdapterUsers(SearchActivity.this, userList);
                    users_rv.setAdapter(adapterUsers);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    if (userList.size() > 0) {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.users).setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                        if(adapterUsers.getItemCount() == initial){
                            moreUsers.setVisibility(View.GONE);
                            currentPageUsers--;
                        }else {
                            moreUsers.setVisibility(View.VISIBLE);
                        }
                    }
                    else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.users).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }

                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });
    }
}
