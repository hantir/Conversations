package com.frizid.timeline.send;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.frizid.timeline.menu.MenuActivity;
import com.frizid.timeline.reel.PostReelActivity;
import com.google.android.material.snackbar.Snackbar;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.post.CreatePostActivity;

import java.io.IOException;
//import com.frizid.timeline.reel.VideoEditActivity;

public class SendMediaActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_send_media);

        //Strings
        String type =getIntent().getStringExtra("type");
        String uri =getIntent().getStringExtra("uri");

        if (type.equals("video")){
            findViewById(R.id.imageEdit).setVisibility(View.GONE);
            findViewById(R.id.videoEdit).setVisibility(View.VISIBLE);
        }else {
            findViewById(R.id.reels).setVisibility(View.GONE);
            findViewById(R.id.imageEdit).setVisibility(View.VISIBLE);
            findViewById(R.id.videoEdit).setVisibility(View.GONE);
        }


        //Back
        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.videoEdit).setOnClickListener(v -> {
            Intent intent = new Intent(SendMediaActivity.this, MenuActivity.class);
            intent.putExtra("uri", uri);
            startActivity(intent);
        });

        findViewById(R.id.imageEdit).setOnClickListener(v -> {
            Intent intent = new Intent(SendMediaActivity.this, MenuActivity.class);
            intent.putExtra("uri", uri);
            startActivity(intent);
        });

        //Post
        findViewById(R.id.post).setOnClickListener(v -> {
            if (type.equals("video")){
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(getApplicationContext(), Uri.parse(uri));
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long timeInMilli = Long.parseLong(time);
                try {
                    retriever.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (timeInMilli > 600000){
                    Snackbar.make(v, "Video must be of 10 minutes or less", Snackbar.LENGTH_LONG).show();
                }else {
                    Intent intent = new Intent(SendMediaActivity.this, CreatePostActivity.class);
                    intent.putExtra("type", type);
                    intent.putExtra("uri", uri);
                    startActivity(intent);
                }
            }else {
                Intent intent = new Intent(SendMediaActivity.this, CreatePostActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("uri", uri);
                startActivity(intent);
            }
        });

        findViewById(R.id.reels).setOnClickListener(v -> {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(SendMediaActivity.this, Uri.parse(uri));
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMilli = Long.parseLong(time);
            try {
                retriever.release();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (timeInMilli > 60000){
                Snackbar.make(v, "Video must be of 1 minutes or less", Snackbar.LENGTH_LONG).show();
            }else
            {
                Intent intent = new Intent(SendMediaActivity.this, PostReelActivity.class);
                intent.putExtra("uri", uri);
                startActivity(intent);
            }
        });
    }
}