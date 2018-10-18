package com.sassaworks.senseplanner.ui;


import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import static com.sassaworks.senseplanner.ui.CreateTaskFragment.CALENDAR_PERMISSION;

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
    ActivityRecord recordToDelete;

    @BindView(R.id.rv_events) RecyclerView mEventRecyclerView;
    @BindView(R.id.et_date_s) EditText mDateText;
    @BindView(R.id.et_date_f) EditText mDateTextF;
    @BindView(R.id.sp_activities) Spinner mActivityType;

    @BindView(R.id.fabToday) FloatingActionButton fabToday;
    @BindView(R.id.fabYesterday) FloatingActionButton mFabYesterday;
    @BindView(R.id.fabTomorrow) FloatingActionButton mFabTomorrow;

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

        FloatingActionButton fab = view.findViewById(R.id.fabMenu);
        LinearLayout todayLayout = view.findViewById(R.id.todayLayout);
        LinearLayout yesterdayLayout = view.findViewById(R.id.yesterdayLayout);
        LinearLayout tomorrowLayout = view.findViewById(R.id.tomorrowLayout);

        todayLayout.setVisibility(View.INVISIBLE);
        yesterdayLayout.setVisibility(View.INVISIBLE);
        tomorrowLayout.setVisibility(View.INVISIBLE);

        Animation mShowButton = AnimationUtils.loadAnimation(getActivity(),R.anim.show_button);
        Animation mHideButton = AnimationUtils.loadAnimation(getActivity(),R.anim.hide_button);
        Animation mShowLayout = AnimationUtils.loadAnimation(getActivity(),R.anim.show_layout);
        Animation mHideLayout = AnimationUtils.loadAnimation(getActivity(),R.anim.hide_layout);
        Animation mShowTopLayout = AnimationUtils.loadAnimation(getActivity(),R.anim.show_top_layout);
        Animation mHideTopLayout = AnimationUtils.loadAnimation(getActivity(),R.anim.hide_top_layout);
        Animation mShowBottomLayout = AnimationUtils.loadAnimation(getActivity(),R.anim.show_bottom_layout);
        Animation mHideBottomLayout = AnimationUtils.loadAnimation(getActivity(),R.anim.hide_bottom_layout);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (todayLayout.getVisibility() == View.VISIBLE)
                {
                    fab.startAnimation(mHideButton);
                    todayLayout.setVisibility(View.INVISIBLE);
                    yesterdayLayout.setVisibility(View.INVISIBLE);
                    tomorrowLayout.setVisibility(View.INVISIBLE);
                    yesterdayLayout.startAnimation(mHideBottomLayout);
                    todayLayout.startAnimation(mHideLayout);
                    tomorrowLayout.startAnimation(mHideTopLayout);
                }
                else {
                    todayLayout.setVisibility(View.VISIBLE);
                    yesterdayLayout.setVisibility(View.VISIBLE);
                    fab.startAnimation(mShowButton);
                    yesterdayLayout.startAnimation(mShowBottomLayout);
                    todayLayout.startAnimation(mShowLayout);
                    tomorrowLayout.startAnimation(mShowTopLayout);
                }
            }
        });

        fabToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),CreateTaskActivity.class);
                intent.putExtra(CreateTaskFragment.STARTING_DAY,0);
                startActivity(intent);
            }
        });

        mFabYesterday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),CreateTaskActivity.class);
                intent.putExtra(CreateTaskFragment.STARTING_DAY,-1);
                startActivity(intent);
            }
        });

        mFabTomorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),CreateTaskActivity.class);
                intent.putExtra(CreateTaskFragment.STARTING_DAY,1);
                startActivity(intent);
            }
        });

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
        //mActivitiesAdapter.setDropDownViewResource(R.layout.item_category);
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

        DatabaseReference ref = db.getReference(getString(R.string.ref_planner)).child(user.getUid()).child(getString(R.string.activity_records));
        Query queryEvents = ref.orderByChild("timestamp");
        if (mDateS==0 && mDateF==0)
        {
            calendar.add(Calendar.DAY_OF_MONTH,1);
            calendar.set(Calendar.HOUR_OF_DAY,23);
            calendar.set(Calendar.MINUTE,59);
            mDateF = calendar.getTimeInMillis();
            mDateTextF.setText(getString(R.string.date_format, String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)),
                    String.valueOf(calendar.get(Calendar.MONTH) + 1), String.valueOf(calendar.get(Calendar.YEAR))));

            calendar.add(Calendar.MONTH,-1);
            calendar.set(Calendar.HOUR_OF_DAY,0);
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
        LinearSpacingItemDecoration dividerItemDecoration = new LinearSpacingItemDecoration();
        mEventRecyclerView.addItemDecoration(dividerItemDecoration);
//
//          ((LinearLayoutManager)mEventRecyclerView.getLayoutManager()).getOrientation());
//        mEventRecyclerView.addItemDecoration(dividerItemDecoration);
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
        recordToDelete = record;

        if (record.getEventId()!= 0)
        {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                requestPermissions(new String[]{Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_CALENDAR},CALENDAR_PERMISSION);
            }
            else
            {
                deleteActivityRecord();
            }
        }
    }

    private void deleteActivityRecord()
    {
        Uri deleteUri = null;
        deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, recordToDelete.getEventId());
        int rows = getActivity().getContentResolver().delete(deleteUri, null, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CALENDAR_PERMISSION)
        {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                deleteActivityRecord();
            }
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
        if (mEventRecyclerView!=null && mEventRecyclerView.getAdapter()!=null && mEventRecyclerView.getAdapter().getItemCount()!= 0)
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

    public class LinearSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private final Paint mPaint = new Paint();

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            //View recordItem = view.findViewById(R.id.item_record);
            View headerItem = view.findViewById(R.id.item_header);
            int position = ((RecyclerView.LayoutParams)view.getLayoutParams()).getViewAdapterPosition();
            mPaint.setColor(Color.DKGRAY);
            float thickness = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,1f,
                    getContext().getResources().getDisplayMetrics());
            mPaint.setStrokeWidth(thickness);
            if (headerItem!=null && position == 0)
            {
                outRect.top = (int)(mPaint.getStrokeWidth() + 2f);
//                outRect.top = (int)(mPaint.getStrokeWidth() + 10f);
            }
            else if (headerItem != null && position > 0) {
                outRect.top = (int)(mPaint.getStrokeWidth() + 20f);
            }
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
//            c.save();
//            int right = parent.getWidth() - parent.getPaddingRight();
            int childCount = parent.getChildCount();

            for (int i = 0; i < childCount; i++)
            {
                final View childView = parent.getChildAt(i);
                View currentItem = childView.findViewById(R.id.item_record);

                if (currentItem != null) {
//                    c.drawLine(childView.getLeft(), childView.getBottom(),
//                            childView.getRight(), childView.getBottom(), mPaint);
//                    c.drawLine(childView.getLeft(), childView.getTop(),
//                            childView.getRight(), childView.getTop(), mPaint);
//                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                            ViewGroup.LayoutParams.WRAP_CONTENT);
//                    int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,8,
//                            getContext().getResources().getDisplayMetrics());
//                    params.setMargins(0,px,0,px);
                    //childView.setLayoutParams(params);
                }
            }

        }
    }

}
