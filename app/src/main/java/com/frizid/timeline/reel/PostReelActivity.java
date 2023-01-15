package com.frizid.timeline.reel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.iceteck.silicompressorr.SiliCompressor;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.frizid.timeline.App;
import com.frizid.timeline.MainActivity;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.model.Reel;
import com.frizid.timeline.post.PrivacyPick;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.UUID;

import timber.log.Timber;

@SuppressWarnings("ALL")
public class PostReelActivity extends AppCompatActivity implements PrivacyPick.SingleChoiceListener{

    String uri;
    String privacy = "";
    String comment = "yes";
    EditText socialEditText;

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
        setContentView(R.layout.activity_post_reel);

        //back
        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        //String
        uri =getIntent().getStringExtra("uri");

        //Video
        ImageView video = findViewById(R.id.imageView3);
        Glide.with(getApplicationContext()).asBitmap().load(uri).thumbnail(0.1f).into(video);

        //Privacy
        findViewById(R.id.privacy).setOnClickListener(v -> {
            DialogFragment dialogFragment = new PrivacyPick();
            dialogFragment.setCancelable(false);
            dialogFragment.show(getSupportFragmentManager(), "Single Choice Dialog");
        });

        //Comments
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch aSwitch = findViewById(R.id.comment);
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                comment = "yes";
            }else {
                comment = "no";
            }
        });

        //Post
         socialEditText = findViewById(R.id.socialEditText);
        findViewById(R.id.post).setOnClickListener(v -> {
            if (socialEditText.getText().toString().isEmpty()){
                Snackbar.make(v,"Enter Description", Snackbar.LENGTH_SHORT).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }else {
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                compressVideo();
            }
        });

    }

    private void compressVideo() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        new CompressVideo().execute("false",uri.toString(),file.getPath());
    }

    @SuppressLint("StaticFieldLeak")
    private class CompressVideo extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            String videoPath = null;
            try {
                Uri mUri = Uri.parse(strings[1]);
                videoPath = SiliCompressor.with(PostReelActivity.this)
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
        //Upload
        String mime = "mp3";
        byte[] videoBytes = App.getParseFileVideoBytes(this, videoUri.getPath(), mime);
        ParseFile video = new ParseFile(UUID.randomUUID().toString()+mime, videoBytes);
        video.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e==null){
                    Reel reelq = new Reel();
                    reelq.setUserObj(ParseUser.getCurrentUser());
                    reelq.setText(Objects.requireNonNull(socialEditText.getText()).toString());
                    reelq.setComment(comment);
                    reelq.setVideo(video);
                    reelq.setPrivacy(privacy);
                    reelq.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                Snackbar.make(findViewById(R.id.main),"Post Uploaded", Snackbar.LENGTH_LONG).show();
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                new Handler().postDelayed(() -> {
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class );
                                    startActivity(intent);
                                    finish();
                                },200);
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                            }}
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
   public void onPositiveButtonClicked(String[] list, int position) {
      privacy = list[position];
  }

   @Override
  public void onNegativeButtonClicked() {

  }

}