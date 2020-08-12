package com.jadoo.risha.besafe;

import android.app.Application;

import com.firebase.client.Firebase;

public class fire extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
