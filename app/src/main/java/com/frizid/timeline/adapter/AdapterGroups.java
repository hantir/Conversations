package com.frizid.timeline.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.R;
import com.frizid.timeline.group.GroupProfileActivity;
import com.frizid.timeline.model.Group;
import com.frizid.timeline.model.Participants;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class AdapterGroups extends RecyclerView.Adapter<AdapterGroups.MyHolder>{

    final Context context;
    final List<Group> userList;

    public AdapterGroups(Context context, List<Group> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.group_list, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        holder.name.setText(userList.get(position).getGName());

        holder.username.setText(userList.get(position).getGUsername());

        if (userList.get(position).getGIcon().getUrl().isEmpty()){
            Picasso.get().load(R.drawable.group).into(holder.dp);
        }else {
            Picasso.get().load(userList.get(position).getGIcon().getUrl()).into(holder.dp);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, GroupProfileActivity.class);
            intent.putExtra("group", userList.get(position).getObjectId());
            intent.putExtra("type", "");
            context.startActivity(intent);
        });

        //Private
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        query.getInBackground(userList.get(position).getObjectId(), new GetCallback<Group>() {
            public void done(Group object, ParseException e) {
                if (e == null) {
                        String privacy = Objects.requireNonNull(object.get("type")).toString();
                        if (privacy.equals("private")){
                            ParseQuery<Participants> pq = ParseQuery.getQuery(Participants.class);
                            pq.whereEqualTo("groupObj", userList.get(position).getObjectId());
                            pq.whereEqualTo("userObj", ParseUser.getCurrentUser());
                            pq.getFirstInBackground(new GetCallback<Participants>() {
                                public void done(Participants pobj, ParseException e) {
                                    if (e == null) {
                                        if (pobj.isDataAvailable()) {
                                            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                                            params.height = 0;
                                            holder.itemView.setLayoutParams(params);
                                        }
                                    }
                                    else {
                                        Timber.d("Error: %s", e.getMessage());
                                    }}
                            });
                        }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return userList.size();
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
