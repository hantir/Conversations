package com.frizid.timeline.adapter;

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
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.App;
import com.frizid.timeline.R;
import com.frizid.timeline.model.Notification;
import com.frizid.timeline.model.Reel;
import com.frizid.timeline.model.ReelView;
import com.frizid.timeline.profile.UserProfileActivity;

import org.json.JSONObject;

import java.util.List;

import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterReportReelView extends RecyclerView.Adapter<AdapterReportReelView.AdapterReelHolder>{

    private final List<Reel> reels;
    private RequestQueue requestQueue;
    private boolean notify = false;

    public AdapterReportReelView(List<Reel> reels) {
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
            ParseQuery<ReelView> rvq = ParseQuery.getQuery(ReelView.class);
            rvq.whereEqualTo("postObj",reel.getObjectId());
            rvq.findInBackground(new FindCallback<ReelView>() {
                public void done(List<ReelView> rvList, ParseException e) {
                    if (e == null) {
                        if (rvList.size()>0){
                            views.setVisibility(View.VISIBLE);
                            views.setText(String.valueOf(rvList.size()));
                        }else {
                            views.setVisibility(View.GONE);
                        }
                    } else {
                        Timber.d("Error: %s", e.getMessage());
                    }
                }
            });

            //Video
            Glide.with(itemView.getContext()).asBitmap().load(reel.getVideo()).thumbnail(0.1f).into(video);

            //Click

            video.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(itemView.getContext(), v, Gravity.END);


                popupMenu.getMenu().add(Menu.NONE,1,0, "Send warning to user");
                popupMenu.getMenu().add(Menu.NONE,2,0, "Remove from report");
                popupMenu.getMenu().add(Menu.NONE,3,0, "View user profile");
                popupMenu.getMenu().add(Menu.NONE,4,0, "Delete reel");

                popupMenu.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();

                    if (id == 1) {
                        ParseQuery<ParseUser> uq = ParseQuery.getQuery("Users");
                        uq.getInBackground(reel.getUserObj().getObjectId(), new GetCallback<ParseUser>() {
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
                        nq.setReelObj(reel);
                        nq.setPUserObj( reel.getUserObj());
                        nq.setNotification( "You have got a warning by the admin");
                        nq.setSUserObj( ParseUser.getCurrentUser());
                        nq.saveInBackground();
                        ParseUser uobj = ParseObject.createWithoutData(ParseUser.class,reel.getUserObj().getObjectId());
                        uobj.increment("count");
                        uobj.saveInBackground();
                        notify = true;
                        if (notify){
                            App.sendNotification( reel.getUserObj().getObjectId(), uobj.getString("name").toString(), "You have got a warning by the admin");
                            Snackbar.make(v, "Warning sent", Snackbar.LENGTH_LONG).show();
                        }
                        notify = false;
                    }

                    if (id == 2) {
                        ParseQuery<ParseUser> uq = ParseUser.getQuery();
                        uq.getInBackground(reel.getUserObj().getObjectId(), new GetCallback<ParseUser>() {
                            public void done(ParseUser object, ParseException e) {
                                if (e == null) {
                                    object.put("warn",JSONObject.NULL);
                                    object.saveInBackground();
                                    Snackbar.make(v, "Removed", Snackbar.LENGTH_LONG).show();
                                    ViewGroup.LayoutParams params = itemView.getLayoutParams();
                                    params.height = 0;
                                    itemView.setLayoutParams(params);
                                } else {
                                    Timber.d("Error: %s", e.getMessage());
                                }
                            }
                        });
                    }

                    if (id == 3) {
                        Intent intent = new Intent(itemView.getContext(), UserProfileActivity.class);
                        intent.putExtra("hisUID", reel.getUserObj());
                        itemView.getContext().startActivity(intent);
                    }

                    if (id == 4){
                        ParseQuery<ReelView> rvq1 = ParseQuery.getQuery(ReelView.class);
                        rvq1.whereEqualTo("postObj",reel.getObjectId());
                        rvq1.getFirstInBackground(new GetCallback<ReelView>() {
                            public void done(ReelView rvobj, ParseException e) {
                                if (e == null) {
                                    rvobj.deleteInBackground();
                                    Snackbar.make(v, "Removed", Snackbar.LENGTH_LONG).show();
                                } else {
                                    Timber.d("Error: %s", e.getMessage());
                                }
                            }
                        });
                        ViewGroup.LayoutParams params = itemView.getLayoutParams();
                        params.height = 0;
                        itemView.setLayoutParams(params);
                        notify = true;
                        ParseQuery<ParseUser> uq = ParseUser.getQuery();
                        uq.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
                            public void done(ParseUser uobj1, ParseException e) {
                                if (e == null) {
                                    if (notify){
                                        App.sendNotification( reel.getUserObj().getObjectId(), uobj1.getString("name"), "Your reel has been deleted");
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

    }
}
