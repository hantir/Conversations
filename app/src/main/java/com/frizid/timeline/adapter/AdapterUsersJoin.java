package com.frizid.timeline.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.frizid.timeline.R;
import com.frizid.timeline.group.JoinRequestActivity;
import com.frizid.timeline.model.Group;
import com.frizid.timeline.model.Participants;
import com.frizid.timeline.model.Request;
import com.frizid.timeline.profile.UserProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class AdapterUsersJoin extends RecyclerView.Adapter<AdapterUsersJoin.MyHolder>{

    final Context context;
    final List<ParseUser> userList;

    public AdapterUsersJoin(Context context, List<ParseUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.user_list_join, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        holder.name.setText(userList.get(position).get("name").toString());

        holder.username.setText(userList.get(position).getUsername());

        try{
            Picasso.get().load(userList.get(position).getParseFile("photo").getUrl()).into(holder.dp);
        }catch(NullPointerException ignored){
            Picasso.get().load(R.drawable.avatar).into(holder.dp);
        }

        if (userList.get(position).getBoolean("verified"))  holder.verified.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("hisUID", userList.get(position).getObjectId());
            context.startActivity(intent);
        });

        holder.reject.setOnClickListener(v -> {
            ParseQuery<Request> rq = ParseQuery.getQuery(Request.class);
            rq.whereEqualTo("groupId", JoinRequestActivity.getGroup());
            rq.whereEqualTo("userObj",userList.get(position).getObjectId());
            rq.getFirstInBackground(new GetCallback<Request>() {
                public void done(Request rq1, ParseException e) {
                    if (e == null) {
                        rq1.deleteInBackground();
                        Toast.makeText(context, "User rejected", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        holder.accept.setOnClickListener(v -> {
            Participants pq = new Participants();
            pq.setUserObj(userList.get(position));
            pq.setRole( "participant");
            pq.setGroupObj(ParseObject.createWithoutData(Group.class, JoinRequestActivity.getGroup()));
            pq.saveInBackground(new SaveCallback(){
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(context, "User added", Toast.LENGTH_SHORT).show();
                        ParseQuery<Request> rq = ParseQuery.getQuery(Request.class);
                        rq.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, JoinRequestActivity.getGroup()));
                        rq.whereEqualTo("userObj",userList.get(position).getObjectId());
                        rq.getFirstInBackground(new GetCallback<Request>() {
                            public void done(Request rq1, ParseException e) {
                                if (e == null) {
                                    rq1.deleteInBackground();
                                }
                            }
                        });
                    }else {
                        Timber.d("Error: %s", e.getMessage());
                    }
                }
            });
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            params.height = 0;
            holder.itemView.setLayoutParams(params);
         });

        //UserInfo
        ParseQuery<ParseUser> hvq1 = ParseQuery.getQuery(ParseUser.class);
        hvq1.getInBackground(userList.get(position).getObjectId(), new GetCallback<ParseUser>() {
            public void done(ParseUser uobj, ParseException e) {
                if (e == null) {
                    if (Objects.requireNonNull(uobj.get("status")).toString().equals("online")) holder.online.setVisibility(View.VISIBLE);
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
        final ImageView verified;
        final ImageView online;
        final TextView name;
        final TextView username;
        final Button accept;
        final Button reject;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            verified = itemView.findViewById(R.id.verified);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
            online = itemView.findViewById(R.id.imageView2);
            accept = itemView.findViewById(R.id.accept);
            reject = itemView.findViewById(R.id.reject);

        }

    }
}
