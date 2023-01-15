package com.frizid.timeline.adapter;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;
import com.nguyencse.URLEmbeddedData;
import com.nguyencse.URLEmbeddedView;
import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.App;
import com.frizid.timeline.MediaViewActivity;
import com.frizid.timeline.R;
import com.frizid.timeline.group.CommentGroupActivity;
import com.frizid.timeline.group.GroupProfileActivity;
import com.frizid.timeline.model.Comment;
import com.frizid.timeline.model.Follow;
import com.frizid.timeline.model.Group;
import com.frizid.timeline.model.Post;
import com.frizid.timeline.model.PostExtra;
import com.frizid.timeline.model.PostReaction;
import com.frizid.timeline.model.PostView;
import com.frizid.timeline.model.Saves;
import com.frizid.timeline.profile.UserProfileActivity;
import com.frizid.timeline.search.SearchActivity;
import com.frizid.timeline.who.LikedActivity;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.tylersuehr.socialtextview.SocialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterGroupPost extends RecyclerView.Adapter<AdapterGroupPost.MyHolder>{

    final Context context;
    final List<Post> posts;
    String username_poster;

    public AdapterGroupPost(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }
    MediaPlayer mp;

    public static List<String> extractUrls(String text)
    {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find())
        {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.post_list, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        mp = MediaPlayer.create(context, R.raw.like);
        if (position>1 && (position+1) % 4 == 0) {
            holder.ad.setVisibility(View.VISIBLE);
        }


        //UserInfo
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.getInBackground(posts.get(position).getUserObj().getObjectId(), new GetCallback<ParseUser>() {
            public void done(ParseUser object, ParseException e) {
                if (e == null) {
                    username_poster = object.getString("username");
                    holder.username.setText("By " + username_poster);
                    //SetOnClick
                    holder.username.setOnClickListener(v -> {
                        if (!posts.get(position).getUserObj().equals(ParseUser.getCurrentUser())){
                            Intent intent = new Intent(context, UserProfileActivity.class);
                            intent.putExtra("hisUID", posts.get(position).getUserObj());
                            context.startActivity(intent);
                        }else {
                            Snackbar.make(v,"It's you",Snackbar.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        String type = posts.get(position).getType();

        holder.urlEmbeddedView.setOnClickListener(v -> {

            List<String> extractedUrls = extractUrls(posts.get(position).getText());

            for (String s : extractedUrls)
            {
                if (!s.startsWith("https://") && !s.startsWith("http://")){
                    s = "http://" + s;
                }
                Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                context.startActivity(openUrlIntent);
            }
        });

        if (!posts.get(position).getText().isEmpty()) {

            List<String> extractedUrls = extractUrls(posts.get(position).getText());

            for (String url : extractedUrls)
            {
                holder.urlEmbeddedView.setVisibility(View.VISIBLE);

                holder.urlEmbeddedView.setURL(url, new URLEmbeddedView.OnLoadURLListener() {
                    @Override
                    public void onLoadURLCompleted(URLEmbeddedData data) {
                        holder.urlEmbeddedView.title(data.getTitle());
                        holder.urlEmbeddedView.description(data.getDescription());
                        holder.urlEmbeddedView.host(data.getHost());
                        holder.urlEmbeddedView.thumbnail(data.getThumbnailURL());
                        holder.urlEmbeddedView.favor(data.getFavorURL());
                    }
                });
            }
        }


        //GroupInfo
        ParseQuery<Group> gpq = ParseQuery.getQuery(Group.class);
        gpq.whereEqualTo("objectId", posts.get(position).getGroupObj());
        gpq.getFirstInBackground(new GetCallback<Group>() {
            public void done(Group gobj, ParseException e) {
                if (e == null) {
                    if (gobj.getGIcon().isDataAvailable()) {
                        Picasso.get().load(gobj.getGIcon().getUrl()).into(holder.dp);
                    }else {
                        Picasso.get().load(R.drawable.group).into(holder.dp);
                    }
                    holder.name.setText(gobj.getGName());

                    //SetOnClick
                    holder.name.setOnClickListener(v -> {
                        Intent intent = new Intent(context, GroupProfileActivity.class);
                        intent.putExtra("groupObj", posts.get(position).getGroupObj());
                        intent.putExtra("type", "");
                        context.startActivity(intent);
                    });
                    holder.dp.setOnClickListener(v -> {
                        Intent intent = new Intent(context, GroupProfileActivity.class);
                        intent.putExtra("groupObj", posts.get(position).getGroupObj());
                        intent.putExtra("type", "");
                        context.startActivity(intent);
                    });
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });


        //Time
        long lastTime = Long.parseLong(posts.get(position).getCreatedAt().toString());
        holder.time.setText(App.getTimeAgo(lastTime));

        //Extra
        ParseQuery<PostExtra> peq = ParseQuery.getQuery(PostExtra.class);
        peq.getInBackground(posts.get(position).getObjectId(), new GetCallback<PostExtra>() {
            public void done(PostExtra peobj, ParseException e) {
                if (e == null) {
                    if (peobj.isDataAvailable()) {
                        if (!peobj.getLocation().isEmpty()) holder.location.setText(" . " + peobj.getLocation());
                        holder.location.setOnClickListener(v -> {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.co.in/maps?q=" + peobj.getLocation()));
                            context.startActivity(i);
                        });

                        if(!peobj.getFeeling().isEmpty()){
                            holder.feeling.setText(" - " + peobj.getFeeling());

                            String mFeeling = peobj.getFeeling();
                            if (mFeeling.contains("Traveling")){
                                holder.activity.setImageResource(R.drawable.airplane);
                            }else if (mFeeling.contains("Watching")){
                                holder.activity.setImageResource(R.drawable.watching);
                            }else if (mFeeling.contains("Listening")){
                                holder.activity.setImageResource(R.drawable.listening);
                            }else if (mFeeling.contains("Thinking")){
                                holder.activity.setImageResource(R.drawable.thinking);
                            }else if (mFeeling.contains("Celebrating")){
                                holder.activity.setImageResource(R.drawable.celebration);
                            }else if (mFeeling.contains("Looking")){
                                holder.activity.setImageResource(R.drawable.looking);
                            }else if (mFeeling.contains("Playing")){
                                holder.activity.setImageResource(R.drawable.playing);
                            }else if (mFeeling.contains("happy")){
                                holder.activity.setImageResource(R.drawable.smiling);
                            } else if (mFeeling.contains("loved")){
                                holder.activity.setImageResource(R.drawable.love);
                            } else if (mFeeling.contains("sad")){
                                holder.activity.setImageResource(R.drawable.sad);
                            }else if (mFeeling.contains("crying")){
                                holder.activity.setImageResource(R.drawable.crying);
                            }else if (mFeeling.contains("angry")){
                                holder.activity.setImageResource(R.drawable.angry);
                            }else if (mFeeling.contains("confused")){
                                holder.activity.setImageResource(R.drawable.confused);
                            }else if (mFeeling.contains("broken")){
                                holder.activity.setImageResource(R.drawable.broken);
                            }else if (mFeeling.contains("cool")){
                                holder.activity.setImageResource(R.drawable.cool);
                            }else if (mFeeling.contains("funny")){
                                holder.activity.setImageResource(R.drawable.joy);
                            }else if (mFeeling.contains("tired")){
                                holder.activity.setImageResource(R.drawable.tired);
                            }else if (mFeeling.contains("shock")){
                                holder.activity.setImageResource(R.drawable.shocked);
                            }else if (mFeeling.contains("love")){
                                holder.activity.setImageResource(R.drawable.heart);
                            }else if (mFeeling.contains("sleepy")){
                                holder.activity.setImageResource(R.drawable.sleeping);
                            }else if (mFeeling.contains("expressionless")){
                                holder.activity.setImageResource(R.drawable.muted);
                            }else if (mFeeling.contains("blessed")){
                                holder.activity.setImageResource(R.drawable.angel);
                            }
                        }

                        if (peobj.get("privacy").toString().equals("No one")){
                            if (!posts.get(position).getUserObj().equals(ParseUser.getCurrentUser())){
                                ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                                params.height = 0;
                                holder.itemView.setLayoutParams(params);
                            }
                        } else if (peobj.get("privacy").toString().equals("Followers")){
                            ParseQuery<Follow> gpc = ParseQuery.getQuery(Follow.class);
                            gpc.whereEqualTo("toObj", posts.get(position).getUserObj());
                            gpc.findInBackground(new FindCallback<Follow>() {
                                public void done(List<Follow> followerList, ParseException e) {
                                    if (e == null) {
                                        if (!followerList.isEmpty()) {
                                            for (Follow follower : followerList)
                                            {
                                                if (!follower.getToObj().equals(ParseUser.getCurrentUser()) &&
                                                        !posts.get(position).getUserObj().equals(ParseUser.getCurrentUser())){
                                                    ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                                                    params.height = 0;
                                                    holder.itemView.setLayoutParams(params);
                                                }
                                            }
                                        }
                                    } else {
                                        Timber.d("Error: %s", e.getMessage());
                                    }
                                }
                            });
                        }
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        //PostDetails
        if (!type.equals("bg")){
            holder.text.setLinkText(posts.get(position).getText());
            holder.text.setOnLinkClickListener((i, s) -> {
                if (i == 1){

                    Intent intent = new Intent(context, SearchActivity.class);
                    intent.putExtra("hashtag", s);
                    context.startActivity(intent);

                }else
                if (i == 2){
                    String username = s.replaceFirst("@","");
                    ParseQuery<ParseUser> uq = ParseUser.getQuery();
                    uq.whereEqualTo("username", username.trim());
                    uq.orderByAscending("username");
                    uq.findInBackground(new FindCallback<ParseUser>() {
                        public void done(List<ParseUser> userList, ParseException e) {
                            if (e == null) {
                                if (userList.isEmpty()){
                                    for (ParseUser ds : userList) {
                                        String id = ds.getObjectId().toString();
                                        if (id.equals(ParseUser.getCurrentUser())) {
                                            Snackbar.make(holder.itemView, "It's you", Snackbar.LENGTH_LONG).show();
                                        } else {
                                            Intent intent = new Intent(context, UserProfileActivity.class);
                                            intent.putExtra("hisUID", id);
                                            context.startActivity(intent);
                                        }
                                    }
                            }else {
                                    Snackbar.make(holder.itemView,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                            }
                            }else {
                                Timber.d("Error: %s", e.getMessage());
                                Snackbar.make(holder.itemView,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
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
        }

        if (type.equals("image")){
            holder.mediaView.setVisibility(View.VISIBLE);
            Picasso.get().load(posts.get(position).getMeme().getUrl()).into(holder.mediaView);
        }
        if (type.equals("gif")){
            holder.mediaView.setVisibility(View.VISIBLE);
            Glide.with(context).load(posts.get(position).getMeme()).thumbnail(0.1f).into(holder.mediaView);
        }
        if (type.equals("video")){
            holder.mediaView.setVisibility(View.VISIBLE);
            holder.play.setVisibility(View.VISIBLE);
            Glide.with(context).asBitmap().load(posts.get(position).getVine()).thumbnail(0.1f).into(holder.mediaView);
        }
        if (type.equals("bg")){
            Picasso.get().load(posts.get(position).getUrl()).into(holder.mediaView);
            holder.bg_text.setLinkText(posts.get(position).getText());
            holder.bg_text.setOnLinkClickListener((i, s) -> {
                if (i == 1){

                    Intent intent = new Intent(context, SearchActivity.class);
                    intent.putExtra("hashtag", s);
                    context.startActivity(intent);

                }else
                if (i == 2){
                    String username = s.replaceFirst("@","");
                    ParseQuery<ParseUser> uq = ParseUser.getQuery();
                    uq.whereEqualTo("username", username.trim());
                    uq.orderByAscending("username");
                    uq.findInBackground(new FindCallback<ParseUser>() {
                        public void done(List<ParseUser> userList, ParseException e) {
                            if (e == null) {
                                if (userList.isEmpty()){
                                    for (ParseUser ds : userList){
                                        String id = ds.getObjectId().toString();
                                        if (id.equals(ParseUser.getCurrentUser())){
                                            Snackbar.make(holder.itemView,"It's you", Snackbar.LENGTH_LONG).show();
                                        }else {
                                            Intent intent = new Intent(context, UserProfileActivity.class);
                                            intent.putExtra("hisUID", id);
                                            context.startActivity(intent);
                                        }
                                    }
                                }else {
                                    Snackbar.make(holder.itemView,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                                }
                            }else {
                                Timber.d("Error: %s", e.getMessage());
                                Snackbar.make(holder.itemView,e.getMessage(), Snackbar.LENGTH_LONG).show();
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
            holder.bg_text.setVisibility(View.VISIBLE);
            holder.text.setVisibility(View.GONE);
            holder.mediaView.setVisibility(View.VISIBLE);
        }
        if (type.equals("audio")){
            holder.mediaView.setVisibility(View.GONE);
            holder.voicePlayerView.setVisibility(View.VISIBLE);
            holder.voicePlayerView.setAudio(String.valueOf(posts.get(position).getMeme()));
        }

        //CheckComments
        ParseQuery<Comment> gpc = ParseQuery.getQuery(Comment.class);
        gpc.whereEqualTo("postObj", posts.get(position));
        gpc.whereEqualTo("pType", "group");
        gpc.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                    if (count> 0) {
                        holder.layout.setVisibility(View.VISIBLE);
                        holder.commentLayout.setVisibility(View.VISIBLE);
                        holder.noComments.setText(String.valueOf(count));
                    } else {
                        holder.commentLayout.setVisibility(View.GONE);
                        holder.noComments.setText("");
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        //CheckViews
        ParseQuery<PostView> vq = ParseQuery.getQuery(PostView.class);
        vq.whereEqualTo("postObj", posts.get(position));
        vq.findInBackground(new FindCallback<PostView>() {
            public void done(List<PostView> viewsList, ParseException e) {
                if (e == null) {
                    if (viewsList.size() > 0) {
                        holder.layout.setVisibility(View.VISIBLE);
                        holder.viewsLayout.setVisibility(View.VISIBLE);
                        holder.noViews.setText(String.valueOf(viewsList.get(0).get("count")));
                    } else {
                        holder.viewsLayout.setVisibility(View.GONE);
                        holder.noViews.setText("");
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        //CheckLikes
        ParseQuery<PostReaction> vq1 = ParseQuery.getQuery(PostReaction.class);
        vq1.whereEqualTo("postObj", posts.get(position));
        vq1.findInBackground(new FindCallback<PostReaction>() {
            public void done(List<PostReaction> likesList, ParseException e) {
                if (e == null) {
                    if (likesList.size() > 0) {
                        holder.likeLayout.setVisibility(View.VISIBLE);
                        holder.line.setVisibility(View.VISIBLE);
                        holder.noLikes.setText(likesList.size());
                        for (PostReaction likesUser : likesList){
                            if (likesUser.getUserObj().equals(ParseUser.getCurrentUser())){
                                String react = likesUser.getValue();
                                if (react.equals("like")){
                                    holder.like_img.setImageResource(R.drawable.ic_thumb);
                                    holder.like_text.setText("Like");
                                }
                                if (react.equals("love")){
                                    holder.like_img.setImageResource(R.drawable.ic_love);
                                    holder.like_text.setText("Love");
                                }
                                if (react.equals("laugh")){
                                    holder.like_img.setImageResource(R.drawable.ic_laugh);
                                    holder.like_text.setText("Haha");
                                }
                                if (react.equals("wow")){
                                    holder.like_img.setImageResource(R.drawable.ic_wow);
                                    holder.like_text.setText("Wow");
                                }
                                if (react.equals("sad")){
                                    holder.like_img.setImageResource(R.drawable.ic_sad);
                                    holder.like_text.setText("Sad");
                                }
                                if (react.equals("angry")){
                                    holder.like_img.setImageResource(R.drawable.ic_angry);
                                    holder.like_text.setText("Angry");
                                }
                            }else{
                                holder.like_img.setImageResource(R.drawable.ic_like);
                                holder.like_text.setText("Like");
                            }
                        }

                        holder.thumb.setVisibility(View.GONE);
                        holder.love.setVisibility(View.GONE);
                        holder.wow.setVisibility(View.GONE);
                        holder.angry.setVisibility(View.GONE);
                        holder.laugh.setVisibility(View.GONE);
                        holder.sad.setVisibility(View.GONE);

                        ParseQuery<PostReaction> rq = ParseQuery.getQuery(PostReaction.class);
                        rq.whereEqualTo("postObj", posts.get(position));
                        rq.findInBackground(new FindCallback<PostReaction>() {
                            public void done(List<PostReaction> rList, ParseException e) {
                                if (e == null) {
                                    for (PostReaction rListObj : rList){
                                        if (rListObj.getValue().equals("thumb")) holder.thumb.setVisibility(View.VISIBLE);
                                        if (rListObj.getValue().equals("love")) holder.love.setVisibility(View.VISIBLE);
                                        if (rListObj.getValue().equals("wow")) holder.wow.setVisibility(View.VISIBLE);
                                        if (rListObj.getValue().equals("angry")) holder.angry.setVisibility(View.VISIBLE);
                                        if (rListObj.getValue().equals("laugh")) holder.laugh.setVisibility(View.VISIBLE);
                                        if (rListObj.getValue().equals("sad")) holder.sad.setVisibility(View.VISIBLE);
                                    }
                                }
                                else {
                                    Timber.d("Error: %s", e.getMessage());
                                }
                            }
                        });
                        if (likesList.size() > 0) holder.thumb.setVisibility(View.VISIBLE);
                    }
                    else {
                        holder.likeLayout.setVisibility(View.GONE);
                        holder.line.setVisibility(View.GONE);
                        holder.like_img.setImageResource(R.drawable.ic_like);
                        holder.like_text.setText("Like");
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });

        //PostLike
        ReactionsConfig config = new ReactionsConfigBuilder(context).withReactions(new int[]{
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

            mp.start();

            if (position1 == 0) {
                return updateLikesReactions(posts.get(position),"like", holder);
            }else if (position1 == 1) {
                return updateLikesReactions(posts.get(position),"love", holder);
            }
            else if (position1 == 2) {
                return updateLikesReactions(posts.get(position),"laugh", holder);
            }      else if (position1 == 3) {
                return updateLikesReactions(posts.get(position),"wow", holder);
            }
            else if (position1 == 4) {
                return updateLikesReactions(posts.get(position),"sad", holder);
            }
            else if (position1 == 5) {
                return updateLikesReactions(posts.get(position),"angry", holder);
            }
            return true;
        });

        //LikeFunctions
        holder.likeButtonTwo.setOnTouchListener(popup);
        ParseQuery<PostReaction> vqld1 = ParseQuery.getQuery(PostReaction.class);
        vqld1.whereEqualTo("postObj", posts.get(position));
        vqld1.whereEqualTo("value", "like");
        vqld1.findInBackground(new FindCallback<PostReaction>() {
            public void done(List<PostReaction> likesList, ParseException e) {
                if (e == null) {
                    if (likesList.size()>0) {
                        for (PostReaction likesUser : likesList){
                            if (likesUser.getUserObj().equals(ParseUser.getCurrentUser())){
                                holder.likeButtonTwo.setVisibility(View.GONE);
                                holder.likeButton.setVisibility(View.VISIBLE);
                            }else{
                                holder.likeButton.setVisibility(View.GONE);
                                holder.likeButtonTwo.setVisibility(View.VISIBLE);
                            }
                        }
                    }else{
                        holder.likeButton.setVisibility(View.GONE);
                        holder.likeButtonTwo.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                    if(e.getCode()==101){
                        holder.likeButton.setVisibility(View.GONE);
                        holder.likeButtonTwo.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        holder.likeButton.setOnClickListener(v -> {
            updateLikesReactions(posts.get(position),"like", holder);
        });

        //Share
        holder.share.setOnClickListener(v -> {
            if (type.equals("text")){
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, posts.get(position).getText());
                context.startActivity(Intent.createChooser(intent, "Share Via"));
            }else if (type.equals("image")){
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, posts.get(position).getText() + " " + posts.get(position).getMeme());
                context.startActivity(Intent.createChooser(intent, "Share Via"));
            }else if (type.equals("audio")){
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, posts.get(position).getText() + " " + posts.get(position).getMeme());
                context.startActivity(Intent.createChooser(intent, "Share Via"));
            }else if (type.equals("gif")){
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, posts.get(position).getText() + " " + posts.get(position).getMeme());
                context.startActivity(Intent.createChooser(intent, "Share Via"));
            }else if (type.equals("video")){
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, posts.get(position).getText() + " " + posts.get(position).getVine());
                context.startActivity(Intent.createChooser(intent, "Share Via"));
            }else {
                Snackbar.make(holder.itemView,"This type of post can't be shared", Snackbar.LENGTH_LONG).show();
            }
        });

        //More
        Context moreWrapper = new ContextThemeWrapper(context, R.style.popupMenuStyle);
        PopupMenu morePop = new PopupMenu(moreWrapper, holder.more);
        morePop.getMenu().add(Menu.NONE,1,1, "Save");
        morePop.getMenu().add(Menu.NONE,2,2, "Download");
        morePop.getMenu().add(Menu.NONE,4,4, "Copy");
        morePop.getMenu().add(Menu.NONE,9,9, "Liked by");
        if (posts.get(position).getObjectId().equals(ParseUser.getCurrentUser())){
            morePop.getMenu().add(Menu.NONE,7,7, "Delete");
        }
        if (posts.get(position).getType().equals("image") || posts.get(position).getType().equals("video")){
            morePop.getMenu().add(Menu.NONE,8,8, "Fullscreen");
        }
        morePop.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 9){
                Intent intent = new Intent(context, LikedActivity.class);
                intent.putExtra("id", posts.get(position));
                context.startActivity(intent);
            }

            if (item.getItemId() == 1){
                ParseQuery<Saves> sq1 = ParseQuery.getQuery(Saves.class);
                sq1.whereEqualTo("postObj", posts.get(position));
                sq1.whereEqualTo("userObj", ParseUser.getCurrentUser());
                sq1.getFirstInBackground(new GetCallback<Saves>() {
                    public void done(Saves sobj, ParseException e) {
                        if (e == null) {
                            if (sobj.isDataAvailable()) {
                                sobj.deleteInBackground();
                                Snackbar.make(holder.itemView,"Unsaved", Snackbar.LENGTH_LONG).show();
                            } else {
                                Saves mySaves = new Saves();
                                mySaves.setUserObj(ParseUser.getCurrentUser());
                                mySaves.setPostObj(posts.get(position));
                                mySaves.saveInBackground();
                                Snackbar.make(holder.itemView,"Saved", Snackbar.LENGTH_LONG).show();
                            }
                        }
                        else {
                            Timber.d("Error: %s", e.getMessage());
                        }
                    }
                });
            }
            if (item.getItemId() == 2){
               if (type.equals("text") || type.equals("bg") || type.equals("gif")){
                   Snackbar.make(holder.itemView,"This type of post can't be downloaded", Snackbar.LENGTH_LONG).show();
               }else if (type.equals("video")){
                   Snackbar.make(holder.itemView,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                   DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                   DownloadManager.Request request = new DownloadManager.Request(Uri.parse(posts.get(position).getVine().getUrl()));
                   request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                   request.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".mp4");
                   Objects.requireNonNull(downloadManager).enqueue(request);
               }else if (type.equals("image")){
                   Snackbar.make(holder.itemView,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                   DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                   DownloadManager.Request request = new DownloadManager.Request(Uri.parse(posts.get(position).getMeme().getUrl()));
                   request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                   request.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".png");
                   Objects.requireNonNull(downloadManager).enqueue(request);
               }else if (type.equals("audio")){
                   Snackbar.make(holder.itemView,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                   DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                   DownloadManager.Request request = new DownloadManager.Request(Uri.parse(posts.get(position).getMeme().getUrl()));
                   request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                   request.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".mp3");
                   Objects.requireNonNull(downloadManager).enqueue(request);
               }
            }else if (item.getItemId() == 4){
                Snackbar.make(holder.itemView,"Copied", Snackbar.LENGTH_LONG).show();
                if (type.equals("text")){
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", posts.get(position).getText());
                    clipboard.setPrimaryClip(clip);
                }else if (type.equals("image")){

                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", posts.get(position).getText() + " " + posts.get(position).getMeme());
                    clipboard.setPrimaryClip(clip);

                }else if (type.equals("audio")){

                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", posts.get(position).getText() + " " + posts.get(position).getMeme());
                    clipboard.setPrimaryClip(clip);

                }else if (type.equals("gif")){

                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", posts.get(position).getText() + " " + posts.get(position).getMeme());
                    clipboard.setPrimaryClip(clip);

                }else if (type.equals("video")){

                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", posts.get(position).getText() + " " + posts.get(position).getVine());
                    clipboard.setPrimaryClip(clip);

                }else {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", posts.get(position).getText() + " " + posts.get(position).getMeme());
                    clipboard.setPrimaryClip(clip);

                }
            }else  if (item.getItemId() == 7){
                ParseQuery<Post> pgq = ParseQuery.getQuery(Post.class);
                pgq.whereEqualTo("groupObj", posts.get(position).getGroupObj());
                pgq.whereEqualTo("postObj", ParseUser.getCurrentUser());
                pgq.getFirstInBackground(new GetCallback<Post>() {
                    public void done(Post pgobj, ParseException e) {
                        if (e == null) {
                            if (pgobj.isDataAvailable()) {
                                pgobj.deleteInBackground();
                            }
                        }
                        else {
                            Timber.d("Error: %s", e.getMessage());
                        }
                    }
                });
                Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();
            }
            else if (item.getItemId() == 8){
                switch (posts.get(position).getType()) {
                    case "image":

                        Intent intent = new Intent(context, MediaViewActivity.class);
                        intent.putExtra("type", "image");
                        intent.putExtra("uri", posts.get(position).getMeme());
                        context.startActivity(intent);

                        break;
                    case "video":

                        Intent intent1 = new Intent(context, MediaViewActivity.class);
                        intent1.putExtra("type", "video");
                        intent1.putExtra("uri", posts.get(position).getVine());
                        context.startActivity(intent1);

                        break;
                }
            }
            return false;
        });
        holder.more.setOnClickListener(v -> morePop.show());

        holder.comment.setOnClickListener(v -> {

            if (posts.get(position).getType().equals("video")){
                ParseQuery<PostView> vsq1 = ParseQuery.getQuery(PostView.class);
                vsq1.whereEqualTo("postObj", posts.get(position));
                vsq1.getFirstInBackground(new GetCallback<PostView>() {
                    public void done(PostView vobj, ParseException e) {
                        if (e == null) {
                            if (vobj.isDataAvailable()) {
                                vobj.setUserObj(ParseUser.getCurrentUser());
                                vobj.saveInBackground();
                                Intent intent = new Intent(context, CommentGroupActivity.class);
                                intent.putExtra("postObj", posts.get(position));
                                intent.putExtra("groupObj", posts.get(position).getGroupObj());
                            }
                        }
                        else {
                            Timber.d("Error: %s", e.getMessage());
                        }
                    }
                });
            }else {
                Intent intent = new Intent(context, CommentGroupActivity.class);
                intent.putExtra("postObj", posts.get(position));
                intent.putExtra("groupObj", posts.get(position).getGroupObj());
                context.startActivity(intent);
            }

        });

        holder.itemView.setOnClickListener(v -> {
            if (posts.get(position).getType().equals("video")){
                ParseQuery<PostView> vvq = ParseQuery.getQuery(PostView.class);
                vvq.whereEqualTo("postObj", posts.get(position));
                vvq.getFirstInBackground(new GetCallback<PostView>() {
                    public void done(PostView vobj, ParseException e) {
                        if (e == null) {
                            if (vobj.isDataAvailable()) {
                                vobj.setUserObj(ParseUser.getCurrentUser());
                                vobj.setPostObj(posts.get(position));
                                vobj.saveInBackground();
                                Intent intent = new Intent(context, CommentGroupActivity.class);
                                intent.putExtra("postObj", posts.get(position));
                                intent.putExtra("groupObj", posts.get(position).getGroupObj());
                                context.startActivity(intent);
                            }
                        }
                     else {
                        Timber.d("Error: %s", e.getMessage());
                    }
                    }
                });
            }else {
                Intent intent = new Intent(context, CommentGroupActivity.class);
                intent.putExtra("postObj", posts.get(position));
                intent.putExtra("groupObj", posts.get(position).getGroupObj());
                context.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView dp;
        final ImageView verified;
        final ImageView activity;
        final ImageView mediaView;
        final ImageView play;
        final ImageView like_img;
        final ImageView more;
        final TextView name;
        final TextView username;
        final TextView time;
        final TextView feeling;
        final TextView location;
        final TextView like_text;
        final SocialTextView text;
        final SocialTextView bg_text;
        final VoicePlayerView voicePlayerView;
        final LinearLayout likeLayout;
        final LinearLayout commentLayout;
        final LinearLayout viewsLayout;
        final LinearLayout layout;
        final LinearLayout share;
        final TextView noLikes;
        final TextView reactions;
        final TextView noComments;
        final TextView noViews;
        final ImageView thumb;
        final ImageView love;
        final ImageView laugh;
        final ImageView wow;
        final ImageView angry;
        final ImageView sad;
        final LinearLayout likeButton;
        final LinearLayout likeButtonTwo;
        final LinearLayout comment;
        final RelativeLayout line;
        final RelativeLayout ad;
        URLEmbeddedView urlEmbeddedView;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            urlEmbeddedView = itemView.findViewById(R.id.uev);
            dp = itemView.findViewById(R.id.dp);
            verified = itemView.findViewById(R.id.verified);
            name = itemView.findViewById(R.id.name);
            ad = itemView.findViewById(R.id.ad);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            time = itemView.findViewById(R.id.time);
            activity = itemView.findViewById(R.id.activity);
            feeling = itemView.findViewById(R.id.feeling);
            location = itemView.findViewById(R.id.location);
            text = itemView.findViewById(R.id.text);
            mediaView = itemView.findViewById(R.id.mediaView);
            bg_text = itemView.findViewById(R.id.bg_text);
            share = itemView.findViewById(R.id.share);
            play = itemView.findViewById(R.id.play);
            voicePlayerView = itemView.findViewById(R.id.voicePlayerView);
            likeLayout = itemView.findViewById(R.id.likeLayout);
            commentLayout = itemView.findViewById(R.id.commentLayout);
            viewsLayout = itemView.findViewById(R.id.viewsLayout);
            layout = itemView.findViewById(R.id.layout);
            noLikes =  itemView.findViewById(R.id.noLikes);
            reactions =  itemView.findViewById(R.id.reactions);
            noComments  =  itemView.findViewById(R.id.noComments);
            noViews  =  itemView.findViewById(R.id.noViews);
            like_text =  itemView.findViewById(R.id.like_text);
            like_img  =  itemView.findViewById(R.id.like_img);
            thumb  =  itemView.findViewById(R.id.thumb);
            love  =  itemView.findViewById(R.id.love);
            laugh  =  itemView.findViewById(R.id.laugh);
            wow  =  itemView.findViewById(R.id.wow);
            angry  =  itemView.findViewById(R.id.angry);
            likeButton  =  itemView.findViewById(R.id.likeButton);
            sad =  itemView.findViewById(R.id.sad);
            likeButtonTwo =  itemView.findViewById(R.id.likeButtonTwo);
            line = itemView.findViewById(R.id.line);
            more = itemView.findViewById(R.id.more);

            MobileAds.initialize(itemView.getContext(), initializationStatus -> {

            });
            AdLoader.Builder builder = new AdLoader.Builder(itemView.getContext(), itemView.getContext().getString(R.string.native_ad_unit_id));
            builder.forUnifiedNativeAd(unifiedNativeAd -> {
                TemplateView templateView = itemView.findViewById(R.id.my_template);
                templateView.setNativeAd(unifiedNativeAd);
            });

            AdLoader adLoader = builder.build();
            AdRequest adRequest = new AdRequest.Builder().build();
            //adLoader.loadAd(adRequest);
        }
    }

    public boolean updateLikesReactions(Post postObj, String value, MyHolder holder)
    {
        ParseQuery<PostReaction> vq1 = ParseQuery.getQuery(PostReaction.class);
        vq1.whereEqualTo("postObj", postObj);
        vq1.whereEqualTo("userObj", ParseUser.getCurrentUser());
        vq1.getFirstInBackground(new GetCallback<PostReaction>() {
            public void done(PostReaction lobj, ParseException e) {
                if (e == null) {
                    if (lobj.isDataAvailable()) {
                        lobj.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    updatePost(postObj, holder);
                                }
                            }
                        });
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                    if(e.getCode()==101){
                        PostReaction myReaction = new PostReaction();
                        myReaction.setUserObj(ParseUser.getCurrentUser());
                        myReaction.setPostObj(postObj);
                        myReaction.setValue(value);
                        myReaction.saveInBackground( new SaveCallback() {
                            public void done(ParseException e) {
                                if (e == null) {
                                    updatePost(postObj, holder);
                                }
                            }
                        });
                    }
                }
            }
        });
        return true;
    };

    void updatePost(Post postObj, MyHolder holder){
        ParseQuery<PostReaction> vq1 = ParseQuery.getQuery(PostReaction.class);
        vq1.whereEqualTo("postObj", postObj);
        vq1.findInBackground(new FindCallback<PostReaction>() {
            public void done(List<PostReaction> likesList, ParseException e) {
                if (e == null) {
                    if (likesList.size() > 0) {

                        holder.thumb.setVisibility(View.GONE);
                        holder.love.setVisibility(View.GONE);
                        holder.wow.setVisibility(View.GONE);
                        holder.angry.setVisibility(View.GONE);
                        holder.laugh.setVisibility(View.GONE);
                        holder.sad.setVisibility(View.GONE);

                        holder.likeLayout.setVisibility(View.VISIBLE);
                        holder.line.setVisibility(View.VISIBLE);
                        holder.noLikes.setText(String.valueOf(likesList.size()));
                        holder.reactions.setVisibility(View.VISIBLE);

                        for (PostReaction likesUser : likesList){
                            String value = likesUser.getValue();
                            if (value.equals("like")) holder.thumb.setVisibility(View.VISIBLE);
                            if (value.equals("love")) holder.love.setVisibility(View.VISIBLE);
                            if (value.equals("wow")) holder.wow.setVisibility(View.VISIBLE);
                            if (value.equals("angry")) holder.angry.setVisibility(View.VISIBLE);
                            if (value.equals("laugh")) holder.laugh.setVisibility(View.VISIBLE);
                            if (value.equals("sad")) holder.sad.setVisibility(View.VISIBLE);

                            if (likesUser.getUserObj().equals(ParseUser.getCurrentUser())){
                                if(likesList.size()==1){
                                    holder.reactions.setText("You");
                                }else {
                                    holder.reactions.setText("You and "+String.valueOf(likesList.size()-1)+" Others");
                                }

                                if (value.equals("like")){
                                    holder.like_img.setImageResource(R.drawable.ic_thumb);
                                    holder.like_text.setText("Like");
                                    holder.likeButtonTwo.setVisibility(View.GONE);
                                    holder.likeButton.setVisibility(View.VISIBLE);
                                }
                                if (value.equals("love")){
                                    holder.like_img.setImageResource(R.drawable.ic_love);
                                    holder.like_text.setText("Love");
                                    holder.likeButtonTwo.setVisibility(View.GONE);
                                    holder.likeButton.setVisibility(View.VISIBLE);
                                }
                                if (value.equals("laugh")){
                                    holder.like_img.setImageResource(R.drawable.ic_laugh);
                                    holder.like_text.setText("Haha");
                                    holder.likeButtonTwo.setVisibility(View.GONE);
                                    holder.likeButton.setVisibility(View.VISIBLE);
                                }
                                if (value.equals("wow")){
                                    holder.like_img.setImageResource(R.drawable.ic_wow);
                                    holder.like_text.setText("Wow");
                                    holder.likeButtonTwo.setVisibility(View.GONE);
                                    holder.likeButton.setVisibility(View.VISIBLE);
                                }
                                if (value.equals("sad")){
                                    holder.like_img.setImageResource(R.drawable.ic_sad);
                                    holder.like_text.setText("Sad");
                                    holder.likeButtonTwo.setVisibility(View.GONE);
                                    holder.likeButton.setVisibility(View.VISIBLE);
                                }
                                if (value.equals("angry")){
                                    holder.like_img.setImageResource(R.drawable.ic_angry);
                                    holder.like_text.setText("Angry");
                                    holder.likeButtonTwo.setVisibility(View.GONE);
                                    holder.likeButton.setVisibility(View.VISIBLE);
                                }
                            }else{
                                holder.like_img.setImageResource(R.drawable.ic_like);
                                holder.like_text.setText("Like");
                            }
                        }
                    } else {
                        holder.likeButton.setVisibility(View.GONE);
                        holder.likeButtonTwo.setVisibility(View.VISIBLE);
                        holder.likeLayout.setVisibility(View.GONE);
                        holder.line.setVisibility(View.GONE);
                        holder.like_img.setImageResource(R.drawable.ic_like);
                        holder.like_text.setText("Like");
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                    if(e.getCode()==101){
                        holder.likeButton.setVisibility(View.GONE);
                        holder.likeButtonTwo.setVisibility(View.VISIBLE);
                        holder.likeLayout.setVisibility(View.GONE);
                        holder.line.setVisibility(View.GONE);
                        holder.like_img.setImageResource(R.drawable.ic_like);
                        holder.like_text.setText("Like");
                    }
                }
            }
        });
    };
}
