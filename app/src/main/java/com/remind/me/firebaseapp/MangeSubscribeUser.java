package com.remind.me.firebaseapp;

import android.content.Context;


import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;

import static com.remind.me.firebaseapp.RM_Login.fireIDData;


/**
 * Created by nero on 02/08/18.
 */

public class MangeSubscribeUser {
    Context context;
    String userName;
    private Firebase fireRef;
    static MangeSubscribeUser subscribeUser;
    public JSONArray jsonArraySubUser;

    public MangeSubscribeUser(String username, Context context) {
        this.fireRef = new Firebase("https://"+fireIDData+".firebaseio.com/subscribe");
        this.context = context;
        this.userName = username;
        subscribeUser = this;

    }

    public static MangeSubscribeUser getSubscribeUser() {
        return subscribeUser;
    }


    public void setUpSubscribeUser(final OnSuccessListener<JSONArray> successListener) {
        new Firebase("https://"+fireIDData+".firebaseio.com/subscribe")
                .addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(userName).exists()) {
                    SubscribeUser subscribeUser = new SubscribeUser("1",
                            new JSONArray().put(userName).toString());
                    fireRef.child(userName).setValue(subscribeUser);
                } else {

                    SubscribeUser subscribeUser = (dataSnapshot.child(userName).getValue(SubscribeUser.class));
                    try {
                        jsonArraySubUser = new JSONArray(subscribeUser.getSubUser());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                if (jsonArraySubUser != null)
                    successListener.onSuccess(jsonArraySubUser);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void setSubscribeUser(final OnSuccessListener<Boolean> successListener, final String subUser) {


        JSONArray jsonArray = jsonArraySubUser;
        int newCount = jsonArray.length() + 1;
        fireRef.child(userName).setValue(new SubscribeUser(Integer.toString(newCount),
                jsonArray.put(subUser).toString()));

        successListener.onSuccess(true);

    }

    public boolean isSub(String user) {
        for (int i = 0; i < jsonArraySubUser.length(); i++)
            try {
                if (user.equals(jsonArraySubUser.get(i))) return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        return false;
    }

    public void removeSubscribeUser(final OnSuccessListener<Boolean> successListener, final String subUser) {
        JSONArray jsonArray = jsonArraySubUser;
        for (int i = 0; i < jsonArray.length(); i++)
            try {
                if (subUser.equals(jsonArray.get(i)))
                    jsonArray.remove(i);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        int newCount = jsonArray.length();
        fireRef.child(userName).setValue(new SubscribeUser(Integer.toString(newCount),
                jsonArray.toString()));

        successListener.onSuccess(true);
    }
}
