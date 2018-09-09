package com.sassaworks.senseplanner.firebaseutils;

import android.content.Context;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sassaworks.senseplanner.data.Activity;
import com.sassaworks.senseplanner.data.ActivityRecord;
import com.sassaworks.senseplanner.data.Category;
import com.sassaworks.senseplanner.data.DayStatistics;

import java.util.ArrayList;
import java.util.Map;

public class FirebaseDatabaseHelper {

    DatabaseReference dbRef;
    String type;
    Query query;
    FirebaseDatabase db;

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

    public interface OnRemoveFromStatistics
    {
        void onRemoveStatisticsLoaded (DayStatistics ds);
    }
    private OnRemoveFromStatistics onRemoveFromStatisticsInstance;

    public FirebaseDatabaseHelper(DatabaseReference ref)
    {
        dbRef = ref;
    }

    public FirebaseDatabaseHelper(DatabaseReference ref, OnGetItem getItem, String type, String orderField)
    {
        this.dbRef = ref.child(type);
        this.query = dbRef.orderByChild(orderField);
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

        this.query.addChildEventListener(new ChildEventListener() {

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


    public void removeRecordFromStat(DatabaseReference ref, ActivityRecord removeRecord,
                                     Map<String,Integer> appealing, Map<String,Integer> mood, OnRemoveFromStatistics context) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DayStatistics ds = dataSnapshot.getValue(DayStatistics.class);
                if (ds!=null)
                {
                    removeFromStatistics(ds,removeRecord,appealing,mood,context,ref);
                }
                else
                {
                    onRemoveFromStatisticsInstance.onRemoveStatisticsLoaded(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void removeFromStatistics(DayStatistics ds, ActivityRecord removeRecord,
                                      Map<String,Integer> appealing, Map<String,
            Integer> mood,OnRemoveFromStatistics context,
                                      DatabaseReference ref) {
        onRemoveFromStatisticsInstance = context;
        DayStatistics newDs = new DayStatistics();
        int newMoodNum = ds.getMood_num() - 1;
        int newAppealNum = ds.getAppeal_num() - 1;

        if (newMoodNum == 0)
        {
            ref.removeValue();
        }
        else {
            float oldTotalMood = ds.getMood_avg() * ds.getMood_num();
            float oldTotalAppeal = ds.getAppeal_avg() * ds.getAppeal_num();

            float newMoodAvg = (oldTotalMood - mood.get(removeRecord.getMoodType()))/newMoodNum;
            float newAppealAvg = (oldTotalAppeal - appealing.get(removeRecord.getJobAddiction()))/newAppealNum;

            newDs.setMood_num(newMoodNum);
            newDs.setAppeal_num(newAppealNum);
            newDs.setMood_avg(newMoodAvg);
            newDs.setAppeal_avg(newAppealAvg);
            newDs.setTimestamp(ds.getTimestamp());
            ref.setValue(newDs);
        }
        onRemoveFromStatisticsInstance.onRemoveStatisticsLoaded(newDs);
    }


    public void GetEvents(OnActivityRecordsGetCompleted context, String activityClause)
    {
        this.onActivityLoadedInstance = context;

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<ActivityRecord> records = new ArrayList<>();

                for (DataSnapshot ds:dataSnapshot.getChildren())
                {
                    ActivityRecord record = ds.getValue(ActivityRecord.class);
                    if (activityClause.equals("") || record.getCategory().equals(activityClause)) {
                        record.setKey(ds.getKey());
                        records.add(record);
                    }
                }
                context.onActivityRecordsLoaded(records);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



}
