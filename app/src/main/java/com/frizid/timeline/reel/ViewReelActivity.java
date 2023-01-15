package com.frizid.timeline.reel;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.frizid.timeline.post.CommentActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.R;
import com.frizid.timeline.model.Comment;
import com.frizid.timeline.model.Reel;
import com.frizid.timeline.model.ReelLike;
import com.frizid.timeline.model.ReelView;
import com.frizid.timeline.model.ReportReel;
import com.frizid.timeline.model.SavesReel;
import com.frizid.timeline.profile.UserProfileActivity;
import com.frizid.timeline.search.SearchActivity;
import com.frizid.timeline.send.SendToUserActivity;
import com.frizid.timeline.who.ReelLikedActivity;
import com.squareup.picasso.Picasso;
import com.tylersuehr.socialtextview.SocialTextView;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class ViewReelActivity extends AppCompatActivity {

    VideoView videoView;
    LinearLayout like, comment;
    ImageView share;
    CircleImageView avatar;
    TextView name,textLike,textComment;
    SocialTextView description;
    ImageView like_img;
    String reelId;
    String uri;
    String text,hisId;

    BottomSheetDialog share_options;
    LinearLayout app,groups,users;

    BottomSheetDialog reel_options;
    LinearLayout download,save,delete,copy,report,liked;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reel);
        
        uri = getIntent().getStringExtra("id");

        videoView = findViewById(R.id.videoView);
        like = findViewById(R.id.like);
        comment = findViewById(R.id.comment);
        share = findViewById(R.id.share);
        name = findViewById(R.id.name);
        description = findViewById(R.id.description);
        avatar = findViewById(R.id.avatar);
        like_img  = findViewById(R.id.like_img);
        textLike = findViewById(R.id.textLike);
        textComment = findViewById(R.id.textComment);

        ParseQuery<Reel> uq = ParseQuery.getQuery(Reel.class);
        uq.getInBackground(uri, new GetCallback<Reel>() {
            public void done(Reel robj, ParseException e) {
                if (e == null) {

                    text = robj.getText();

                    hisId = robj.getUserObj().getObjectId();

                    //TextView
                    description.setLinkText(robj.getText());
                    ParseQuery<ParseUser> uq = ParseQuery.getQuery("Users");
                    uq.getInBackground(hisId, new GetCallback<ParseUser>() {
                        public void done(ParseUser uobj, ParseException e) {
                            if (e == null) {
                                name.setText("@"+uobj.getString("name"));
                                name.setOnClickListener(v -> {
                                    Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
                                    intent.putExtra("hisUID", hisId);
                                    v.getContext().startActivity(intent);
                                });
                                avatar.setOnClickListener(v -> {
                                    Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
                                    intent.putExtra("hisUID", hisId);
                                    v.getContext().startActivity(intent);
                                });
                                try{
                                    if (uobj.getParseFile("photo").isDataAvailable()){
                                        Picasso.get().load(uobj.getParseFile("photo").getUrl()).into(avatar);
                                    }
                                }catch(NullPointerException ignored){
                                }
                            } else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });

                    //Views
                    TextView views = findViewById(R.id.views);
                    ParseQuery<ReelView> rlcq = ParseQuery.getQuery(ReelView.class);
                    rlcq.whereEqualTo("reelObj", ParseObject.createWithoutData(Reel.class, reelId));
                    rlcq.countInBackground(new CountCallback() {
                        public void done(int count, ParseException e) {
                            if (e == null) {
                                if(count>0){
                                    views.setText(String.valueOf(count));
                                    views.setVisibility(View.VISIBLE);
                                } else {
                                    views.setVisibility(View.GONE);
                                }
                            }else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });

                    //more
                    createMoreBottom();
                    findViewById(R.id.more).setOnClickListener(v1 -> {
                        reel_options.show();
                    });

                    //SocialText
                    description.setOnLinkClickListener((i, s) -> {
                        if (i == 1){

                            Intent intent = new Intent(ViewReelActivity.this, SearchActivity.class);
                            intent.putExtra("hashtag", s);
                            startActivity(intent);

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
                                                    Snackbar.make(findViewById(R.id.main),"It's you", Snackbar.LENGTH_LONG).show();
                                                }else {
                                                    Intent intent = new Intent(ViewReelActivity.this, UserProfileActivity.class);
                                                    intent.putExtra("hisUID", id);
                                                    startActivity(intent);
                                                }
                                            }
                                        } else {Snackbar.make(findViewById(R.id.main),"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                                        }
                                    } else {
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

                    //Likes
                    like.setOnClickListener(v -> {
                        ParseQuery<ReelLike> rlq = ParseQuery.getQuery(ReelLike.class);
                        rlq.whereEqualTo("reelObj", ParseObject.createWithoutData(Reel.class, reelId));
                        rlq.whereEqualTo("userObj", ParseUser.getCurrentUser());
                        rlq.getFirstInBackground(new GetCallback<ReelLike>() {
                            public void done(ReelLike rlobj, ParseException e) {
                                if (e == null) {
                                    if(rlobj.isDataAvailable()){
                                        rlobj.setReelObj( ParseObject.createWithoutData(Reel.class, reelId));
                                        rlobj.setUserObj(ParseUser.getCurrentUser());
                                        rlobj.deleteInBackground();
                                        Snackbar.make(v,"Unsaved", Snackbar.LENGTH_LONG).show();
                                    } else {
                                        ReelLike rlq1 = new ReelLike();
                                        rlq1.setReelObj( ParseObject.createWithoutData(Reel.class, reelId));
                                        rlq1.setUserObj(ParseUser.getCurrentUser());
                                        rlq1.saveInBackground();
                                        Snackbar.make(v,"Saved", Snackbar.LENGTH_LONG).show();
                                    }
                                }else {
                                    Timber.d("Error: %s", e.getMessage());
                                }
                            }
                        });
                    });
                    ParseQuery<ReelLike> rlq = ParseQuery.getQuery(ReelLike.class);
                    rlq.whereEqualTo("reelObj", ParseObject.createWithoutData(Reel.class, reelId));
                    rlq.whereEqualTo("userObj", ParseUser.getCurrentUser());
                    rlq.getFirstInBackground(new GetCallback<ReelLike>() {
                        public void done(ReelLike rlobj, ParseException e) {
                            if (e == null) {
                                if(rlobj.isDataAvailable()){
                                    like_img.setImageResource(R.drawable.ic_liked_reel);
                                } else {
                                    like_img.setImageResource(R.drawable.ic_liked_reel);
                                }
                            }else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });
                    ParseQuery<ReelLike> rlq1 = ParseQuery.getQuery(ReelLike.class);
                    rlq1.whereEqualTo("reelObj", ParseObject.createWithoutData(Reel.class, reelId));
                    rlq1.countInBackground(new CountCallback() {
                        public void done(int count, ParseException e) {
                            if (e == null) {
                                if(count>0){
                                    textLike.setText(String.valueOf(count));
                                    textLike.setVisibility(View.VISIBLE);
                                } else {
                                    textLike.setVisibility(View.GONE);
                                }
                            }else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });

                    //Comment
                    ParseQuery<Comment> rlcq1 = ParseQuery.getQuery(Comment.class);
                    rlcq1.whereEqualTo("postObj", ParseObject.createWithoutData(Reel.class, reelId));
                    rlcq1.countInBackground(new CountCallback() {
                        public void done(int count, ParseException e) {
                            if (e == null) {
                                if(count>0){
                                    textComment.setText(String.valueOf(count));
                                    textComment.setVisibility(View.VISIBLE);
                                } else {
                                    textComment.setVisibility(View.GONE);
                                }
                            }else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });

                    //Video
                    videoView.setVideoPath(robj.getVideo().getUrl());
                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            findViewById(R.id.pb).setVisibility(View.GONE);
                            mp.start();
                            mp.setLooping(true);
                            ReelView rvq = new ReelView();
                            rvq.setUserObj(ParseUser.getCurrentUser());
                            rvq.setReelObj(ParseObject.createWithoutData(Reel.class, reelId));
                            rvq.saveInBackground();
                        }
                    });

                    //Comment
                    comment.setOnClickListener(v -> {
                        Intent intent = new Intent(ViewReelActivity.this, CommentActivity.class);
                        intent.putExtra("item", "0");
                        intent.putExtra("reelId", reelId);
                        intent.putExtra("type", "view");
                        startActivity(intent);
                    });

                    //Share
                    share.setOnClickListener(v1 -> {
                        share_bottom();
                        share_options.show();
                    });
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    private void createMoreBottom() {
        if (reel_options == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(ViewReelActivity.this).inflate(R.layout.reel_options, null);
            delete = view.findViewById(R.id.delete);
            download = view.findViewById(R.id.download);
            save = view.findViewById(R.id.save);
            copy = view.findViewById(R.id.copy);
            report = view.findViewById(R.id.report);
            liked = view.findViewById(R.id.liked);

            if (!hisId.equals(ParseUser.getCurrentUser())){
                delete.setVisibility(View.GONE);
            }else {
                report.setVisibility(View.GONE);
            }

            liked.setOnClickListener(v -> {
                Intent intent = new Intent(ViewReelActivity.this, ReelLikedActivity.class);
                intent.putExtra("reelId", reelId);
                startActivity(intent);
            });

            delete.setOnClickListener(v -> {
                ParseQuery<Reel> rq = ParseQuery.getQuery(Reel.class);
                rq.getInBackground(reelId, new GetCallback<Reel>() {
                    public void done(Reel robj, ParseException e) {
                        if (e == null) {
                            robj.setIsDeleted(true);
                            robj.saveInBackground();
                        } else {
                            Timber.d("Error: %s", e.getMessage());
                        }
                    }
                });
                Snackbar.make(v,"Deleted", Snackbar.LENGTH_LONG).show();
                onBackPressed();
            });

            save.setOnClickListener(v -> {

                ParseQuery<SavesReel> srq = ParseQuery.getQuery(SavesReel.class);
                srq.whereEqualTo("reelObj", ParseObject.createWithoutData(Reel.class, reelId));
                srq.whereEqualTo("userObj", ParseUser.getCurrentUser());
                srq.getFirstInBackground(new GetCallback<SavesReel>() {
                    public void done(SavesReel srobj, ParseException e) {
                        if (e == null) {
                            if(!srobj.getObjectId().isEmpty()){
                                srobj.setReelObj( ParseObject.createWithoutData(Reel.class, reelId));
                                srobj.setUserObj(ParseUser.getCurrentUser());
                                srobj.deleteInBackground();
                                Snackbar.make(v,"Unsaved", Snackbar.LENGTH_LONG).show();
                            } else {
                                SavesReel srq1 = new SavesReel();
                                srq1.setReelObj( ParseObject.createWithoutData(Reel.class, reelId));
                                srq1.setUserObj(ParseUser.getCurrentUser());
                                srq1.saveInBackground();
                                Snackbar.make(v,"Saved", Snackbar.LENGTH_LONG).show();
                            }
                        }else {
                            Timber.d("Error: %s", e.getMessage());
                        }
                    }
                });

            });

            view.findViewById(R.id.share).setOnClickListener(view1 -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, " \nWatch the reels "+"www.app.myfriend.com/reel/"+reelId);
                startActivity(Intent.createChooser(intent, "Share Via"));
            });


            download.setOnClickListener(v -> {
                Snackbar.make(v,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                DownloadManager downloadManager = (DownloadManager) v.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalFilesDir(v.getContext(), DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".mp4");
                Objects.requireNonNull(downloadManager).enqueue(request);
            });

            copy.setOnClickListener(v -> {

                ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", text+ " " +uri);
                clipboard.setPrimaryClip(clip);
                Snackbar.make(v,"Copied", Snackbar.LENGTH_LONG).show();

            });

            report.setOnClickListener(v -> {
                ReportReel rq = new ReportReel();
                rq.setReelObj( ParseObject.createWithoutData(Reel.class, reelId));
                rq.setUserObj(ParseUser.getCurrentUser());
                rq.saveInBackground();
                Snackbar.make(v,"Reported", Snackbar.LENGTH_LONG).show();
            });

            reel_options = new BottomSheetDialog(ViewReelActivity.this);
            reel_options.setContentView(view);
        }
    }

    private void share_bottom() {
        if (share_options == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(ViewReelActivity.this).inflate(R.layout.share_options, null);
            app = view.findViewById(R.id.app);
            groups = view.findViewById(R.id.groups);
            users = view.findViewById(R.id.users);

            app.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, text + " " + uri);
                 startActivity(Intent.createChooser(intent, "Share Via"));
            });

            groups.setOnClickListener(v -> {
                /*Intent intent = new Intent( ViewReelActivity.this, SendToGroupActivity.class);
                intent.putExtra("type", "reel");
                intent.putExtra("uri", uri);
                startActivity(intent);*/
            });

            users.setOnClickListener(v -> {
                Intent intent = new Intent( ViewReelActivity.this, SendToUserActivity.class);
                intent.putExtra("type", "reel");
                intent.putExtra("uri", uri);
               startActivity(intent);
            });

            share_options = new BottomSheetDialog(ViewReelActivity.this);
            share_options.setContentView(view);
        }
    }

}