package com.frizid.timeline.group;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.frizid.timeline.App;
import com.frizid.timeline.MainActivity;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.adapter.AdapterPost;
import com.frizid.timeline.model.Group;
import com.frizid.timeline.model.Notification;
import com.frizid.timeline.model.Participants;
import com.frizid.timeline.model.Post;
import com.frizid.timeline.model.ReportGroup;
import com.frizid.timeline.model.Request;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class GroupProfileActivity extends AppCompatActivity {

    String groupId,myGroupRole;
    LinearLayout link_layout;

    RecyclerView post;
    AdapterPost adapterPost;
    List<Post> modelPosts;
    TextView name;
    //Bottom
    BottomSheetDialog more_options;
    LinearLayout members,add,announcement,mEdit,mLeave,delete,addPost,report,requestJoin;

    private RequestQueue requestQueue;
    private boolean notify = false;

    boolean sendRequest = false;
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
        setContentView(R.layout.activity_group_profile);

        requestQueue = Volley.newRequestQueue(GroupProfileActivity.this);

        groupId = getIntent().getStringExtra("group");
        String type = getIntent().getStringExtra("type");

        //Post
        post = findViewById(R.id.post);
        post.setLayoutManager(new LinearLayoutManager(GroupProfileActivity.this));
        modelPosts = new ArrayList<>();
        getAllPost();

        //Back
        findViewById(R.id.back).setOnClickListener(v -> {
            if (type.equals("create")){
                Intent intent = new Intent(GroupProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else {
                onBackPressed();
            }
        });

        //PostIntent
        findViewById(R.id.create_post).setOnClickListener(v -> {
            Intent intent = new Intent(GroupProfileActivity.this, CreateGroupPostActivity.class);
            intent.putExtra("group", groupId);
            startActivity(intent);
            finish();
        });
        ParseUser currentUser = ParseUser.getCurrentUser();
        CircleImageView circleImageView = findViewById(R.id.circleImageView);
        try{
            if (currentUser.getParseFile("photo").isDataAvailable()){
                Picasso.get().load(currentUser.getParseFile("photo").getUrl()).into(circleImageView);
            }
        }catch(NullPointerException ignored){
        }

        findViewById(R.id.more).setOnClickListener(v -> more_options.show());
        findViewById(R.id.menu).setOnClickListener(v -> more_options.show());

        //Id
        TextView topName = findViewById(R.id.topName);
         name = findViewById(R.id.name);
        TextView bio = findViewById(R.id.bio);
        TextView username = findViewById(R.id.username);
        TextView link = findViewById(R.id.link);
        TextView created = findViewById(R.id.location);
        CircleImageView dp = findViewById(R.id.dp);
        ImageView cover = findViewById(R.id.cover);
        TextView members = findViewById(R.id.members);
        TextView posts = findViewById(R.id.posts);
        link_layout = findViewById(R.id.link_layout);

        //Buttons
        Button edit = findViewById(R.id.edit);
        Button request = findViewById(R.id.request);
        Button cancel = findViewById(R.id.cancel);
        Button leave = findViewById(R.id.leave);

        //GroupInfo
        ParseQuery<Group> groups = ParseQuery.getQuery(Group.class);
        groups.getInBackground(groupId, new GetCallback<Group>() {
            public void done(Group gobject, ParseException e) {
                if (e == null) {
                    topName.setText(gobject.getGUsername().toString());

                    name.setText(gobject.getGName().toString());

                    bio.setText(gobject.getGBio().toString());

                    username.setText(gobject.getGUsername().toString());

                    link.setText(gobject.getGLink().toString());

                    long lastTime = gobject.getUpdatedAt().getTime();

                    //Visibility
                    if (bio.getText().length()>0){
                        bio.setVisibility(View.VISIBLE);
                    }

                    if (link.getText().length()>0){
                        link_layout.setVisibility(View.VISIBLE);
                    }else{
                        link_layout.setVisibility(View.GONE);
                    }

                    created.setText("Created by " + gobject.getGUsername() + " - " + App.getTimeAgo(lastTime));

                    Picasso.get().load(gobject.getGIcon().toString()).into(dp);

                    //Cover
                    ParseQuery<Group> uq = ParseQuery.getQuery(Group.class);
                    uq.getInBackground(groupId, new GetCallback<Group>() {
                        public void done(Group object, ParseException e) {
                            if (e == null) {
                                if (object.getParseFile("cover").isDataAvailable()){
                                    Picasso.get().load(object.getParseFile("cover").getUrl()).into(cover);
                                }
                            } else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });

                    //Participants
                    ParseQuery<Participants> gp = ParseQuery.getQuery(Participants.class);
                    gp.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, groupId));
                    gp.findInBackground(new FindCallback<Participants>() {
                        public void done(List<Participants> gpList, ParseException e) {
                            if (e == null) {
                                if (!gpList.isEmpty()) {
                                    members.setText(String.valueOf(gpList.size()));
                                }
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                            }}
                    });


                    //Posts
                    ParseQuery<Post> gl1 = ParseQuery.getQuery(Post.class);
                    gl1.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, groupId));
                    gl1.findInBackground(new FindCallback<Post>() {
                        public void done(List<Post> gpList, ParseException e) {
                            if (e == null) {
                                if (!gpList.isEmpty()) {
                                    posts.setText(String.valueOf(gpList.size()));
                                }
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                            }}
                    });

                    findViewById(R.id.progressBar).setVisibility(View.GONE);

                    //Buttons
                    ParseQuery<Participants> gp1 = ParseQuery.getQuery(Participants.class);
                    gp1.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, groupId));
                    gp1.whereEqualTo("userObj", ParseUser.getCurrentUser());
                    gp1.getFirstInBackground(new GetCallback<Participants>() {
                        public void done(Participants gpobj, ParseException e) {
                            if (e == null) {
                                if (gpobj.isDataAvailable()) {
                                    myGroupRole = ""+gpobj.get("role");
                                }else {
                                    myGroupRole = "visitor";
                                }
                                checkUserType();
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                            }}
                    });


                    //EditProfile
                    edit.setOnClickListener(v -> {
                        Intent intent = new Intent(GroupProfileActivity.this, EditGroupActivity.class);
                        intent.putExtra("group", groupId);
                        startActivity(intent);
                        finish();
                    });

                    //Leave
                    ParseQuery<Participants> gpd = ParseQuery.getQuery(Participants.class);
                    gpd.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, groupId));
                    gpd.whereEqualTo("userObj", ParseUser.getCurrentUser());
                    gpd.getFirstInBackground(new GetCallback<Participants>() {
                        public void done(Participants gpobj, ParseException e) {
                            if (e == null) {
                                if (gpobj.isDataAvailable()) {
                                    gpobj.deleteInBackground();
                                    checkUserType();
                                }
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                            }}
                    });

                    //Request
                    request.setOnClickListener(v -> {
                        sendRequest = true;
                        Request rq = new Request();
                        rq.setUserObj(ParseUser.getCurrentUser());
                        rq.setGroupObj( ParseObject.createWithoutData(Group.class, groupId));
                        rq.saveInBackground(new SaveCallback() {
                            public void done(ParseException e) {
                                if (e == null) {
                                    sendRequest = false;
                                    checkUserType();
                                    Snackbar.make(v, "Request sent", Snackbar.LENGTH_LONG).show();
                                }else {
                                    Timber.d("Error: %s", e.getMessage());
                                }
                            }
                        });
                    });

                    //Cancel
                    cancel.setOnClickListener(v -> {ParseQuery<Request> rq = ParseQuery.getQuery(Request.class);
                        rq.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, groupId));
                        rq.whereEqualTo("userObj", ParseUser.getCurrentUser());
                        rq.getFirstInBackground(new GetCallback<Request>() {
                            public void done(Request robj, ParseException e) {
                                if (e == null) {
                                    robj.deleteInBackground();
                                    checkUserType();
                                    Snackbar.make(v, "Request canceled", Snackbar.LENGTH_LONG).show();
                                }
                                else {
                                    Timber.d("Error: %s", e.getMessage());
                                }}
                        });
                    });
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });
    }

    private void checkUserType() {
        //Admin & Creator
        switch (myGroupRole) {
            case "admin":
            case "creator":
                findViewById(R.id.edit).setVisibility(View.VISIBLE);
                findViewById(R.id.leave).setVisibility(View.GONE);
                findViewById(R.id.request).setVisibility(View.GONE);
                findViewById(R.id.cancel).setVisibility(View.GONE);
                break;
            case "participant":
                findViewById(R.id.leave).setVisibility(View.VISIBLE);
                findViewById(R.id.edit).setVisibility(View.GONE);
                findViewById(R.id.request).setVisibility(View.GONE);
                findViewById(R.id.cancel).setVisibility(View.GONE);
                break;
            case "visitor":
                checkRequest();
                break;
        }
        options();
    }


    private void checkRequest() {
        ParseQuery<Request> rq = ParseQuery.getQuery(Request.class);
        rq.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, groupId));
        rq.whereEqualTo("userObj", ParseUser.getCurrentUser());
        rq.getFirstInBackground(new GetCallback<Request>() {
            public void done(Request rqobj, ParseException e) {
                if (e == null) {
                    if (rqobj.isDataAvailable()) {
                        findViewById(R.id.cancel).setVisibility(View.VISIBLE);
                        findViewById(R.id.edit).setVisibility(View.GONE);
                        findViewById(R.id.request).setVisibility(View.GONE);
                        findViewById(R.id.leave).setVisibility(View.GONE);
                    } else {
                        findViewById(R.id.request).setVisibility(View.VISIBLE);
                        findViewById(R.id.edit).setVisibility(View.GONE);
                        findViewById(R.id.cancel).setVisibility(View.GONE);
                        findViewById(R.id.leave).setVisibility(View.GONE);
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });
    }

    private void getAllPost() {
        modelPosts.clear();
        ParseQuery<Post> rq = ParseQuery.getQuery(Post.class);
        rq.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, groupId));
        rq.findInBackground(new FindCallback<Post>() {
            public void done(List<Post> modelPosts, ParseException e) {
                if (e == null) {
                    adapterPost = new AdapterPost(GroupProfileActivity.this, modelPosts);
                    post.setAdapter(adapterPost);
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
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });
    }

    private void options() {
        if (more_options == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.group_more, null);

            members = view.findViewById(R.id.members);
            add = view.findViewById(R.id.add);
            announcement = view.findViewById(R.id.announcement);
            mEdit = view.findViewById(R.id.edit);
            mLeave = view.findViewById(R.id.leave);
            delete = view.findViewById(R.id.delete);
            addPost = view.findViewById(R.id.addPost);
            report = view.findViewById(R.id.report);
            requestJoin = view.findViewById(R.id.requestJoin);

            view.findViewById(R.id.shareurl).setOnClickListener(view1 -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, name.getText().toString() + " Group link " + "www.app.myfriend.com/group/" + groupId + "\nDownload the app "+"https://play.google.com/store/apps/details?id=com.frizid.timeline");
                startActivity(Intent.createChooser(intent, "Share Via"));
            });

            //Admin & Creator
            switch (myGroupRole) {
                case "admin":
                case "creator":
                    mLeave.setVisibility(View.GONE);
                    break;
                case "participant":
                    delete.setVisibility(View.GONE);
                    announcement.setVisibility(View.GONE);
                    mEdit.setVisibility(View.GONE);
                    add.setVisibility(View.GONE);
                    requestJoin.setVisibility(View.GONE);
                    break;
                case "visitor":
                    delete.setVisibility(View.GONE);
                    announcement.setVisibility(View.GONE);
                    mEdit.setVisibility(View.GONE);
                    mLeave.setVisibility(View.GONE);
                    add.setVisibility(View.GONE);
                    addPost.setVisibility(View.GONE);
                    requestJoin.setVisibility(View.GONE);
                    break;
            }

            requestJoin.setOnClickListener(v -> {
                more_options.cancel();
                Intent intent = new Intent(GroupProfileActivity.this, JoinRequestActivity.class);
                intent.putExtra("group", groupId);
                startActivity(intent);
            });

            announcement.setOnClickListener(v -> {
                more_options.cancel();
                findViewById(R.id.extra).setVisibility(View.VISIBLE);
            });

            findViewById(R.id.imageView4).setOnClickListener(v -> {
                more_options.cancel();
                findViewById(R.id.extra).setVisibility(View.GONE);
            });

            EditText email = findViewById(R.id.email);
            findViewById(R.id.login).setOnClickListener(v -> {
                if (email.getText().toString().isEmpty()){
                    Snackbar.make(v, "Enter a message", Snackbar.LENGTH_SHORT).show();
                }else {
                    notify = true;
                    ParseQuery<Participants> prt = ParseQuery.getQuery(Participants.class);
                    prt.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, groupId));
                    prt.findInBackground(new FindCallback<Participants>() {
                        public void done(List<Participants> prtList, ParseException e) {
                            if (e == null) {
                                if (!prtList.isEmpty()) {
                                    for (Participants ps: prtList) {
                                        Toast.makeText(GroupProfileActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                                        App.sendNotification(ps.getObjectId(), ParseUser.getCurrentUser().getUsername(), email.getText().toString());
                                        addToHisNotification(ps.getObjectId(), email.getText().toString());
                                        email.setText("");
                                        findViewById(R.id.extra).setVisibility(View.GONE);
                                    }
                                }
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                            }}

                    });
                    notify = false;
                }
            });

            members.setOnClickListener(v -> {
                more_options.cancel();
                Intent intent = new Intent(GroupProfileActivity.this, GroupMembersActivity.class);
                intent.putExtra("group", groupId);
                startActivity(intent);
                finish();
            });
            add.setOnClickListener(v -> {
                more_options.cancel();
                Intent intent = new Intent(GroupProfileActivity.this, AddGroupActivity.class);
                intent.putExtra("group", groupId);
                startActivity(intent);
                finish();
            });
            mEdit.setOnClickListener(v -> {
                more_options.cancel();
                Intent intent = new Intent(GroupProfileActivity.this, EditGroupActivity.class);
                intent.putExtra("group", groupId);
                startActivity(intent);
                finish();
            });

            mLeave.setOnClickListener(v -> {
                more_options.cancel();
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupProfileActivity.this);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to leave this group ?");
                builder.setPositiveButton("Delete", (dialog, which) -> {
                    ParseQuery<Participants> rq1 = ParseQuery.getQuery(Participants.class);
                rq1.whereEqualTo("groupObj", Participants.createWithoutData(Group.class, groupId));
                rq1.whereEqualTo("userObj", ParseUser.getCurrentUser());
                rq1.getFirstInBackground(new GetCallback<Participants>() {
                    public void done(Participants rqobj, ParseException e) {
                        if (e == null) {
                            if (rqobj.isDataAvailable()) {
                                rqobj.deleteInBackground();
                                checkUserType();
                            }
                        }
                        else {
                            Timber.d("Error: %s", e.getMessage());
                        }
                    }
                });
                }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.create().show();
            });


            delete.setOnClickListener(v -> {
                more_options.cancel();
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupProfileActivity.this);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this group ?");
                builder.setPositiveButton("Delete", (dialog, which) -> {

                    ParseObject object = ParseObject.createWithoutData(Group.class, groupId);
                    object.deleteInBackground();
                    Intent intent = new Intent(GroupProfileActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.create().show();
            });

            addPost.setOnClickListener(v -> {
                more_options.cancel();
                Intent intent = new Intent(GroupProfileActivity.this, CreateGroupPostActivity.class);
                intent.putExtra("group", groupId);
                startActivity(intent);
                finish();
            });

            report.setOnClickListener(v -> {
                more_options.cancel();
                ReportGroup gr = new ReportGroup();
                gr.setGroupObj( ParseObject.createWithoutData(Group.class, groupId));
                gr.setUserObj(ParseUser.getCurrentUser());
                gr.saveInBackground();
                Snackbar.make(v, "Reported", Snackbar.LENGTH_LONG).show();
            });

            more_options = new BottomSheetDialog(this);
            more_options.setContentView(view);
        }
    }

    private void addToHisNotification(String hisUid, String message){
        Notification notif = new Notification();
        notif.setPostObj(null);
        notif.setPUserObj( ParseObject.createWithoutData(ParseUser.class, hisUid));
        notif.setNotification( message);
        notif.setSUserObj( ParseUser.getCurrentUser());
    }
}