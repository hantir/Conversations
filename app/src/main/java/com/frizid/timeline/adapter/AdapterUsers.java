package com.frizid.timeline.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.R;
import com.frizid.timeline.profile.UserProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    final Context context;
    final List<ParseUser> userList;

    public AdapterUsers(Context context, List<ParseUser> userList) {
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

        holder.name.setText(Objects.requireNonNull(userList.get(position).get("name")).toString());

        holder.username.setText(userList.get(position).getUsername());

        try{
            Picasso.get().load(Objects.requireNonNull(userList.get(position).getParseFile("photo")).getUrl()).into(holder.dp);
        }catch(NullPointerException ignored){
            Picasso.get().load(R.drawable.avatar).into(holder.dp);
        }

        if (userList.get(position).getBoolean("verified"))  holder.verified.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("hisUID", userList.get(position).getObjectId());
            context.startActivity(intent);
        });

        //UserInfo
        ParseQuery<ParseUser> uq = ParseUser.getQuery();
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
