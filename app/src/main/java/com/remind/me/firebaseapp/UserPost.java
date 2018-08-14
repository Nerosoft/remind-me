package com.remind.me.firebaseapp;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by nero on 27/07/18.
 */

@IgnoreExtraProperties
public class UserPost {

   public String time;
   public String text;
   public String key;

    public UserPost() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UserPost(String time, String text) {
        this.time = time;
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
