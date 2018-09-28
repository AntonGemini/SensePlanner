package com.sassaworks.senseplanner.firebaseutils;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class FIrebaseApp extends Application {

    private static FIrebaseApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        /* Enable disk persistence  */
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    public static synchronized FIrebaseApp getInstance()
    {
        return mInstance;
    }
}
