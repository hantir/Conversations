package com.frizid.timeline.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.frizid.timeline.App;
import com.frizid.timeline.R;
import com.frizid.timeline.model.Group;
import com.frizid.timeline.model.Participants;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterParticipants extends RecyclerView.Adapter<AdapterParticipants.HolderParticipantsAdd>{

    private final Context context;
    private final List<ParseUser> userList;
    private final String groupId;
    private final String myGroupRole;


    private RequestQueue requestQueue;
    private boolean notify = false;

    public AdapterParticipants(Context context, List<ParseUser> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantsAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.user_list, parent, false);
        return new HolderParticipantsAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantsAdd holder, int position) {

        requestQueue = Volley.newRequestQueue(context);

        ParseUser modelUser = userList.get(position);
        String mName = modelUser.getString("name");
        String mUsername = modelUser.getUsername();
        try{
            String dp = modelUser.getParseFile("photo").getUrl();
            Picasso.get().load(dp).placeholder(R.drawable.avatar).into(holder.circleImageView);
        }catch(NullPointerException ignored){
            Picasso.get().load(R.drawable.avatar).into(holder.circleImageView);
        }
        String uid = modelUser.getObjectId();

        holder.name.setText(mName);

        if (userList.get(position).getBoolean("verified"))  holder.verified.setVisibility(View.VISIBLE);

        checkAlreadyExists(modelUser, holder,mUsername);
        holder.itemView.setOnClickListener(v -> {
            ParseQuery<Participants> pq = ParseQuery.getQuery(Participants.class);
            pq.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, groupId));
            pq.getFirstInBackground(new GetCallback<Participants>() {
                public void done(Participants pobj, ParseException e) {
                    if (e == null) {
                        if (pobj.isDataAvailable()) {
                            String hisPrevRole = ""+pobj.getRole();
                            String[] options;
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Choose Option");
                            if (myGroupRole.equals("creator")){
                                if (hisPrevRole.equals("admin")){
                                    options = new String[]{"Remove Admin", "Remove User"};
                                    builder.setItems(options, (dialog, which) -> {
                                        if (which == 0) {
                                            removeAdmin(modelUser, holder);

                                        } else {
                                            removeParticipants(modelUser, holder);
                                        }
                                    }).show();
                                }
                                else if (hisPrevRole.equals("participant"))
                                {
                                    options = new String[]{"Make Admin", "Remove User"};
                                    builder.setItems(options, (dialog, which) -> {
                                        if (which == 0) {
                                            makeAdmin(modelUser, holder);

                                        } else {
                                            removeParticipants(modelUser, holder);
                                        }
                                    }).show();

                                }
                            }
                            else if (myGroupRole.equals("admin")){
                                switch (hisPrevRole) {
                                    case "creator":
                                        Snackbar.make(holder.itemView, "Creator of the group", Snackbar.LENGTH_LONG).show();
                                        break;
                                    case "admin":
                                        options = new String[]{"Remove Admin", "Remove User"};
                                        builder.setItems(options, (dialog, which) -> {
                                            if (which == 0) {
                                                removeAdmin(modelUser, holder);

                                            } else {
                                                removeParticipants(modelUser, holder);
                                            }
                                        }).show();
                                        break;
                                    case "participant":
                                        options = new String[]{"Make Admin", "Remove User"};
                                        builder.setItems(options, (dialog, which) -> {
                                            if (which == 0) {
                                                makeAdmin(modelUser, holder);

                                            } else {
                                                removeParticipants(modelUser, holder);
                                            }
                                        }).show();
                                        break;
                                }
                            }
                        }else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Add Participant")
                                    .setMessage("Add this user in this group?")
                                    .setPositiveButton("Add", (dialog, which) -> addParticipants(modelUser, holder)).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).show();

                        }
                    }
                    else {
                        Timber.d("Error: %s", e.getMessage());
                    }}
            });
        });
        holder.username.setText(mUsername);
    }

    private void addParticipants(ParseUser modelUser, HolderParticipantsAdd holder) {
        Participants pq = new Participants();
        pq.setUserObj(modelUser);
        pq.setRole("participant");
        pq.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(context, "User added", Toast.LENGTH_SHORT).show();
                }else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
        notify = true;
        if (notify){
            App.sendNotification(modelUser.getObjectId(), ParseUser.getCurrentUser().getUsername(), "added you to group");
        }
        notify = false;

    }

    private void makeAdmin(ParseUser modelUser, HolderParticipantsAdd holder) {
        ParseQuery<Participants> pq = ParseQuery.getQuery(Participants.class);
        pq.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, groupId));
        pq.whereEqualTo("userObj", modelUser.getObjectId());
        pq.getFirstInBackground(new GetCallback<Participants>() {
            public void done(Participants pobj, ParseException e) {
                if (e == null) {
                    if (pobj.isDataAvailable()) {
                        pobj.setRole("admin");
                        pobj.saveInBackground();
                        Toast.makeText(context, "Admin made", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });

        notify = true;
        if (notify){
            App.sendNotification(modelUser.getObjectId(), ParseUser.getCurrentUser().getUsername(), "Made you admin");
        }
        notify = false;

    }

    private void removeParticipants(ParseUser modelUser, HolderParticipantsAdd holder) {
        ParseQuery<Participants> pq = ParseQuery.getQuery(Participants.class);
        pq.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, groupId));
        pq.whereEqualTo("userObj", modelUser.getObjectId());
        pq.getFirstInBackground(new GetCallback<Participants>() {
            public void done(Participants pobj, ParseException e) {
                if (e == null) {
                    if (pobj.isDataAvailable()) {
                        pobj.deleteInBackground();
                        Toast.makeText(context, "User removed from the group", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });
    }

    private void removeAdmin(ParseUser modelUser, HolderParticipantsAdd holder) {
        ParseQuery<Participants> pq = ParseQuery.getQuery(Participants.class);
        pq.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, groupId));
        pq.whereEqualTo("userObj", modelUser.getObjectId());
        pq.getFirstInBackground(new GetCallback<Participants>() {
            public void done(Participants pobj, ParseException e) {
                if (e == null) {
                    if (pobj.isDataAvailable()) {
                        pobj.setRole("participant");
                        pobj.saveInBackground();
                        Toast.makeText(context, "Admin removed", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });
    }

    private void checkAlreadyExists(ParseUser modelUser, HolderParticipantsAdd holder, String mUsername) {
        ParseQuery<Participants> pq = ParseQuery.getQuery(Participants.class);
        pq.whereEqualTo("groupObj", ParseObject.createWithoutData(Group.class, groupId));
        pq.whereEqualTo("userObj", modelUser.getObjectId());
        pq.getFirstInBackground(new GetCallback<Participants>() {
            public void done(Participants pobj, ParseException e) {
                if (e == null) {
                    if (pobj.isDataAvailable()) {
                        String hisRole = ""+pobj.getRole();
                        holder.username.setText(mUsername + " - " +hisRole);
                    }
                }
                else {
                    Timber.d("Error: %s", e.getMessage());
                }}
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class HolderParticipantsAdd extends RecyclerView.ViewHolder{

        private final CircleImageView circleImageView;
        private final TextView name;
        private final TextView username;
        private final ImageView verified;

        public HolderParticipantsAdd(@NonNull View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
            verified  = itemView.findViewById(R.id.verified);

        }
    }
}
