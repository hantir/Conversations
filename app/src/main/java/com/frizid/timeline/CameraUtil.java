package com.frizid.timeline;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;

public class CameraUtil {

    public static final int MAX_FILE_SIZE_IN_KB = 60;

    Activity activity;

    public String mCurrentPhotoPath, fileName;

    public CameraUtil(Activity activity) {
        this.activity = activity;
    }

    public void openCamera(Activity activity, int requestCode) {


        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            File photo = createImageFile();
            if (photo != null) {

                Context ctx = activity.getBaseContext();
                Uri urx =FileProvider.getUriForFile(ctx,BuildConfig.APPLICATION_ID + ".fileproviderr", photo);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,urx);

                activity.startActivityForResult(takePictureIntent, requestCode);
            }
        } else {
            Toast.makeText(activity, "Image file can't be created", Toast.LENGTH_LONG).show();
        }
    }

    private File createImageFile() {
        try {

            long currentTime = System.currentTimeMillis();
            String imageFileName = "image" + "_" + currentTime;

            File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .getAbsolutePath() + "Frizid_Images");

            if (!storageDir.exists())
                storageDir.mkdirs();

            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".png",         /* suffix */
                    storageDir      /* directory */
            );
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = image.getAbsolutePath();
            fileName = image.getName();
            return image;
        } catch (IOException io) {
            return null;
        }
    }

}
