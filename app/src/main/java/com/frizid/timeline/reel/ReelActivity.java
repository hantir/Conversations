package com.frizid.timeline.reel;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.frizid.timeline.R;
import com.frizid.timeline.adapter.AdapterReel;
import com.frizid.timeline.model.Follow;
import com.frizid.timeline.model.Reel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class ReelActivity extends AppCompatActivity {

    ViewPager2 viewPager2,viewPager1;
    List<Reel> reels, reelList;
    List<String> idList;

    private static String type = "one";
    public static String getType() {
        return type;
    }
    public ReelActivity(){
    }

    long startTime;

    @Override
    protected void onStart() {
        super.onStart();
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_reel);


        viewPager2 = findViewById(R.id.videoPager);
        viewPager1 = findViewById(R.id.videoPagerOne);

        reels = new ArrayList<>();
        reelList = new ArrayList<>();

        idList = new ArrayList<>();

        getAllReels();

        //TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tabLayout.getSelectedTabPosition() == 0) {
                    getAllReels();
                    viewPager1.setVisibility(View.GONE);
                    viewPager2.setVisibility(View.VISIBLE);
                    type = "one";
                 } else if (tabLayout.getSelectedTabPosition() == 1) {
                    getFollowing();
                    viewPager1.setVisibility(View.VISIBLE);
                    viewPager2.setVisibility(View.GONE);
                    type = "two";
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    private void getFollowing() {
        ParseQuery<Follow> obj = new ParseQuery<Follow>(Follow.class);
        obj.whereEqualTo("fromObj",ParseUser.getCurrentUser());
        obj.findInBackground(new FindCallback<Follow>() {
            @Override
            public void done(List<Follow> objects, ParseException e) {
                idList.clear();
                if (e == null) {
                    if (!objects.isEmpty()) {
                        idList.add(ParseUser.getCurrentUser().getObjectId());
                        for (Follow flo : objects) {
                            ParseQuery<Reel> query = ParseQuery.getQuery(Reel.class);
                            query.whereEqualTo("userObj", flo.getToObj());
                            query.findInBackground(new FindCallback<Reel>() {
                                @Override
                                public void done(List<Reel> objects, ParseException e) {
                                    reelList.addAll(objects);
                                    Collections.shuffle(reels);
                                    viewPager1.setAdapter(new AdapterReel(reelList));
                                    if (getIntent().hasExtra("position")) {
                                        String position = getIntent().getStringExtra("position");
                                        String mType = getIntent().getStringExtra("type");
                                        if (mType.equals("one")) {
                                            viewPager2.setCurrentItem(Integer.parseInt(position));
                                        } else {
                                            viewPager1.setCurrentItem(Integer.parseInt(position));
                                        }
                                    }
                                    if (new AdapterReel(reelList).getItemCount() == 0) {
                                        findViewById(R.id.no).setVisibility(View.VISIBLE);
                                        viewPager1.setVisibility(View.GONE);
                                    } else {
                                        findViewById(R.id.no).setVisibility(View.GONE);
                                        viewPager1.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                    } else {
                        findViewById(R.id.no).setVisibility(View.VISIBLE);
                        viewPager1.setVisibility(View.GONE);
                    }
                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }

    private void getAllReels() {
        ParseQuery<Reel> reelq = ParseQuery.getQuery(Reel.class);
        reelq.findInBackground(new FindCallback<Reel>() {
            public void done(List<Reel> reell , ParseException e) {
                if (e == null) {
                    if(!reell.isEmpty()){
                        for (Reel reply: reell){
                            reels.add(reply);
                            Collections.shuffle(reels);
                            viewPager2.setAdapter(new AdapterReel(reels));
                            if (getIntent().hasExtra("position")){
                                String position  = getIntent().getStringExtra("position");
                                String mType  = getIntent().getStringExtra("type");
                                if (mType.equals("one")){
                                    viewPager2.setCurrentItem(Integer.parseInt(position));
                                }else {
                                    viewPager1.setCurrentItem(Integer.parseInt(position));
                                }
                            }
                            if (new AdapterReel(reels).getItemCount() == 0){
                                findViewById(R.id.no).setVisibility(View.VISIBLE);
                                viewPager2.setVisibility(View.GONE);
                            }else {
                                findViewById(R.id.no).setVisibility(View.GONE);
                                viewPager2.setVisibility(View.VISIBLE);
                            }
                        }
                    }else{
                        findViewById(R.id.no).setVisibility(View.VISIBLE);
                        viewPager2.setVisibility(View.GONE);
                    }

                } else {
                    Timber.d("Error: %s", e.getMessage());
                }
            }
        });
    }
}