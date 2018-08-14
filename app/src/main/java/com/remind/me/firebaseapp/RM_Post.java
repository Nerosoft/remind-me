package com.remind.me.firebaseapp;

/**
 * Created by nero on 27/07/18.
 */

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by root on 21/01/18.
 */


public class RM_Post
        extends RecyclerView.Adapter<RM_Post.ViewHolder> {


    private final TypedValue mTypedValue = new TypedValue();
    private RecyclerView recyclerView;
    Drawable profileUser;
    String userName, stateOfUser;
    public ArrayList<UserPost> userPosts;
    public ArrayList<SubUser> subPosts=null;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        //int OldHight;
        TextView UserName, PostTime, PostText;
        CircleImageView userPhoto;
        ImageView option;
        boolean okMore=true,onMore=true;
        ImageView morLine;

        // كونستراكرتور لاجل تعريف كل العناصر للعرض في الريسيكل
        public ViewHolder(View view) {
            super(view);
            mView = view;
            // this.OldHight = this.mView.getLayoutParams().height;
            UserName = view.findViewById(R.id.user_name);
            PostTime = view.findViewById(R.id.post_time);
            PostText = view.findViewById(R.id.post_text);
            userPhoto = view.findViewById(R.id.user_photo);
            option = view.findViewById(R.id.option);
            morLine = view.findViewById(R.id.mor_line);

        }


        public void setUpPost(UserPost post, Drawable profileUser, String userName, String stateOfUser) {
            UserName.setText(userName);
            PostTime.setText(post.getTime());
            PostText.setText(post.getText());
            //image
            userPhoto.setImageDrawable(profileUser);
            if (stateOfUser.equals("me"))
                deletPost(post.getKey());
            else option.setVisibility(View.GONE);
            checkPostLine();

        }

        void deletPost(final String key) {
            option.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MangePost.getMangePost().removePost(key);
                }
            });
        }


        @Override
        public String toString() {
            return super.toString();
        }


        void checkPostLine() {
            final ViewTreeObserver obs = PostText.getViewTreeObserver();
            obs.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (PostText.getLineCount() > 3 && okMore) {
                        okMore=false;
                        morLine.setVisibility(View.VISIBLE);
                        PostText.setLines(3);
                    }
                }
            });

            morLine.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if(onMore){
                      PostText.setLines(PostText.getLineCount());
                        onMore=false;
                        morLine.setRotationX(180);
                    }else {
                        PostText.setLines(3);
                        onMore=true;
                        morLine.setRotationX(360);
                    }

                    //else if (okMore==false){
                      //  okMore=true;
                     //   PostText.setSingleLine(true);
                   // }
                }
            });
        }
    }


    public RM_Post(Context context, Drawable profileUser, String userName,String stateOfUser) {
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        this.userPosts = new ArrayList<>();
        this.profileUser = profileUser;
        this.userName = userName;
        this.stateOfUser = stateOfUser;
        this.subPosts=subPosts;
    }

    public RM_Post(Context context, ArrayList<SubUser>subPosts) {
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        this.stateOfUser = "??";
        this.subPosts=subPosts;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rm_post, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (subPosts!=null)
            holder.setUpPost(this.subPosts.get(position).userPost,
                    this.subPosts.get(position).imgUser, this.subPosts.get(position).nameUser ,stateOfUser);
        else
        holder.setUpPost(this.userPosts.get(position), this.profileUser, userName ,stateOfUser);

    } // end setup view

    @Override
    public int getItemCount() {
        if (subPosts!=null) return subPosts.size();
        else
        return userPosts.size();
    }


    public void setupRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        this.recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        this.recyclerView.setAdapter(this);


    }


    public RecyclerView getRecyclerView() {
        return this.recyclerView;
    }

}