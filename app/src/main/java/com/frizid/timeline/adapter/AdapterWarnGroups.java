package com.frizid.timeline.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.frizid.timeline.App;
import com.frizid.timeline.R;
import com.frizid.timeline.group.GroupProfileActivity;
import com.frizid.timeline.model.Group;
import com.frizid.timeline.model.GroupWarn;
import com.frizid.timeline.model.Participants;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterWarnGroups extends RecyclerView.Adapter<AdapterWarnGroups.MyHolder>{

    final Context context;
    final List<Group> groupList;

    private RequestQueue requestQueue;
    private boolean notify = false;

    public AdapterWarnGroups(Context context, List<Group> groupList) {
        this.context = context;
        this.groupList = groupList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.group_list, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        requestQueue = Volley.newRequestQueue(context);

        holder.name.setText(groupList.get(position).getGName());

        holder.username.setText(groupList.get(position).getGUsername());

        if (groupList.get(position).getGIcon().getUrl().isEmpty()){
            Picasso.get().load(R.drawable.group).into(holder.dp);
        }else {
            Picasso.get().load(groupList.get(position).getGIcon().getUrl()).into(holder.dp);
        }

        holder.itemView.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v, Gravity.END);

            popupMenu.getMenu().add(Menu.NONE,1,0, "Send warning to group");
            popupMenu.getMenu().add(Menu.NONE,2,0, "Remove from warning");
            popupMenu.getMenu().add(Menu.NONE,3,0, "View group profile");
            popupMenu.getMenu().add(Menu.NONE,4,0, "Delete group");

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == 1) {
                    notify = true;
                    ParseQuery<Participants> pq = ParseQuery.getQuery(Participants.class);
                    pq.whereEqualTo("groupObj", groupList.get(position).getObjectId());
                    pq.findInBackground(new FindCallback<Participants>() {
                        public void done(List<Participants> uList, ParseException e) {
                            if (e == null) {
                                for(Participants pu :uList){
                                    App.sendNotification(pu.getUserObj().getObjectId(), ParseUser.getCurrentUser().getUsername(), "Your group got a warning by the admin");
                                }
                                notify = false;
                            } else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });
                    GroupWarn wq = new GroupWarn();
                    wq.setGroupObj( groupList.get(position));
                    wq.setValue( true);
                    wq.setIsDeleted( false);
                    wq.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                Snackbar.make(v, "Warning sent", Snackbar.LENGTH_LONG).show();
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });
                }

                if (id == 2) {
                    GroupWarn wq = new GroupWarn();
                    wq.setGroupObj( groupList.get(position));
                    wq.setValue( false);
                    wq.setIsDeleted( false);
                    wq.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                Snackbar.make(v, "Removed", Snackbar.LENGTH_LONG).show();
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });
                    ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                    params.height = 0;
                    holder.itemView.setLayoutParams(params);
                }

                if (id == 3) {
                    Intent intent = new Intent(context, GroupProfileActivity.class);
                    intent.putExtra("group", groupList.get(position).getObjectId());
                    intent.putExtra("type", "");
                    context.startActivity(intent);
                }

                if (id == 4) {
                    Group gr = ParseObject.createWithoutData(Group.class, groupList.get(position).getObjectId());
                    gr.setIsDeleted(true);
                    gr.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                Snackbar.make(v, "Removed", Snackbar.LENGTH_LONG).show();
                                ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                                params.height = 0;
                                holder.itemView.setLayoutParams(params);
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });

                    notify = true;
                    ParseQuery<Participants> pq = ParseQuery.getQuery(Participants.class);
                    pq.whereEqualTo("groupObj", groupList.get(position).getObjectId());
                    pq.findInBackground(new FindCallback<Participants>() {
                        public void done(List<Participants> uList, ParseException e) {
                            if (e == null) {
                                for(Participants pu :uList){
                                    App.sendNotification(pu.getUserObj().getObjectId(), ParseUser.getCurrentUser().getUsername(), "Your group has been deleted");
                                }
                                notify = false;
                            } else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });
                }

                return false;
            });
            popupMenu.show();
        });


    }


    @Override
    public int getItemCount() {
        return groupList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView dp;
        final TextView name;
        final TextView username;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
        }

    }
}
