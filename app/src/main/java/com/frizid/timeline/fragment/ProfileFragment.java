package com.frizid.timeline.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.MediaViewActivity;
import com.frizid.timeline.R;
import com.frizid.timeline.adapter.AdapterHigh;
import com.frizid.timeline.adapter.AdapterPost;
import com.frizid.timeline.adapter.AdapterReelView;
import com.frizid.timeline.menu.MenuActivity;
import com.frizid.timeline.model.Follow;
import com.frizid.timeline.model.High;
import com.frizid.timeline.model.Post;
import com.frizid.timeline.model.Reel;
import com.frizid.timeline.profile.EditProfileActivity;
import com.frizid.timeline.profile.UserProfileActivity;
import com.frizid.timeline.search.SearchActivity;
import com.frizid.timeline.who.FollowersActivity;
import com.frizid.timeline.who.FollowingActivity;
import com.squareup.picasso.Picasso;
import com.tylersuehr.socialtextview.SocialTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class ProfileFragment extends Fragment {

    //Id
    VideoView videoView;
    ImageView cover,verify;
    CircleImageView dp;
    TextView name,username,location;
    SocialTextView bio,link;
    TextView followers,following,posts,topName;
    LinearLayout following_ly,followers_ly,link_layout,location_layout;
    View view;

    //Post
    AdapterPost adapterPost;
    List<Post> modelPosts;
    RecyclerView post;

    //Story
    private AdapterHigh adapterHigh;
    private List<High> highs;
    RecyclerView storyView;

    private static final int TOTAL_ITEM_EACH_LOAD = 6;
    private int currentPage = 1;
    Button load;
    long initial;
    TextView nothing;
    ProgressBar progressBar;

    //Reel
    RecyclerView reelView;
    AdapterReelView adapterReelView;
    List<Reel> reel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        //Declaring
        videoView = view.findViewById(R.id.video);
        cover = view.findViewById(R.id.cover);
        dp = view.findViewById(R.id.dp);
        name = view.findViewById(R.id.name);
        bio = view.findViewById(R.id.bio);
        username = view.findViewById(R.id.username);
        location = view.findViewById(R.id.location);
        link = view.findViewById(R.id.link);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        posts = view.findViewById(R.id.posts);
        following_ly = view.findViewById(R.id.linearLayout5);
        followers_ly = view.findViewById(R.id.linearLayout4);
        verify = view.findViewById(R.id.verify);
        location_layout = view.findViewById(R.id.location_layout);
        link_layout = view.findViewById(R.id.link_layout);
        topName = view.findViewById(R.id.topName);

        //Post
        storyView = view.findViewById(R.id.story);
        LinearLayoutManager linearLayoutManager5 = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        storyView.setLayoutManager(linearLayoutManager5);
        highs = new ArrayList<>();
        readStory();


        //OnStart
        view.findViewById(R.id.details).setVisibility(View.GONE);
        view.findViewById(R.id.bio).setVisibility(View.GONE);
        view.findViewById(R.id.name).setVisibility(View.GONE);
        view.findViewById(R.id.followers).setVisibility(View.GONE);
        view.findViewById(R.id.following).setVisibility(View.GONE);
        view.findViewById(R.id.posts).setVisibility(View.GONE);

         view.findViewById(R.id.edit).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
            getActivity().finish();
         });

        view.findViewById(R.id.menu).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MenuActivity.class);
            startActivity(intent);
            getActivity().finish();
        });


        //VideoView
        MediaController ctrl = new MediaController(getContext());
        ctrl.setVisibility(View.GONE);
        videoView.setMediaController(ctrl);
        setDimension();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            public void done(ParseUser object, ParseException e) {
                if (e == null) {
                    String mName = object.getString("name");
                    String mUsername = object.getString("username");
                    String mBio = object.getString("bio");
                    String mLocation = object.getString("location");
                    String mLink = object.getString("link");
                    boolean mVerify = object.getBoolean("verified");

                    try {
                        Picasso.get().load(object.fetchIfNeeded().getParseFile("photo").getUrl()).into(dp);
                    } catch (NullPointerException | ParseException e1) {
                    }

                name.setText(mName);
                username.setText(mUsername);
                location.setText(mLocation);
                bio.setLinkText(mBio);
                link.setLinkText(mLink);
                topName.setText(mUsername);

                if (mVerify){
                    verify.setVisibility(View.VISIBLE);
                }else {
                    verify.setVisibility(View.GONE);
                }

                if (bio.getText().length()>0){
                    bio.setVisibility(View.VISIBLE);
                }else {
                    bio.setVisibility(View.GONE);
                }

                if (location.getText().length()>0){
                    location_layout.setVisibility(View.VISIBLE);
                }else{
                    location_layout.setVisibility(View.GONE);
                }

                if (link.getText().length()>0){
                    link_layout.setVisibility(View.VISIBLE);
                }else{
                    link_layout.setVisibility(View.GONE);
                }


                bio.setOnLinkClickListener((i, s) -> {
                    if (i == 1){

                        Intent intent = new Intent(getContext(), SearchActivity.class);
                        intent.putExtra("hashtag", s);
                        startActivity(intent);
                        getActivity().finish();
                    }else
                    if (i == 2){
                        String username = s.replaceFirst("@","");
                        ParseQuery<ParseUser> user = ParseUser.getQuery();
                        user.whereEqualTo("username", username.trim());
                        user.findInBackground(new FindCallback<ParseUser>() {
                            public void done(List<ParseUser> userList, ParseException e) {
                                if (e == null) {
                                    if(!userList.isEmpty()){
                                        for (ParseUser obj : userList) {
                                            String id = Objects.requireNonNull(obj.getObjectId()).toString();
                                            if (id.equals(ParseUser.getCurrentUser())){
                                                Snackbar.make(view,"It's you", Snackbar.LENGTH_LONG).show();
                                            }else {
                                                Intent intent = new Intent(getContext(), UserProfileActivity.class);
                                                intent.putExtra("hisUID", obj);
                                                startActivity(intent);
                                                getActivity().finish();
                                            }
                                        }
                                    } else {Snackbar.make(view,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                                    }
                                } else {
                                    Snackbar.make(view.findViewById(R.id.scroll),e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    else if (i == 16){
                        if (!s.startsWith("https://") && !s.startsWith("http://")){
                            s = "http://" + s;
                        }
                        Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                        startActivity(openUrlIntent);
                        getActivity().finish();
                    }else if (i == 4){
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                        startActivity(intent);
                        getActivity().finish();
                    }else if (i == 8){
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"));
                        intent.putExtra(Intent.EXTRA_EMAIL, s);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "");
                        startActivity(intent);
                        getActivity().finish();
                    }
                });
                link.setOnLinkClickListener((i, s) -> {
                    if (i == 1){

                        Intent intent = new Intent(getContext(), SearchActivity.class);
                        intent.putExtra("hashtag", s);
                        startActivity(intent);
                        getActivity().finish();
                    }else
                    if (i == 2){
                        String username = s.replaceFirst("@","");
                        ParseQuery<ParseUser> user = new ParseQuery("User");
                        user.whereEqualTo("username", username.trim());
                        user.findInBackground(new FindCallback<ParseUser>() {
                            public void done(List<ParseUser> userList, ParseException e) {
                                if (e == null) {
                                    if(!userList.isEmpty()){
                                        for (ParseUser obj : userList) {
                                            String id = Objects.requireNonNull(obj.getObjectId()).toString();
                                            if (id.equals(ParseUser.getCurrentUser())){
                                                Snackbar.make(view,"It's you", Snackbar.LENGTH_LONG).show();
                                            }else {
                                                Intent intent = new Intent(getContext(), UserProfileActivity.class);
                                                intent.putExtra("hisUID", obj);
                                                startActivity(intent);
                                                getActivity().finish();
                                            }
                                        }
                                    } else {Snackbar.make(view,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                                    }
                                } else {
                                    Snackbar.make(view.findViewById(R.id.scroll),e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    else if (i == 16){
                        if (!s.startsWith("https://") && !s.startsWith("http://")){
                            s = "http://" + s;
                        }
                        Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                        startActivity(openUrlIntent);
                        getActivity().finish();
                    }else if (i == 4){
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                        startActivity(intent);
                        getActivity().finish();
                    }else if (i == 8){
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"));
                        intent.putExtra(Intent.EXTRA_EMAIL, s);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "");
                        startActivity(intent);
                        getActivity().finish();
                    }
                });

                location.setOnClickListener(v -> {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.co.in/maps?q=" + mLocation));
                    startActivity(i);
                    getActivity().finish();
                });

                //OnDone
                view.findViewById(R.id.details).setVisibility(View.VISIBLE);
                view.findViewById(R.id.name).setVisibility(View.VISIBLE);
                view.findViewById(R.id.followers).setVisibility(View.VISIBLE);
                view.findViewById(R.id.following).setVisibility(View.VISIBLE);
                view.findViewById(R.id.posts).setVisibility(View.VISIBLE);
                view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                }
                else {
                    Snackbar.make(view,e.getMessage(),Snackbar.LENGTH_LONG).show();
                }
            }
        });

        //Cover
        ParseQuery<ParseUser> uq = ParseUser.getQuery();
        uq.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            public void done(ParseUser object, ParseException e) {
                if (e == null) {
                    String type = Objects.requireNonNull(object.get("type")).toString();
                    ParseFile coverFile = object.getParseFile("cover");

                    if (type.equals("image")){
                        Picasso.get().load(coverFile.getUrl()).placeholder(R.drawable.cover).into(cover);
                        videoView.setVisibility(View.GONE);
                        cover.setVisibility(View.VISIBLE);
                    }else if (type.equals("video")){
                        videoView.setVisibility(View.VISIBLE);
                        cover.setVisibility(View.GONE);
                        videoView.setVideoURI(Uri.parse(coverFile.getUrl()));
                        videoView.start();
                        videoView.requestFocus();
                        videoView.setOnPreparedListener(mp -> {
                            mp.setLooping(true);
                            mp.setVolume(0, 0);
                        });

                        videoView.setOnClickListener(v -> {
                            Intent i = new Intent(getContext(), MediaViewActivity.class);
                            i.putExtra("type", "video");
                            i.putExtra("uri", coverFile.getUrl());
                            startActivity(i);
                            getActivity().finish();
                        });

                    }
                } else {
                    Snackbar.make(getView(),e.getMessage(),Snackbar.LENGTH_LONG).show();
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        ParseQuery<Post> flwto = ParseQuery.getQuery(Post.class);
        flwto.whereEqualTo("userObj", ParseUser.getCurrentUser());
        flwto.findInBackground(new FindCallback<Post>() {
            public void done(List<Post> pList, ParseException e) {
                if (e == null) {
                    posts.setText(""+pList.size());

                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });


        //Post
        post = view.findViewById(R.id.post);
        post.setLayoutManager(new LinearLayoutManager(getContext()));
        modelPosts = new ArrayList<>();
        getAllPost();

        load = view.findViewById(R.id.load);
        load.setOnClickListener(v1 -> loadMoreData());
        nothing = view.findViewById(R.id.nothing);
        progressBar = view.findViewById(R.id.progressBar);

        getFollowers();
        getFollowing();

        followers_ly.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FollowersActivity.class);
            intent.putExtra("Id", ParseUser.getCurrentUser().getObjectId());
            startActivity(intent);
            getActivity().finish();
        });

        following_ly.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FollowingActivity.class);
            intent.putExtra("Id", ParseUser.getCurrentUser().getObjectId());
            startActivity(intent);
            getActivity().finish();
        });

        //Reel
        reelView = view.findViewById(R.id.reel);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        reelView.setLayoutManager(gridLayoutManager);
        reel = new ArrayList<>();

        //TabLayout
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tabLayout.getSelectedTabPosition() == 1) {
                    view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    getReel();
                    reelView.setVisibility(View.VISIBLE);
                    post.setVisibility(View.GONE);
                    load.setVisibility(View.GONE);
                } else if (tabLayout.getSelectedTabPosition() == 0) {
                    view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    load.setVisibility(View.VISIBLE);
                    reelView.setVisibility(View.GONE);
                    post.setVisibility(View.VISIBLE);


                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }

    private void getReel() {
        ParseQuery<Reel> rls = ParseQuery.getQuery(Reel.class);
        rls.whereEqualTo("userObj", ParseUser.getCurrentUser());
        rls.findInBackground(new FindCallback<Reel>() {
            public void done(List<Reel> rlsList, ParseException e) {
                if (e == null) {
                    reel.clear();
                    if (!rlsList.isEmpty()) {
                        for (Reel modelReel: rlsList) {
                            reel.add(modelReel);
                            adapterReelView = new AdapterReelView(reel);
                            reelView.setAdapter(adapterReelView);
                            progressBar.setVisibility(View.GONE);
                            load.setVisibility(View.GONE);
                            if (adapterReelView.getItemCount() == 0){
                                progressBar.setVisibility(View.GONE);
                                reelView.setVisibility(View.GONE);
                                nothing.setVisibility(View.VISIBLE);
                            }else {
                                progressBar.setVisibility(View.GONE);
                                reelView.setVisibility(View.VISIBLE);
                                nothing.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            }
        });
    }

    private void  getFollowers(){
        ParseQuery<Follow> flwr = ParseQuery.getQuery(Follow.class);
        flwr.whereEqualTo("toObj",ParseUser.getCurrentUser());
        flwr.whereNotEqualTo("fromObj",ParseUser.getCurrentUser());
        flwr.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                    followers.setText(""+count);
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }
    private void  getFollowing(){
        ParseQuery<Follow> flwing = ParseQuery.getQuery(Follow.class);
        flwing.whereEqualTo("fromObj",ParseUser.getCurrentUser());
        flwing.whereNotEqualTo("toObj",ParseUser.getCurrentUser());
        flwing.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                    following.setText(""+count);
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

    private void readStory(){
        ParseQuery<High> high = ParseQuery.getQuery(High.class);
        high.whereEqualTo("userObj",ParseUser.getCurrentUser());
        high.findInBackground(new FindCallback<High>() {
            public void done(List<High> highList, ParseException e) {
                if (e == null) {
                    highs.clear();
                    for(High modelStory : highList){
                        highs.add(modelStory);
                    }
                    adapterHigh = new AdapterHigh(getContext(), highs);
                    storyView.setAdapter(adapterHigh);
                    adapterHigh.notifyDataSetChanged();
                } else {
                    Log.d("High", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void getAllPost() {
        ParseQuery<Post> pobj = ParseQuery.getQuery(Post.class);
        pobj.whereEqualTo("userObj", ParseUser.getCurrentUser());
        pobj.setLimit(currentPage*TOTAL_ITEM_EACH_LOAD);
        pobj.findInBackground(new FindCallback<Post>() {
            public void done(List<Post> rlsList, ParseException e) {
                if (e == null) {
                    if (!rlsList.isEmpty()) {
                        modelPosts.clear();
                        for (Post modelPost: rlsList) {
                            modelPosts.add(modelPost);
                            Collections.reverse(modelPosts);
                            adapterPost = new AdapterPost(getActivity(), modelPosts);
                            ProfileFragment.this.post.setAdapter(adapterPost);
                            progressBar.setVisibility(View.GONE);
                            if (adapterPost.getItemCount() == 0){
                                progressBar.setVisibility(View.GONE);
                                ProfileFragment.this.post.setVisibility(View.GONE);
                                nothing.setVisibility(View.VISIBLE);
                            }else {
                                progressBar.setVisibility(View.GONE);
                                ProfileFragment.this.post.setVisibility(View.VISIBLE);
                                nothing.setVisibility(View.GONE);
                                if(adapterPost.getItemCount() == initial){
                                    load.setVisibility(View.GONE);
                                    currentPage--;
                                }else {
                                    load.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    } else
                    {
                        progressBar.setVisibility(View.GONE);
                        post.setVisibility(View.GONE);
                        nothing.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void setDimension() {

        float videoProportion = getVideoProportion();
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        float screenProportion = (float) screenHeight / (float) screenWidth;
        android.view.ViewGroup.LayoutParams lp = videoView.getLayoutParams();

        if (videoProportion < screenProportion) {
            lp.height= screenHeight;
            lp.width = (int) ((float) screenHeight / videoProportion);
        } else {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth * videoProportion);
        }
        videoView.setLayoutParams(lp);
    }

    private float getVideoProportion(){
        return 1.5f;
    }

}