package com.remind.me.firebaseapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.remind.me.firebaseapp.RM_Login.fireIDData;


/**
 * Created by nero on 23/07/18.
 */

public class RM_SharedPreference {

    private Context context;
    private SharedPreferences preferences;

    public RM_SharedPreference(Context context){
        this.context=context;
        preferences = context.getSharedPreferences("remindpre",Context.MODE_PRIVATE );
    }

    public static void AddPhoto(Context context) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction("android.intent.action.GET_CONTENT");
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0);
        } else {
            Toast.makeText(context, "Please it is not allowed here", Toast.LENGTH_SHORT).show();
        }
    }

    public static void setImage(Context context, ImageView Login_Photo, Intent data) {
        try {
            Login_Photo.setImageResource(0);
            Login_Photo.setBackgroundResource(0);
            Login_Photo.setImageDrawable(new BitmapDrawable(context.getResources(),
                    MediaStore.Images.Media.getBitmap(context.getContentResolver(), data.getData())));
            Login_Photo.buildDrawingCache();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deletePhoto(CircleImageView user_photo) {

        if (preferences.contains("userphoto")) {
            user_photo.setImageResource(0);
            user_photo.setBackgroundResource(0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove("userphoto");
            editor.commit();
        }
    }

    public static CircleImageView showSavedImage(Context context, CircleImageView profile_photo) {
        //Show saved image when app is open
        SharedPreferences  preferences = context.getSharedPreferences("remindpre",Context.MODE_PRIVATE );
        String img_str = preferences.getString("userphoto", "");//BuildConfig.FLAVOR
        if (!img_str.equals("")) {
            byte[] imageAsBytes = Base64.decode(img_str.getBytes(), 0);
            profile_photo.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
        }
        return profile_photo;
    }

    public void saveImageInPreferences(CircleImageView user_photo) {
            this.deletePhoto(user_photo);
            BitmapDrawable bitmapDrawable = (BitmapDrawable) user_photo.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            String img_str = Base64.encodeToString(stream.toByteArray(), 0);
            SharedPreferences.Editor editor = this.preferences.edit();
            editor.putString("userphoto", img_str);
            editor.commit();

    }

    public void saveLoginImageInPreferences(byte[] bytes){
        String img_str = Base64.encodeToString(bytes, 0);
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putString("userphoto", img_str);
        editor.commit();
    }

    public void seveUserState(String user_name){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_login_state", user_name);
        editor.commit();
    }

    public boolean checkUserLoginState(){
        return preferences.contains("user_login_state")?
                true:false;
    }

    public RM_User getUserInfo(){
        try {
            JSONObject User=new JSONObject(preferences.getString("user_login_state",""));
            return new RM_User(User.getString("name"), User.getString("gender"),
                    User.getString("number"), User.getString("birthday"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void userLogOut(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("user_login_state");
        editor.remove("userphoto");
        editor.commit();
    }

    public static void loadUserPhoto(String UserName, OnSuccessListener<byte[]> successListener){
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://"+fireIDData+".appspot.com/");
        StorageReference storageRef = storage.getReference();
        StorageReference islandRef = storageRef.child("user_photo/"+UserName+".jpg");

        final long ONE_MEGABYTE = 1024 * 1024 *5;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(successListener).
                addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors

                    }
                });


    }

}

