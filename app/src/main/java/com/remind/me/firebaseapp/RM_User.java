package com.remind.me.firebaseapp;


import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by nero on 24/07/18.
 */
@IgnoreExtraProperties
public class RM_User {
     String name;
     String password;
     String number;
     String birthday;
     String gender;
     String key;

    public RM_User(){

    }
    public RM_User(String name, String password,
                String number, String birthday, String gender, String key) {
        this.name = name;
        this.password = password;
        this.gender = gender;
        this.number = number;
        this.birthday = birthday;
        this.key = key;

    }

    public RM_User(String name, String number,
                   String birthday, String gender) {
        this.name = name;
        this.gender = gender;
        this.number = number;
        this.birthday = birthday;

    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    public String getBirthday() {
        return birthday;
    }
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getKey() {
        return this.key;
    }
    public void setKey(String Key) {this.key = Key;}

}


