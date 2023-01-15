package com.frizid.timeline.story;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.frizid.timeline.CameraUtil;
import com.frizid.timeline.photoeditor.EditImageActivity;
import com.frizid.timeline.post.CreatePostActivity;
import com.google.android.material.snackbar.Snackbar;
import com.iceteck.silicompressorr.SiliCompressor;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.frizid.timeline.App;
import com.frizid.timeline.MainActivity;
import com.frizid.timeline.R;
import com.frizid.timeline.model.Story;
import com.frizid.timeline.send.MediaSelectActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.UUID;

import timber.log.Timber;

@SuppressWarnings("ALL")
public class AddStoryActivity extends AppCompatActivity {

    //Permission
    private static final int IMAGE_PICKER_SELECT = 1000;
    private CameraUtil cameraUtil;
    //Uri
    Uri selectedMediaUri;
    private static final int CAMERA_REQUEST_CODE = 1001;
    File file = null;

    //Id
    ImageView image;
    RelativeLayout main;
    VideoView video;

    //Strings
    String type;

    @SuppressLint("IntentReset")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_add_story);

        main = findViewById(R.id.main);
        //back
        findViewById(R.id.back).setOnClickListener(v -> {
            Intent i = new Intent(AddStoryActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        });

        //Camera
        findViewById(R.id.camera).setOnClickListener(v -> {

            cameraIntent();

            /*Intent intent = new Intent(AddStoryActivity.this, MediaSelectActivity.class);
            intent.putExtra("iType", "story");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);*/
        });

        //Gallery
        findViewById(R.id.gallery).setOnClickListener(v -> {
            @SuppressLint("IntentReset") Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/* video/*");
            startActivityForResult(pickIntent, IMAGE_PICKER_SELECT);
        });

        //Id
        image = findViewById(R.id.image);
        video = findViewById(R.id.videoView);

        //edit
        findViewById(R.id.edit).setOnClickListener(v -> {
            Intent i;
            if (type.equals("image")){
                i = new Intent(AddStoryActivity.this, AddStoryActivity.class);
            }else {
                i = new Intent(AddStoryActivity.this, AddStoryActivity.class);
            }
            i.putExtra("itype", "story");
            i.putExtra("uri", selectedMediaUri.toString());
            startActivity(i);
        });

        if (getIntent().hasExtra("type")){
            String uri = getIntent().getStringExtra("uri");
            if (getIntent().getStringExtra("type").equals("image")){
                image.setVisibility(View.VISIBLE);
                video.setVisibility(View.GONE);
                Picasso.get().load(uri).into(image);
                selectedMediaUri = Uri.parse(uri);
                type = "image";
                findViewById(R.id.edit).setVisibility(View.VISIBLE);
            }else {
                video.setVisibility(View.VISIBLE);
                image.setVisibility(View.GONE);
                findViewById(R.id.edit).setVisibility(View.VISIBLE);
                type = "video";
                video.setVideoURI(Uri.parse(uri));
                video.start();
                video.setOnPreparedListener(mp -> {
                    mp.setLooping(true);
                    mp.setVolume(0, 0);
                });
            }
        }

        //post
        findViewById(R.id.post).setOnClickListener(v -> {
            if (selectedMediaUri != null) {
                if (type.equals("image")) {
                    uploadImage();
                    Snackbar.make(v, "Please wait...", Snackbar.LENGTH_SHORT).show();
                } else if (type.equals("video")) {
                    compressVideo();
                    Snackbar.make(v, "Please wait...", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(v, "Add a image or video", Snackbar.LENGTH_SHORT).show();
                }
            }else {
                Snackbar.make(v, "Add a image or video", Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    private void cameraIntent() {
        if (cameraUtil == null) {
            cameraUtil = new CameraUtil(AddStoryActivity.this);
            cameraUtil.mCurrentPhotoPath = null;
            cameraUtil.openCamera(AddStoryActivity.this, CAMERA_REQUEST_CODE);
        }
    }

    private void compressVideo() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        new CompressVideo().execute("false",selectedMediaUri.toString(),file.getPath());
    }

    @SuppressLint("StaticFieldLeak")
    private class CompressVideo extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            String videoPath = null;
            try {
                Uri mUri = Uri.parse(strings[1]);
                videoPath = SiliCompressor.with(AddStoryActivity.this)
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
        ParseFile video1 = new ParseFile(UUID.randomUUID().toString()+mime, videoBytes);
        video1.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e==null){
                    Story sq = new Story();
                    sq.setImageObj(video1);
                    sq.setType("video");
                    sq.setUserObj(Objects.requireNonNull(ParseUser.getCurrentUser()));
                    sq.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                video.setVisibility(View.GONE);
                                image.setVisibility(View.GONE);
                                findViewById(R.id.edit).setVisibility(View.GONE);
                                type = "";

                                Snackbar.make(findViewById(R.id.main), "Story uploaded", Snackbar.LENGTH_LONG).show();
                                onBackPressed();
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadImage() {
        Bitmap file_bit = App.uriToBitmap(this, selectedMediaUri);
        String mime=".jpg";
        byte[] scaledData = App.getParseFileBytes(this, file_bit, mime);
        ParseFile image1 = new ParseFile(UUID.randomUUID().toString()+mime, scaledData);
        image1.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e==null){
                    Story sq = new Story();
                    sq.setImageObj(image1);
                    sq.setType("image");
                    sq.setUserObj(Objects.requireNonNull(ParseUser.getCurrentUser()));
                    sq.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                video.setVisibility(View.GONE);
                                image.setVisibility(View.GONE);
                                findViewById(R.id.edit).setVisibility(View.GONE);
                                type = "";

                                Snackbar.make(findViewById(R.id.main), "Story uploaded", Snackbar.LENGTH_LONG).show();
//                    onBackPressed();
                                startActivity(new Intent(AddStoryActivity.this, MainActivity.class));
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Camera
        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST_CODE){
            try {
                if (resultCode == RESULT_OK) {
                    if (cameraUtil != null && cameraUtil.mCurrentPhotoPath != null) {
                        Log.d("path", cameraUtil.mCurrentPhotoPath + " ----------- " + cameraUtil.fileName);
                        file = new File(cameraUtil.mCurrentPhotoPath);
                        new FileURI(cameraUtil.mCurrentPhotoPath, cameraUtil.fileName, AddStoryActivity.this).execute();

                    } else {
                        showSnackBar(main, "फोटो लेने का पुनः प्रयास करें ");
                    }
                } else {
                    if (cameraUtil != null)
                        cameraUtil.mCurrentPhotoPath = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (resultCode == RESULT_OK && requestCode == IMAGE_PICKER_SELECT) {
            assert data != null;
            selectedMediaUri = data.getData();
            if (selectedMediaUri.toString().contains("image")) {
                image.setVisibility(View.VISIBLE);
                video.setVisibility(View.GONE);
//                Picasso.get().load(selectedMediaUri).into(image);

                Intent intent = new Intent(AddStoryActivity.this, EditImageActivity.class);
                intent.putExtra("imageUri", selectedMediaUri.toString());
                intent.putExtra("type", "story");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                type = "image";
                findViewById(R.id.edit).setVisibility(View.VISIBLE);
            } else if (selectedMediaUri.toString().contains("video")) {

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(getApplicationContext(), selectedMediaUri);
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long timeInMilli = Long.parseLong(time);
                try {
                    retriever.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (timeInMilli > 30000){
                    Snackbar.make(findViewById(R.id.main), "Video must be of 30 seconds or less", Snackbar.LENGTH_LONG).show();
                }else {

                    video.setVisibility(View.VISIBLE);
                    image.setVisibility(View.GONE);
                    findViewById(R.id.edit).setVisibility(View.VISIBLE);
                    type = "video";
                    video.setVideoURI(selectedMediaUri);
                    video.start();
                    video.setOnPreparedListener(mp -> {
                        mp.setLooping(true);
                        mp.setVolume(0, 0);
                    });

                }

            }else {
                type = "";
                video.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
                findViewById(R.id.edit).setVisibility(View.GONE);
            }
        }else {
            type = "";
            video.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            findViewById(R.id.edit).setVisibility(View.GONE);
        }
    }


    public void showSnackBar(RelativeLayout constraintLayout, String msg) {
        Snackbar snackbar = Snackbar
                .make(constraintLayout, msg, Snackbar.LENGTH_LONG);
        snackbar.setTextColor(getResources().getColor(R.color.white));
        snackbar.setBackgroundTint(getResources().getColor(R.color.colorPrimary));
        snackbar.show();

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
            Intent intent = new Intent(AddStoryActivity.this, EditImageActivity.class);
            intent.putExtra("imageUri", dataURI.toString());
            intent.putExtra("type", "story");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

             /*Intent intent = new Intent(AddStoryActivity.this, MediaSelectActivity.class);
            intent.putExtra("iType", "story");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);*/

        }}
}