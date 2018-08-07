package com.sassaworks.senseplanner.firebaseutils;

import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.sassaworks.senseplanner.data.Activity;
import com.sassaworks.senseplanner.data.ActivityRecord;

import java.util.ArrayList;

public class FirebaseDatabaseHelper {

    DatabaseReference dbRef;
    String type;

    public interface OnGetItem
    {
        void getItem(String categoryName, String type);
    }
    private OnGetItem onGetItemInstance;

    public interface OnEventsGetCompleted
    {
        void onEventsGet(ArrayList<ActivityRecord> events);
    }
    private OnEventsGetCompleted onEventsGetinstance;

    public FirebaseDatabaseHelper(DatabaseReference ref, OnGetItem getItem, String type)
    {
        this.dbRef = ref.child(type);
        this.onGetItemInstance = getItem;
        this.type = type;
    }

    public FirebaseDatabaseHelper(DatabaseReference ref, OnEventsGetCompleted context)
    {
        this.dbRef = ref;
        this.onEventsGetinstance = context;
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

    public void getActivityRecords(DatabaseReference ref)
    {
        ArrayList<ActivityRecord> events = new ArrayList<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 1;
                for(DataSnapshot ds:dataSnapshot.getChildren())
                {
                    ActivityRecord record = ds.getValue(ActivityRecord.class);
                    events.add(record);
                }
                Log.d("RSC3","Method called " + String.valueOf(i));
                onEventsGetinstance.onEventsGet(events);
                i++;
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
