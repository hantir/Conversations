package com.frizid.timeline.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.R;
import com.frizid.timeline.model.Verification;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterVerification extends RecyclerView.Adapter<AdapterVerification.MyHolder>{

    final Context context;
    final List<Verification> userList;

    public AdapterVerification(Context context, List<Verification> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.verification_view, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        ParseQuery<ParseUser> uq = ParseUser.getQuery();
        uq.getInBackground(userList.get(position).getUserObj().getObjectId(), new GetCallback<ParseUser>() {
            public void done(ParseUser uobj1, ParseException e) {
                if (e == null) {
                    holder.name.setText(Objects.requireNonNull(uobj1.get("name")).toString());
                    holder.username.setText(Objects.requireNonNull(uobj1.getUsername()).toString());
                    if (!Objects.requireNonNull(uobj1.getParseFile("photo")).getUrl().isEmpty()){
                        Picasso.get().load(Objects.requireNonNull(uobj1.getParseFile("photo")).getUrl()).into(holder.dp);
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        holder.fName.setText(userList.get(position).getString("name"));
        holder.fUsername.setText(userList.get(position).getUsername());
        holder.known.setText(userList.get(position).getKnown());
        holder.govt.setText(userList.get(position).getLink());

        holder.reject.setOnClickListener(v -> {
            ParseQuery<Verification> uq1 = ParseQuery.getQuery(Verification.class);
            uq1.getInBackground(userList.get(position).getObjectId(), new GetCallback<Verification>() {
                public void done(Verification object, ParseException e) {
                    if (e == null) {
                        object.deleteInBackground();
                        Snackbar.make(v, "Rejected", Snackbar.LENGTH_LONG).show();
                        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                        params.height = 0;
                        holder.itemView.setLayoutParams(params);
                    } else {
                        Timber.d("Error: %s", e.getMessage());
                    }
                }
            });
        });

        holder.accept.setOnClickListener(v -> {
            ParseQuery<ParseUser> uq2 = ParseUser.getQuery();
            uq2.getInBackground(userList.get(position).getUserObj().getObjectId(), new GetCallback<ParseUser>() {
                public void done(ParseUser uobj1, ParseException e) {
                    if (e == null) {
                        uobj1.put("verified", true);
                    } else {
                        Timber.d("Error: %s", e.getMessage());
                    }
                }
            });
            ParseQuery<Verification> uq1 = ParseQuery.getQuery(Verification.class);
            uq1.getInBackground(userList.get(position).getObjectId(), new GetCallback<Verification>() {
                public void done(Verification object, ParseException e) {
                    if (e == null) {
                        object.deleteInBackground();
                        Snackbar.make(v, "Accepted", Snackbar.LENGTH_LONG).show();
                        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                        params.height = 0;
                        holder.itemView.setLayoutParams(params);
                    } else {
                        Timber.d("Error: %s", e.getMessage());
                    }
                }
            });
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
        final TextView fName;
        final TextView fUsername;
        final TextView known;
        final TextView govt;
        final Button accept;
        final Button reject;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
            fName = itemView.findViewById(R.id.fName);
            fUsername = itemView.findViewById(R.id.fUsername);
            known = itemView.findViewById(R.id.known);
            govt = itemView.findViewById(R.id.govt);
            accept = itemView.findViewById(R.id.accept);
            reject = itemView.findViewById(R.id.reject);

        }

    }
}
