package com.remind.me.firebaseapp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingProfile extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener {

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.6f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;

    private LinearLayout mTitleContainer;
    private TextView mTitle;
    static Drawable drawable;

    public static byte[] bytes;

    SP sp = new SP();

    public Menu menu;
    public boolean isSub = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_profile);
        bindActivity();

        startAlphaAnimation(mTitle, 0, View.INVISIBLE);

        MangePost mangePost = new MangePost(getIntent()
                .getExtras().getString("name"), this);

        mangePost.setUpFirePost(new OnSuccessListener<ArrayList<UserPost>>() {
            @Override
            public void onSuccess(ArrayList<UserPost> userPost) {

                sp.rm_post.userPosts = userPost;
                sp.rm_post.notifyDataSetChanged();
            }
        });

    }


    public static class SP extends Fragment {
        public RM_Post rm_post;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            FrameLayout frameLayout = (FrameLayout) inflater
                    .inflate(R.layout.content_main2_activity_navigation, container, false);
            RecyclerView recyclerView = frameLayout.findViewById(R.id.recyclerview);

            rm_post = new RM_Post(getActivity(),
                    drawable,
                    getActivity().getIntent().getExtras().getString("name"),
                    getActivity().getIntent().getExtras().getString("state"));
            rm_post.setupRecyclerView(recyclerView);
            return frameLayout;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (getIntent().getExtras().getString("state").equals("me") ||
                MangeSubscribeUser.getSubscribeUser().isSub(getIntent().getExtras().getString("name"))) {
            menu.findItem(R.id.menu_share).setIcon(R.drawable.ic_heart_outline_red_24dp);
            isSub = true;
        }
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home)
            finish();
        else if (menuItem.getItemId() == R.id.menu_share) {

            if (getIntent().getExtras().getString("state").equals("me"))
                Toast.makeText(this, "is you", Toast.LENGTH_SHORT).show();
            else
                if (isSub)
                    removeSubUser();
                else
                    subUser();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    void removeSubUser() {
        new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Do you really want to un subscribe user ?")
                .setIcon(R.drawable.warning2)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int whichButton) {
                        MangeSubscribeUser.getSubscribeUser().removeSubscribeUser(new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                menu.findItem(R.id.menu_share).setIcon(R.drawable.ic_heart_outline_white_24dp);
                                Toast.makeText(getApplicationContext(),
                                        "Un subscribe user Successfully", Toast.LENGTH_LONG).show();
                                isSub = false;
                                dialog.dismiss();
                            }
                        }, getIntent().getExtras().getString("name"));
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    void subUser() {
        new AlertDialog.Builder(this)
                .setTitle("Subscribe")
                .setMessage("Do you really want to subscribe the user ?")
                .setIcon(R.drawable.boy)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int whichButton) {

                        MangeSubscribeUser.getSubscribeUser().setSubscribeUser(new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                menu.findItem(R.id.menu_share).setIcon(R.drawable.ic_heart_outline_red_24dp);
                                Toast.makeText(getApplicationContext(),
                                        "User Subscribe is Successfully", Toast.LENGTH_LONG).show();
                                isSub = true;
                                dialog.dismiss();

                            }
                        }, getIntent().getExtras().getString("name"));


                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }


    //------------------------------------------------------------------


    private void bindActivity() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mTitle = (TextView) findViewById(R.id.main_textview_title);
        mTitleContainer = (LinearLayout) findViewById(R.id.main_linearlayout_title);
        TextView userSubs = (TextView) findViewById(R.id.user_subs);
        CircleImageView profilePhoto = (CircleImageView) findViewById(R.id.profile_photo);

        if (getIntent().getExtras().getString("state").equals("me")) {
            RM_SharedPreference.showSavedImage(this, profilePhoto);
            drawable = RM_SharedPreference.showSavedImage(this,
                    new CircleImageView(this)).getDrawable();

            //'int org.json.JSONArray.length()' on a null object reference//---------------------------------------------------------
            userSubs.setText(getIntent().getExtras().getString("name") + " : " +
                    MangeSubscribeUser.getSubscribeUser().jsonArraySubUser.length() + " subscribe");

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MangePost.getMangePost().setPost(MangePost.getMangePost().setPost())
                            .show(getFragmentManager(), "");
                }
            });
        } else {
            //byte[] imageAsBytes = getIntent().getExtras().getByteArray("img");
            profilePhoto.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            drawable = profilePhoto.getDrawable();
            userSubs.setText(getIntent().getExtras().getString("name")+"@rmindme.com");
        }
        TextView userName = (TextView) findViewById(R.id.user_name);
        userName.setText(getIntent().getExtras().getString("name"));



        AppBarLayout mAppBarLayout = (AppBarLayout) findViewById(R.id.main_appbar);
        mAppBarLayout.addOnOffsetChangedListener(this);


        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.post_profile, sp);
        fragmentTransaction.commit();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if (!mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }


}
