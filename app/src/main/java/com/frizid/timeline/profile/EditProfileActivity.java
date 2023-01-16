package com.frizid.timeline.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.hendraanggrian.appcompat.widget.SocialEditText;
import com.iceteck.silicompressorr.SiliCompressor;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.frizid.timeline.App;
import com.frizid.timeline.FilePath;
import com.frizid.timeline.MediaViewActivity;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    //ID
    ImageView cover,editCover,editDp;
    CircleImageView dp;
    VideoView videoView;
    EditText name,username,loc,link;
    SocialEditText bio;
    ConstraintLayout main;

    //Bottom
    BottomSheetDialog dp_edit,cover_edit;
    LinearLayout upload,delete,video,image,trash;

    //String
    ParseFile mDp;
    String getUsername;
    boolean isThere = false;

    //Permission
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int COVER_IMAGE_PICK_CODE = 1002;
    private static final int COVER_VIDEO_PICK_CODE = 1003;
    private static final int PERMISSION_CODE = 1001;

    NightMode sharedPref;

    ParseUser currentuser = ParseUser.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState().equals("night")){
            setTheme(R.style.DarkTheme);
        }else if (sharedPref.loadNightModeState().equals("dim")){
            setTheme(R.style.DimTheme);
        }else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Declaring
        cover = findViewById(R.id.cover);
        editCover = findViewById(R.id.editCover);
        editDp = findViewById(R.id.editDp);
        dp = findViewById(R.id.dp);
        videoView = findViewById(R.id.video);
        name = findViewById(R.id.name);
        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);
        loc = findViewById(R.id.location);
        link = findViewById(R.id.link);
        main = findViewById(R.id.main);

        //Back
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        //Cam
        if (ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, 99);
        }

        //Cover
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.getInBackground(currentuser.getObjectId(), new GetCallback<ParseUser>() {
            public void done(ParseUser object, ParseException e) {
                if (e == null) {
                    String mName = object.getString("name");
                    getUsername = object.getString("username");
                    String mBio = object.getString("bio");
                    String mLocation = object.getString("location");
                    String mLink = object.getString("link");

                    try {
                        Picasso.get().load(object.getParseFile("photo").getUrl()).into(dp);
                    } catch (NullPointerException e1) {
                        Picasso.get().load(R.drawable.avatar).into(dp);
                        delete.setVisibility(View.GONE);
                    }

                    name.setText(mName);
                    username.setText(getUsername);
                    loc.setText(mLocation);
                    bio.setText(mBio);
                    link.setText(mLink);

                    try {
                        String type = currentuser.getString("type");
                        String uri = currentuser.getParseFile("cover").getUrl();

                        isThere = true;

                        if (type.equals("image")){
                            Picasso.get().load(uri).placeholder(R.drawable.cover).into(cover);
                            videoView.setVisibility(View.GONE);
                            cover.setVisibility(View.VISIBLE);
                        }else if (type.equals("video")){

                            videoView.setVisibility(View.VISIBLE);
                            cover.setVisibility(View.GONE);
                            videoView.setVideoURI(Uri.parse(uri));
                            videoView.start();
                            videoView.setOnPreparedListener(mp -> {
                                mp.setLooping(true);
                                mp.setVolume(0, 0);
                            });
                            setDimension();

                            videoView.setOnClickListener(v -> {
                                Intent i = new Intent(getApplicationContext(), MediaViewActivity.class);
                                i.putExtra("type", "video");
                                i.putExtra("uri", uri);
                                startActivity(i);
                                finish();
                            });
                        }
                    }  catch (NullPointerException e1)   {
                            Picasso.get().load(R.drawable.cover).into(cover);
                            videoView.setVisibility(View.GONE);
                            cover.setVisibility(View.VISIBLE);
                            trash.setVisibility(View.GONE);
                    }
                } else {
                    Snackbar.make(main, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });

    //Save
        findViewById(R.id.signUp).setOnClickListener(v -> {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            String mName = name.getText().toString().trim();
            String mUsername = username.getText().toString().trim();
            String mBio = bio.getText().toString().trim();
            String mLink = link.getText().toString().trim();
            String mLocation = loc.getText().toString().trim();

            if (mName.isEmpty()){
                Snackbar.make(v, "Enter your name", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }else if (mUsername.isEmpty()){
                Snackbar.make(v, "Enter your username", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }else {
                if (!getUsername.equals(mUsername)){
                    userQuery.whereEqualTo("username", mUsername);
                    userQuery.findInBackground(new FindCallback<ParseUser>() {
                        public void done(List<ParseUser> results, ParseException e) {
                            if (e == null) {
                                if (!results.isEmpty()){
                                    Snackbar.make(v,"Username already exist, try with new one", Snackbar.LENGTH_LONG).show();
                                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                                }else {
                                    currentuser.put("name", mName);
                                    currentuser.put("username", mUsername);
                                    currentuser.put("bio", mBio);
                                    currentuser.put("location",mLocation);
                                    currentuser.put("link",mLink);
                                    currentuser.saveInBackground(new SaveCallback() {
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                Snackbar.make(v, "Saved", Snackbar.LENGTH_LONG).show();
                                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                            } else {
                                                Snackbar.make(main, e.getMessage(), Snackbar.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            } else {
                                Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_LONG).show();
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                            }
                        }
                    });
                }else {
                    currentuser.put("name", mName);
                    currentuser.put("username", mUsername);
                    currentuser.put("bio", mBio);
                    currentuser.put("location",mLocation);
                    currentuser.put("link",mLink);
                    currentuser.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                Snackbar.make(v, "Saved", Snackbar.LENGTH_LONG).show();
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                            } else {
                                Snackbar.make(main, e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
            onBackPressed();
        });

        //EditImage
        editDp.setOnClickListener(v -> dp_edit.show());

        //EditCover
        editCover.setOnClickListener(v -> cover_edit.show());

        //Bottom
        edit_dp();
        edit_cover();

    }

    private void edit_cover() {
        if (cover_edit == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.cover_edit, null);
            image = view.findViewById(R.id.image);
            video = view.findViewById(R.id.video);
            trash = view.findViewById(R.id.trash);
            LinearLayout camera = view.findViewById(R.id.camera);
            camera.setOnClickListener(v -> {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent , 130);
            });
            image.setOnClickListener(this);
            video.setOnClickListener(this);
            trash.setOnClickListener(this);
            cover_edit = new BottomSheetDialog(this);
            cover_edit.setContentView(view);
        }
    }

    private void edit_dp() {
        if (dp_edit == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.dp_edit, null);
            upload = view.findViewById(R.id.upload);
            delete = view.findViewById(R.id.delete);
            LinearLayout camera = view.findViewById(R.id.camera);
            camera.setOnClickListener(v -> {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent , 120);
            });
            upload.setOnClickListener(this);
            delete.setOnClickListener(this);
            dp_edit = new BottomSheetDialog(this);
            dp_edit.setContentView(view);
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private void pickCoverVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, COVER_VIDEO_PICK_CODE);
    }

    private void pickCoverImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, COVER_IMAGE_PICK_CODE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        String mime = ".jpg";
        if (resultCode == RESULT_OK && (requestCode == IMAGE_PICK_CODE || requestCode == 120) && data != null){
            Uri dp_uri = Objects.requireNonNull(data).getData();
            byte[] scaledData = App.getParseFileBytes(EditProfileActivity.this, App.getBitmap(EditProfileActivity.this, dp_uri), ".jpg");
            Picasso.get().load(dp_uri).into(dp);
            ParseFile image = new ParseFile(UUID.randomUUID().toString()+mime, scaledData);
            image.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e==null){
                        uploadDp(image);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            Snackbar.make(main, "Please wait, uploading...", Snackbar.LENGTH_LONG).show();
        }
        if (resultCode == RESULT_OK && (requestCode == 130 || requestCode == COVER_IMAGE_PICK_CODE) && data != null){
            Uri cover_uri = Objects.requireNonNull(data).getData();
            Picasso.get().load(cover_uri).into(cover);
            videoView.setVisibility(View.GONE);
            cover.setVisibility(View.VISIBLE);
            byte[] scaledData = App.getParseFileBytes(EditProfileActivity.this, App.getBitmap(EditProfileActivity.this, cover_uri), ".jpg");
            ParseFile image = new ParseFile(UUID.randomUUID().toString()+mime, scaledData);
            image.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e==null){
                        uploadCoverImage(image);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            Snackbar.make(main, "Please wait, uploading...", Snackbar.LENGTH_LONG).show();

        }
        if (resultCode == RESULT_OK && requestCode == COVER_VIDEO_PICK_CODE && data != null){

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

            if (timeInMilli > 7000){
                Snackbar.make(main, "Cover video must be of 7 seconds or less", Snackbar.LENGTH_LONG).show();
            }else {
                videoView.setVisibility(View.VISIBLE);
                cover.setVisibility(View.GONE);
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                videoView.setVideoURI(video_uri);
                videoView.start();
                videoView.setOnPreparedListener(mp -> {
                    mp.setLooping(true);
                    mp.setVolume(0, 0);
                });
                Snackbar.make(main, "Please wait, uploading...", Snackbar.LENGTH_LONG).show();
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
                new CompressVideo().execute("false",video_uri.toString(),file.getPath());
            }


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("StaticFieldLeak")
    private class CompressVideo extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... strings) {
            String videoPath = null;
            try {
                Uri mUri = Uri.parse(strings[1]);
                videoPath = SiliCompressor.with(EditProfileActivity.this)
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
            String mime=".mp4";
            byte[] videoBytes = null;
            try {
                videoBytes = App.getParseFileVideoBytes(EditProfileActivity.this, FilePath.getPath(EditProfileActivity.this, videoUri), mime);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ParseFile video = new ParseFile(UUID.randomUUID().toString()+mime, videoBytes);
            video.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e==null){
                        uploadVideo(video);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void uploadVideo(ParseFile video_file){
        currentuser.put("cover", video_file);
        currentuser.put("type", "video");
        currentuser.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Snackbar.make(main, "Cover video updated", Snackbar.LENGTH_LONG).show();
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                } else {
                    Snackbar.make(main, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void uploadCoverImage(ParseFile cover_file) {
        currentuser.put("cover", cover_file);
        currentuser.put("type", "image");
        currentuser.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Snackbar.make(main, "Cover photo updated", Snackbar.LENGTH_LONG).show();
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                } else {
                    Snackbar.make(main, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void uploadDp(ParseFile dp_file){
        currentuser.put("photo", dp_file);
        currentuser.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Snackbar.make(main, "Profile photo updated", Snackbar.LENGTH_LONG).show();
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                } else {
                    Snackbar.make(main, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.upload:

                dp_edit.cancel();

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
            case R.id.delete:

                dp_edit.cancel();

                if (currentuser.getParseFile("photo").isDataAvailable()){
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    currentuser.remove("photo");
                    currentuser.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                Snackbar.make(main, "Profile photo deleted", Snackbar.LENGTH_LONG).show();
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                            } else {
                                Snackbar.make(main, e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                break;
            case R.id.image:

                cover_edit.cancel();

                //Check Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                    else {
                       pickCoverImage();
                    }
                }
                else {
                    pickCoverImage();
                }

                break;
            case R.id.video:

                cover_edit.cancel();

                //Check Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                    else {
                        pickCoverVideo();
                    }
                }
                else {
                    pickCoverVideo();
                }

                break;

            case R.id.trash:

                cover_edit.cancel();
                ParseQuery<ParseUser> uq = ParseUser.getQuery();
                uq.getInBackground(currentuser.getObjectId(), new GetCallback<ParseUser>() {
                    public void done(ParseUser object, ParseException e) {
                        if (e == null) {
                            object.put("cover", JSONObject.NULL);
                            object.put("type", JSONObject.NULL);
                            object.saveInBackground();
                            Snackbar.make(main, "Cover deleted", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(main, e.getMessage(), Snackbar.LENGTH_LONG).show();
                            Timber.d("Error: %s", e.getMessage());
                        }
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

}