package com.frizid.timeline.adapter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.frizid.timeline.model.Comment;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.App;
import com.frizid.timeline.NightMode;
import com.frizid.timeline.R;
import com.frizid.timeline.model.CommentReaction;
import com.frizid.timeline.model.ReportComment;
import com.frizid.timeline.profile.UserProfileActivity;
import com.frizid.timeline.search.SearchActivity;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.tylersuehr.socialtextview.SocialTextView;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings({"ALL", "unused"})
public class AdapterCommentReel extends RecyclerView.Adapter<AdapterCommentReel.MyHolder>{

    final Context context;
    final List<Comment> modelComments;
    NightMode nightMode;
    ParseUser currentUser = ParseUser.getCurrentUser();

    @SuppressWarnings("unused")
    public AdapterCommentReel(Context context, List<Comment> modelComments) {
        this.context = context;
        this.modelComments = modelComments;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        nightMode = new NightMode(context);
        if (nightMode.loadNightModeState().equals("night")){
            View view = LayoutInflater.from(context).inflate(R.layout.comment_list_night, parent, false);   return new MyHolder(view);
        }else if (nightMode.loadNightModeState().equals("dim")){
            View view = LayoutInflater.from(context).inflate(R.layout.comment_list_dim, parent, false);   return new MyHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.comment_list, parent, false);   return new MyHolder(view);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //UserInfo
        ParseQuery<ParseUser> userquery = ParseUser.getQuery();
        userquery.getInBackground(modelComments.get(position).getUserObj().getObjectId(), new GetCallback<ParseUser>() {
            public void done(ParseUser object, ParseException e) {
                holder.name.setText(object.getString("name"));
                try{
                    if (object.getParseFile("photo").isDataAvailable()) Picasso.get().load(object.getParseFile("photo").getUrl()).into(holder.dp);
                }catch(NullPointerException ignored){
            }
                if (object.getBoolean("verified")) holder.verified.setVisibility(View.VISIBLE);

                //SetOnClick
                holder.dp.setOnClickListener(v -> {
                    if (modelComments.get(position).getUserObj().equals(currentUser)){
                        Intent intent = new Intent(context, UserProfileActivity.class);
                        intent.putExtra("hisUID", modelComments.get(position).getUserObj().getObjectId());
                        context.startActivity(intent);
                    }else {
                        Snackbar.make(v,"It's you",Snackbar.LENGTH_LONG).show();
                    }
                });
                holder.name.setOnClickListener(v -> {
                    if (!modelComments.get(position).getUserObj().equals(currentUser)){
                        Intent intent = new Intent(context, UserProfileActivity.class);
                        intent.putExtra("hisUID", modelComments.get(position).getUserObj().getObjectId());
                        context.startActivity(intent);
                    }else {
                        Snackbar.make(v,"It's you",Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });

        //Comment
        holder.comment.setVisibility(View.VISIBLE);

        holder.comment.setLinkText(modelComments.get(position).getComment());
        holder.comment.setOnLinkClickListener((i, s) -> {
            if (i == 1){

                Intent intent = new Intent(context, SearchActivity.class);
                intent.putExtra("hashtag", s);
                context.startActivity(intent);
            }else
            if (i == 2){
                String username = s.replaceFirst("@","");
                ParseQuery<ParseUser> user = ParseUser.getQuery();
                user.whereEqualTo("username", username.trim());
                user.findInBackground(new FindCallback<ParseUser>() {
                    public void done(List<ParseUser> userList, ParseException e) {
                        if (e == null) {
                            if(userList.size()>0){
                                for (ParseUser obj : userList) {
                                    String id = Objects.requireNonNull(obj.getObjectId());
                                    if (id.equals(currentUser.getObjectId())){
                                        Snackbar.make(holder.itemView,"It's you", Snackbar.LENGTH_LONG).show();
                                    }else {
                                        Intent intent = new Intent(context, UserProfileActivity.class);
                                        intent.putExtra("hisUID", id);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                    }
                                }
                            } else {Snackbar.make(holder.itemView,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                            }
                        } else {
                            Snackbar.make(holder.itemView,e.getMessage(), Snackbar.LENGTH_LONG).show();
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
                context.startActivity(openUrlIntent);
            }else if (i == 4){
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                context.startActivity(intent);
            }else if (i == 8){
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, s);
                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                context.startActivity(intent);

            }
        });

        //More
        Context moreWrapper = new ContextThemeWrapper(context, R.style.popupMenuStyle);
        PopupMenu morePop = new PopupMenu(moreWrapper, holder.more);
        morePop.getMenu().add(Menu.NONE,1,1, "Report");
        morePop.getMenu().add(Menu.NONE,2,2, "Delete");
        morePop.getMenu().add(Menu.NONE,4,4, "Copy");
        morePop.setOnMenuItemClickListener(item -> {
           if (item.getItemId() == 4){
                Snackbar.make(holder.itemView,"Copied", Snackbar.LENGTH_LONG).show();
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", modelComments.get(position).getComment());
                clipboard.setPrimaryClip(clip);
            }else if (item.getItemId() == 1){
               ReportComment rco = new ReportComment();
               rco.setUserObj(ParseUser.getCurrentUser());
               rco.setCommentObj(modelComments.get(position));
               rco.saveInBackground();
                Snackbar.make(holder.itemView,"Reported", Snackbar.LENGTH_LONG).show();
            }else  if (item.getItemId() == 2){
               ParseQuery<Comment> pc = ParseQuery.getQuery(Comment.class);
               pc.getInBackground(modelComments.get(position).getObjectId(), new GetCallback<Comment>() {
                   public void done(Comment pco, ParseException e) {
                       if (e == null) {
                           pco.setIsDeleted(true);
                           pco.saveInBackground();
                           Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();
                       }
                   }
               });
            }
            return false;
        });
        holder.more.setOnClickListener(v -> morePop.show());

        //Time
        long lastTime = modelComments.get(position).getUpdatedAt().getTime();
        holder.time.setText(App.getTimeAgo(lastTime));

        //CheckLikes
        ParseQuery<CommentReaction> cLikesQuery = ParseQuery.getQuery(CommentReaction.class);
        cLikesQuery.whereEqualTo("commentObj", modelComments.get(position));
        cLikesQuery.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                    if(count>0)
                    {
                        holder.likeText.setVisibility(View.VISIBLE);
                        holder.noLikes.setText(String.valueOf(count));
                    } else {
                        holder.likeText.setVisibility(View.GONE);
                        holder.noLikes.setText("");
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        //PostLike
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(new int[]{
                        R.drawable.ic_thumb,
                        R.drawable.ic_love,
                        R.drawable.ic_laugh,
                        R.drawable.ic_wow,
                        R.drawable.ic_sad,
                        R.drawable.ic_angry
                })
                .withPopupAlpha(1)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (position1) -> {

            if (position1 == 0) {
                updateLikesReactions(modelComments.get(position), "like", holder);
                return true;
            }else if (position1 == 1) {
                updateLikesReactions(modelComments.get(position), "love", holder);
                return true;
            }
            else if (position1 == 2) {
                updateLikesReactions(modelComments.get(position), "laugh", holder);
                return true;
            }else if (position1 == 3) {
                updateLikesReactions(modelComments.get(position), "wow", holder);
                return true;
            }
            else if (position1 == 4) {
                updateLikesReactions(modelComments.get(position), "sad", holder);
                return true;
            }
            else if (position1 == 5) {
                updateLikesReactions(modelComments.get(position), "angry", holder);
                return true;
            }

            return true;
        });

        //LikeFunctions
        holder.like.setOnTouchListener(popup);
        ParseQuery<CommentReaction> cl = ParseQuery.getQuery(CommentReaction.class);
        cl.whereEqualTo("commentObj", modelComments.get(position));
        cl.whereEqualTo("userObj", currentUser);
        cl.whereEqualTo("value", "like");
        cl.getFirstInBackground(new GetCallback<CommentReaction>() {
            public void done(CommentReaction ulObj, ParseException e) {
                if (e == null) {
                    if(ulObj.isDataAvailable())
                    {
                        holder.like.setVisibility(View.GONE);
                        holder.liked.setVisibility(View.VISIBLE);
                    } else {
                        holder.liked.setVisibility(View.GONE);
                        holder.like.setVisibility(View.VISIBLE);
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        holder.liked.setOnClickListener(v -> {ParseQuery<CommentReaction> cl1 = ParseQuery.getQuery(CommentReaction.class);
        cl1.whereEqualTo("commentObj", modelComments.get(position));
        cl1.whereEqualTo("userObj", currentUser);
        cl1.whereEqualTo("value", "like");
        cl1.getFirstInBackground(new GetCallback<CommentReaction>() {
            public void done(CommentReaction ulobj, ParseException e) {
                if (e == null) {
                    ulobj.deleteInBackground();
                    ParseQuery<CommentReaction> rq = ParseQuery.getQuery(CommentReaction.class);
                    rq.whereEqualTo("commentObj", modelComments.get(position));
                    rq.whereEqualTo("userObj", currentUser);
                    rq.whereEqualTo("value", "like");
                    rq.getFirstInBackground(new GetCallback<CommentReaction>() {
                        public void done(CommentReaction robj, ParseException e) {
                            if (e == null) {
                                robj.deleteInBackground();
                            } else {
                                Timber.d("Error: %s", e.getMessage());
                            }
                        }
                    });
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    });

        ParseQuery<CommentReaction> query = ParseQuery.getQuery(CommentReaction.class);
        query.whereEqualTo("commentObj", modelComments.get(position));
        query.orderByAscending("value");
        query.getFirstInBackground(new GetCallback<CommentReaction>() {
            public void done(CommentReaction robj, ParseException e) {
                if (e == null) {
                    if(robj.get("value").toString().equals("sad"))
                    {
                        holder.sad.setVisibility(View.VISIBLE);
                    } else {
                        holder.sad.setVisibility(View.GONE);
                    }
                    if(robj.get("value").toString().equals("laugh"))
                    {
                        holder.laugh.setVisibility(View.VISIBLE);
                    } else {
                        holder.laugh.setVisibility(View.GONE);
                    }
                    if(robj.get("value").toString().equals("angry"))
                    {
                        holder.angry.setVisibility(View.VISIBLE);
                    } else {
                        holder.angry.setVisibility(View.GONE);
                    }
                    if(robj.get("value").toString().equals("wow"))
                    {
                        holder.wow.setVisibility(View.VISIBLE);
                    } else {
                        holder.wow.setVisibility(View.GONE);
                    }
                    if(robj.get("value").toString().equals("love"))
                    {
                        holder.love.setVisibility(View.VISIBLE);
                    } else {
                        holder.love.setVisibility(View.GONE);
                    }
                    if(robj.get("value").toString().equals("like"))
                    {
                        holder.thumb.setVisibility(View.VISIBLE);
                    } else {
                        ParseQuery<CommentReaction> query = ParseQuery.getQuery(CommentReaction.class);
                        query.whereEqualTo("commentObj", modelComments.get(position));
                        query.orderByAscending("value");
                        query.getFirstInBackground(new GetCallback<CommentReaction>() {
                            public void done(CommentReaction lobj, ParseException e) {
                                if (e == null) {
                                    if(lobj.getValue().equals("like"))
                                    {
                                        holder.thumb.setVisibility(View.VISIBLE);
                                    } else {
                                        holder.thumb.setVisibility(View.GONE);
                                    }
                                } else {
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


    @Override
    public int getItemCount() {
        return modelComments.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView dp;
        final TextView name;
        final TextView time;
        final TextView like;
        final TextView liked;
        final TextView noLikes;
        final TextView likeText;
        final ImageView verified;
        final ImageView play;
        final ImageView more;
        final SocialTextView comment;
        final RelativeLayout media_layout;
        final ImageView media;
        final ImageView thumb;
        final ImageView love;
        final ImageView laugh;
        final ImageView wow;
        final ImageView angry;
        final ImageView sad;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            thumb  =  itemView.findViewById(R.id.thumb);
            love  =  itemView.findViewById(R.id.love);
            laugh  =  itemView.findViewById(R.id.laugh);
            wow  =  itemView.findViewById(R.id.wow);
            angry  =  itemView.findViewById(R.id.angry);
            sad =  itemView.findViewById(R.id.sad);
            liked = itemView.findViewById(R.id.liked);
            more = itemView.findViewById(R.id.more);
            like = itemView.findViewById(R.id.like);
            time = itemView.findViewById(R.id.time);
            media = itemView.findViewById(R.id.media);
            play = itemView.findViewById(R.id.play);
            media_layout = itemView.findViewById(R.id.media_layout);
            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            comment = itemView.findViewById(R.id.username);
            verified = itemView.findViewById(R.id.verified);
            likeText  = itemView.findViewById(R.id.likeText);
            noLikes  = itemView.findViewById(R.id.noLikes);
        }

    }

    public boolean updateLikesReactions(Comment commentObj, String value, MyHolder holder)
    {
        ParseQuery<CommentReaction> vq1 = ParseQuery.getQuery(CommentReaction.class);
        vq1.whereEqualTo("commentObj", commentObj);
        vq1.whereEqualTo("userObj", ParseUser.getCurrentUser());
        vq1.getFirstInBackground(new GetCallback<CommentReaction>() {
            public void done(CommentReaction lobj, ParseException e) {
                if (e == null) {
                    if (lobj.isDataAvailable()) {
                        lobj.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    //updateComment(commentObj, holder);
                                }
                            }
                        });
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                    if(e.getCode()==101){
                        CommentReaction myReaction = new CommentReaction();
                        myReaction.setUserObj(ParseUser.getCurrentUser());
                        myReaction.setCommentObj(commentObj);
                        myReaction.setValue(value);
                        myReaction.saveInBackground( new SaveCallback() {
                            public void done(ParseException e) {
                                if (e == null) {
                                    //updateComment(commentObj, holder);
                                }
                            }
                        });
                    }
                }
            }
        });
        return true;
    };

    void updateComment(Comment commentObj, MyHolder holder){
        ParseQuery<CommentReaction> vq1 = ParseQuery.getQuery(CommentReaction.class);
        vq1.whereEqualTo("commentObj", commentObj);
        vq1.findInBackground(new FindCallback<CommentReaction>() {
            public void done(List<CommentReaction> likesList, ParseException e) {
                if (e == null) {
                    if (likesList.size() > 0) {

                        holder.thumb.setVisibility(View.GONE);
                        holder.love.setVisibility(View.GONE);
                        holder.wow.setVisibility(View.GONE);
                        holder.angry.setVisibility(View.GONE);
                        holder.laugh.setVisibility(View.GONE);
                        holder.sad.setVisibility(View.GONE);

                        holder.noLikes.setText(String.valueOf(likesList.size()));

                        for (CommentReaction likesUser : likesList){
                            String value = likesUser.getValue();
                            if (value.equals("like")) holder.thumb.setVisibility(View.VISIBLE);
                            if (value.equals("love")) holder.love.setVisibility(View.VISIBLE);
                            if (value.equals("wow")) holder.wow.setVisibility(View.VISIBLE);
                            if (value.equals("angry")) holder.angry.setVisibility(View.VISIBLE);
                            if (value.equals("laugh")) holder.laugh.setVisibility(View.VISIBLE);
                            if (value.equals("sad")) holder.sad.setVisibility(View.VISIBLE);

                            if (likesUser.getUserObj().equals(ParseUser.getCurrentUser())){

                                if (value.equals("like")){
                                    holder.thumb.setVisibility(View.VISIBLE);
                                }
                                if (value.equals("love")){
                                    holder.love.setVisibility(View.VISIBLE);
                                }
                                if (value.equals("laugh")){
                                    holder.laugh.setVisibility(View.VISIBLE);
                                }
                                if (value.equals("wow")){
                                    holder.wow.setVisibility(View.VISIBLE);
                                }
                                if (value.equals("sad")){
                                    holder.sad.setVisibility(View.VISIBLE);
                                }
                                if (value.equals("angry")){
                                    holder.angry.setVisibility(View.VISIBLE);
                                }
                            }else{
                                holder.like.setVisibility(View.GONE);
                                holder.liked.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        holder.like.setVisibility(View.GONE);
                        holder.liked.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                    if(e.getCode()==101){
                        holder.like.setVisibility(View.GONE);
                        holder.liked.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    };
}
