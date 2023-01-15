package com.frizid.timeline.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.App;
import com.frizid.timeline.R;
import com.frizid.timeline.model.Notification;
import com.frizid.timeline.profile.UserProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterReportUsers extends RecyclerView.Adapter<AdapterReportUsers.MyHolder>{

    final Context context;
    final List<ParseUser> userList;

    private RequestQueue requestQueue;
    private boolean notify = false;

    public AdapterReportUsers(Context context, List<ParseUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.user_list, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        requestQueue = Volley.newRequestQueue(context);

        holder.name.setText(userList.get(position).get("name").toString());

        holder.username.setText(userList.get(position).getUsername());

        try{
            Picasso.get().load(userList.get(position).getParseFile("photo").getUrl()).into(holder.dp);
        }catch(NullPointerException ignored){
            Picasso.get().load(R.drawable.avatar).into(holder.dp);
        }

        if (userList.get(position).getBoolean("verified"))  holder.verified.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v, Gravity.END);

            popupMenu.getMenu().add(Menu.NONE,1,0, "Send warning to user");
            popupMenu.getMenu().add(Menu.NONE,2,0, "Remove from report");
            popupMenu.getMenu().add(Menu.NONE,3,0, "View user profile");

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == 1) {
                    Snackbar.make(v, "Warning sent", Snackbar.LENGTH_LONG).show();
                    ParseQuery<ParseUser> uq = ParseUser.getQuery();
                    uq.getInBackground(userList.get(position).getObjectId(), new GetCallback<ParseUser>() {
                        public void done(ParseUser object, ParseException e) {
                            if (e == null) {
                                object.put("warn", true);
                                object.saveInBackground();
                            } else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });
                    //Notification
                    Notification nq = new Notification();
                    nq.setPostObj(null);
                    nq.setPUserObj( userList.get(position));
                    nq.setNotification( "You have got a warning by the admin");
                    nq.setSUserObj( ParseUser.getCurrentUser());
                    nq.saveInBackground();
                    notify = true;
                    if (notify){
                        App.sendNotification(userList.get(position).getObjectId(), ParseUser.getCurrentUser().getString("name"), "You have got a warning by the admin");
                    }
                    notify = false;
                }

                if (id == 2) {
                    ParseQuery<ParseUser> uq = ParseUser.getQuery();
                    uq.getInBackground(userList.get(position).getObjectId(), new GetCallback<ParseUser>() {
                        public void done(ParseUser object, ParseException e) {
                            if (e == null) {
                                object.put("warn", null);
                                object.saveInBackground();
                                Snackbar.make(v, "Removed", Snackbar.LENGTH_LONG).show();
                                ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                                params.height = 0;
                                holder.itemView.setLayoutParams(params);
                            } else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });
                }

                if (id == 3) {
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra("hisUID", userList.get(position).getObjectId());
                    context.startActivity(intent);
                }

                return false;
            });
            popupMenu.show();
        });

        //UserInfo
        ParseQuery<ParseUser> uq = ParseQuery.getQuery(ParseUser.class);
        uq.getInBackground(userList.get(position).getObjectId(), new GetCallback<ParseUser>() {
            public void done(ParseUser uobj, ParseException e) {
                if (e == null) {
                    //Time
                    if (Objects.equals(uobj.get("status"), "online")) holder.online.setVisibility(View.VISIBLE);
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView dp;
        final ImageView verified;
        final ImageView online;
        final TextView name;
        final TextView username;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            verified = itemView.findViewById(R.id.verified);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
            online = itemView.findViewById(R.id.imageView2);
        }

    }
}
