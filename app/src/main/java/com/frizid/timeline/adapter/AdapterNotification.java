package com.frizid.timeline.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.App;
import com.frizid.timeline.R;
import com.frizid.timeline.model.Notification;
import com.frizid.timeline.post.CommentActivity;
import com.frizid.timeline.profile.UserProfileActivity;
import com.frizid.timeline.reel.ViewReelActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.Holder>  {

    private final Context context;
    private final ArrayList<Notification> notifications;
    private String userId;

    public AdapterNotification(Context context, ArrayList<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_list, parent, false);
        return new Holder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        Notification modelNotification = notifications.get(position);
        String notification = modelNotification.getNotification();
        String objectId = modelNotification.getObjectId();
        long timestamp = modelNotification.getUpdatedAt().getTime();
        String senderUid = modelNotification.getSUserObj().getObjectId();
        String postId = modelNotification.getPostObj().getObjectId();

        String lastSeenTime = App.getTimeAgo(timestamp);
        holder.username.setText(notification+ " - "+ lastSeenTime);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.getInBackground(senderUid, new GetCallback<ParseUser>() {
            public void done(ParseUser uobj, ParseException e) {
                if (e == null) {
                    if (uobj.getBoolean("verified"))  holder.verified.setVisibility(View.VISIBLE);
                    holder.name.setText(Objects.requireNonNull(uobj.get("name")).toString());
                    try{
                        if (uobj.getParseFile("photo").isDataAvailable())  Picasso.get().load(uobj.getParseFile("photo").getUrl()).into(holder.circleImageView);
                    }catch(NullPointerException ignored){
                    }
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (!postId.isEmpty()){
                ParseQuery<Notification> pq = ParseQuery.getQuery(Notification.class);
                pq.getInBackground(objectId, new GetCallback<Notification>() {
                    public void done(Notification nobj, ParseException e) {
                        if (e == null) {
                            if (nobj.get("type").equals("reel")){
                                Intent intent3 = new Intent(context, ViewReelActivity.class);
                                intent3.putExtra("id", postId);
                                context.startActivity(intent3);
                            }else {
                                Intent intent = new Intent(context, CommentActivity.class);
                                intent.putExtra("postId", postId);
                                context.startActivity(intent);
                            }
                        } else {
                            Timber.d("Error: %s", e.getMessage());
                        }
                    }
                });
            }else {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("hisUID", senderUid);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
         AlertDialog.Builder builder = new AlertDialog.Builder(context);
         builder.setTitle("Delete");
         builder.setMessage("Are you sure to delete this notification?");
         builder.setPositiveButton("Delete", (dialog, which) -> {
             ParseObject pq = ParseObject.createWithoutData("Notification", objectId);
             pq.deleteInBackground(new DeleteCallback() {
                 public void done(ParseException e) {
                     if (e == null) {
                         Snackbar.make(v, "Deleted", Snackbar.LENGTH_SHORT).show();
                     } else {
                         Timber.d("Error: %s", e.getMessage());
                     }
                 }
             });
         }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
         builder.create().show();
         return false;
     });

    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class Holder extends RecyclerView.ViewHolder{

        final CircleImageView circleImageView;
        final TextView username;
        final TextView name;
        final ImageView verified;

        public Holder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.dp);
            username = itemView.findViewById(R.id.username);
            name = itemView.findViewById(R.id.name);
            verified = itemView.findViewById(R.id.verified);
        }
    }
}
