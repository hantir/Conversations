package com.frizid.timeline.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.adapter.AdapterGroups;
import com.frizid.timeline.adapter.AdapterPost;
import com.frizid.timeline.model.Group;
import com.frizid.timeline.model.Participants;
import com.frizid.timeline.model.Post;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class GroupFragment extends AppCompatActivity {

    //Group
    AdapterGroups adapterGroups;
    List<Group> groups;
    RecyclerView groupsRv;

    RecyclerView post;
    AdapterPost adapterPost;
    List<Post> modelPosts;

    //AdapterGroupsChatList getAdapterGroups;
    List<Group> groupsList;

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
        setContentView(R.layout.activity_group_fragment);

        //Group
        groupsRv = findViewById(R.id.groups);
        groupsRv.setLayoutManager(new LinearLayoutManager(GroupFragment.this));
        groups = new ArrayList<>();

        //Post
        post = findViewById(R.id.groups_post);
        post.setLayoutManager(new LinearLayoutManager(GroupFragment.this));
        modelPosts = new ArrayList<>();

        //TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tabLayout.getSelectedTabPosition() == 0) {
                    groupsRv.setVisibility(View.GONE);
                    post.setVisibility(View.GONE);
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                } else if (tabLayout.getSelectedTabPosition() == 1) {
                    groupsRv.setVisibility(View.GONE);
                    post.setVisibility(View.VISIBLE);
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    getAllPost();
                } else if (tabLayout.getSelectedTabPosition() == 2) {
                    groupsRv.setVisibility(View.VISIBLE);
                    post.setVisibility(View.GONE);
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    getMyGroups();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.create).setOnClickListener(v -> startActivity(new Intent(GroupFragment.this, StepOneActivity.class)));


    }

    private void getAllPost() {
        ParseQuery<Participants> pq = ParseQuery.getQuery(Participants.class);
        pq.whereEqualTo("userObj", ParseUser.getCurrentUser());
        pq.findInBackground(new FindCallback<Participants>() {
            public void done(List<Participants> gList, ParseException e) {
                if (e == null) {
                    if (gList.size() > 0) {
                        for (Participants mg : gList){
                        ParseQuery<Post> pq = ParseQuery.getQuery(Post.class);
                        pq.whereEqualTo("groupObj", mg.getGroupObj());
                        pq.findInBackground(new FindCallback<Post>() {
                            public void done(List<Post> gpList, ParseException e) {
                                if (e == null) {
                                    if (gpList.size() > 0) {
                                        for (Post modelPost : gpList) {
                                            modelPosts.add(modelPost);
                                            adapterPost = new AdapterPost(GroupFragment.this, modelPosts);
                                            post.setAdapter(adapterPost);
                                            adapterPost.notifyDataSetChanged();
                                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                                            if (adapterPost.getItemCount() == 0) {
                                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                                post.setVisibility(View.GONE);
                                                findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                                            } else {
                                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                                post.setVisibility(View.VISIBLE);
                                                findViewById(R.id.nothing).setVisibility(View.GONE);
                                            }

                                        }
                                    } else {
                                        post.setVisibility(View.GONE);
                                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                                        findViewById(R.id.groups_post).setVisibility(View.GONE);
                                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    Timber.d("Error: %s", e.getMessage());
                                }
                            }
                        });
                    }
                }else {
                        post.setVisibility(View.GONE);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.groups_post).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    private void getMyGroups() {
        ParseQuery<Participants> pq = ParseQuery.getQuery(Participants.class);
        pq.whereEqualTo("userObj", ParseUser.getCurrentUser());
        pq.findInBackground(new FindCallback<Participants>() {
            public void done(List<Participants> gList, ParseException e) {
                if (e == null) {
                    if (gList.size() > 0) {
                        for (Participants mg : gList){
                            ParseQuery<Group> pq = ParseQuery.getQuery(Group.class);
                            pq.getInBackground(mg.getObjectId(), new GetCallback<Group>() {
                                public void done(Group group, ParseException e) {
                                    if (e == null) {
                                        if (group != null) {
                                            groups.add(group);
                                        } else {
                                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                                            groupsRv.setVisibility(View.GONE);
                                            findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                                        }
                                    }else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });
                            }
                        adapterGroups = new AdapterGroups(GroupFragment.this, groups);
                        groupsRv.setAdapter(adapterGroups);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        if (adapterGroups.getItemCount() == 0) {
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            groupsRv.setVisibility(View.GONE);
                            findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            groupsRv.setVisibility(View.VISIBLE);
                            findViewById(R.id.nothing).setVisibility(View.GONE);
                        }
                        }
                    }
                    else {
                        Timber.d("Error: %s", e.getMessage());
                    }
                }
            });
    }

}