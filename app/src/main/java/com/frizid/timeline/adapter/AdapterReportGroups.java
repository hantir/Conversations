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
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.App;
import com.frizid.timeline.R;
import com.frizid.timeline.group.GroupProfileActivity;
import com.frizid.timeline.model.Group;
import com.frizid.timeline.model.Participants;
import com.frizid.timeline.model.ReportGroup;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterReportGroups extends RecyclerView.Adapter<AdapterReportGroups.MyHolder>{

    final Context context;
    final List<Group> groupsList;

    private RequestQueue requestQueue;
    private boolean notify = false;

    public AdapterReportGroups(Context context, List<Group> groupsList) {
        this.context = context;
        this.groupsList = groupsList;
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

        holder.name.setText(groupsList.get(position).getGName());

        holder.username.setText(groupsList.get(position).getGUsername());

        if (groupsList.get(position).getGIcon().getUrl().isEmpty()){
            Picasso.get().load(R.drawable.group).into(holder.dp);
        }else {
            Picasso.get().load(groupsList.get(position).getGIcon().getUrl()).into(holder.dp);
        }

        holder.itemView.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v, Gravity.END);

            popupMenu.getMenu().add(Menu.NONE,1,0, "Send warning to group");
            popupMenu.getMenu().add(Menu.NONE,2,0, "Remove from report");
            popupMenu.getMenu().add(Menu.NONE,3,0, "View group profile");

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == 1) {
                    notify = true;
                    ParseQuery<Participants> gp = ParseQuery.getQuery(Participants.class);
                    gp.whereEqualTo("groupObj", groupsList.get(position));
                    gp.whereEqualTo("role", "admin");
                    gp.whereEqualTo("member", ParseUser.getCurrentUser());
                    gp.findInBackground(new FindCallback<Participants>() {
                        public void done(List<Participants> gpList, ParseException e) {
                            if (e == null) {
                                if (notify){
                                    for (Participants po:gpList)
                                    {
                                        App.sendNotification(po.getUserObj().getObjectId(), po.get("name").toString(), "Your group got a warning by the admin");
                                    }
                                    notify = false;
                                }
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                            }}
                    });
                    ParseQuery<Group> rq1 = ParseQuery.getQuery(Group.class);
                    rq1.getInBackground(groupsList.get(position).getObjectId(), new GetCallback<Group>() {
                        public void done(Group gobj, ParseException e) {
                            if (e == null) {
                                gobj.setWarn(true);
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                            }}
                    });
                    Snackbar.make(v, "Warning sent", Snackbar.LENGTH_LONG).show();
                }

                if (id == 2) {
                    ParseQuery<ReportGroup> rqd = ParseQuery.getQuery(ReportGroup.class);
                    rqd.whereEqualTo("groupObj", groupsList.get(position));
                    rqd.getFirstInBackground(new GetCallback<ReportGroup>() {
                        public void done(ReportGroup gobj, ParseException e) {
                            if (e == null) {
                                gobj.deleteInBackground();
                            }
                            else {
                                Timber.d("Error: %s", e.getMessage());
                            }}
                    });
                    Snackbar.make(v, "Removed", Snackbar.LENGTH_LONG).show();
                    ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                    params.height = 0;
                    holder.itemView.setLayoutParams(params);
                }

                if (id == 3) {
                    Intent intent = new Intent(context, GroupProfileActivity.class);
                    intent.putExtra("group", groupsList.get(position).getObjectId());
                    intent.putExtra("type", "");
                    context.startActivity(intent);
                }

                return false;
            });
            popupMenu.show();
        });


    }


    @Override
    public int getItemCount() {
        return groupsList.size();
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
