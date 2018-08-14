package com.remind.me.firebaseapp;

import android.graphics.drawable.Drawable;

/**
 * Created by nero on 02/08/18.
 */

public class SubUser {
    public String nameUser;
    public Drawable imgUser;
    public UserPost userPost;

    public SubUser(String nameUser, Drawable imgUser, UserPost userPost) {
        this.nameUser = nameUser;
        this.imgUser = imgUser;
        this.userPost = userPost;
    }

}
