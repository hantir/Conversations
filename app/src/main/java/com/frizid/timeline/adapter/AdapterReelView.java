package com.frizid.timeline.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.frizid.timeline.R;
import com.frizid.timeline.model.Reel;
import com.frizid.timeline.model.ReelView;
import com.frizid.timeline.reel.ViewReelActivity;

import java.util.List;

import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterReelView extends RecyclerView.Adapter<AdapterReelView.AdapterReelHolder>{

    private final List<Reel> reels;

    public AdapterReelView(List<Reel> reels) {
        this.reels = reels;
    }

    @NonNull
    @Override
    public AdapterReelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterReelHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.reel_post_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterReelHolder holder, int position) {
        holder.setVideoData(reels.get(position));
    }


    @Override
    public int getItemCount() {
        return reels.size();
    }

    class AdapterReelHolder extends RecyclerView.ViewHolder{

        final ImageView video;
        final TextView views;

        public AdapterReelHolder(@NonNull View itemView) {
            super(itemView);

            video = itemView.findViewById(R.id.image);
            views = itemView.findViewById(R.id.views);

        }

        void setVideoData(Reel reel){

            //Views
            ParseQuery<ReelView> gq = ParseQuery.getQuery(ReelView.class);
            gq.whereEqualTo("reelObj",reel);
            gq.findInBackground(new FindCallback<ReelView>() {
                public void done(List<ReelView> rList, ParseException e) {
                    if (e == null) {
                        if (rList.size() > 0) {
                            views.setVisibility(View.VISIBLE);
                            views.setText(String.valueOf(rList.size()));}
                        else {
                            views.setVisibility(View.GONE);}

                    } else {
                        Timber.d("Error: %s", e.getMessage());
                    }
                }
            });

            //Video
            Glide.with(itemView.getContext()).asBitmap().load(reel.getVideo()).thumbnail(0.1f).into(video);

            //Click

            video.setOnClickListener(v -> {

                Intent intent = new Intent(itemView.getContext(), ViewReelActivity.class);
                intent.putExtra("id", reel.getVideo());
                itemView.getContext().startActivity(intent);

            });

        }

    }

}
