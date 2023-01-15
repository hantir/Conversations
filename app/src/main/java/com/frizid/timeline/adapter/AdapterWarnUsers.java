package com.frizid.timeline.adapter;

import android.app.AlertDialog;
import android.content.Context;
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
import com.android.volley.toolbox.Volley;
import com.frizid.timeline.model.CommentReaction;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.App;
import com.frizid.timeline.R;
import com.frizid.timeline.model.Comment;
import com.frizid.timeline.model.Follow;
import com.frizid.timeline.model.Group;
import com.frizid.timeline.model.Notification;
import com.frizid.timeline.model.Participants;
import com.frizid.timeline.model.Post;
import com.frizid.timeline.model.PostReaction;
import com.frizid.timeline.model.Reel;
import com.frizid.timeline.model.ReelLike;
import com.frizid.timeline.model.Saves;
import com.frizid.timeline.profile.UserProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterWarnUsers extends RecyclerView.Adapter<AdapterWarnUsers.MyHolder>{

    final Context context;
    final List<ParseUser> userList;

    private RequestQueue requestQueue;
    private boolean notify = false;

    public AdapterWarnUsers(Context context, List<ParseUser> userList) {
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

        requestQueue = Volley.newRequestQueue(context);

        holder.name.setText(userList.get(position).get("name").toString());

        holder.username.setText(userList.get(position).getUsername());

        if (!userList.get(position).getParseFile("photo").isDataAvailable()){
            Picasso.get().load(R.drawable.avatar).into(holder.dp);
        }else {
            Picasso.get().load(userList.get(position).getParseFile("photo").getUrl()).into(holder.dp);
        }

        if (userList.get(position).getBoolean("verified"))  holder.verified.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v, Gravity.END);

            popupMenu.getMenu().add(Menu.NONE,1,0, "Send warning to user");
            popupMenu.getMenu().add(Menu.NONE,2,0, "Remove from warning");
            popupMenu.getMenu().add(Menu.NONE,3,0, "View user profile");
            popupMenu.getMenu().add(Menu.NONE,4,0, "Delete user");

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == 1) {
                    Snackbar.make(v, "Warning sent", Snackbar.LENGTH_LONG).show();
                    ParseQuery<ParseUser> uq = ParseUser.getQuery();
                    uq.getInBackground(userList.get(position).getObjectId(), new GetCallback<ParseUser>() {
                        public void done(ParseUser object, ParseException e) {
                            if (e == null) {
                                object.put("warn",true);
                                object.saveInBackground();
                            } else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });
                    //Notification
                    Notification nq = new Notification();
                    nq.setPostObj(null);
                    nq.setPUserObj( userList.get(position));
                    nq.setNotification( "You have got a warning by the admin");
                    nq.setSUserObj( ParseUser.getCurrentUser());
                    nq.saveInBackground();
                    notify = true;
                    if (notify){
                        App.sendNotification(userList.get(position).getObjectId(), ParseUser.getCurrentUser().getUsername(), "You have got a warning by the admin");
                    }
                    notify = false;
                }

                if (id == 2) {
                    ParseQuery<ParseUser> uq = ParseUser.getQuery();
                    uq.getInBackground(userList.get(position).getObjectId(), new GetCallback<ParseUser>() {
                        public void done(ParseUser object, ParseException e) {
                            if (e == null) {
                                object.put("warn", null);
                                object.saveInBackground();
                                Snackbar.make(v, "Removed", Snackbar.LENGTH_LONG).show();
                                ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                                params.height = 0;
                                holder.itemView.setLayoutParams(params);
                            } else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });
                }

                if (id == 3) {
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra("hisUID", userList.get(position).getObjectId());
                    context.startActivity(intent);
                }

                if (id == 4) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete user")
                            .setMessage("Do you really want to delete this user?")
                            .setPositiveButton("Yes", (dialog, whichButton) -> {
                                dialog.dismiss();
                                Snackbar.make(v, "Please wait deleting...", Snackbar.LENGTH_LONG).show();
                                notify = true;
                                if (notify){
                                    App.sendNotification(userList.get(position).getObjectId(), ParseUser.getCurrentUser().getUsername(), "Your account has been deleted");
                                }
                                notify = false;
                                //Cover
                                ParseQuery<ParseUser> uq = ParseUser.getQuery();
                                uq.getInBackground(userList.get(position).getObjectId(), new GetCallback<ParseUser>() {
                                    public void done(ParseUser object, ParseException e) {
                                        if (e == null) {
                                            object.put("IsDeleted",true);
                                            object.saveInBackground();
                                        } else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });
                                //Followers
                                ParseQuery<Follow> fq = ParseQuery.getQuery(Follow.class);
                                fq.whereEqualTo("fromObj", userList.get(position));
                                fq.findInBackground(new FindCallback<Follow>() {
                                    public void done(List<Follow> followList, ParseException e) {
                                        if (e == null) {
                                            for (Follow follow : followList) {
                                                follow.setIsDeleted(true);
                                                follow.saveInBackground();
                                            }
                                        } else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });
                                ParseQuery<Follow> fqt = ParseQuery.getQuery(Follow.class);
                                fqt.whereEqualTo("toObj", userList.get(position));
                                fqt.findInBackground(new FindCallback<Follow>() {
                                    public void done(List<Follow> followList, ParseException e) {
                                        if (e == null) {
                                            for (Follow follow : followList) {
                                                follow.setIsDeleted(true);
                                                follow.saveInBackground();
                                            }
                                        } else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });
                                //Group
                                ParseQuery<Group> gq = ParseQuery.getQuery(Group.class);
                                gq.whereEqualTo("userObj", userList.get(position));
                                gq.findInBackground(new FindCallback<Group>() {
                                    public void done(List<Group> gList, ParseException e) {
                                        if (e == null) {
                                            for (Group group : gList) {
                                                group.setIsDeleted(true);
                                                group.saveInBackground();
                                            }
                                        } else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });
                                ParseQuery<Participants> pq = ParseQuery.getQuery(Participants.class);
                                pq.whereEqualTo("userObj", userList.get(position));
                                pq.findInBackground(new FindCallback<Participants>() {
                                    public void done(List<Participants> pList, ParseException e) {
                                        if (e == null) {
                                            for (Participants part : pList) {
                                                part.setIsDeleted(true);
                                                part.saveInBackground();
                                            }
                                        } else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });
                                //Likes
                                ParseQuery<PostReaction> lpq = ParseQuery.getQuery(PostReaction.class);
                                lpq.whereEqualTo("userObj", userList.get(position));
                                lpq.findInBackground(new FindCallback<PostReaction>() {
                                    public void done(List<PostReaction> lpList, ParseException e) {
                                        if (e == null) {
                                            for (PostReaction lp : lpList) {
                                                lp.setIsDeleted(true);
                                                lp.saveInBackground();
                                            }
                                        } else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });
                                //Posts
                                ParseQuery<Post> sq = ParseQuery.getQuery(Post.class);
                                sq.whereEqualTo("userObj", userList.get(position));
                                sq.findInBackground(new FindCallback<Post>() {
                                    public void done(List<Post> postList, ParseException e) {
                                        if (e == null) {
                                            for (Post post : postList) {
                                                post.setIsDeleted(true);
                                                post.saveInBackground();
                                            }
                                        } else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });
                                //Comment
                                ParseQuery<Comment> cq = ParseQuery.getQuery(Comment.class);
                                cq.whereEqualTo("userObj", userList.get(position));
                                cq.findInBackground(new FindCallback<Comment>() {
                                    public void done(List<Comment> cmntList, ParseException e) {
                                        if (e == null) {
                                            for (Comment comment : cmntList) {
                                                comment.setIsDeleted(true);
                                                comment.saveInBackground();
                                            }
                                        } else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });
                                //PostReaction
                                ParseQuery<PostReaction> rlq = ParseQuery.getQuery(PostReaction.class);
                                rlq.whereEqualTo("userObj", userList.get(position));
                                rlq.findInBackground(new FindCallback<PostReaction>() {
                                    public void done(List<PostReaction> rlist, ParseException e) {
                                        if (e == null) {
                                            for (PostReaction rn : rlist) {
                                                rn.setIsDeleted(true);
                                                rn.saveInBackground();
                                            }
                                        } else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });
                                //Reel
                                ParseQuery<Reel> rlq1 = ParseQuery.getQuery(Reel.class);
                                rlq1.whereEqualTo("userObj", userList.get(position));
                                rlq1.findInBackground(new FindCallback<Reel>() {
                                    public void done(List<Reel> rlist, ParseException e) {
                                        if (e == null) {
                                            for (Reel rl : rlist) {
                                                rl.setIsDeleted(true);
                                                rl.saveInBackground();
                                            }
                                        } else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });
                                //Likes
                                ParseQuery<ReelLike> clq = ParseQuery.getQuery(ReelLike.class);
                                clq.whereEqualTo("userObj", userList.get(position));
                                clq.findInBackground(new FindCallback<ReelLike>() {
                                    public void done(List<ReelLike> rlikes, ParseException e) {
                                        if (e == null) {
                                            for (ReelLike rlk : rlikes) {
                                                rlk.setIsDeleted(true);
                                                rlk.saveInBackground();
                                            }
                                        } else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });

                                //Saves
                                ParseQuery<Saves> sq1 = ParseQuery.getQuery(Saves.class);
                                sq1.whereEqualTo("userObj", userList.get(position));
                                sq1.findInBackground(new FindCallback<Saves>() {
                                    public void done(List<Saves> savesList, ParseException e) {
                                        if (e == null) {
                                            for (Saves saves : savesList) {
                                                saves.setIsDeleted(true);
                                                saves.saveInBackground();
                                            }
                                        } else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });
                                //Users
                                ParseUser.getCurrentUser().put("isDeleted",true);
                                ParseUser.getCurrentUser().saveInBackground();

                                ParseQuery<Notification> nobj = ParseQuery.getQuery(Notification.class);
                                nobj.whereEqualTo("sUserObj", userList.get(position));
                                nobj.findInBackground(new FindCallback<Notification>() {
                                    public void done(List<Notification> nuobjList, ParseException e) {
                                        if (e == null) {
                                            for (Notification nuobj : nuobjList) {
                                                nuobj.setIsDeleted(true);
                                                nuobj.saveInBackground();
                                            }
                                        } else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });
                                //cLikes
                                ParseQuery<CommentReaction> clq1 = ParseQuery.getQuery(CommentReaction.class);
                                clq1.whereEqualTo("userObj", userList.get(position));
                                clq1.findInBackground(new FindCallback<CommentReaction>() {
                                    public void done(List<CommentReaction> clikes, ParseException e) {
                                        if (e == null) {
                                            for (CommentReaction clk : clikes) {
                                                clk.setIsDeleted(true);
                                                clk.saveInBackground();
                                            }
                                        } else {
                                            Timber.d("Error: %s", e.getMessage());
                                        }
                                    }
                                });


                                ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                                params.height = 0;
                                holder.itemView.setLayoutParams(params);

                                Snackbar.make(v, "Deleted", Snackbar.LENGTH_LONG).show();

                            }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
                }

                return false;
            });
            popupMenu.show();
        });

        //UserInfo


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
