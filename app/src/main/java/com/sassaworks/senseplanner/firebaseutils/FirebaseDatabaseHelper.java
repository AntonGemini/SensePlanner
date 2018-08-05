package com.sassaworks.senseplanner.firebaseutils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.sassaworks.senseplanner.data.Activity;

import java.util.ArrayList;

public class FirebaseDatabaseHelper {

    DatabaseReference dbRef;
    String type;

    public interface OnGetItem
    {
        void getItem(String categoryName, String type);
    }

    private OnGetItem onGetItemInstance;

    public FirebaseDatabaseHelper(DatabaseReference ref, OnGetItem getItem, String type)
    {
        this.dbRef = ref.child(type);
        this.onGetItemInstance = getItem;
        this.type = type;
    }

    public void getCategoriesList()
    {

        dbRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Activity activity = dataSnapshot.getValue(Activity.class);
                onGetItemInstance.getItem(activity.getName(),type);
                //categories.updateViewData(activities);
                //getData(dataSnapshot, categories);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

//    private void getData(DataSnapshot dataSnapshot, ArrayList<String> categories) {
//        categories.clear();
//        for (DataSnapshot snapshot:dataSnapshot.getChildren())
//        {
//            Activity category = snapshot.getValue(Activity.class);
//            categories.add(category.getName());
//        }
//    }
}
