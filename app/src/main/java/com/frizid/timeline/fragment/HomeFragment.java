package com.frizid.timeline.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.frizid.timeline.CameraUtil;
import com.frizid.timeline.MainActivity;
import com.frizid.timeline.photoeditor.EditImageActivity;
import com.frizid.timeline.reel.PostReelActivity;
import com.google.android.material.snackbar.Snackbar;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.R;
import com.frizid.timeline.adapter.AdapterPost;
import com.frizid.timeline.adapter.AdapterStory;
import com.frizid.timeline.group.GroupFragment;
import com.frizid.timeline.model.Follow;
import com.frizid.timeline.model.Post;
import com.frizid.timeline.model.Story;
import com.frizid.timeline.notifications.NotificationScreen;
import com.frizid.timeline.post.CreatePostActivity;
import com.frizid.timeline.search.TrendingActivity;
import com.frizid.timeline.send.MediaSelectActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class HomeFragment extends Fragment {

    //Post
    AdapterPost adapterPost;
    List<Post> posts;
    RecyclerView post;

    //Story
    private AdapterStory adapterStory;
    private List<Story> modelStories;
    RecyclerView storyView;

    //Follow
    List<String> followingList = new ArrayList<>();

    //OtherId;
    ProgressBar progressBar;
    TextView nothing;

    private CameraUtil cameraUtil;
    File file = null;

    private static final int TOTAL_ITEM_EACH_LOAD = 18;
    private static final int CAMERA_REQUEST_CODE = 1001;
    private int currentPage = 1;
    Button more;
    long initial;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        //Post
        post = v.findViewById(R.id.post);
        post.setLayoutManager(new LinearLayoutManager(getContext()));
        posts = new ArrayList<>();
        checkFollowing();

        more = v.findViewById(R.id.more);
        v.findViewById(R.id.more).setOnClickListener(view -> {
            more.setText("Loading...");
            loadMoreData();
        });

        //PostIntent
        v.findViewById(R.id.create_post).setOnClickListener(v1 -> startActivity(new Intent(getActivity(), CreatePostActivity.class)));

        //Search
        v.findViewById(R.id.search).setOnClickListener(v1 -> startActivity(new Intent(getActivity(), TrendingActivity.class)));

        //Group
        v.findViewById(R.id.add).setOnClickListener(v1 -> startActivity(new Intent(getActivity(), GroupFragment.class)));

        //Camera
        v.findViewById(R.id.camera).setOnClickListener(v1 -> {
            /*Intent intent = new Intent(getContext(), MediaSelectActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);*/

                    ((MainActivity)getActivity()).cameraIntent();

        }
        );

        //Notification
        ParseUser user = ParseUser.getCurrentUser();
                if (Objects.equals(user.get("count"), 0)){
                    v.findViewById(R.id.bell).setVisibility(View.GONE);
                    v.findViewById(R.id.count).setVisibility(View.VISIBLE);
                    TextView count =  v.findViewById(R.id.count);
                    count.setText(Objects.requireNonNull(user.get("count")).toString());
                }else {
                    v.findViewById(R.id.bell).setVisibility(View.VISIBLE);
                    v.findViewById(R.id.count).setVisibility(View.GONE);
                }

        v.findViewById(R.id.bell).setOnClickListener(v1 -> {
            startActivity(new Intent(getActivity(), NotificationScreen.class));
            v.findViewById(R.id.bell).setVisibility(View.VISIBLE);
            v.findViewById(R.id.count).setVisibility(View.GONE);
        });
        v.findViewById(R.id.count).setOnClickListener(v1 -> {
            startActivity(new Intent(getActivity(), NotificationScreen.class));
            v.findViewById(R.id.bell).setVisibility(View.VISIBLE);
            v.findViewById(R.id.count).setVisibility(View.GONE);
        });

        CircleImageView circleImageView = v.findViewById(R.id.circleImageView);
        try {
            Picasso.get().load(user.getParseFile("photo").getUrl()).into(circleImageView);
        } catch (NullPointerException e) {
            Picasso.get().load(R.drawable.avatar).into(circleImageView);
        }

        //Story
        storyView = v.findViewById(R.id.story_list);
        LinearLayoutManager linearLayoutManager5 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        storyView.setLayoutManager(linearLayoutManager5);
        modelStories = new ArrayList<>();

        //OtherId
        progressBar = v.findViewById(R.id.progressBar);
        nothing = v.findViewById(R.id.nothing);

        return v;
    }
    

    private void loadMoreData() {
        currentPage++;
        getAllPost();
    }

    private void getAllPost() {
        if (currentPage*TOTAL_ITEM_EACH_LOAD>0){
            ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
            query.whereContainedIn("userObj", followingList);
            query.findInBackground(new FindCallback<Post>() {
                @Override
                public void done(List<Post> posts, ParseException e) {
                    Collections.reverse(posts);
                    adapterPost = new AdapterPost(getActivity(), posts);
                    post.setAdapter(adapterPost);
                    progressBar.setVisibility(View.GONE);
                    if (adapterPost.getItemCount() == 0) {
                        progressBar.setVisibility(View.GONE);
                        post.setVisibility(View.GONE);
                        nothing.setVisibility(View.VISIBLE);
                        more.setVisibility(View.GONE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        post.setVisibility(View.VISIBLE);
                        nothing.setVisibility(View.GONE);
                        if (adapterPost.getItemCount() >= initial) {
                            more.setVisibility(View.GONE);
                            currentPage--;
                            more.setText("Load more");
                        } else {
                            more.setVisibility(View.VISIBLE);
                            more.setText("Load more");
                        }
                    }
                }
            });
        }
    }

    private void checkFollowing(){
        ParseQuery<Follow> obj = new ParseQuery<Follow>(Follow.class);
        obj.whereEqualTo("fromObj",ParseUser.getCurrentUser());
        obj.findInBackground(new FindCallback<Follow>() {
            @Override
            public void done(List<Follow> objects, ParseException e) {
                if (e == null) {
                    followingList.add(ParseUser.getCurrentUser().getObjectId());
                    for(Follow o : objects){
                        followingList.add(o.getToObj().getObjectId());
                        }
                readStory();

                ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
                query.whereContainedIn("userObj", followingList);
                query.findInBackground(new FindCallback<Post>() {
                    @Override
                    public void done(List<Post> objects, ParseException e) {
                        if (e == null) {
                            initial = objects.size();
                            getAllPost();
                    }
                else {
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

    private void readStory(){
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        ParseQuery<Story> sq = ParseQuery.getQuery(Story.class);
        sq.whereContainedIn("userObj",followingList);
        sq.whereGreaterThan("createdAt", cal.getTime());
        sq.setLimit(100);
        sq.orderByDescending("createdAt");
        sq.findInBackground(new FindCallback<Story>() {
            @Override
            public void done(List<Story> objects, ParseException e) {
                if (e == null) {
                    modelStories.clear();
                    if (objects.size() > 0){
                        modelStories.addAll(objects);
                    }
                    adapterStory = new AdapterStory(getContext(), modelStories);
                    storyView.setAdapter(adapterStory);
                    adapterStory.notifyDataSetChanged();
                }else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }
}