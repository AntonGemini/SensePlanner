package com.sassaworks.senseplanner.firebaseutils;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class FIrebaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        /* Enable disk persistence  */
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
