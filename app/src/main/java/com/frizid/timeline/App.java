package com.frizid.timeline;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.frizid.timeline.model.Ads;
import com.frizid.timeline.model.BlockedUser;
import com.frizid.timeline.model.Comment;
import com.frizid.timeline.model.CommentReaction;
import com.frizid.timeline.model.CommentReply;
import com.frizid.timeline.model.Follow;
import com.frizid.timeline.model.Group;
import com.frizid.timeline.model.GroupWarn;
import com.frizid.timeline.model.High;
import com.frizid.timeline.model.HighView;
import com.frizid.timeline.model.Notification;
import com.frizid.timeline.model.Participants;
import com.frizid.timeline.model.Post;
import com.frizid.timeline.model.PostExtra;
import com.frizid.timeline.model.PostReaction;
import com.frizid.timeline.model.PostView;
import com.frizid.timeline.model.Reel;
import com.frizid.timeline.model.ReelLike;
import com.frizid.timeline.model.ReelView;
import com.frizid.timeline.model.ReportComment;
import com.frizid.timeline.model.ReportGroup;
import com.frizid.timeline.model.ReportPost;
import com.frizid.timeline.model.ReportReel;
import com.frizid.timeline.model.ReportUser;
import com.frizid.timeline.model.Request;
import com.frizid.timeline.model.Saves;
import com.frizid.timeline.model.SavesReel;
import com.frizid.timeline.model.Story;
import com.frizid.timeline.model.StoryView;
import com.frizid.timeline.model.Verification;
import com.parse.facebook.ParseFacebookUtils;
import com.parse.livequery.ParseLiveQueryClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public class App extends Application {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    static boolean state = false;

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Ads.class);
        ParseObject.registerSubclass(BlockedUser.class);
        ParseObject.registerSubclass(Comment.class);
        ParseObject.registerSubclass(CommentReaction.class);
        ParseObject.registerSubclass(CommentReply.class);
        ParseObject.registerSubclass(Follow.class);
        ParseObject.registerSubclass(Group.class);
        ParseObject.registerSubclass(GroupWarn.class);
        ParseObject.registerSubclass(High.class);
        ParseObject.registerSubclass(HighView.class);
        ParseObject.registerSubclass(Notification.class);
        ParseObject.registerSubclass(Participants.class);
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(PostExtra.class);
        ParseObject.registerSubclass(PostReaction.class);
        ParseObject.registerSubclass(PostView.class);
        ParseObject.registerSubclass(Reel.class);
        ParseObject.registerSubclass(Comment.class);
        ParseObject.registerSubclass(CommentReaction.class);
        ParseObject.registerSubclass(CommentReply.class);
        ParseObject.registerSubclass(ReelLike.class);
        ParseObject.registerSubclass(ReelView.class);
        ParseObject.registerSubclass(ReportComment.class);
        ParseObject.registerSubclass(ReportGroup.class);
        ParseObject.registerSubclass(ReportPost.class);
        ParseObject.registerSubclass(ReportPost.class);
        ParseObject.registerSubclass(ReportReel.class);
        ParseObject.registerSubclass(ReportComment.class);
        ParseObject.registerSubclass(ReportUser.class);
        ParseObject.registerSubclass(Request.class);
        ParseObject.registerSubclass(Saves.class);
        ParseObject.registerSubclass(SavesReel.class);
        ParseObject.registerSubclass(Story.class);
        ParseObject.registerSubclass(StoryView.class);
        ParseObject.registerSubclass(Verification.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.server_id))
                .clientKey(getString(R.string.server_key))
                .server(getString(R.string.server_url))
                .enableLocalDataStore()
                .build());
        ParseFacebookUtils.initialize(this);

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
        ParseInstallation.getCurrentInstallation().saveInBackground();

        ParseLiveQueryClient parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient();

    }

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    public static Bitmap getBitmap(Context context, Uri uri){

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static byte[] getParseFileBytes(Context context, Bitmap bitmap, String mime){

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(mime == ".jpg" || mime == ".jpeg")
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        else
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        byte[] scaledData = stream.toByteArray();

        return scaledData;
    }

    public static byte[] getParseFileVideoBytes(Context context, String path, String mime) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        File inputFile = new File(path);
        FileInputStream fis = new FileInputStream(inputFile);

        byte[] buf = new byte[(int)inputFile.length()];
        int n;
        while (-1 != (n = fis.read(buf)))
            baos.write(buf, 0, n);

        byte[] videoBytes = baos.toByteArray();

        return videoBytes;
    }

    public static Bitmap uriToBitmap(Context context, Uri selectedFileUri) {
        Bitmap file_bit = null;
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(selectedFileUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            file_bit = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file_bit;
    }

    public static void sendNotification(final String hisId, final String name,final String message){}
}
