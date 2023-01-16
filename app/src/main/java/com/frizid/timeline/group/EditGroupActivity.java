package com.frizid.timeline.group;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.hendraanggrian.appcompat.widget.SocialEditText;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.frizid.timeline.App;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.model.Group;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class EditGroupActivity extends AppCompatActivity implements View.OnClickListener {

    //ID
    ImageView cover,editCover,editDp;
    CircleImageView dp;
    EditText name,username,link;
    SocialEditText bio;
    ConstraintLayout main;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch mSwitch;

    //Permission
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int COVER_IMAGE_PICK_CODE = 1002;
    private static final int PERMISSION_CODE = 1001;

    //String
    String mDp;
    String id;
    String getUsername;
    boolean isThere = false;

    //Bottom
    BottomSheetDialog dp_edit,cover_edit;
    LinearLayout upload,delete,video,image,trash;

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
        setContentView(R.layout.activity_edit_group);

        id = getIntent().getStringExtra("group");

        //Declaring
        cover = findViewById(R.id.cover);
        editCover = findViewById(R.id.editCover);
        editDp = findViewById(R.id.editDp);
        dp = findViewById(R.id.dp);
        name = findViewById(R.id.name);
        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);
        link = findViewById(R.id.link);
        main = findViewById(R.id.main);
        mSwitch = findViewById(R.id.mSwitch);

        //EditImage
        editDp.setOnClickListener(v -> dp_edit.show());

        //EditCover
        editCover.setOnClickListener(v -> cover_edit.show());

        //Back
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        //GroupInfo
        ParseQuery<Group> uq = ParseQuery.getQuery(Group.class);
        uq.getInBackground(id, new GetCallback<Group>() {
            public void done(Group group, ParseException e) {
                if (e == null) {
                    name.setText(Objects.requireNonNull(group.getGName()));

                    bio.setText(Objects.requireNonNull(group.getGBio()));

                    username.setText(Objects.requireNonNull(group.getGUsername()));
                    getUsername = group.getGUsername();

                    link.setText(Objects.requireNonNull(group.getGLink()));

                    if (!(group.getGIcon() == null)) {
                        Picasso.get().load(Objects.requireNonNull(group.getGIcon().getUrl())).into(dp);
                    }

                    mDp = group.getGIcon().getUrl();

                    //Private
                    String privacy = group.getGPrivacy();
                    mSwitch.setChecked(privacy.equals("private"));

                    //Cover
                    isThere = true;
                    if (!group.getGCover().equals(null)){
                        Picasso.get().load(Objects.requireNonNull(group.getGCover().getUrl())).into(cover);
                    }

                    findViewById(R.id.progressBar).setVisibility(View.GONE);

                    //Bottom
                    edit_dp();
                    edit_cover();
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        //Privacy
        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                //private
                ParseQuery<Group> uq1 = ParseQuery.getQuery(Group.class);
                uq1.getInBackground(id, new GetCallback<Group>() {
                    public void done(Group group, ParseException e) {
                        if (e == null) {
                            group.setGPrivacy("private");
                            group.saveInBackground(new SaveCallback()
                            {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Snackbar.make(buttonView, "Set to private", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        else {
                            Timber.d("Error: %s", e.getMessage());
                        }
                    }
                });
            }else {
                //public
                ParseQuery<Group> uq2 = ParseQuery.getQuery(Group.class);
                uq2.getInBackground(id, new GetCallback<Group>() {
                    public void done(Group group, ParseException e) {
                        if (e == null) {
                            group.setGPrivacy(null);
                            group.saveInBackground(new SaveCallback()
                            {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Snackbar.make(buttonView, "Set to public", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        else {
                            Timber.d("Error: %s", e.getMessage());
                        }
                    }
                });
            }
        });

        //Save
        findViewById(R.id.signUp).setOnClickListener(v -> {

            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            String mName = name.getText().toString().trim();
            String mUsername = username.getText().toString().trim();
            String mBio = bio.getText().toString().trim();
            String mLink = link.getText().toString().trim();

            if (mName.isEmpty()){
                Snackbar.make(v, "Enter your name", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }else if (mUsername.isEmpty()){
                Snackbar.make(v, "Enter your username", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }else {
                if (!getUsername.equals(mUsername)){
                    ParseQuery<Group> uq3 = ParseQuery.getQuery(Group.class);
                    uq3.getInBackground(id, new GetCallback<Group>() {
                        public void done(Group group, ParseException e) {
                            if (e == null) {
                                if(group.getGUsername()==mUsername)
                                {
                                    Snackbar.make(v,"Username already exist, try with new one", Snackbar.LENGTH_LONG).show();
                                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                                }
                                else {
                                    group.setGName(mName);
                                    group.setGroupname(mUsername);
                                    group.setGBio(mBio);
                                    group.setGLink(mLink);
                                    group.saveInBackground(new SaveCallback()
                                    {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                Snackbar.make(v, "Saved", Snackbar.LENGTH_LONG).show();
                                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                            }
                                        }
                                    });
                                }
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                                Snackbar.make(v,e.getMessage(), Snackbar.LENGTH_LONG).show();
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                            }
                        }
                    });
                }else {
                    ParseQuery<Group> uq4 = ParseQuery.getQuery(Group.class);
                    uq4.getInBackground(id, new GetCallback<Group>() {
                        public void done(Group group, ParseException e) {
                            if (e == null) {
                                group.setGName(mName);
                                group.setGroupname(mUsername);
                                group.setGBio(mBio);
                                group.setGLink(mLink);
                                group.saveInBackground(new SaveCallback()
                                {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Snackbar.make(v, "Saved", Snackbar.LENGTH_LONG).show();
                                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                                        }
                                    }
                                });
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });
                }
            }

        });

    }

    private void edit_cover() {
        if (cover_edit == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.cover_edit, null);
            image = view.findViewById(R.id.image);
            video = view.findViewById(R.id.video);
            trash = view.findViewById(R.id.trash);
            video.setVisibility(View.GONE);
            image.setOnClickListener(this);
            LinearLayout camera = view.findViewById(R.id.camera);
            camera.setOnClickListener(v -> {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent , 130);
            });
            trash.setOnClickListener(this);
            if (!isThere){
                trash.setVisibility(View.GONE);
            }
            cover_edit = new BottomSheetDialog(this);
            cover_edit.setContentView(view);
        }
    }

    private void edit_dp() {
        if (dp_edit == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.dp_edit, null);
            upload = view.findViewById(R.id.upload);
            delete = view.findViewById(R.id.delete);
            upload.setOnClickListener(this);
            delete.setOnClickListener(this);
            LinearLayout camera = view.findViewById(R.id.camera);
            camera.setOnClickListener(v -> {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent , 120);
            });
            dp_edit = new BottomSheetDialog(this);
            dp_edit.setContentView(view);
            if (mDp.isEmpty()){
                delete.setVisibility(View.GONE);
            }
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
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
            Picasso.get().load(dp_uri).into(dp);
            dp.setVisibility(View.VISIBLE);
            byte[] scaledData = App.getParseFileBytes(this, App.getBitmap(this, dp_uri), mime);
            ParseFile image = new ParseFile(UUID.randomUUID().toString()+mime, scaledData);
            image.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e==null){
                        try {
                            uploadDp(image);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
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
            cover.setVisibility(View.VISIBLE);
            byte[] scaledData = App.getParseFileBytes(this, App.getBitmap(this, cover_uri), mime);
            ParseFile image = new ParseFile(UUID.randomUUID().toString()+mime, scaledData);
            image.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e==null){
                        try {
                            uploadCoverImage(image);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            Snackbar.make(main, "Please wait, uploading...", Snackbar.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void uploadCoverImage(ParseFile cover_uri) throws IOException {

        ParseQuery<Group> uq5 = ParseQuery.getQuery(Group.class);
        uq5.getInBackground(id, new GetCallback<Group>() {
            public void done(Group group, ParseException e) {
                if (e == null) {
                    group.setGCover(cover_uri);
                    group.saveInBackground(new SaveCallback()
                    {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Snackbar.make(main, "Cover photo updated", Snackbar.LENGTH_LONG).show();
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                            }
                        }
                    });
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    private void uploadDp(ParseFile dp_uri) throws IOException {
        ParseQuery<Group> uq6 = ParseQuery.getQuery(Group.class);
        uq6.getInBackground(id, new GetCallback<Group>() {
            public void done(Group group, ParseException e) {
                if (e == null) {
                    group.setGIcon(dp_uri);
                    group.saveInBackground(new SaveCallback()
                    {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Snackbar.make(main, "Profile photo updated", Snackbar.LENGTH_LONG).show();
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                            }
                        }
                    });
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
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

                if (!mDp.isEmpty()){
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    ParseQuery<Group> uq7 = ParseQuery.getQuery(Group.class);
                    uq7.getInBackground(id, new GetCallback<Group>() {
                        public void done(Group group, ParseException e) {
                            if (e == null) {
                                group.setGIcon(null);
                                group.saveInBackground(new SaveCallback()
                                {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Snackbar.make(main, "Profile photo deleted", Snackbar.LENGTH_LONG).show();
                                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                                        }
                                    }
                                });
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
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
            case R.id.trash:

                cover_edit.cancel();

                //Cover
                ParseQuery<Group> uq8 = ParseQuery.getQuery(Group.class);
                uq8.getInBackground(id, new GetCallback<Group>() {
                    public void done(Group group, ParseException e) {
                        if (e == null) {
                            group.setIsDeleted(true);
                            group.saveInBackground(new SaveCallback()
                            {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Snackbar.make(main, "Cover deleted", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        else {
                            Timber.d("Error: %s", e.getMessage());
                        }
                    }
                });

                break;
        }
    }


}