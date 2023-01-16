package com.frizid.timeline.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.frizid.timeline.App;
import com.frizid.timeline.MediaViewActivity;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.adapter.AdapterHigh;
import com.frizid.timeline.adapter.AdapterPost;
import com.frizid.timeline.adapter.AdapterReelView;
import com.frizid.timeline.model.BlockedUser;
import com.frizid.timeline.model.Follow;
import com.frizid.timeline.model.High;
import com.frizid.timeline.model.Notification;
import com.frizid.timeline.model.Post;
import com.frizid.timeline.model.Reel;
import com.frizid.timeline.model.ReportUser;
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
public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {

    //Id
    VideoView videoView;
    ImageView cover,verify,more;
    CircleImageView dp;
    TextView name,username,location;
    SocialTextView bio,link;
    TextView followers,following,posts,topName;
    LinearLayout following_ly,followers_ly,link_layout,location_layout;
    Button follow,unfollow;
    LinearLayout scroll;

    //String
    String hisUID;
    boolean isBlocked = false;

    //Bottom
    BottomSheetDialog more_options;
    LinearLayout message,report,block,poke;
    TextView text;

    //Post
    AdapterPost adapterPost;
    List<Post> modelPosts;
    RecyclerView post;

    private static final int TOTAL_ITEM_EACH_LOAD = 6;
    private int currentPage = 1;
    Button load;
    long initial;
    TextView nothing;

    //Story
    private AdapterHigh adapterHigh;
    private List<High> highs;
    RecyclerView storyView;

    private RequestQueue requestQueue;
    private boolean notify = false;

    //Reel
    RecyclerView reelView;
    AdapterReelView adapterReelView;
    List<Reel> reel;

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
        setContentView(R.layout.activity_user_profile);

        requestQueue = Volley.newRequestQueue(UserProfileActivity.this);

        //GetHisId

        hisUID = getIntent().getStringExtra("hisUID");

        //Declaring
        videoView = findViewById(R.id.video);
        cover = findViewById(R.id.cover);
        dp = findViewById(R.id.dp);
        name = findViewById(R.id.name);
        bio = findViewById(R.id.bio);
        username = findViewById(R.id.username);
        location = findViewById(R.id.location);
        link = findViewById(R.id.link);
        followers = findViewById(R.id.followers);
        following = findViewById(R.id.following);
        posts = findViewById(R.id.posts);
        following_ly = findViewById(R.id.linearLayout5);
        followers_ly = findViewById(R.id.linearLayout4);
        verify = findViewById(R.id.verify);
        follow = findViewById(R.id.follow);
        unfollow = findViewById(R.id.unfollow);
        more = findViewById(R.id.more);
        scroll = findViewById(R.id.scroll);
        location_layout = findViewById(R.id.location_layout);
        link_layout = findViewById(R.id.link_layout);
        topName = findViewById(R.id.topName);

        //OnStart
        findViewById(R.id.details).setVisibility(View.GONE);
        findViewById(R.id.bio).setVisibility(View.GONE);
        findViewById(R.id.name).setVisibility(View.GONE);
        findViewById(R.id.followers).setVisibility(View.GONE);
        findViewById(R.id.following).setVisibility(View.GONE);
        findViewById(R.id.posts).setVisibility(View.GONE);

        //more
        more.setOnClickListener(v -> more_options.show());

        findViewById(R.id.menu).setOnClickListener(v -> more_options.show());

        findViewById(R.id.edit).setOnClickListener(v -> onBackPressed());

        //Reel
        reelView = findViewById(R.id.reel);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        reelView.setLayoutManager(gridLayoutManager);
        reel = new ArrayList<>();

        
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.getInBackground(hisUID, new GetCallback<ParseUser>() {
            public void done(ParseUser object, ParseException e) {
                if (e == null) {
                    try{
                        ParseFile mDp = Objects.requireNonNull(object.getParseFile("photo"));
                        if (mDp.isDataAvailable()){
                            Picasso.get().load(mDp.getUrl()).placeholder(R.drawable.avatar).into(dp);
                        }
                    }catch(NullPointerException ignored){
                    }
                    String mName = Objects.requireNonNull(object.get("name")).toString();
                    String mUsername = Objects.requireNonNull(object.get("username")).toString();
                    String mBio = Objects.requireNonNull(object.get("bio")).toString();
                    String mLocation = Objects.requireNonNull(object.get("location")).toString();
                    String mLink = Objects.requireNonNull(object.get("link")).toString();
                    Boolean mVerify = Objects.requireNonNull(object.getBoolean("verified"));

                    name.setText(mName);
                    username.setText(mUsername);
                    location.setText(mLocation);
                    bio.setLinkText(mBio);
                    link.setLinkText(mLink);
                    topName.setText(mUsername);

                    bio.setOnLinkClickListener((i, s) -> {
                        if (i == 1){

                            Intent intent = new Intent(UserProfileActivity.this, SearchActivity.class);
                            intent.putExtra("hashtag", s);
                            startActivity(intent);
                            finish();
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
                                                    Snackbar.make(scroll,"It's you", Snackbar.LENGTH_LONG).show();
                                                }else {
                                                    Intent intent = new Intent(UserProfileActivity.this, UserProfileActivity.class);
                                                    intent.putExtra("hisUID", id);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        } else {Snackbar.make(scroll,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Snackbar.make(scroll,e.getMessage(), Snackbar.LENGTH_LONG).show();
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
                            finish();
                        }else if (i == 4){
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                            startActivity(intent);
                            finish();
                        }else if (i == 8){
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse("mailto:"));
                            intent.putExtra(Intent.EXTRA_EMAIL, s);
                            intent.putExtra(Intent.EXTRA_SUBJECT, "");
                            startActivity(intent);
                            finish();
                        }
                    });
                    link.setOnLinkClickListener((i, s) -> {
                        if (i == 1){

                            Intent intent = new Intent(UserProfileActivity.this, SearchActivity.class);
                            intent.putExtra("hashtag", s);
                            startActivity(intent);
                            finish();
                        }else
                        if (i == 2){
                            String username = s.replaceFirst("@","");
                            ParseQuery<ParseUser> flwto = ParseUser.getQuery();
                            flwto.whereEqualTo("username", username.trim());
                            flwto.findInBackground(new FindCallback<ParseUser>() {
                                public void done(List<ParseUser> userList, ParseException e) {
                                    if (e == null) {
                                        if(!userList.isEmpty()){
                                            for (ParseUser obj : userList) {
                                                String id = Objects.requireNonNull(obj.getObjectId()).toString();
                                                if (id.equals(ParseUser.getCurrentUser())){
                                                    Snackbar.make(scroll,"It's you", Snackbar.LENGTH_LONG).show();
                                                }else {
                                                    Intent intent = new Intent(UserProfileActivity.this, UserProfileActivity.class);
                                                    intent.putExtra("hisUID", id);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        } else {Snackbar.make(scroll,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Snackbar.make(scroll,e.getMessage(), Snackbar.LENGTH_LONG).show();
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
                            finish();
                        }else if (i == 4){
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                            startActivity(intent);
                            finish();
                        }else if (i == 8){
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse("mailto:"));
                            intent.putExtra(Intent.EXTRA_EMAIL, s);
                            intent.putExtra(Intent.EXTRA_SUBJECT, "");
                            startActivity(intent);
                            finish();
                        }
                    });

                    location.setOnClickListener(v -> {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.co.in/maps?q=" + mLocation));
                        startActivity(i);
                        finish();
                    });

                    if (mVerify){
                        verify.setVisibility(View.VISIBLE);
                    }else {
                        verify.setVisibility(View.GONE);
                    }

                    if (bio.getText().length()>0){
                        bio.setVisibility(View.VISIBLE);
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

                    //OnDone
                    findViewById(R.id.details).setVisibility(View.VISIBLE);
                    findViewById(R.id.name).setVisibility(View.VISIBLE);
                    findViewById(R.id.followers).setVisibility(View.VISIBLE);
                    findViewById(R.id.following).setVisibility(View.VISIBLE);
                    findViewById(R.id.posts).setVisibility(View.VISIBLE);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                }
                else {
                    Snackbar.make(scroll,e.getMessage(),Snackbar.LENGTH_LONG).show();
                }
            }
        });

        //Cover
        ParseQuery<ParseUser> uq = ParseUser.getQuery();
        uq.getInBackground(hisUID, new GetCallback<ParseUser>() {
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
                        videoView.setOnPreparedListener(mp -> {
                            mp.setLooping(true);
                            mp.setVolume(0, 0);
                        });
                        setDimension();
                        videoView.setOnClickListener(v -> {
                            Intent i = new Intent(getApplicationContext(), MediaViewActivity.class);
                            i.putExtra("type", "video");
                            i.putExtra("uri", coverFile.getUrl());
                            startActivity(i);
                            finish();
                        });
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        //Follow
        follow.setOnClickListener(v -> {
            Follow followobj = new Follow();
            followobj.setToObj( ParseObject.createWithoutData(ParseUser.class, hisUID));
            followobj.setFromObj(ParseUser.getCurrentUser());
            followobj.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        follow.setVisibility(View.GONE);
                        unfollow.setVisibility(View.VISIBLE);
                        addToHisNotification(hisUID, "Started following you");
                        notify = true;
                        if (notify) {
                            App.sendNotification(hisUID, ParseUser.getCurrentUser().getUsername(), "Started following you");
                        }
                        notify = false;
                    }else{
                    Timber.d("Error: %s", e.getMessage());
                }
            }
            });
        });

        //Post
        storyView = findViewById(R.id.story);
        LinearLayoutManager linearLayoutManager5 = new LinearLayoutManager(UserProfileActivity.this,
                LinearLayoutManager.HORIZONTAL, false);
        storyView.setLayoutManager(linearLayoutManager5);
        highs = new ArrayList<>();
        readStory();

        //UnFollow
        unfollow.setOnClickListener(v -> {
            ParseQuery<ParseObject> flwto = new ParseQuery(Follow.class);
            flwto.whereEqualTo("toObj", ParseObject.createWithoutData(ParseUser.class, hisUID));
            flwto.whereEqualTo("fromObj",ParseUser.getCurrentUser());
            flwto.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> flwtoList, ParseException e) {
                    if (e == null) {
                        for (ParseObject obj : flwtoList) {
                            obj.deleteInBackground();
                            unfollow.setVisibility(View.GONE);
                            follow.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Timber.d("Error: %s", e.getMessage());
                    }
                }
                });
        });

        //method
        isFollowing();
        checkBlocked();

        //bottom
        options();

        ParseQuery<Post> flwto = new ParseQuery(Post.class);
        flwto.whereEqualTo("userObj", ParseObject.createWithoutData(ParseUser.class, hisUID));
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
        post = findViewById(R.id.post);
        post.setLayoutManager(new LinearLayoutManager(UserProfileActivity.this));
        modelPosts = new ArrayList<>();
        getAllPost();

        load = findViewById(R.id.load);
        load.setOnClickListener(v1 -> loadMoreData());
        nothing = findViewById(R.id.nothing);

        getFollowers();
        getFollowing();

        followers_ly.setOnClickListener(v -> {
        Intent intent = new Intent(UserProfileActivity.this, FollowersActivity.class);
        intent.putExtra("Id", hisUID);
        startActivity(intent);
            finish();
        });

        following_ly.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, FollowingActivity.class);
            intent.putExtra("Id", hisUID);
            startActivity(intent);
            finish();
        });

        //TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tabLayout.getSelectedTabPosition() == 1) {
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    getReel();
                    reelView.setVisibility(View.VISIBLE);
                    post.setVisibility(View.GONE);
                    load.setVisibility(View.GONE);
                } else if (tabLayout.getSelectedTabPosition() == 0) {
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    post.setVisibility(View.VISIBLE);
                    reelView.setVisibility(View.GONE);
                    load.setVisibility(View.VISIBLE);
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



    private void getReel() {
        ParseQuery<Reel> rls = new ParseQuery(Reel.class);
        rls.whereEqualTo("userObj", ParseObject.createWithoutData(ParseUser.class, hisUID));
        rls.findInBackground(new FindCallback<Reel>() {
            public void done(List<Reel> rlsList, ParseException e) {
                if (e == null) {
                    reel.clear();
                    if (!rlsList.isEmpty()) {
                        for (Reel modelReel: rlsList) {
                            reel.add(modelReel);
                            adapterReelView = new AdapterReelView(reel);
                            reelView.setAdapter(adapterReelView);
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            if (adapterReelView.getItemCount() == 0){
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                reelView.setVisibility(View.GONE);
                                nothing.setVisibility(View.VISIBLE);
                            }else {
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                reelView.setVisibility(View.VISIBLE);
                                nothing.setVisibility(View.GONE);
                            }
                        }
                    } else
                    {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        reelView.setVisibility(View.GONE);
                        nothing.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void  getFollowers(){
        ParseQuery<ParseObject> flwr = new ParseQuery(Follow.class);
        flwr.whereEqualTo("toObj",ParseObject.createWithoutData(ParseUser.class, hisUID));
        flwr.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> flwtoList, ParseException e) {
                if (e == null) {
                    followers.setText(""+flwtoList.size());
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }
    private void  getFollowing(){
        ParseQuery<ParseObject> flwing = new ParseQuery(Follow.class);
        flwing.whereEqualTo("fromObj",ParseObject.createWithoutData(ParseUser.class, hisUID));
        flwing.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> flwtoList, ParseException e) {
                if (e == null) {
                    following.setText(""+flwtoList.size());
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    private void readStory() {
        ParseQuery<High> high = new ParseQuery(High.class);
        high.whereEqualTo("userObj",ParseObject.createWithoutData(ParseUser.class, hisUID));
        high.findInBackground(new FindCallback<High>() {
            public void done(List<High> highList, ParseException e) {
                if (e == null) {
                    highs.clear();
                    for(High modelStory : highList){
                        highs.add(modelStory);
                    }
                    adapterHigh = new AdapterHigh(UserProfileActivity.this, highs);
                    storyView.setAdapter(adapterHigh);
                    adapterHigh.notifyDataSetChanged();
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

    private void getAllPost() {
        ParseQuery<Post> rls = ParseQuery.getQuery(Post.class);
        rls.whereEqualTo("userObj", ParseObject.createWithoutData(ParseUser.class, hisUID));
        rls.findInBackground(new FindCallback<Post>() {
            public void done(List<Post> rlsList, ParseException e) {
                if (e == null) {
                    if (!rlsList.isEmpty()) {
                        modelPosts.clear();
                        for (Post modelPost: rlsList) {
                            modelPosts.add(modelPost);
                            Collections.reverse(modelPosts);
                            adapterPost = new AdapterPost(UserProfileActivity.this, modelPosts);
                            UserProfileActivity.this.post.setAdapter(adapterPost);
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            if (adapterPost.getItemCount() == 0){
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                UserProfileActivity.this.post.setVisibility(View.GONE);
                                nothing.setVisibility(View.VISIBLE);
                            }else {
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                UserProfileActivity.this.post.setVisibility(View.VISIBLE);
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
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        post.setVisibility(View.GONE);
                        nothing.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void checkBlocked() {
        ParseQuery<BlockedUser> blkd = ParseQuery.getQuery(BlockedUser.class);
        blkd.whereEqualTo("bUserObj", ParseObject.createWithoutData(ParseUser.class, hisUID));
        blkd.whereEqualTo("userObj",ParseUser.getCurrentUser());
        blkd.findInBackground(new FindCallback<BlockedUser>() {
            public void done(List<BlockedUser> blkdList, ParseException e) {
                if (e == null) {
                    if (!blkdList.isEmpty()) {
                        text.setText("Unblock");
                        isBlocked = true;
                    }
                }
            }
        });
    }

    private void isFollowing() {
        ParseQuery<Follow> blkd = ParseQuery.getQuery(Follow.class);
        blkd.whereEqualTo("toObj", ParseObject.createWithoutData(ParseUser.class, hisUID));
        blkd.whereEqualTo("fromObj",ParseUser.getCurrentUser());
        blkd.findInBackground(new FindCallback<Follow>() {
            public void done(List<Follow> blkdList, ParseException e) {
                if (e == null) {
                    if (!blkdList.isEmpty()) {
                        follow.setVisibility(View.GONE);
                        unfollow.setVisibility(View.VISIBLE);
                    } else
                    {
                        follow.setVisibility(View.VISIBLE);
                        unfollow.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void options() {
        if (more_options == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.user_more, null);
            message = view.findViewById(R.id.message);
            poke = view.findViewById(R.id.poke);
            block = view.findViewById(R.id.block);
            report = view.findViewById(R.id.report);
            text = view.findViewById(R.id.text);
            message.setOnClickListener(this);
            poke.setOnClickListener(this);
            block.setOnClickListener(this);
            report.setOnClickListener(this);

            view.findViewById(R.id.shareurl).setOnClickListener(view1 -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, name.getText().toString() + " Profile link " + "www.app.myfriend.com/user/" + hisUID + "\nDownload the app "+"https://play.google.com/store/apps/details?id=com.frizid.timeline");
                startActivity(Intent.createChooser(intent, "Share Via"));
                finish();
            });

            more_options = new BottomSheetDialog(this);
            more_options.setContentView(view);
        }
    }

    private void BlockUser() {
        BlockedUser blkd =new  BlockedUser();
        blkd.setBUserObj(ParseObject.createWithoutData(ParseUser.class, hisUID));
        blkd.setUserObj(ParseUser.getCurrentUser());
        blkd.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                Snackbar.make(scroll, "Blocked",Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void unBlockUser() {
        ParseQuery<BlockedUser> blkd = ParseQuery.getQuery(BlockedUser.class);
        blkd.whereEqualTo("bUserObj", ParseObject.createWithoutData(ParseUser.class, hisUID));
        blkd.whereEqualTo("userObj",ParseUser.getCurrentUser());
        blkd.findInBackground(new FindCallback<BlockedUser>() {
            public void done(List<BlockedUser> blkdList, ParseException e) {
                if (e == null) {
                    for (BlockedUser obj : blkdList) {
                        obj.deleteInBackground(new DeleteCallback() {
                            public void done(ParseException e) {
                                Snackbar.make(scroll, "UnBlocked", Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.message:
                more_options.cancel();
                //Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
                //intent.putExtra("hisUID", hisUID);
                //startActivity(intent);
                finish();
                break;
            case R.id.poke:
                more_options.cancel();
                notify = true;
                if (notify){
                    App.sendNotification(hisUID, ParseUser.getCurrentUser().getUsername(), "Has poked you");
                }
                notify = false;
                addToHisNotification(hisUID, "Has poked you");
                break;
            case R.id.block:

                more_options.cancel();
                if (isBlocked){
                    unBlockUser();
                }else {
                    BlockUser();
                }

                break;
            case R.id.report:

                more_options.cancel();
                ReportUser report = new ReportUser();
                report.setRUserObj( ParseObject.createWithoutData(ParseUser.class, hisUID));
                report.setUserObj(ParseUser.getCurrentUser());
                report.saveInBackground(new SaveCallback() {
                    public void done(ParseException e) {
                        Snackbar.make(scroll, "Reported",Snackbar.LENGTH_LONG).show();
                    }
                });
                break;
        }
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

    private void addToHisNotification(String hisUid, String message){
        Notification notif = new Notification();
        notif.setPUserObj( ParseObject.createWithoutData(ParseUser.class, hisUID));
        notif.setNotification(message);
        notif.setSUserObj( ParseUser.getCurrentUser());
        notif.saveInBackground();
        }
}