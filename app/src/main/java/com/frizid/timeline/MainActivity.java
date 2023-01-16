package com.frizid.timeline;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.frizid.timeline.photoeditor.EditImageActivity;
import com.frizid.timeline.reel.PostReelActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.fragment.HomeFragment;
import com.frizid.timeline.fragment.ProfileFragment;
import com.frizid.timeline.group.GroupProfileActivity;
import com.frizid.timeline.menu.TranslationActivity;
import com.frizid.timeline.model.Group;
import com.frizid.timeline.model.Post;
import com.frizid.timeline.model.Reel;
import com.frizid.timeline.post.CommentActivity;
import com.frizid.timeline.post.CreatePostActivity;
import com.frizid.timeline.profile.UserProfileActivity;
import com.frizid.timeline.reel.ReelActivity;
//import com.frizid.timeline.reel.VideoEditActivity;
import com.frizid.timeline.reel.ViewReelActivity;
import com.frizid.timeline.send.MediaSelectActivity;
import com.frizid.timeline.story.AddStoryActivity;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import timber.log.Timber;


@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;

    SharedMode sharedMode;
    private static final int CAMERA_REQUEST_CODE = 1001;
    private CameraUtil cameraUtil;
    File file = null;

    //Bottom
    BottomSheetDialog more;
    LinearLayout post, reel, camera, stories;

    //Permission
    private static final int VIDEO_PICK_CODE = 1002;
    private static final int PERMISSION_CODE = 1001;

    private static final int PERMISSION_REQ_ID = 22;

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    NightMode sharedPref;

    long startTime;
    long endTime;


    @Override
    protected void onStart() {
        super.onStart();
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState().equals("night")) {
            setTheme(R.style.DarkTheme);
        } else if (sharedPref.loadNightModeState().equals("dim")) {
            setTheme(R.style.DimTheme);
        } else setTheme(R.style.AppTheme);
        sharedMode = new SharedMode(this);
        if (!sharedMode.loadNightModeState().isEmpty()) {
            setApplicationLocale(sharedMode.loadNightModeState());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri uri = getIntent().getData();
        if (uri != null) {
            List<String> params = uri.getPathSegments();
            String id = params.get(params.size() - 1);
            if (uri.toString().contains("user")) {
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.getInBackground(id, new GetCallback<ParseUser>() {
                    public void done(ParseUser uobj, ParseException e) {
                        if (e == null) {
                            if (uobj.isDataAvailable()) {
                                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                                intent.putExtra("userId", id);
                                startActivity(intent);
                            }
                        } else {
                            Timber.d("Error: %s", e.getMessage());
                        }
                    }
                });
            } else if (uri.toString().contains("group")) {
                ParseQuery<Group> query = new ParseQuery(Group.class);
                query.getInBackground(id, new GetCallback<Group>() {
                    public void done(Group gobj, ParseException e) {
                        if (e == null) {
                            if (gobj.isDataAvailable()) {
                                Intent intent = new Intent(MainActivity.this, GroupProfileActivity.class);
                                intent.putExtra("groupId", id);
                                intent.putExtra("type", "");
                                startActivity(intent);
                            }
                        } else {
                            Timber.d("Error: %s", e.getMessage());
                        }
                    }
                });
            } else if (uri.toString().contains("post")) {
                ParseQuery<Post> query = new ParseQuery(Post.class);
                query.getInBackground(id, new GetCallback<Post>() {
                    public void done(Post pobj, ParseException e) {
                        if (e == null) {
                            if (pobj.isDataAvailable()) {
                                Intent intent = new Intent(MainActivity.this, CommentActivity.class);
                                intent.putExtra("postId", id);
                                startActivity(intent);
                            }
                        } else {
                            Timber.d("Error: %s", e.getMessage());
                        }
                    }
                });
            } else if (uri.toString().contains("reel")) {
                ParseQuery<Reel> query = new ParseQuery(Reel.class);
                query.getInBackground(id, new GetCallback<Reel>() {
                    public void done(Reel robj, ParseException e) {
                        if (e == null) {
                            if (robj.isDataAvailable()) {
                                Intent intent = new Intent(MainActivity.this, ViewReelActivity.class);
                                intent.putExtra("reelId", id);
                                startActivity(intent);
                            }
                        } else {
                            Timber.d("Error: %s", e.getMessage());
                        }
                    }
                });
            }
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationSelected);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();

        addPost();

        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
        }

    }

    @SuppressLint("NonConstantResourceId")
    private final BottomNavigationView.OnNavigationItemSelectedListener navigationSelected =
            item -> {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.nav_add:
                        more.show();
                        break;
                    case R.id.nav_reels:
                        Intent intent = new Intent(MainActivity.this, ReelActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_chat:
                        //selectedFragment = new ChatFragment();
                        break;
                    case R.id.nav_user:
                        selectedFragment = new ProfileFragment();
                        break;
                }
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                }
                return true;
            };

    private void addPost() {
        if (more == null) {
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.add_bottom, null);
            post = view.findViewById(R.id.post);
            post.setOnClickListener(this);
            reel = view.findViewById(R.id.reel);
            reel.setOnClickListener(this);
            stories = view.findViewById(R.id.stories);
            stories.setOnClickListener(this);
            camera = view.findViewById(R.id.camera);
            camera.setOnClickListener(this);

            view.findViewById(R.id.translation).setOnClickListener(view1 -> {

                more.cancel();
                startActivity(new Intent(MainActivity.this, TranslationActivity.class));

            });

            more = new BottomSheetDialog(this);
            more.setContentView(view);
        }
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }

        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.post:
                more.cancel();
                startActivity(new Intent(MainActivity.this, CreatePostActivity.class));
                break;
            case R.id.reel:
                more.cancel();
                selectReel();
                break;
            case R.id.meeting:
                //more.cancel();
                //startActivity(new Intent(MainActivity.this, MeetingActivity.class));
                break;
            case R.id.sell:
                more.cancel();
                //startActivity(new Intent(MainActivity.this, PostProductActivity.class));
                break;
            case R.id.stories:
                more.cancel();
                startActivity(new Intent(MainActivity.this, AddStoryActivity.class));
                break;
            case R.id.camera:
                more.cancel();
                /*Intent intent = new Intent(this, MediaSelectActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);*/
                cameraIntent();
                break;
        }
    }

    private void selectReel() {

        //Check Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERMISSION_CODE);
            } else {
                pickVideo();
            }
        } else {
            pickVideo();
        }

    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, VIDEO_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(findViewById(R.id.main), "Storage permission allowed", Snackbar.LENGTH_LONG).show();
                pickVideo();
            } else {
                Snackbar.make(findViewById(R.id.main), "Storage permission is required", Snackbar.LENGTH_LONG).show();
            }
        }

        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                showLongToast("Need permissions " + Manifest.permission.RECORD_AUDIO +
                        "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                finish();
                return;
            }

        }

    }

    private void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == VIDEO_PICK_CODE && data != null) {
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

            if (timeInMilli > 60000) {
                Snackbar.make(findViewById(R.id.main), "Video must be of 1 minutes or less", Snackbar.LENGTH_LONG).show();
            } else {
                sendVideo(video_uri);
            }
        }

        //Camera
        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST_CODE) {
            try {
                if (resultCode == Activity.RESULT_OK) {
                    if (cameraUtil != null && cameraUtil.mCurrentPhotoPath != null) {
                        Log.d("path", cameraUtil.mCurrentPhotoPath + " ----------- " + cameraUtil.fileName);
                        file = new File(cameraUtil.mCurrentPhotoPath);
                        new FileURI(cameraUtil.mCurrentPhotoPath, cameraUtil.fileName, this).execute();

                    }
                } else {
                    if (cameraUtil != null)
                        cameraUtil.mCurrentPhotoPath = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void cameraIntent() {
        if (cameraUtil == null) {
            cameraUtil = new CameraUtil(MainActivity.this);
            cameraUtil.mCurrentPhotoPath = null;
            cameraUtil.openCamera(MainActivity.this, CAMERA_REQUEST_CODE);
        }
    }

    private void sendVideo(Uri videoUri) {
        Intent intent = new Intent(MainActivity.this, PostReelActivity.class);
        intent.putExtra("uri", videoUri.toString());
        startActivity(intent);
    }

    private void setApplicationLocale(String locale) {
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(new Locale(locale.toLowerCase()));
        } else {
            config.locale = new Locale(locale.toLowerCase());
        }
        resources.updateConfiguration(config, dm);
    }

    private class FileURI extends AsyncTask<Void, Integer, String> {

        Context context;
        String path, name;
        //        File filea;
        Uri uri;

        public FileURI(String path, String name, Context context) {
            this.name = name;
            this.path = path;
            this.context = context;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before executing doInBackground
            // update your UI
            // exp; make progressbar visible
        }

        @Override
        protected String doInBackground(Void... params) {

            uri = Uri.fromFile(file);
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // back to main thread after finishing doInBackground
            // update your UI or take action after
            // exp; make progressbar gone
            try {
                passData(name, uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void passData(String name, Uri dataURI) {
            Intent intent = new Intent(MainActivity.this, EditImageActivity.class);
            intent.putExtra("imageUri", dataURI.toString());
            intent.putExtra("type", "image");
            startActivity(intent);
        }
    }
}