package com.sassaworks.senseplanner.ui;


import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.sassaworks.senseplanner.CreateTaskActivity;
import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.adapter.ActivityRecordAdapter;
import com.sassaworks.senseplanner.adapter.ActivityViewHolder;
import com.sassaworks.senseplanner.adapter.CategoriesAdapter;
import com.sassaworks.senseplanner.data.ActivityRecord;
import com.sassaworks.senseplanner.data.Appealing;
import com.sassaworks.senseplanner.data.Category;
import com.sassaworks.senseplanner.data.CollectionItem;
import com.sassaworks.senseplanner.data.DayStatistics;
import com.sassaworks.senseplanner.data.HeaderRecord;
import com.sassaworks.senseplanner.data.Mood;
import com.sassaworks.senseplanner.firebaseutils.FirebaseDatabaseHelper;

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
    private static final String DATE_S = "dateS";
    private static final String DATE_F = "dateF";
    private static final String ADAPTER_POSITION = "adapterPosition";
    private static final String FINAL_DATE = "date_f";
    private static final String START_DATE = "date_s";
    private static final String ACTIVITY_POSITION = "activity_position";

    // TODO: Rename and change types of parameters
    private long mDateS=0;
    private long mDateF=0;
    FirebaseDatabase db;
    FirebaseUser user;
    ActivityRecord selectedRecord;

    @BindView(R.id.rv_events) RecyclerView mEventRecyclerView;
    @BindView(R.id.et_date_s) EditText mDateText;
    @BindView(R.id.et_date_f) EditText mDateTextF;
    @BindView(R.id.sp_activities) Spinner mActivityType;

    private ArrayList<String> activitiesList;
    private String selectedCategory = "";
    private int adapterPosition = -1;
    private long finalDate = 0;
    private long startDate = 0;
    private int activityPosition;


    public EventsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EventsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventsFragment newInstance() {
        return new EventsFragment();
    }

    public static EventsFragment newInstance(long timestampS, long timestampF) {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();
        args.putLong(DATE_S,timestampS);
        args.putLong(DATE_F,timestampF);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDateS = getArguments().getLong(DATE_S);
            mDateF = getArguments().getLong(DATE_F);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        ButterKnife.bind(this,view);
        db = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDateText.setOnClickListener(onDateClickListener);
        mDateTextF.setOnClickListener(onDateClickListener);
        if (savedInstanceState != null) {
            adapterPosition = savedInstanceState.getInt(ADAPTER_POSITION);
            activityPosition = savedInstanceState.getInt(ACTIVITY_POSITION);
            mDateS = savedInstanceState.getLong(START_DATE);
            mDateF = savedInstanceState.getLong(FINAL_DATE);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mDateS);
            mDateText.setText(getString(R.string.date_format, String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)),
                    String.valueOf(calendar.get(Calendar.MONTH) + 1), String.valueOf(calendar.get(Calendar.YEAR))));
            calendar.setTimeInMillis(mDateF);
            mDateTextF.setText(getString(R.string.date_format, String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)),
                    String.valueOf(calendar.get(Calendar.MONTH) + 1), String.valueOf(calendar.get(Calendar.YEAR))));
        }

        loadActivitiesList();
        return view;
    }

    public void loadActivitiesList()
    {
        DatabaseReference referActivities = db.getReference().child(getString(R.string.ref_planner)).child(user.getUid()).child(getString(R.string.ref_activities));
        activitiesList = new ArrayList<>();
        activitiesList.add(getString(R.string.all_types));
        RxFirebaseDatabase.observeSingleValueEvent(referActivities, DataSnapshotMapper.listOf(com.sassaworks.senseplanner.data.Activity.class))
                .subscribe(activities -> {
                    for (Category c : activities) {
                        activitiesList.add(c.getName());
                    }
                    fillCategorySpinner();
                });
    }

    private void fillCategorySpinner()
    {
        CategoriesAdapter mActivitiesAdapter = new CategoriesAdapter(getActivity(), activitiesList.toArray(new String[0]));
        mActivitiesAdapter.setDropDownViewResource(R.layout.item_category);
        mActivityType.setAdapter(mActivitiesAdapter);
        mActivityType.setOnItemSelectedListener(onActivityItemSelected);
        if (activityPosition!=0)
        {
            mActivityType.setSelection(activityPosition);
        }
        loadEventsData();
    }

    private View.OnClickListener onDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dateSelected(v);
        }
    };

    private void loadEventsData()
    {
        Calendar calendar = Calendar.getInstance();
//        user = FirebaseAuth.getInstance().getCurrentUser();
//        db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(getString(R.string.ref_planner)).child(user.getUid()).child(getString(R.string.activity_records));
        Query queryEvents = ref.orderByChild("timestamp");
        if (mDateS==0 && mDateF==0)
        {
            calendar.set(Calendar.HOUR,23);
            calendar.set(Calendar.MINUTE,59);
            mDateF = calendar.getTimeInMillis();
            mDateTextF.setText(getString(R.string.date_format, String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)),
                    String.valueOf(calendar.get(Calendar.MONTH) + 1), String.valueOf(calendar.get(Calendar.YEAR))));

            calendar.add(Calendar.MONTH,-1);
            calendar.set(Calendar.HOUR,0);
            calendar.set(Calendar.MINUTE,0);
            mDateS = calendar.getTimeInMillis();
            mDateText.setText(getString(R.string.date_format, String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)),
                    String.valueOf(calendar.get(Calendar.MONTH) + 1), String.valueOf(calendar.get(Calendar.YEAR))));

        }
        queryEvents = queryEvents.startAt(mDateS).endAt(mDateF);
        FirebaseDatabaseHelper helper = new FirebaseDatabaseHelper(queryEvents);
        helper.GetEvents(this,selectedCategory);
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
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mEventRecyclerView.getContext(),
                ((LinearLayoutManager)mEventRecyclerView.getLayoutManager()).getOrientation());
        mEventRecyclerView.addItemDecoration(dividerItemDecoration);
        if (adapterPosition != -1)
            mEventRecyclerView.getLayoutManager().scrollToPosition(adapterPosition);
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
        DatabaseReference statRef = ref.child(getString(R.string.ref_daystatistics)).child(user.getUid()).child(sdf);
        DatabaseReference referAppealing = ref.child(getString(R.string.ref_planner)).child(user.getUid()).child(getString(R.string.ref_appealing));
        DatabaseReference referMood = ref.child(getString(R.string.ref_planner)).child(user.getUid()).child(getString(R.string.ref_mood));


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
        DatabaseReference ref1 = db.getReference(getString(R.string.ref_planner)).child(user.getUid()).child(getString(R.string.activity_records)).child(selectedRecord.getKey());
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

    public void dateSelected(View v) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar c = Calendar.getInstance();
                if (v.getId() == R.id.et_date_s) {
                    mDateText.setText(getString(R.string.date_format, String.valueOf(dayOfMonth), String.valueOf(month + 1), String.valueOf(year)));
                    c.set(year,month,dayOfMonth,0,0);
                    mDateS = c.getTimeInMillis();
                } else if (v.getId() == R.id.et_date_f) {
                    mDateTextF.setText(getString(R.string.date_format, String.valueOf(dayOfMonth), String.valueOf(month + 1), String.valueOf(year)));
                    c.set(year,month,dayOfMonth,23,59);
                    mDateF = c.getTimeInMillis();
                }
                loadEventsData();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }


    public AdapterView.OnItemSelectedListener onActivityItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            TextView tv = view.findViewById(R.id.item_tv_category);
            if (pos > 0) {
                selectedCategory = tv.getText().toString();
            } else if (pos == 0) {
                selectedCategory = "";
            }
            loadEventsData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mEventRecyclerView!=null)
        {
            outState.putInt(ADAPTER_POSITION,
                    ((LinearLayoutManager)mEventRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition());
        }
        if (mActivityType!=null) {
            outState.putInt(ACTIVITY_POSITION, mActivityType.getSelectedItemPosition());
        }
        outState.putLong(START_DATE,mDateS);
        outState.putLong(FINAL_DATE,mDateF);

    }

}
