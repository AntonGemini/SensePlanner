package com.sassaworks.senseplanner;

import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.idling.CountingIdlingResource;

public class FirebaseIdlingResource {

    private static final String RESOURCE = "TESTING";

    private static CountingIdlingResource mCountingIdlingRes = new CountingIdlingResource(RESOURCE);

    public static void increment()
    {
        mCountingIdlingRes.increment();
    }

    public static void decrement()
    {
        mCountingIdlingRes.decrement();
    }

    public static IdlingResource getIdlingResource()
    {
        return mCountingIdlingRes;
    }

}
