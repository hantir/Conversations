package com.frizid.timeline.post;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.frizid.timeline.adapter.AdapterPost;
import com.frizid.timeline.photoeditor.EditImageActivity;
import com.frizid.timeline.story.AddStoryActivity;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.iceteck.silicompressorr.SiliCompressor;
import com.nguyencse.URLEmbeddedData;
import com.nguyencse.URLEmbeddedView;
import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.App;
import com.frizid.timeline.MediaViewActivity;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
//import com.frizid.timeline.StickersPost;
import com.frizid.timeline.adapter.AdapterComment;
import com.frizid.timeline.model.Comment;
import com.frizid.timeline.model.Notification;
import com.frizid.timeline.model.Post;
import com.frizid.timeline.model.PostExtra;
import com.frizid.timeline.model.PostReaction;
import com.frizid.timeline.model.ReportPost;
import com.frizid.timeline.model.Saves;
import com.frizid.timeline.profile.UserProfileActivity;
import com.frizid.timeline.search.SearchActivity;
import com.frizid.timeline.send.SendToUserActivity;
import com.frizid.timeline.who.LikedActivity;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.tylersuehr.socialtextview.SocialTextView;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class CommentActivity extends AppCompatActivity implements View.OnClickListener{

    //String
    String postId,hisId;

    //Bottom
    BottomSheetDialog comment_more;
    LinearLayout image,video,gif;

    //Post
    CircleImageView dp;
    ImageView verified,activity,mediaView,like_img,more;
    TextView name,username,time,feeling,location,like_text,topName;
    SocialTextView text,bg_text;
    VoicePlayerView voicePlayerView;
    LinearLayout likeLayout,commentLayout,viewsLayout,layout,share;
    TextView noLikes,noComments,noViews, reactions;
    ImageView thumb,love,laugh,wow,angry,sad;
    LinearLayout likeButton,likeButtonTwo,main;
    RelativeLayout line;

    VideoView play;
    URLEmbeddedView urlEmbeddedView;
    MediaPlayer mp;

    public static List<String> extractUrls(String text)
    {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find())
        {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }


    //Comments
    List<Comment> commentsList;
    AdapterComment adapterComments;
    RecyclerView recyclerView;

    //Permission
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int VIDEO_PICK_CODE = 1003;
    private static final int PERMISSION_CODE = 1001;

    private RequestQueue requestQueue;
    private boolean notify = false;
    
    Post postObj;

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
        setContentView(R.layout.activity_comment);

        mp = MediaPlayer.create(getApplicationContext(), R.raw.like);

        main = findViewById(R.id.main);
        urlEmbeddedView = findViewById(R.id.uev);
        requestQueue = Volley.newRequestQueue(CommentActivity.this);

        //GetPostId
        //GIF
        if (getIntent().hasExtra("gif")){
            Comment pq = new Comment();
            pq.setPostObj(ParseObject.createWithoutData(Post.class, getIntent().getStringExtra("postId")));
            pq.setComment(getIntent().getStringExtra("gif"));
            pq.setUserObj(ParseUser.getCurrentUser());
            pq.setType("gif");
            pq.saveInBackground();
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            Snackbar.make(main, "Comment sent", Snackbar.LENGTH_LONG).show();
            onBackPressed();
             }else {
            postId = getIntent().getStringExtra("postId");
            
            postObj = ParseObject.createWithoutData(Post.class, postId);
        }

        if (getIntent().hasExtra("uri") && getIntent().hasExtra("type")){
            String uri = getIntent().getStringExtra("uri");
            uploadImage(Uri.parse(uri));
        }

        //Post
        dp = findViewById(R.id.dp);
        recyclerView = findViewById(R.id.recycler_view);
        verified = findViewById(R.id.verified);
        name = findViewById(R.id.name);
        username = findViewById(R.id.username);
        time = findViewById(R.id.time);
        activity = findViewById(R.id.activity);
        topName = findViewById(R.id.topName);
        feeling = findViewById(R.id.feeling);
        location = findViewById(R.id.location);
        text = findViewById(R.id.text);
        mediaView = findViewById(R.id.mediaView);
        bg_text = findViewById(R.id.bg_text);
        share = findViewById(R.id.share);
        play = findViewById(R.id.play);
        voicePlayerView = findViewById(R.id.voicePlayerView);
        likeLayout = findViewById(R.id.likeLayout);
        commentLayout = findViewById(R.id.commentLayout);
        viewsLayout = findViewById(R.id.viewsLayout);
        layout = findViewById(R.id.layout);
        noLikes =  findViewById(R.id.noLikes);
        reactions = findViewById(R.id.reactions);
        noComments  =  findViewById(R.id.noComments);
        noViews  =  findViewById(R.id.noViews);
        like_text =  findViewById(R.id.like_text);
        like_img  =  findViewById(R.id.like_img);
        thumb  =  findViewById(R.id.thumb);
        love  =  findViewById(R.id.love);
        laugh  =  findViewById(R.id.laugh);
        wow  =  findViewById(R.id.wow);
        angry  = findViewById(R.id.angry);
        likeButton  =  findViewById(R.id.likeButton);
        sad = findViewById(R.id.sad);
        likeButtonTwo = findViewById(R.id.likeButtonTwo);
        line = findViewById(R.id.line);
        more = findViewById(R.id.more);

        //Header
        findViewById(R.id.back).setOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        //Comment
        loadComments();

        //Add
        findViewById(R.id.add).setOnClickListener(v -> {
            comment_more.show();
        });
        commentMore();

        //CommentText
        EditText editText = findViewById(R.id.editText);
        findViewById(R.id.comment_send).setOnClickListener(v -> {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            String mComment = editText.getText().toString();
            if (mComment.isEmpty()){
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(main, "Type something", Snackbar.LENGTH_LONG).show();
            }else {
                Comment pq = new Comment();
                pq.setPostObj(ParseObject.createWithoutData(Post.class, postId));
                pq.setComment(mComment);
                pq.setUserObj(ParseUser.getCurrentUser());
                pq.setType("text");
                pq.saveInBackground();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(main, "Comment sent", Snackbar.LENGTH_LONG).show();
                onBackPressed();
                editText.setText("");
                addToHisNotification(hisId, "Commented on your post");
                notify = true;
                if (notify){
                    App.sendNotification(hisId, ParseUser.getCurrentUser().get("name").toString(), "Commented on your post");
                }
                notify = false;
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        postInfo();
    }

    private void postInfo() {
        ParseQuery<Post> uq = ParseQuery.getQuery("Post");
        uq.getInBackground(postId, new GetCallback<Post>() {
            public void done(Post pobj, ParseException e) {
                if (e == null) {
                    urlEmbeddedView.setOnClickListener(v -> {

                        List<String> extractedUrls = extractUrls(pobj.getString("text"));

                        for (String s : extractedUrls)
                        {
                            if (!s.startsWith("https://") && !s.startsWith("http://")){
                                s = "http://" + s;
                            }
                            Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                            startActivity(openUrlIntent);
                        }


                    });

                    if (!pobj.getString("text").isEmpty()) {

                        List<String> extractedUrls = extractUrls(pobj.getString("text").toString());

                        for (String url : extractedUrls)
                        {
                            urlEmbeddedView.setVisibility(View.VISIBLE);

                            urlEmbeddedView.setURL(url, new URLEmbeddedView.OnLoadURLListener() {
                                @Override
                                public void onLoadURLCompleted(URLEmbeddedData data) {
                                    urlEmbeddedView.title(data.getTitle());
                                    urlEmbeddedView.description(data.getDescription());
                                    urlEmbeddedView.host(data.getHost());
                                    urlEmbeddedView.thumbnail(data.getThumbnailURL());
                                    urlEmbeddedView.favor(data.getFavorURL());
                                }
                            });
                        }

                    }

                    //UserInfo
                    ParseQuery<ParseUser> uq = ParseUser.getQuery();
                    uq.getInBackground(pobj.getUserObj().getObjectId(), new GetCallback<ParseUser>() {
                        public void done(ParseUser uobj, ParseException e) {
                            if (e == null) {
                                hisId = pobj.getUserObj().getObjectId();
                                try{
                                    if (uobj.getParseFile("photo").isDataAvailable()) Picasso.get().load(uobj.getParseFile("photo").getUrl()).into(dp);
                                }catch(NullPointerException ignored){
                                }
                                name.setText(uobj.getString("name"));
                                username.setText(uobj.getUsername());
                                topName.setText(uobj.getString("name"));

                                //SetOnClick
                                dp.setOnClickListener(v -> {
                                    if (!uobj.getObjectId().equals(ParseUser.getCurrentUser())){
                                        Intent intent = new Intent(CommentActivity.this, UserProfileActivity.class);
                                        intent.putExtra("hisUID", uobj.getObjectId());
                                        startActivity(intent);
                                    }else {
                                        Snackbar.make(v,"It's you",Snackbar.LENGTH_LONG).show();
                                    }
                                });
                                name.setOnClickListener(v -> {
                                    if (!uobj.getObjectId().equals(ParseUser.getCurrentUser())){
                                        Intent intent = new Intent(CommentActivity.this, UserProfileActivity.class);
                                        intent.putExtra("hisUID", uobj.getObjectId());
                                        startActivity(intent);
                                    }else {
                                        Snackbar.make(v,"It's you",Snackbar.LENGTH_LONG).show();
                                    }
                                });
                                username.setOnClickListener(v -> {
                                    if (!uobj.getObjectId().equals(ParseUser.getCurrentUser())){
                                        Intent intent = new Intent(CommentActivity.this, UserProfileActivity.class);
                                        intent.putExtra("hisUID", uobj.getObjectId());
                                        startActivity(intent);
                                    }else {
                                        Snackbar.make(v,"It's you",Snackbar.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });

                    //Time
                    long lastTime = pobj.getUpdatedAt().getTime();
                    time.setText(App.getTimeAgo(lastTime));

                    //Extra
                    ParseQuery<PostExtra> peq = ParseQuery.getQuery(PostExtra.class);
                    peq.whereEqualTo("postObj",ParseObject.createWithoutData(Post.class, postId));
                    peq.getFirstInBackground(new GetCallback<PostExtra>() {
                        public void done(PostExtra peobj, ParseException e) {
                            if (e == null) {
                                if(!peobj.getObjectId().isEmpty()){
                                if (!peobj.getLocation().isEmpty())
                                    location.setText(" . " + peobj.getLocation().isEmpty());

                                location.setOnClickListener(v -> {
                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.co.in/maps?q=" + peobj.getLocation()));
                                    startActivity(i);
                                });

                                if (!peobj.getFeeling().isEmpty())
                                    feeling.setText(" - " + peobj.getFeeling());

                                if (!peobj.getFeeling().isEmpty()) {
                                    String mFeeling = peobj.getFeeling().toString();
                                    if (mFeeling.contains("Traveling")) {
                                        activity.setImageResource(R.drawable.airplane);
                                    } else if (mFeeling.contains("Watching")) {
                                        activity.setImageResource(R.drawable.watching);
                                    } else if (mFeeling.contains("Listening")) {
                                        activity.setImageResource(R.drawable.listening);
                                    } else if (mFeeling.contains("Thinking")) {
                                        activity.setImageResource(R.drawable.thinking);
                                    } else if (mFeeling.contains("Celebrating")) {
                                        activity.setImageResource(R.drawable.celebration);
                                    } else if (mFeeling.contains("Looking")) {
                                        activity.setImageResource(R.drawable.looking);
                                    } else if (mFeeling.contains("Playing")) {
                                        activity.setImageResource(R.drawable.playing);
                                    } else if (mFeeling.contains("happy")) {
                                        activity.setImageResource(R.drawable.smiling);
                                    } else if (mFeeling.contains("loved")) {
                                        activity.setImageResource(R.drawable.love);
                                    } else if (mFeeling.contains("sad")) {
                                        activity.setImageResource(R.drawable.sad);
                                    } else if (mFeeling.contains("crying")) {
                                        activity.setImageResource(R.drawable.crying);
                                    } else if (mFeeling.contains("angry")) {
                                        activity.setImageResource(R.drawable.angry);
                                    } else if (mFeeling.contains("confused")) {
                                        activity.setImageResource(R.drawable.confused);
                                    } else if (mFeeling.contains("broken")) {
                                        activity.setImageResource(R.drawable.broken);
                                    } else if (mFeeling.contains("cool")) {
                                        activity.setImageResource(R.drawable.cool);
                                    } else if (mFeeling.contains("funny")) {
                                        activity.setImageResource(R.drawable.joy);
                                    } else if (mFeeling.contains("tired")) {
                                        activity.setImageResource(R.drawable.tired);
                                    } else if (mFeeling.contains("shock")) {
                                        activity.setImageResource(R.drawable.shocked);
                                    } else if (mFeeling.contains("love")) {
                                        activity.setImageResource(R.drawable.heart);
                                    } else if (mFeeling.contains("sleepy")) {
                                        activity.setImageResource(R.drawable.sleeping);
                                    } else if (mFeeling.contains("expressionless")) {
                                        activity.setImageResource(R.drawable.muted);
                                    } else if (mFeeling.contains("blessed")) {
                                        activity.setImageResource(R.drawable.angel);
                                    }
                                }
                            }
                        }else {
                                Log.d("PostExtra", "Error: " + e.getMessage());
                            }
                        }
                    });

                    //PostDetails
                    String type = pobj.getType();
                    if (!type.equals("bg")){
                        text.setLinkText(pobj.getText());
                        text.setOnLinkClickListener((i, s) -> {
                            if (i == 1){

                                Intent intent = new Intent(CommentActivity.this, SearchActivity.class);
                                intent.putExtra("hashtag", s);
                                startActivity(intent);

                            }else
                            if (i == 2){
                                String username = s.replaceFirst("@","");
                                ParseQuery<ParseUser> uq1 = ParseUser.getQuery();
                                uq1.whereEqualTo("username", username.trim());
                                uq1.findInBackground(new FindCallback<ParseUser>() {
                                    public void done(List<ParseUser> usersList, ParseException e) {
                                        if (e == null) {
                                            if (!usersList.isEmpty()) {
                                                for (ParseUser user : usersList){
                                                    String id = user.getObjectId();
                                                    if (id.equals(ParseUser.getCurrentUser())){
                                                        Snackbar.make(main,"It's you", Snackbar.LENGTH_LONG).show();
                                                    }else {
                                                        Intent intent = new Intent(CommentActivity.this, UserProfileActivity.class);
                                                        intent.putExtra("hisUID", id);
                                                        startActivity(intent);
                                                    }
                                                }
                                            } else {
                                                Snackbar.make(main,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                                            }

                                        }else {
                                            Snackbar.make(main,e.getMessage(), Snackbar.LENGTH_LONG).show();
                                            Timber.d("Error: %s", e.getMessage());
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
                            }else if (i == 4){
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                                startActivity(intent);
                            }else if (i == 8){
                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                intent.setData(Uri.parse("mailto:"));
                                intent.putExtra(Intent.EXTRA_EMAIL, s);
                                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                                startActivity(intent);

                            }
                        });
                    }
                    if (type.equals("image")){
                        mediaView.setVisibility(View.VISIBLE);
                        findViewById(R.id.media).setVisibility(View.VISIBLE);
                        Picasso.get().load(pobj.getParseFile("meme").getUrl()).into(mediaView);
                    }
                    if (type.equals("gif")){
                        mediaView.setVisibility(View.VISIBLE);
                        findViewById(R.id.media).setVisibility(View.VISIBLE);
                        Glide.with(getApplicationContext()).load(pobj.getParseFile("meme").getUrl()).thumbnail(0.1f).into(mediaView);
                    }
                    if (type.equals("video")){
                        mediaView.setVisibility(View.VISIBLE);
                        play.setVisibility(View.VISIBLE);
                        findViewById(R.id.media).setVisibility(View.VISIBLE);
                        play.setVideoURI(Uri.parse(pobj.getParseFile("vine").getUrl()));
                        play.start();
                        play.setOnPreparedListener(mp -> mp.setLooping(true));
                        MediaController mediaController = new MediaController(CommentActivity.this);
                        mediaController.setAnchorView(play);
                        play.setMediaController(mediaController);
                    }
                    if (type.equals("bg")){
                        findViewById(R.id.media).setVisibility(View.VISIBLE);
                        Picasso.get().load(pobj.getUrl()).into(mediaView);
                        bg_text.setLinkText(pobj.getText());
                        bg_text.setOnLinkClickListener((i, s) -> {
                            if (i == 1){

                                Intent intent = new Intent(CommentActivity.this, SearchActivity.class);
                                intent.putExtra("hashtag", s);
                                startActivity(intent);

                            }else
                            if (i == 2){
                                String username = s.replaceFirst("@","");
                                ParseQuery<ParseUser> uq1 = ParseUser.getQuery();
                                uq1.whereEqualTo("username", username.trim());
                                uq1.findInBackground(new FindCallback<ParseUser>() {
                                    public void done(List<ParseUser> usersList, ParseException e) {
                                        if (e == null) {
                                            if (!usersList.isEmpty()) {
                                                for (ParseUser user : usersList){
                                                    String id = user.getObjectId();
                                                    if (id.equals(ParseUser.getCurrentUser())){
                                                        Snackbar.make(main,"It's you", Snackbar.LENGTH_LONG).show();
                                                    }else {
                                                        Intent intent = new Intent(CommentActivity.this, UserProfileActivity.class);
                                                        intent.putExtra("hisUID", id);
                                                        startActivity(intent);
                                                    }
                                                }
                                            } else {
                                                Snackbar.make(main,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                                            }

                                        }else {
                                            Snackbar.make(main,e.getMessage(), Snackbar.LENGTH_LONG).show();
                                            Timber.d("Error: %s", e.getMessage());
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
                            }else if (i == 4){
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                                startActivity(intent);
                            }else if (i == 8){
                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                intent.setData(Uri.parse("mailto:"));
                                intent.putExtra(Intent.EXTRA_EMAIL, s);
                                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                                startActivity(intent);
                            }
                        });
                        bg_text.setVisibility(View.VISIBLE);
                        text.setVisibility(View.GONE);
                        mediaView.setVisibility(View.VISIBLE);
                    }
                    if (type.equals("audio")){
                        findViewById(R.id.media).setVisibility(View.VISIBLE);
                        mediaView.setVisibility(View.GONE);
                        voicePlayerView.setVisibility(View.VISIBLE);
                        voicePlayerView.setAudio(pobj.getParseFile("meme").getUrl());
                    }

                    //CheckLikes
                    ParseQuery<PostReaction> query = ParseQuery.getQuery(PostReaction.class);
                    query.whereEqualTo("postObj",ParseObject.createWithoutData(Post.class, postId));
                    query.countInBackground(new CountCallback() {
                        @Override
                        public void done(int i, ParseException e) {
                            if ( e == null) {
                                if(i>0){
                                    likeLayout.setVisibility(View.VISIBLE);
                                    line.setVisibility(View.VISIBLE);
                                    noLikes.setText(String.valueOf(i));
                                    ParseQuery<PostReaction> plq1 = ParseQuery.getQuery(PostReaction.class);
                                    plq1.whereEqualTo("postObj",ParseObject.createWithoutData(Post.class, postId));
                                    plq1.whereEqualTo("userObj",ParseUser.getCurrentUser());
                                    plq1.getFirstInBackground(new GetCallback<PostReaction>() {
                                        public void done(PostReaction clobj, ParseException e) {
                                            if (e == null) {
                                                if(clobj.isDataAvailable()){
                                                    String react = clobj.getValue();
                                                    if (react.equals("like")){
                                                        like_img.setImageResource(R.drawable.ic_thumb);
                                                        like_text.setText("Like");
                                                    }
                                                    if (react.equals("love")){
                                                        like_img.setImageResource(R.drawable.ic_love);
                                                        like_text.setText("Love");
                                                    }
                                                    if (react.equals("laugh")){
                                                        like_img.setImageResource(R.drawable.ic_laugh);
                                                        like_text.setText("Haha");
                                                    }
                                                    if (react.equals("wow")){
                                                        like_img.setImageResource(R.drawable.ic_wow);
                                                        like_text.setText("Wow");
                                                    }
                                                    if (react.equals("sad")){
                                                        like_img.setImageResource(R.drawable.ic_sad);
                                                        like_text.setText("Sad");
                                                    }
                                                    if (react.equals("angry")){
                                                        like_img.setImageResource(R.drawable.ic_angry);
                                                        like_text.setText("Angry");
                                                    }
                                                }else{
                                                    like_img.setImageResource(R.drawable.ic_like);
                                                    like_text.setText("Like");
                                                }
                                            }else {
                                                Timber.d("Error: %s", e.getMessage());
                                            }
                                        }
                                    });

                                    //QuickShow
                                    int likeCount = reactionCount(postId, "like");
                                    if (likeCount>0){
                                        thumb.setVisibility(View.VISIBLE);
                                    }else {
                                        ParseQuery<PostReaction> clq2 = ParseQuery.getQuery(PostReaction.class);
                                        clq2.whereEqualTo("postObj",ParseObject.createWithoutData(Post.class, postId));
                                        clq2.countInBackground(new CountCallback() {
                                            public void done(int count, ParseException e) {
                                                if (e == null) {
                                                    if(count>0){
                                                        thumb.setVisibility(View.VISIBLE);
                                                    }else{
                                                        thumb.setVisibility(View.GONE);
                                                    }
                                                }else {
                                                    Timber.d("Error: %s", e.getMessage());
                                                }
                                            }
                                        });
                                    }
                                    int loveCount = reactionCount(postId, "love");
                                    if (loveCount>0){
                                        love.setVisibility(View.VISIBLE);
                                    }else {
                                        love.setVisibility(View.GONE);
                                    }
                                    int wowCount = reactionCount(postId, "wow");
                                    if (wowCount>0){
                                        wow.setVisibility(View.VISIBLE);
                                    }else {
                                        wow.setVisibility(View.GONE);
                                    }
                                    int angryCount = reactionCount(postId, "angry");
                                    if (angryCount>0){
                                        angry.setVisibility(View.VISIBLE);
                                    }else {
                                        angry.setVisibility(View.GONE);
                                    }
                                    int laughCount = reactionCount(postId, "laugh");
                                    if (laughCount>0){
                                        laugh.setVisibility(View.VISIBLE);
                                    }else {
                                        laugh.setVisibility(View.GONE);
                                    }
                                    int sadCount = reactionCount(postId, "sad");
                                    if (sadCount>0){
                                        sad.setVisibility(View.VISIBLE);
                                    }else {
                                        sad.setVisibility(View.GONE);
                                    }
                                }else {
                                    likeLayout.setVisibility(View.GONE);
                                    line.setVisibility(View.GONE);
                                    like_img.setImageResource(R.drawable.ic_like);
                                    like_text.setText("Like");
                                }
                            }
                        }
                    });

                    //Like
                    ReactionsConfig config = new ReactionsConfigBuilder(CommentActivity.this)
                            .withReactions(new int[]{
                                    R.drawable.ic_thumb,
                                    R.drawable.ic_love,
                                    R.drawable.ic_laugh,
                                    R.drawable.ic_wow,
                                    R.drawable.ic_sad,
                                    R.drawable.ic_angry
                            })
                            .withPopupAlpha(1)
                            .build();

                    ReactionPopup popup = new ReactionPopup(CommentActivity.this, config, (position1) -> {
                        mp.start();
                        
                        if (position1 == 0) {
                            return updateLikesReactions(postObj,"like");
                        }else if (position1 == 1) {
                            return updateLikesReactions(postObj,"love");
                        }
                        else if (position1 == 2) {
                            return updateLikesReactions(postObj,"laugh");
                        }else if (position1 == 3) {
                            return updateLikesReactions(postObj,"wow");
                        }
                        else if (position1 == 4) {
                            return updateLikesReactions(postObj,"sad");
                        }
                        else if (position1 == 5) {
                            return updateLikesReactions(postObj,"angry");
                        }
                        return true;
                    });

                    //LikeFunctions
                    likeButtonTwo.setOnTouchListener(popup);

                    ParseQuery<PostReaction> vqld1 = ParseQuery.getQuery(PostReaction.class);
                    vqld1.whereEqualTo("postObj", postObj );
                    vqld1.whereEqualTo("value", "like");
                    vqld1.findInBackground(new FindCallback<PostReaction>() {
                        public void done(List<PostReaction> likesList, ParseException e) {
                            if (e == null) {
                                if (likesList.size()>0) {
                                    for (PostReaction likesUser : likesList){
                                        if (likesUser.getUserObj().equals(ParseUser.getCurrentUser())){
                                            likeButtonTwo.setVisibility(View.GONE);
                                            likeButton.setVisibility(View.VISIBLE);
                                        }else{
                                            likeButton.setVisibility(View.GONE);
                                            likeButtonTwo.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }else{
                                    likeButton.setVisibility(View.GONE);
                                    likeButtonTwo.setVisibility(View.VISIBLE);
                                }
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                                if(e.getCode()==101){
                                    likeButton.setVisibility(View.GONE);
                                    likeButtonTwo.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    });

                    likeButton.setOnClickListener(v -> {
                        updateLikesReactions(postObj,"like");
                    });

                    //Share
                    Context wrapper = new ContextThemeWrapper(CommentActivity.this, R.style.popupMenuStyle);
                    PopupMenu sharePop = new PopupMenu(wrapper, share);
                    sharePop.getMenu().add(Menu.NONE,0,0, "App");
                    sharePop.getMenu().add(Menu.NONE,1,1, "Chat");
                    sharePop.getMenu().add(Menu.NONE,2,2, "Group");
                    sharePop.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == 0){
                            if (type.equals("text")){
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/*");
                                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                                intent.putExtra(Intent.EXTRA_TEXT, pobj.getText()+ " \nSee the post "+"www.app.myfriend.com/post/"+postId);
                                startActivity(Intent.createChooser(intent, "Share Via"));
                            }else if (type.equals("image")){
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/*");
                                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                                intent.putExtra(Intent.EXTRA_TEXT, pobj.getText() + " \nSee the post "+"www.app.myfriend.com/post/"+postId);
                                startActivity(Intent.createChooser(intent, "Share Via"));
                            }else if (type.equals("audio")){
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/*");
                                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                                intent.putExtra(Intent.EXTRA_TEXT, pobj.getText() + " \nSee the post "+"www.app.myfriend.com/post/"+postId);
                                startActivity(Intent.createChooser(intent, "Share Via"));
                            }else if (type.equals("gif")){
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/*");
                                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                                intent.putExtra(Intent.EXTRA_TEXT, pobj.getText() + " \nSee the post "+"www.app.myfriend.com/post/"+postId);
                                startActivity(Intent.createChooser(intent, "Share Via"));
                            }else if (type.equals("video")){
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/*");
                                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                                intent.putExtra(Intent.EXTRA_TEXT, pobj.getText() + " \nSee the post "+"www.app.myfriend.com/post/"+postId);
                                startActivity(Intent.createChooser(intent, "Share Via"));
                            }else {
                                Snackbar.make(main,"This type of post can't be shared", Snackbar.LENGTH_LONG).show();
                            }
                        }
                        if (item.getItemId() == 1){
                            Intent intent = new Intent(CommentActivity.this, SendToUserActivity.class);
                            intent.putExtra("type", "post");
                            intent.putExtra("uri", postId);
                            startActivity(intent);
                        }
                        if (item.getItemId() == 2){
                        /*Intent intent = new Intent( CommentActivity.this, SendToGroupActivity.class);
                        intent.putExtra("type", "post");
                        intent.putExtra("uri", postId);
                        startActivity(intent);*/
                        }
                        return false;
                    });
                    share.setOnClickListener(v -> sharePop.show());
                    findViewById(R.id.send).setOnClickListener(v -> sharePop.show());

                    //More
                    Context moreWrapper = new ContextThemeWrapper(CommentActivity.this, R.style.popupMenuStyle);
                    PopupMenu morePop = new PopupMenu(moreWrapper, more);
                    morePop.getMenu().add(Menu.NONE,1,1, "Save");
                    morePop.getMenu().add(Menu.NONE,2,2, "Download");
                    morePop.getMenu().add(Menu.NONE,4,4, "Copy");
                    morePop.getMenu().add(Menu.NONE,5,5, "Report");
                    morePop.getMenu().add(Menu.NONE,9,9, "Liked by");
                    if (type.equals("image") || type.equals("video")){
                        morePop.getMenu().add(Menu.NONE,8,8, "Fullscreen");
                    }
                    morePop.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == 1){
                            ParseQuery<Saves> clq = ParseQuery.getQuery(Saves.class);
                            clq.whereEqualTo("postObj",ParseObject.createWithoutData(Post.class, postId));
                            clq.whereEqualTo("userObj",ParseUser.getCurrentUser());
                            clq.getFirstInBackground(new GetCallback<Saves>() {
                                public void done(Saves clobj, ParseException e) {
                                    if (e == null) {
                                        if(clobj.isDataAvailable()){
                                            Saves clq1 = new Saves();
                                            clq1.deleteInBackground();
                                            Snackbar.make(main,"Unsaved", Snackbar.LENGTH_LONG).show();
                                        } else {
                                            Saves clq1 = new Saves();
                                            clq1.setPostObj(ParseObject.createWithoutData(Post.class, postId));
                                            clq1.setUserObj(ParseUser.getCurrentUser());
                                            clq1.saveInBackground();
                                            Snackbar.make(main,"Saved", Snackbar.LENGTH_LONG).show();
                                        }
                                    }else {
                                        Snackbar.make(main,e.getMessage(), Snackbar.LENGTH_LONG).show();
                                        Timber.d("Error: %s", e.getMessage());
                                    }
                                }
                            });

                        }
                        if (item.getItemId() == 9){
                            Intent intent = new Intent(CommentActivity.this, LikedActivity.class);
                            intent.putExtra("Id", postId);
                            startActivity(intent);
                        }
                        if (item.getItemId() == 2){
                            if (type.equals("text") || type.equals("bg")){
                                Snackbar.make(main,"This type of post can't be downloaded", Snackbar.LENGTH_LONG).show();
                            }else if (type.equals("video")){
                                Snackbar.make(main,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                                DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pobj.getParseFile("vine").getUrl()));
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalFilesDir(CommentActivity.this, DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".mp4");
                                Objects.requireNonNull(downloadManager).enqueue(request);
                            }else {
                                Snackbar.make(main,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                                DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pobj.getParseFile("meme").getUrl()));
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalFilesDir(CommentActivity.this, DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".png");
                                Objects.requireNonNull(downloadManager).enqueue(request);
                            }
                        }else if (item.getItemId() == 4){
                            Snackbar.make(main,"Copied", Snackbar.LENGTH_LONG).show();
                            if (type.equals("text")){
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("text", pobj.getText());
                                clipboard.setPrimaryClip(clip);
                            }else if (type.equals("image")){

                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("text", pobj.getText() + " " + pobj.getParseFile("meme").getUrl());
                                clipboard.setPrimaryClip(clip);

                            }else if (type.equals("audio")){

                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("text", pobj.getText() + " " + pobj.getParseFile("meme").getUrl());
                                clipboard.setPrimaryClip(clip);

                            }else if (type.equals("gif")){

                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("text", pobj.getText() + " " + pobj.getParseFile("meme").getUrl());
                                clipboard.setPrimaryClip(clip);

                            }else if (type.equals("video")){

                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("text", pobj.getText() + " " + pobj.getParseFile("vine").getUrl());
                                clipboard.setPrimaryClip(clip);

                            }else {
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("text", pobj.getText() + " " +pobj.getParseFile("meme").getUrl());
                                clipboard.setPrimaryClip(clip);

                            }
                        }else if (item.getItemId() == 5){
                            ParseQuery<ReportPost> rq = ParseQuery.getQuery(ReportPost.class);
                            rq.whereEqualTo("userObj", ParseUser.getCurrentUser());
                            rq.getFirstInBackground(new GetCallback<ReportPost>() {
                                public void done(ReportPost object, ParseException e) {
                                    if (e == null) {
                                        Snackbar.make(main,"Already Reported", Snackbar.LENGTH_LONG).show();
                                    } else {
                                        if(e.getCode()==101){
                                            ReportPost pobj = new ReportPost();
                                            pobj.setPostObj(ParseObject.createWithoutData(Post.class, postId));
                                            pobj.saveInBackground();
                                            Snackbar.make(main,"Reported", Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            });
                        }else  if (item.getItemId() == 7){
                            ParseQuery<Post> upq = ParseQuery.getQuery(Post.class);
                            upq.getInBackground(postId, new GetCallback<Post>() {
                                public void done(Post pobj, ParseException e) {
                                    if (e == null) {
                                        pobj.setIsDeleted(true);
                                        pobj.saveInBackground();
                                        Snackbar.make(main,"Deleted", Snackbar.LENGTH_LONG).show();
                                    } else {
                                        Timber.d("Error: %s", e.getMessage());
                                    }
                                }
                            });
                            onBackPressed();
                        }  else if (item.getItemId() == 8){
                            switch (type) {
                                case "image":

                                    Intent intent = new Intent(CommentActivity.this, MediaViewActivity.class);
                                    intent.putExtra("type", "image");
                                    intent.putExtra("uri", pobj.getParseFile("meme").getUrl());
                                    startActivity(intent);

                                    break;
                                case "video":

                                    Intent intent1 = new Intent(CommentActivity.this, MediaViewActivity.class);
                                    intent1.putExtra("type", "video");
                                    intent1.putExtra("uri", pobj.getParseFile("vine").getUrl());
                                    startActivity(intent1);

                                    break;
                            }
                        }
                        return false;
                    });
                    more.setOnClickListener(v -> morePop.show());

                    //ProgressBar
                    findViewById(R.id.progressBar).setVisibility(View.GONE);

                    //MediaLayout
                    RelativeLayout mediaViewLayout = findViewById(R.id.media);
                    mediaViewLayout.setOnClickListener(v -> {
                        switch (type) {
                            case "image":

                                Intent intent = new Intent(CommentActivity.this, MediaViewActivity.class);
                                intent.putExtra("type", "image");
                                intent.putExtra("uri", pobj.getParseFile("meme").getUrl());
                                startActivity(intent);

                                break;
                            case "video":

                                Intent intent1 = new Intent(CommentActivity.this, MediaViewActivity.class);
                                intent1.putExtra("type", "video");
                                intent1.putExtra("uri", pobj.getParseFile("vine").getUrl());
                                startActivity(intent1);

                                break;
                        }
                    });
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    private void commentMore() {
        if (comment_more == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.comment_more, null);
            image = view.findViewById(R.id.image);
            image.setOnClickListener(this);
            video = view.findViewById(R.id.video);
            video.setOnClickListener(this);
            gif = view.findViewById(R.id.gif);
            gif.setOnClickListener(this);
            comment_more = new BottomSheetDialog(this);
            comment_more.setContentView(view);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image:
                comment_more.cancel();

                //Check Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                    else {
                        pickImage();
                    }
                }
                else {
                    pickImage();
                }

                break;
            case R.id.video:
                comment_more.cancel();

                //Check Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                    else {
                        pickVideo();
                    }
                }
                else {
                    pickVideo();
                }

                break;
                case R.id.gif:

                    comment_more.cancel();
                    /*Intent s = new Intent(CommentActivity.this, StickersPost.class);
                    s.putExtra("activity", "comment");
                    s.putExtra("postId", postId);
                    startActivity(s);*/

                    break;
                    
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(main, "Storage permission allowed", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(main, "Storage permission is required", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, VIDEO_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null){
            Uri img_uri = Objects.requireNonNull(data).getData();
            //uploadImage(img_uri);

            callEditPhot(img_uri);

            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            Snackbar.make(main, "Please wait, sending...", Snackbar.LENGTH_LONG).show();
        }
        if (resultCode == RESULT_OK && requestCode == 130 && data != null){
            Uri img_uri = Objects.requireNonNull(data).getData();
            uploadImage(img_uri);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            Snackbar.make(main, "Please wait, sending...", Snackbar.LENGTH_LONG).show();
        }
        if (resultCode == RESULT_OK && requestCode == VIDEO_PICK_CODE && data != null){
            Uri video_uri = Objects.requireNonNull(data).getData();

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getApplicationContext(), video_uri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMilli = Long.parseLong(time);
            try {
                retriever.release();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (timeInMilli > 600000){
                Snackbar.make(main, "Video must be of 10 minutes or less", Snackbar.LENGTH_LONG).show();
            }else {
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                Snackbar.make(main, "Please wait, sending...", Snackbar.LENGTH_LONG).show();
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
                new CommentActivity.CompressVideo().execute("false",video_uri.toString(),file.getPath());
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void callEditPhot(Uri img_uri) {
        Intent intent = new Intent(CommentActivity.this, EditImageActivity.class);
        intent.putExtra("imageUri", img_uri.toString());
        intent.putExtra("type", "comment");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @SuppressLint("StaticFieldLeak")
    private class CompressVideo extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            String videoPath = null;
            try {
                Uri mUri = Uri.parse(strings[1]);
                videoPath = SiliCompressor.with(CommentActivity.this)
                        .compressVideo(mUri,strings[2]);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return videoPath;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            File file = new File(s);
            Uri videoUri = Uri.fromFile(file);
            try {
                uploadVideo(videoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadVideo(Uri videoUri) throws IOException {
        String mime = ".mp4";
        byte[] videoBytes = App.getParseFileVideoBytes(this, videoUri.getPath(), mime);
        ParseFile video = new ParseFile(UUID.randomUUID().toString()+mime, videoBytes);
        video.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e==null){
                    Comment pvq = new Comment();
                    pvq.setPostObj(ParseObject.createWithoutData(Post.class, getIntent().getStringExtra("postId")));
                    pvq.setComment("");
                    pvq.setFile(video);
                    pvq.setUserObj(ParseUser.getCurrentUser());
                    pvq.setType("video");
                    pvq.saveInBackground();
                }
                else{
                    Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        Snackbar.make(main, "Comment sent", Snackbar.LENGTH_LONG).show();
        onBackPressed();
        addToHisNotification(hisId, "Commented on your post");
        notify = true;
        if (notify){
            App.sendNotification(hisId, ParseUser.getCurrentUser().get("name").toString(), "Commented on your post");
        }
        notify = false;
    }

    private void uploadImage(Uri dp_uri) {
        Bitmap file_bit = App.uriToBitmap(this, dp_uri);
        String mime = ".jpg";
        byte[] scaledData = App.getParseFileBytes(this, file_bit, mime);
        ParseFile image = new ParseFile(UUID.randomUUID().toString()+mime, scaledData);
        image.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e==null){
                    Comment pvq = new Comment();
                    pvq.setPostObj(ParseObject.createWithoutData(Post.class, getIntent().getStringExtra("postId")));
                    pvq.setComment("");
                    pvq.setFile(image);
                    pvq.setUserObj(ParseUser.getCurrentUser());
                    pvq.setType("image");
                    pvq.saveInBackground();
                }
                else{
                    Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        Snackbar.make(main, "Comment sent", Snackbar.LENGTH_LONG).show();
        onBackPressed();
        addToHisNotification(hisId, "Commented on your post");
        notify = true;
        if (notify){
            App.sendNotification(hisId, ParseUser.getCurrentUser().get("name").toString(), "Commented on your post");
            finish();
        }
        notify = false;
    }

    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        commentsList = new ArrayList<>();
        ParseQuery<Comment> clq = ParseQuery.getQuery(Comment.class);
        clq.whereEqualTo("postObj",ParseObject.createWithoutData(Post.class, postId));
        clq.findInBackground(new FindCallback<Comment>() {
            public void done(List<Comment> cList, ParseException e) {
                if (e == null) {
                    commentsList.clear();
                    for(Comment cobj: cList){
                        commentsList.add(cobj);
                        Collections.reverse(commentsList);
                        adapterComments = new AdapterComment(CommentActivity.this, commentsList);
                        recyclerView.setAdapter(adapterComments);
                        adapterComments.notifyDataSetChanged();
                    }
                }else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    private void addToHisNotification(String hisUid, String message){
        Notification notif = new Notification();
        notif.setPostObj(ParseObject.createWithoutData(Post.class, postId));
        notif.setPUserObj(ParseObject.createWithoutData(ParseUser.class, hisUid));
        notif.setNotification(message);
        notif.setSUserObj(ParseUser.getCurrentUser());
        notif.saveInBackground();
    }

    public int reactionCount(String postId, String type)
    {
        final int[] toalCount = new int[1];
        ParseQuery<PostReaction> clq = ParseQuery.getQuery(PostReaction.class);
        clq.whereEqualTo("postObj",ParseObject.createWithoutData(Post.class, postId));
        clq.whereEqualTo("type",type);
        clq.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                    toalCount[0] =count;
                }else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
        return toalCount[0];
    };

    public boolean updateLikesReactions(Post postObj, String value)
    {
        ParseQuery<PostReaction> vq1 = ParseQuery.getQuery(PostReaction.class);
        vq1.whereEqualTo("postObj", postObj);
        vq1.whereEqualTo("userObj", ParseUser.getCurrentUser());
        vq1.getFirstInBackground(new GetCallback<PostReaction>() {
            public void done(PostReaction lobj, ParseException e) {
                if (e == null) {
                    if (lobj.isDataAvailable()) {
                        lobj.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    updatePost(postObj);
                                }
                            }
                        });
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                    if(e.getCode()==101){
                        PostReaction myReaction = new PostReaction();
                        myReaction.setUserObj(ParseUser.getCurrentUser());
                        myReaction.setPostObj(postObj);
                        myReaction.setValue(value);
                        myReaction.saveInBackground( new SaveCallback() {
                            public void done(ParseException e) {
                                if (e == null) {
                                    updatePost(postObj);
                                }
                            }
                        });
                    }
                }
            }
        });
        return true;
    };

    void updatePost(Post postObj){
        ParseQuery<PostReaction> vq1 = ParseQuery.getQuery(PostReaction.class);
        vq1.whereEqualTo("postObj", postObj);
        vq1.findInBackground(new FindCallback<PostReaction>() {
            public void done(List<PostReaction> likesList, ParseException e) {
                if (e == null) {
                    if (likesList.size() > 0) {

                        thumb.setVisibility(View.GONE);
                        love.setVisibility(View.GONE);
                        wow.setVisibility(View.GONE);
                        angry.setVisibility(View.GONE);
                        laugh.setVisibility(View.GONE);
                        sad.setVisibility(View.GONE);

                        likeLayout.setVisibility(View.VISIBLE);
                        line.setVisibility(View.VISIBLE);
                        noLikes.setText(String.valueOf(likesList.size()));
                        reactions.setVisibility(View.VISIBLE);

                        for (PostReaction likesUser : likesList){
                            String value = likesUser.getValue();
                            if (value.equals("like")) thumb.setVisibility(View.VISIBLE);
                            if (value.equals("love")) love.setVisibility(View.VISIBLE);
                            if (value.equals("wow")) wow.setVisibility(View.VISIBLE);
                            if (value.equals("angry")) angry.setVisibility(View.VISIBLE);
                            if (value.equals("laugh")) laugh.setVisibility(View.VISIBLE);
                            if (value.equals("sad")) sad.setVisibility(View.VISIBLE);

                            if (likesUser.getUserObj().equals(ParseUser.getCurrentUser())){
                                if(likesList.size()==1){
                                    reactions.setText("You");
                                }else {
                                    reactions.setText("You and "+String.valueOf(likesList.size()-1)+" Others");
                                }

                                if (value.equals("like")){
                                    like_img.setImageResource(R.drawable.ic_thumb);
                                    like_text.setText("Like");
                                    likeButtonTwo.setVisibility(View.GONE);
                                    likeButton.setVisibility(View.VISIBLE);
                                }
                                if (value.equals("love")){
                                    like_img.setImageResource(R.drawable.ic_love);
                                    like_text.setText("Love");
                                    likeButtonTwo.setVisibility(View.GONE);
                                    likeButton.setVisibility(View.VISIBLE);
                                }
                                if (value.equals("laugh")){
                                    like_img.setImageResource(R.drawable.ic_laugh);
                                    like_text.setText("Haha");
                                    likeButtonTwo.setVisibility(View.GONE);
                                    likeButton.setVisibility(View.VISIBLE);
                                }
                                if (value.equals("wow")){
                                    like_img.setImageResource(R.drawable.ic_wow);
                                    like_text.setText("Wow");
                                    likeButtonTwo.setVisibility(View.GONE);
                                    likeButton.setVisibility(View.VISIBLE);
                                }
                                if (value.equals("sad")){
                                    like_img.setImageResource(R.drawable.ic_sad);
                                    like_text.setText("Sad");
                                    likeButtonTwo.setVisibility(View.GONE);
                                    likeButton.setVisibility(View.VISIBLE);
                                }
                                if (value.equals("angry")){
                                    like_img.setImageResource(R.drawable.ic_angry);
                                    like_text.setText("Angry");
                                    likeButtonTwo.setVisibility(View.GONE);
                                    likeButton.setVisibility(View.VISIBLE);
                                }
                            }else{
                                like_img.setImageResource(R.drawable.ic_like);
                                like_text.setText("Like");
                            }
                        }
                    } else {
                        likeButton.setVisibility(View.GONE);
                        likeButtonTwo.setVisibility(View.VISIBLE);
                        likeLayout.setVisibility(View.GONE);
                        line.setVisibility(View.GONE);
                        like_img.setImageResource(R.drawable.ic_like);
                        like_text.setText("Like");
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                    if(e.getCode()==101){
                        likeButton.setVisibility(View.GONE);
                        likeButtonTwo.setVisibility(View.VISIBLE);
                        likeLayout.setVisibility(View.GONE);
                        line.setVisibility(View.GONE);
                        like_img.setImageResource(R.drawable.ic_like);
                        like_text.setText("Like");
                    }
                }
            }
        });
    };
}