package com.frizid.timeline.send;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;

import com.frizid.timeline.MainActivity;
import com.frizid.timeline.post.CreatePostActivity;
import com.frizid.timeline.story.AddStoryActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

@SuppressWarnings("ALL")
public class MediaSelectActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1000;
    String iType;
    public long currentTime = System.currentTimeMillis();

    @SuppressLint("IntentReset")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                    1);
        } else {
            // Permission has already been granted
            initialize();
        }
        Intent intent = getIntent();
        iType = intent.getStringExtra("iType");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initialize() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        Uri uri  = Uri.parse("/storage/emulated/0/"+ "Title-"+ currentTime +".jpg");
//        cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return; // no permission
                }
                initialize();
            }
        }
    }

    public static Bitmap cropAndScale(Bitmap source, int scale) {
        int factor = source.getHeight() <= source.getWidth() ? source.getHeight() : source.getWidth();
        int longer = source.getHeight() >= source.getWidth() ? source.getHeight() : source.getWidth();
        int x = source.getHeight() >= source.getWidth() ? 0 : (longer - factor) / 2;
        int y = source.getHeight() <= source.getWidth() ? 0 : (longer - factor) / 2;
        source = Bitmap.createBitmap(source, x, y, factor, factor);
        source = Bitmap.createScaledBitmap(source, scale, scale, false);
        return source;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST) {

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), photo, "Title-"+ currentTime, null);
            Uri selectedMediaUri = Uri.parse(path);

            if (selectedMediaUri.toString().contains("image")) {
                if (iType == "story") {
                    Intent intent = new Intent(MediaSelectActivity.this, AddStoryActivity.class);
                    intent.putExtra("type", "image");
                    intent.putExtra("uri", selectedMediaUri.toString());
                    startActivity(intent);
                    finish();
                } else {

                   /*Intent intent = new Intent(MediaSelectActivity.this, EditImageActivity.class);
                    intent.putExtra("uri", selectedMediaUri.toString());
                    startActivity(intent);*/

                Intent intent = new Intent(MediaSelectActivity.this, CreatePostActivity.class);
                intent.putExtra("type", "image");
                intent.putExtra("uri", selectedMediaUri.toString());
                startActivity(intent);
                finish();

                }
            } else if (selectedMediaUri.toString().contains("video")) {
                Intent intent = new Intent(MediaSelectActivity.this, CreatePostActivity.class);
                intent.putExtra("type", "video");
                intent.putExtra("uri", selectedMediaUri.toString());
                startActivity(intent);
                finish();
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }

        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
    }
}