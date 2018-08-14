package com.remind.me.firebaseapp;

/**
 * Created by nero on 02/08/18.
 */

public class SubscribeUser {


   public String subCount;
   public String subUser;

    public SubscribeUser(){}

    public SubscribeUser(String subCount, String subUser) {
        this.subCount = subCount;
        this.subUser = subUser;
    }

    public String getSubCount() {
        return subCount;
    }

    public void setSubCount(String subCount) {
        this.subCount = subCount;
    }

    public String getSubUser() {
        return subUser;
    }

    public void setSubUser(String subUser) {
        this.subUser = subUser;
    }
}
