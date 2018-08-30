package com.sassaworks.senseplanner.ui;


import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sassaworks.senseplanner.CreateTaskActivity;
import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.adapter.ActivityRecordAdapter;
import com.sassaworks.senseplanner.adapter.ActivityViewHolder;
import com.sassaworks.senseplanner.data.ActivityRecord;
import com.sassaworks.senseplanner.data.Appealing;
import com.sassaworks.senseplanner.data.Category;
import com.sassaworks.senseplanner.data.CollectionItem;
import com.sassaworks.senseplanner.data.DayStatistics;
import com.sassaworks.senseplanner.data.HeaderRecord;
import com.sassaworks.senseplanner.data.Mood;
import com.sassaworks.senseplanner.firebaseutils.FirebaseDatabaseHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import durdinapps.rxfirebase2.DataSnapshotMapper;
import durdinapps.rxfirebase2.RxFirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventsFragment extends Fragment implements FirebaseDatabaseHelper.OnActivityRecordsGetCompleted,
        ActivityViewHolder.OnMoreClickListener,
        FirebaseDatabaseHelper.OnRemoveFromStatistics{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    FirebaseDatabase db;
    FirebaseUser user;
    ActivityRecord selectedRecord;

    @BindView(R.id.rv_events) RecyclerView mEventRecyclerView;


    public EventsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventsFragment newInstance(String param1, String param2) {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        ButterKnife.bind(this,view);
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("planner").child(user.getUid()).child("activity_records");
        FirebaseDatabaseHelper helper = new FirebaseDatabaseHelper(ref.orderByChild("timestamp"));
        helper.GetEvents(this);
        return view;
    }

    @Override
    public void onActivityRecordsLoaded(ArrayList<ActivityRecord> data) {
        ArrayList<CollectionItem> formatedRecords = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date currentItemDate = new Date(1970,1,1);
        for(ActivityRecord ac : data)
        {
            calendar.setTimeInMillis(ac.getTimestamp());
            calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH),0,0,0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

            if (!dateFormat.format(currentItemDate).equals(dateFormat.format(calendar.getTime())))
            {
                currentItemDate = calendar.getTime();
                HeaderRecord header = new HeaderRecord();
                header.setFormattedDate(calendar.getTimeInMillis());
                formatedRecords.add(header);
            }
            formatedRecords.add(ac);
        }

        ActivityRecordAdapter adapter = new ActivityRecordAdapter(getActivity(),formatedRecords,this);
        mEventRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        mEventRecyclerView.setLayoutManager(layoutManager);
    }


    @Override
    public void onMoreClick(ActivityRecord record, View v) {
        PopupMenu popup = new PopupMenu(getActivity(),v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_activity,popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.edit_activity)
                {
                    EditActivityRecord(record);
                }
                else if (id == R.id.delete_activity)
                {
                    DeleteActivityRecord(record);
                }
                return true;
            }
        });
        popup.show();
    }

    private void EditActivityRecord(ActivityRecord record) {

        Intent intent = new Intent(getActivity(),CreateTaskActivity.class);
        intent.putExtra(CreateTaskFragment.ACTIVITY_RECORD,record);
        startActivity(intent);

    }

    private void DeleteActivityRecord(ActivityRecord record)
    {
        selectedRecord = record;
        final HashMap<String,Integer> moodMap = new HashMap<>();
        final HashMap<String,Integer> appealingMap = new HashMap<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(record.getTimestamp());
        String sdf = new SimpleDateFormat("dd-MM-yyyy").format(calendar.getTime());


        DatabaseReference ref = db.getReference();
        DatabaseReference statRef = ref.child("daystatistics").child(user.getUid()).child(sdf);
        DatabaseReference referAppealing = ref.child("planner").child(user.getUid()).child("appealing");
        DatabaseReference referMood = ref.child("planner").child(user.getUid()).child("mood");


        FirebaseDatabaseHelper fbh = new FirebaseDatabaseHelper(statRef);


        RxFirebaseDatabase.observeSingleValueEvent(referMood,DataSnapshotMapper.listOf(Mood.class))
                 .concatMap(mood->{
                     for(Mood m:mood)
                     {
                        moodMap.put(m.getName(),m.getNumValue());
                     }
                     return RxFirebaseDatabase.observeSingleValueEvent(referAppealing, DataSnapshotMapper.listOf(Appealing.class));
                 }).subscribe(apps->{
                    for(Appealing a:apps)
                    {
                        appealingMap.put(a.getName(),a.getNumValue());
                    }
                    fbh.removeRecordFromStat(statRef,record,appealingMap,moodMap,this);
                    removeEventFromCalendar(record);
                });

    }

    @Override
    public void onRemoveStatisticsLoaded(DayStatistics ds) {
        DatabaseReference ref1 = db.getReference("planner").child(user.getUid()).child("activity_records").child(selectedRecord.getKey());
        ref1.removeValue();
    }

    private void removeEventFromCalendar(ActivityRecord record)
    {
        if (record.getEventId()!= 0)
        {
            Uri deleteUri = null;
            deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, record.getEventId());
            int rows = getActivity().getContentResolver().delete(deleteUri, null, null);
        }
    }
}
