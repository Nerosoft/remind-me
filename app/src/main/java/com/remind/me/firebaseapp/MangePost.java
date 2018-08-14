package com.remind.me.firebaseapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnSuccessListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Stack;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nero on 28/07/18.
 */

public class MangePost {
    public ArrayList<UserPost> userPosts=new ArrayList();
    String userName;
    Context context;
    private Firebase fireRef;
    public static MangePost mangePost=null;

    public  MangePost (String username, Context context){
            this.userName=username;
            this.fireRef = new Firebase("https://yourfirebase/UserPost/"+this.userName);
            this.context=context;
            this.userPosts=new ArrayList<>();
            mangePost=this;
    }

    public static MangePost getMangePost(){
        return mangePost;
    }


    void setUpFirePost(final OnSuccessListener<ArrayList<UserPost>> successListener){
        fireRef.orderByPriority().addValueEventListener(new com.firebase.client.ValueEventListener() {

            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                userPosts=new ArrayList<>();
                int size = (int) dataSnapshot.getChildrenCount();
                for (com.firebase.client.DataSnapshot ds : dataSnapshot.getChildren()){
                   UserPost post=ds.getValue(UserPost.class);
                   post.setKey(ds.getKey());
                    userPosts.add( size==0 ? --size : 0,post);
                }
                successListener.onSuccess(userPosts);

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }



    public void removePost(final String key){
        new AlertDialog.Builder(context)
                .setTitle("Warning")
                .setMessage("Do you really want to delete this post ?")
                .setIcon(R.drawable.warning2)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        fireRef.child(key).removeValue();
                        Toast.makeText(context, "This Post Deleted Successfully", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }


    //----Add Post TO FireBase----------------
    public DialogFragment setPost(Post post ){
        SetPost setPost = new SetPost().getDialogRestPass();
        setPost.setPostListener(post);
        return setPost;
    }
    public Post setPost(){
        return new Post() {
            @Override
            public void editPost(final UserPost post, final Dialog dialog) {
               // fireRef.child(post.getTime()).setValue(post);
                fireRef.push().setValue(post);
                dialog.cancel();
            }
        };
    }
    public static class SetPost extends DialogFragment{
        Button buttonC;
        Button buttonO;
        EditText postText;
        ImageView morLine;
        Post  post;
        public SetPost getDialogRestPass(){
            return this;
        }

        public void setPostListener(Post post){
            this.post =post;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view=inflater.inflate(R.layout.rm_create_post,null);
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            postText=view.findViewById(R.id.post_text);
            morLine=view.findViewById(R.id.mor_line);
            RM_SharedPreference.showSavedImage(getActivity(), (CircleImageView)view.findViewById(R.id.user_photo));
            builder.setView(view)
                    // Add action buttons
                    .setPositiveButton("Post", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    })
                    .setNegativeButton("Cancel" ,new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
            buttonO = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            buttonO.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                    String date = df.format(Calendar.getInstance().getTime());
                    String textPost=postText.getText().toString();
                    post.editPost(new UserPost(date,textPost),getDialog());
                }
            });

            buttonC = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            buttonC.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().cancel();
                }
            });
            postText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    checkPostLine();

                }
            });
            return dialog;
        }

        void checkPostLine(){
            if (postText.getLineCount() > 3 && morLine.getVisibility()==View.INVISIBLE)
                morLine.setVisibility(View.VISIBLE);
        else
            if (postText.getLineCount() <= 3 && morLine.getVisibility()==View.VISIBLE)
                morLine.setVisibility(View.INVISIBLE);
        }

    }


 //--------------
    interface Post{
        public void editPost(UserPost post, Dialog dialog);
    }
}

