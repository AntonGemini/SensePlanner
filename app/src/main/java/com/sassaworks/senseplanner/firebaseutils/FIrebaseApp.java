package com.sassaworks.senseplanner.firebaseutils;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized FIrebaseApp getInstance()
    {
        return mInstance;
    }
}
