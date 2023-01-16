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
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.R;
import com.frizid.timeline.model.Story;
import com.frizid.timeline.model.StoryView;
import com.frizid.timeline.story.StoryViewActivity;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class AdapterStory extends RecyclerView.Adapter<AdapterStory.ViewHolder> {

    private final Context context;
    private final List<Story> storyList;

    public AdapterStory(Context context, List<Story> storyList) {
        this.context = context;
        this.storyList = storyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.story_list, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Story story = storyList.get(position);
        userInfo(viewHolder, story.getUserObj().getObjectId());

        seenStory(viewHolder, story.getUserObj().getObjectId());
        viewHolder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StoryViewActivity.class);
            intent.putExtra("userId", story.getUserObj().getObjectId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final RoundedImageView story_photo;
        public final RoundedImageView story_photo_seen;
        final CircleImageView dp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            story_photo = itemView.findViewById(R.id.roundedImageView);
            story_photo_seen = itemView.findViewById(R.id.seen);
            dp = itemView.findViewById(R.id.dp);
        }
    }

    private void userInfo(ViewHolder viewHolder, String userId) {
        try {
            Picasso.get().load(ParseUser.getCurrentUser().getParseFile("photo").getUrl()).into(viewHolder.dp);
        } catch(NullPointerException ignored){

        } finally{
            Picasso.get().load(R.drawable.avatar).into(viewHolder.dp);
        }


        ParseQuery<ParseUser> uq = ParseUser.getQuery();
        uq.getInBackground(userId, new GetCallback<ParseUser>() {
            public void done(ParseUser object, ParseException e) {
                if (e == null) {
                    String type = Objects.requireNonNull(object.get("type")).toString();
                    ParseFile coverFile = object.getParseFile("cover");
                    if (type.equals("image")) {
                        assert coverFile != null;
                        Glide.with(context).load(coverFile.getUrl()).into(viewHolder.story_photo);
                        Glide.with(context).load(coverFile.getUrl()).into(viewHolder.story_photo_seen);
                    } else if (type.equals("video")) {
                        assert coverFile != null;
                        Glide.with(context).load(coverFile.getUrl()).thumbnail(0.1f).into(viewHolder.story_photo);
                        Glide.with(context).load(coverFile.getUrl()).thumbnail(0.1f).into(viewHolder.story_photo_seen);
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    private void seenStory(ViewHolder viewHolder, String userId) {
        ParseQuery<Story> vl1 = ParseQuery.getQuery(Story.class);
        vl1.whereEqualTo("userObj", ParseObject.createWithoutData(ParseUser.class, userId));
        vl1.orderByDescending("updatedAt");
        vl1.getFirstInBackground(new GetCallback<Story>() {
            public void done(Story storyObj, ParseException e) {
                if (e == null) {
                    if (storyObj.isDataAvailable()) {
                        storyObj.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                            public void done(ParseObject object, ParseException e) {
                                ParseFile imageObj = storyObj.getImageObj();
                                Glide.with(context).load(imageObj.getUrl()).into(viewHolder.story_photo);
                                Glide.with(context).load(imageObj.getUrl()).into(viewHolder.story_photo_seen);
                            }
                        });
                        ParseQuery<StoryView> vl1 = ParseQuery.getQuery(StoryView.class);
                        vl1.whereEqualTo("storyObj", storyObj);
                        vl1.getFirstInBackground(new GetCallback<StoryView>() {
                            public void done(StoryView svobj, ParseException e) {
                                if (e == null) {
                                    if (svobj.isDataAvailable()) {
                                        if (!svobj.getUserObj().equals(Objects.requireNonNull(ParseUser.getCurrentUser()))
                                                && System.currentTimeMillis() < (int) storyObj.getCreatedAt().getTime()) {
                                            viewHolder.story_photo.setVisibility(View.VISIBLE);
                                            viewHolder.story_photo_seen.setVisibility(View.GONE);
                                        }
                                    }else {
                                        viewHolder.story_photo.setVisibility(View.GONE);
                                        viewHolder.story_photo_seen.setVisibility(View.VISIBLE);
                                    }
                                }
                                else {
                                    Timber.d("Error: %s", e.getMessage());
                                }
                            }
                        });
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }
}