package com.frizid.timeline.adapter;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.frizid.timeline.model.Comment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.frizid.timeline.App;
import com.frizid.timeline.R;
import com.frizid.timeline.model.Notification;
import com.frizid.timeline.model.Reel;
import com.frizid.timeline.model.ReelLike;
import com.frizid.timeline.model.ReelView;
import com.frizid.timeline.model.ReportReel;
import com.frizid.timeline.model.SavesReel;
import com.frizid.timeline.profile.UserProfileActivity;
import com.frizid.timeline.reel.ReelActivity;
import com.frizid.timeline.post.CommentActivity;
import com.frizid.timeline.search.SearchActivity;
import com.frizid.timeline.send.SendToUserActivity;
import com.frizid.timeline.who.ReelLikedActivity;
import com.squareup.picasso.Picasso;
import com.tylersuehr.socialtextview.SocialTextView;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterReel extends RecyclerView.Adapter<AdapterReel.AdapterReelHolder>{

    private final List<Reel> reels;

    BottomSheetDialog share_options,reel_options;
    LinearLayout app,groups,users,download,save,delete,copy,report,liked;

    private RequestQueue requestQueue;
    private boolean notify = false;

    public AdapterReel(List<Reel> reels) {
        this.reels = reels;
    }

    @NonNull
    @Override
    public AdapterReelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterReelHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.reel_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterReelHolder holder, int position) {
        holder.setVideoData(reels.get(position));
        holder.comment.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), CommentActivity.class);
            intent.putExtra("item", String.valueOf(position));
            intent.putExtra("id", reels.get(position).getObjectId());
            intent.putExtra("type", ReelActivity.getType());
            intent.putExtra("his", reels.get(position).getUserObj().getObjectId());
            holder.itemView.getContext().startActivity(intent);
        });

        holder.share.setOnClickListener(v1 -> {
            share_bottom(holder, position);
            share_options.show();
        });

        holder.more.setOnClickListener(v1 -> {
            more_bottom(holder, position);
            reel_options.show();
        });

        requestQueue = Volley.newRequestQueue(holder.itemView.getContext());

    }

    private void share_bottom(AdapterReelHolder holder, int position) {
        if (share_options == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.share_options, null);
            app = view.findViewById(R.id.app);
            groups = view.findViewById(R.id.groups);
            users = view.findViewById(R.id.users);

            app.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, reels.get(position).getText() + " " + reels.get(position).getVideo());
                holder.itemView.getContext().startActivity(Intent.createChooser(intent, "Share Via"));
            });

            groups.setOnClickListener(v -> {
                //Intent intent = new Intent( holder.itemView.getContext(), SendToGroupActivity.class);
                //intent.putExtra("type", "reel");
                //intent.putExtra("uri", reels.get(position).getVideo());
                //holder.itemView.getContext().startActivity(intent);
            });

            users.setOnClickListener(v -> {
                Intent intent = new Intent( holder.itemView.getContext(), SendToUserActivity.class);
                intent.putExtra("type", "reel");
                intent.putExtra("uri", reels.get(position).getVideo());
                holder.itemView.getContext().startActivity(intent);
            });

            share_options = new BottomSheetDialog(holder.itemView.getContext());
            share_options.setContentView(view);
        }
    }

    private void more_bottom(AdapterReelHolder holder, int position) {
        if (reel_options == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.reel_options, null);
            delete = view.findViewById(R.id.delete);
            download = view.findViewById(R.id.download);
            save = view.findViewById(R.id.save);
            copy = view.findViewById(R.id.copy);
            report = view.findViewById(R.id.report);
            liked = view.findViewById(R.id.liked);

            if (!reels.get(position).getUserObj().equals(ParseUser.getCurrentUser())){
                delete.setVisibility(View.GONE);
            }else {
                report.setVisibility(View.GONE);
            }

            liked.setOnClickListener(v -> {
                reel_options.cancel();
                Intent intent = new Intent(holder.itemView.getContext(), ReelLikedActivity.class);
                intent.putExtra("reelId", reels.get(position).getObjectId());
                holder.itemView.getContext().startActivity(intent);
            });

            view.findViewById(R.id.share).setOnClickListener(view1 -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, " \nWatch the reels "+"www.app.myfriend.com/reel/"+ reels.get(position).getObjectId());
                holder.itemView.getContext().startActivity(Intent.createChooser(intent, "Share Via"));
            });

            delete.setOnClickListener(v -> {
                reel_options.cancel();
                ParseQuery<Reel> uq = ParseQuery.getQuery(Reel.class);
                uq.getInBackground(reels.get(position).getObjectId(), new GetCallback<Reel>() {
                    public void done(Reel robj, ParseException e) {
                        if (e == null) {
                            robj.deleteInBackground();
                            Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();
                        }
                        else {
                            Timber.d("Error: %s", e.getMessage());
                        }}
                });
            });

            save.setOnClickListener(v -> {
                reel_options.cancel();
                ParseQuery<SavesReel> rq = ParseQuery.getQuery(SavesReel.class);
                rq.whereEqualTo("userObj", ParseUser.getCurrentUser());
                rq.whereEqualTo("reelObj", reels.get(position).getObjectId());
                rq.getFirstInBackground(new GetCallback<SavesReel>() {
                    public void done(SavesReel robj, ParseException e) {
                        if (e == null) {
                            if (robj.isDataAvailable()) {
                                robj.deleteInBackground();
                                Snackbar.make(holder.itemView,"Unsaved", Snackbar.LENGTH_LONG).show();
                            }else {
                                SavesReel srq = new SavesReel();
                                srq.setUserObj(ParseUser.getCurrentUser());
                                srq.setReelObj( reels.get(position));
                                srq.saveInBackground(new SaveCallback() {
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Snackbar.make(holder.itemView,"Saved", Snackbar.LENGTH_LONG).show();
                                        }else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });
                            }
                        }
                        else {
                            Snackbar.make(holder.itemView,e.getMessage(), Snackbar.LENGTH_LONG).show();
                            Timber.d("Error: %s", e.getMessage());
                        }}
                });

            });

            download.setOnClickListener(v -> {
                reel_options.cancel();
                Snackbar.make(holder.itemView,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                DownloadManager downloadManager = (DownloadManager) v.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(reels.get(position).getVideo().getUrl()));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalFilesDir(v.getContext(), DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".mp4");
                Objects.requireNonNull(downloadManager).enqueue(request);
            });

            copy.setOnClickListener(v -> {
                reel_options.cancel();
                ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", reels.get(position).getText() + " " + reels.get(position).getVideo());
                clipboard.setPrimaryClip(clip);
                Snackbar.make(holder.itemView,"Copied", Snackbar.LENGTH_LONG).show();

            });

            report.setOnClickListener(v -> {
                reel_options.cancel();
                ReportReel robj = new ReportReel();
                robj.setUserObj(ParseUser.getCurrentUser());
                robj.setReelObj(reels.get(position));
                robj.saveInBackground();
                Snackbar.make(holder.itemView,"Reported", Snackbar.LENGTH_LONG).show();
            });

            reel_options = new BottomSheetDialog(holder.itemView.getContext());
            reel_options.setContentView(view);
        }
    }


    @Override
    public int getItemCount() {
        return reels.size();
    }

    class AdapterReelHolder extends RecyclerView.ViewHolder{

        final VideoView videoView;
        final LinearLayout like;
        final LinearLayout comment;
        final ImageView share;
        final CircleImageView avatar;
        final TextView name;
        final TextView textLike;
        final TextView textComment;
        final SocialTextView description;
        final ImageView like_img;
        final ImageView more;
        final TextView views;

        public AdapterReelHolder(@NonNull View itemView) {
            super(itemView);

            videoView = itemView.findViewById(R.id.videoView);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            share = itemView.findViewById(R.id.share);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);
            avatar = itemView.findViewById(R.id.avatar);
            like_img  = itemView.findViewById(R.id.like_img);
            textLike = itemView.findViewById(R.id.textLike);
            textComment = itemView.findViewById(R.id.textComment);
            more = itemView.findViewById(R.id.more);
            views = itemView.findViewById(R.id.views);

        }

        void setVideoData(Reel reel){
            //TextView
            description.setLinkText(reel.getText());
            ParseQuery<ParseUser> uq = ParseUser.getQuery();
            uq.getInBackground(reel.getUserObj().getObjectId(), new GetCallback<ParseUser>() {
                public void done(ParseUser object, ParseException e) {
                    if (e == null) {
                        name.setText("@"+object.getUsername());
                        name.setOnClickListener(v -> {
                            Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
                            intent.putExtra("hisUID", reel.getUserObj().getObjectId());
                            v.getContext().startActivity(intent);
                        });
                        avatar.setOnClickListener(v -> {
                            Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
                            intent.putExtra("hisUID", reel.getUserObj().getObjectId());
                            v.getContext().startActivity(intent);
                        });
                        try{
                            if (object.getParseFile("photo").isDataAvailable()){
                                Picasso.get().load(object.get("photo").toString()).into(avatar);
                            }
                        }catch(NullPointerException ignored){
                        }
                    } else {
                        Timber.d("Error: %s", e.getMessage());
                    }
                }
            });

            //Views
            ParseQuery<ReelView> vl1 = ParseQuery.getQuery(ReelView.class);
            vl1.whereEqualTo("reelObj", reel);
            vl1.countInBackground(new CountCallback() {
                public void done(int count, ParseException e) {
                    if (e == null) {
                        if (count > 0) {
                            views.setVisibility(View.VISIBLE);
                            views.setText(String.valueOf(count));
                        } else {views.setVisibility(View.GONE);
                        }
                    }
                    else {
                        Timber.d("Error: %s", e.getMessage());
                    }}
            });

            //SocialText
            description.setOnLinkClickListener((i, s) -> {
                if (i == 1){

                    Intent intent = new Intent(itemView.getContext(), SearchActivity.class);
                    intent.putExtra("hashtag", s);
                    itemView.getContext().startActivity(intent);

                }else
                if (i == 2){
                    String username = s.replaceFirst("@","");
                    ParseQuery<ParseUser> uq1 = ParseUser.getQuery();
                    uq1.whereEqualTo("username", username.trim());
                    uq1.getFirstInBackground(new GetCallback<ParseUser>() {
                        public void done(ParseUser uobj, ParseException e) {
                            if (e == null) {
                                if (uobj.isDataAvailable()) {
                                    if (uobj.equals(ParseUser.getCurrentUser())){
                                        Snackbar.make(itemView,"It's you", Snackbar.LENGTH_LONG).show();
                                    }else {
                                        Intent intent = new Intent(itemView.getContext(), UserProfileActivity.class);
                                        intent.putExtra("hisUID", uobj.getObjectId());
                                        itemView.getContext().startActivity(intent);
                                    }
                                } else {
                                    Snackbar.make(itemView,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                                }

                            }else {
                                Snackbar.make(itemView,e.getMessage(), Snackbar.LENGTH_LONG).show();
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }

                    });
                }
                else if (i == 16){
                    if (!s.startsWith("https://") && !s.startsWith("http://")){
                        s = "http://" + s;
                    }
                    Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                    itemView.getContext().startActivity(openUrlIntent);
                }else if (i == 4){
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                    itemView.getContext().startActivity(intent);
                }else if (i == 8){
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_EMAIL, s);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    itemView.getContext().startActivity(intent);

                }
            });

            //Likes
            like.setOnClickListener(v -> {
                ParseQuery<ReelLike> rl2 = ParseQuery.getQuery(ReelLike.class);
                rl2.whereEqualTo("reelObj", reel);
                rl2.whereEqualTo("userObj", ParseUser.getCurrentUser());
                rl2.getFirstInBackground(new GetCallback<ReelLike>() {
                    public void done(ReelLike rlobj, ParseException e) {
                        if (e == null) {
                            if (rlobj.isDataAvailable()) {
                                rlobj.deleteInBackground();
                            } else {
                                ReelLike srlq = new ReelLike();
                                srlq.setUserObj(ParseUser.getCurrentUser());
                                srlq.setReelObj( reel);
                                srlq.saveInBackground(new SaveCallback() {
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            addToHisNotification(reel.getUserObj(), "Liked on your reel", reel);
                                            notify = true;
                                            if (notify){
                                                App.sendNotification(reel.getUserObj().getObjectId(), Objects.requireNonNull(ParseUser.getCurrentUser().getUsername()), "liked on your reel");
                                            }
                                            notify = false;
                                        }else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });
                            }
                        }
                        else {
                            Timber.d("Error: %s", e.getMessage());
                        }}
                });
            });
            ParseQuery<ReelLike> rl1 = ParseQuery.getQuery(ReelLike.class);
            rl1.whereEqualTo("reelObj", reel);
            rl1.whereEqualTo("userObj", ParseUser.getCurrentUser());
            rl1.countInBackground(new CountCallback() {
                public void done(int count, ParseException e) {
                    if (e == null) {
                        if (count > 0) {
                            like_img.setImageResource(R.drawable.ic_liked_reel);
                        } else {like_img.setImageResource(R.drawable.ic_reel_like);
                        }
                    }
                    else {
                        Timber.d("Error: %s", e.getMessage());
                    }}
            });
            ParseQuery<ReelLike> rl = ParseQuery.getQuery(ReelLike.class);
            rl.whereEqualTo("reelObj", reel);
            rl.countInBackground(new CountCallback() {
                public void done(int count, ParseException e) {
                    if (e == null) {
                        if (count > 0) {
                            textLike.setText(String.valueOf(count));
                            textLike.setVisibility(View.VISIBLE);
                        } else {textLike.setVisibility(View.GONE);
                        }
                    }
                    else {
                        Timber.d("Error: %s", e.getMessage());
                    }}
            });

            //Comment
            ParseQuery<Comment> rc = ParseQuery.getQuery(Comment.class);
            rc.whereEqualTo("reelObj", reel);
            rc.countInBackground(new CountCallback() {
                public void done(int count, ParseException e) {
                    if (e == null) {
                        if (count > 0) {
                            textComment.setText(String.valueOf(count));
                            textComment.setVisibility(View.VISIBLE);
                        } else {textComment.setVisibility(View.GONE);
                        }
                    }
                    else {
                        Timber.d("Error: %s", e.getMessage());
                    }}
            });

            //Video
            videoView.setVideoPath(reel.getVideo().getUrl());
            videoView.setOnPreparedListener(mp -> {
                itemView.findViewById(R.id.pb).setVisibility(View.GONE);
                mp.start();
                mp.setLooping(true);
                ReelView srlq = new ReelView();
                srlq.setUserObj(ParseUser.getCurrentUser());
                srlq.setReelObj( reel);
                srlq.saveInBackground();
            });
        }
    }

    private void addToHisNotification(ParseUser hisUid, String message, Reel reelId){
        Notification notif = new Notification();
        notif.setReelObj(reelId);
        notif.setType("reel");
        notif.setPUserObj( hisUid);
        notif.setNotification( message);
        notif.setSUserObj( ParseUser.getCurrentUser());
        notif.saveInBackground();
    }

}
