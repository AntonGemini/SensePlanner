package com.sassaworks.senseplanner.firebaseutils;

import android.content.Context;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sassaworks.senseplanner.data.Activity;
import com.sassaworks.senseplanner.data.ActivityRecord;
import com.sassaworks.senseplanner.data.Category;
import com.sassaworks.senseplanner.data.DayStatistics;

import java.util.ArrayList;

public class FirebaseDatabaseHelper {

    DatabaseReference dbRef;
    String type;
    Query query;

    public interface OnGetItem
    {
        void getItem(Category category, String type);
    }
    private OnGetItem onGetItemInstance;

    public interface OnEventsGetCompleted
    {
        void onEventsGet(ArrayList<DayStatistics> events);
    }
    private OnEventsGetCompleted onEventsGetinstance;

    public interface OnSingleDataLoaded
    {
        void onDataLoaded(DayStatistics ds);
    }
    private OnSingleDataLoaded onSingleDataLoadedInstance;

    public interface OnActivityRecordsGetCompleted
    {
        void onActivityRecordsLoaded(ArrayList<ActivityRecord> data);
    }

    private OnActivityRecordsGetCompleted onActivityLoadedInstance;

    public FirebaseDatabaseHelper(DatabaseReference ref, OnGetItem getItem, String type)
    {
        this.dbRef = ref.child(type);
        this.onGetItemInstance = getItem;
        this.type = type;
    }

    public FirebaseDatabaseHelper( OnEventsGetCompleted context)
    {
        this.onEventsGetinstance = (OnEventsGetCompleted) context;
    }

    public FirebaseDatabaseHelper(DatabaseReference ref, OnSingleDataLoaded context)
    {
        dbRef = ref;
        this.onSingleDataLoadedInstance = context;
    }

    public FirebaseDatabaseHelper(Query dbRef)
    {
        this.query = dbRef;
    }


    public void getCategoriesList()
    {

        dbRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Category category = dataSnapshot.getValue(Activity.class);

                onGetItemInstance.getItem(category,type);
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
        ArrayList<DayStatistics> events = new ArrayList<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 1;
                for(DataSnapshot ds:dataSnapshot.getChildren())
                {
                    DayStatistics record = ds.getValue(DayStatistics.class);
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


    public DayStatistics GetDayStat(String date)
    {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DayStatistics ds = dataSnapshot.getValue(DayStatistics.class);
                onSingleDataLoadedInstance.onDataLoaded(ds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        DayStatistics stat = null;

        return stat;
    }


    public void GetEvents(OnActivityRecordsGetCompleted context)
    {
        this.onActivityLoadedInstance = context;
        ArrayList<ActivityRecord> records = new ArrayList<>();
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren())
                {
                    ActivityRecord record = ds.getValue(ActivityRecord.class);
                    records.add(record);
                }
                context.onActivityRecordsLoaded(records);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
//        query.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                for (DataSnapshot ds:dataSnapshot.getChildren())
//                {
//                    ActivityRecord record = ds.getValue(ActivityRecord.class);
//                    records.add(record);
//                }
//                context.onActivityRecordsLoaded(records);
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
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
