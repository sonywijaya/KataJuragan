package com.sonywijaya.katajuragan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by ekadarmaputra on 5/28/17.
 */

public class SessionManager{
    public static final String MyPreferences = "MyPref";
    private static final String isLogin = "IsLoggedIn";
    public static final String username = "username";
    public static final String pass = "password";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;

    int PRIVATE_MODE = 0;

    public SessionManager(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(MyPreferences, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void createLoginSession(String name){
        editor.putBoolean(isLogin, true);
        editor.putString(username, name);
        editor.commit();
    }

    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(context, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            context.startActivity(i);
        }

    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(username, sharedPreferences.getString(username, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        context.startActivity(i);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return sharedPreferences.getBoolean(isLogin, false);
    }
}
