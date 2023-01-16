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
import com.frizid.timeline.model.Comment;
import com.frizid.timeline.model.CommentReaction;
import com.frizid.timeline.model.CommentReply;
import com.frizid.timeline.profile.UserProfileActivity;
import com.frizid.timeline.search.SearchActivity;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.tylersuehr.socialtextview.SocialTextView;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterCommentReply extends RecyclerView.Adapter<AdapterCommentReply.MyHolder>{

    final Context context;
    final List<CommentReply> modelComments;
    NightMode nightMode;

    public AdapterCommentReply(Context context, List<CommentReply> modelComments) {
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
        ParseQuery<ParseUser> uq = ParseUser.getQuery();
        uq.getInBackground(modelComments.get(position).getCommentObj().getUserObj().getObjectId(), new GetCallback<ParseUser>() {
            public void done(ParseUser uobj, ParseException e) {
                if (e == null) {
                    holder.name.setText(Objects.requireNonNull(uobj.getString("name")));
                    try{
                        if (uobj.getParseFile("photo").isDataAvailable()) Picasso.get().load(Objects.requireNonNull(uobj.getParseFile("photo")).getUrl()).into(holder.dp);
                    }catch(NullPointerException e1){

                    }
                    if (uobj.getBoolean("verified")) holder.verified.setVisibility(View.VISIBLE);

                    //SetOnClick
                    holder.dp.setOnClickListener(v -> {
                        if (!modelComments.get(position).getCommentObj().equals(Objects.requireNonNull(ParseUser.getCurrentUser()))){
                            Intent intent = new Intent(context, UserProfileActivity.class);
                            intent.putExtra("hisUID", modelComments.get(position).getCommentObj());
                            context.startActivity(intent);
                        }else {
                            Snackbar.make(v,"It's you", Snackbar.LENGTH_LONG).show();
                        }
                    });
                    holder.name.setOnClickListener(v -> {
                        if (!modelComments.get(position).getCommentObj().equals(Objects.requireNonNull(ParseUser.getCurrentUser()))){
                            Intent intent = new Intent(context, UserProfileActivity.class);
                            intent.putExtra("hisUID", modelComments.get(position).getCommentObj());
                            context.startActivity(intent);
                        }else {
                            Snackbar.make(v,"It's you", Snackbar.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
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
                ParseQuery<ParseUser> uq1 = ParseUser.getQuery();
                uq1.whereEqualTo("username", username.trim());
                uq1.findInBackground(new FindCallback<ParseUser>() {
                    public void done(List<ParseUser> usersList, ParseException e) {
                        if (e == null) {
                            if (usersList.size() > 0) {
                                for (ParseUser user : usersList){
                                    String id = user.getObjectId();
                                    if (id.equals(ParseUser.getCurrentUser())){
                                        Snackbar.make(holder.itemView,"It's you", Snackbar.LENGTH_LONG).show();
                                    }else {
                                        Intent intent = new Intent(context, UserProfileActivity.class);
                                        intent.putExtra("hisUID", id);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                    }
                                }
                            } else {
                                Snackbar.make(holder.itemView,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                            }

                        }else {
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
        morePop.getMenu().add(Menu.NONE,2,2, "Delete");
        morePop.getMenu().add(Menu.NONE,4,4, "Copy");
        morePop.setOnMenuItemClickListener(item -> {
           if (item.getItemId() == 4){
                Snackbar.make(holder.itemView,"Copied", Snackbar.LENGTH_LONG).show();
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", modelComments.get(position).getComment());
                clipboard.setPrimaryClip(clip);
            }else  if (item.getItemId() == 2){
               ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
               params.height = 0;
               holder.itemView.setLayoutParams(params);
               ParseQuery<CommentReply> query = ParseQuery.getQuery(CommentReply.class);
               query.getInBackground(modelComments.get(position).getCommentObj().getObjectId(), new GetCallback<CommentReply>() {
                   public void done(CommentReply crobj, ParseException e) {
                       if (e == null) {
                           crobj.deleteInBackground();
                       }else {
                           Timber.d("Error: %s", e.getMessage());
                       }
                   }
               });
               Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();
            }
            return false;
        });
        holder.more.setOnClickListener(v -> morePop.show());

        //Time
        long lastTime = modelComments.get(position).getUpdatedAt().getTime();
        holder.time.setText(App.getTimeAgo(lastTime));

        //CheckLikes
        ParseQuery<CommentReaction> query = ParseQuery.getQuery(CommentReaction.class);
        query.whereEqualTo("commentObj",modelComments.get(position).getCommentObj());
        query.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                    if(count>0){
                        holder.likeText.setVisibility(View.VISIBLE);
                        holder.noLikes.setText(count);
                    } else {
                        holder.likeText.setVisibility(View.GONE);
                        holder.noLikes.setText("");
                    }
                }else {
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
                updateLikesReactions(modelComments.get(position).getCommentObj(), "like", holder);
                return true;
            }else if (position1 == 1) {
                updateLikesReactions(modelComments.get(position).getCommentObj(), "love", holder);
                return true;
            }
            else if (position1 == 2) {
                updateLikesReactions(modelComments.get(position).getCommentObj(), "laugh", holder);
                return true;
            }else if (position1 == 3) {
                updateLikesReactions(modelComments.get(position).getCommentObj(), "wow", holder);
                return true;
            }
            else if (position1 == 4) {
                updateLikesReactions(modelComments.get(position).getCommentObj(), "sad", holder);
                return true;
            }
            else if (position1 == 5) {
                updateLikesReactions(modelComments.get(position).getCommentObj(), "angry", holder);
                return true;
            }
            return true;
        });

        //LikeFunctions
        holder.like.setOnTouchListener(popup);

        ParseQuery<CommentReaction> clq = ParseQuery.getQuery(CommentReaction.class);
        clq.whereEqualTo("commentObj",modelComments.get(position).getCommentObj());
        clq.whereEqualTo("userObj",ParseUser.getCurrentUser());
        clq.getFirstInBackground(new GetCallback<CommentReaction>() {
            public void done(CommentReaction clobj, ParseException e) {
                if (e == null) {
                    if(clobj.isDataAvailable()){
                        holder.like.setVisibility(View.GONE);
                        holder.liked.setVisibility(View.VISIBLE);
                    } else {
                        holder.liked.setVisibility(View.GONE);
                        holder.like.setVisibility(View.VISIBLE);
                    }
                }else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        holder.liked.setOnClickListener(v -> {
            updateLikesReactions(modelComments.get(position).getCommentObj(), "like", holder);
        });

        int likeCount = reactionCount(modelComments.get(position).getCommentObj(), "like");
        if (likeCount>0){
            holder.thumb.setVisibility(View.VISIBLE);
        }else {
            ParseQuery<CommentReaction> clq2 = ParseQuery.getQuery(CommentReaction.class);
            clq2.whereEqualTo("commentObj",modelComments.get(position).getCommentObj());
            clq2.countInBackground(new CountCallback() {
                public void done(int count, ParseException e) {
                    if (e == null) {
                        if(count>0){
                            holder.thumb.setVisibility(View.VISIBLE);
                        }else{
                            holder.thumb.setVisibility(View.GONE);
                        }
                    }else {
                        Timber.d("Error: %s", e.getMessage());
                    }
                }
            });
        }
        int loveCount = reactionCount(modelComments.get(position).getCommentObj(), "love");
        if (loveCount>0){
            holder.love.setVisibility(View.VISIBLE);
        }else {
            holder.love.setVisibility(View.GONE);
        }
        int wowCount = reactionCount(modelComments.get(position).getCommentObj(), "wow");
        if (wowCount>0){
            holder.wow.setVisibility(View.VISIBLE);
        }else {
            holder.wow.setVisibility(View.GONE);
        }
        int angryCount = reactionCount(modelComments.get(position).getCommentObj(), "angry");
        if (angryCount>0){
            holder.angry.setVisibility(View.VISIBLE);
        }else {
            holder.angry.setVisibility(View.GONE);
        }
        int laughCount = reactionCount(modelComments.get(position).getCommentObj(), "laugh");
        if (laughCount>0){
            holder.laugh.setVisibility(View.VISIBLE);
        }else {
            holder.laugh.setVisibility(View.GONE);
        }
        int sadCount = reactionCount(modelComments.get(position).getCommentObj(), "sad");
        if (sadCount>0){
            holder.sad.setVisibility(View.VISIBLE);
        }else {
            holder.sad.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return modelComments.size();
    }

    @SuppressWarnings("unused")
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
                                    updateComment(commentObj, holder);
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
                                    updateComment(commentObj, holder);
                                }
                            }
                        });
                    }
                }
            }
        });
        return true;
    };

    public int reactionCount(Comment commentObj, String value)
    {
        final int[] toalCount = new int[1];
        ParseQuery<CommentReaction> clq = ParseQuery.getQuery(CommentReaction.class);
        clq.whereEqualTo("commentObj",commentObj);
        clq.whereEqualTo("value",value);
        clq.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                    toalCount[0] =count;
                }else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
        return toalCount[0];
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
