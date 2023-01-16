package com.frizid.timeline.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.R;
import com.frizid.timeline.model.High;
import com.frizid.timeline.model.StoryView;
import com.frizid.timeline.story.HighViewActivity;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class AdapterHigh extends RecyclerView.Adapter<AdapterHigh.MyHolder>{

    final Context context;
    final List<High> highs;

    public AdapterHigh(Context context, List<High> highs) {
        this.context = context;
        this.highs = highs;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.story_list, parent, false);
        return new MyHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        seenStory(holder, highs.get(position).getUserObj().getObjectId());

        ParseQuery<ParseUser> uq = ParseQuery.getQuery(ParseUser.class);
        uq.getInBackground(highs.get(position).getUserObj().getObjectId(), new GetCallback<ParseUser>() {
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    try{
                        if (Objects.requireNonNull(user.getParseFile("photo")).isDataAvailable()){
                            Picasso.get().load(user.getParseFile("photo").getUrl()).into(holder.dp);
                        }
                    }catch(NullPointerException ignored){
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        if (highs.get(position).type.equals("image")){
            Picasso.get().load(highs.get(position).getFile().getUrl()).into(holder.story_photo);
            Picasso.get().load(highs.get(position).getFile().getUrl()).into(holder.story_photo_seen);
        }else {
            Glide.with(context).load(highs.get(position).getFile().getUrl()).thumbnail(0.1f).into(holder.story_photo);
            Glide.with(context).load(highs.get(position).getFile().getUrl()).thumbnail(0.1f).into(holder.story_photo_seen);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HighViewActivity.class);
            intent.putExtra("userObj", highs.get(position).getUserObj().getObjectId());
            intent.putExtra("storyObj", highs.get(position).getStoryObj().imageObj);
            context.startActivity(intent);
        });

    }

    private void seenStory(MyHolder holder, String userId) {
        ParseQuery<StoryView> pq = ParseQuery.getQuery(StoryView.class);
        pq.whereEqualTo("userObj", ParseObject.createWithoutData(ParseUser.class,userId));
        pq.whereNotEqualTo("userObj",ParseUser.getCurrentUser());
        pq.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                    if (count > 0){
                        holder.story_photo.setVisibility(View.VISIBLE);
                        holder.story_photo_seen.setVisibility(View.GONE);
                    }else {
                        holder.story_photo.setVisibility(View.GONE);
                        holder.story_photo_seen.setVisibility(View.VISIBLE);
                    }

                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });
    }


    @Override
    public int getItemCount() {
        return highs.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        public final RoundedImageView story_photo;
        public final RoundedImageView story_photo_seen;
        final CircleImageView dp;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            story_photo = itemView.findViewById(R.id.roundedImageView);
            story_photo_seen = itemView.findViewById(R.id.seen);
            dp =  itemView.findViewById(R.id.dp);
        }
    }
}
