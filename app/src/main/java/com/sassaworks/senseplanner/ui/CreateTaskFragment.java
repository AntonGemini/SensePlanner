package com.sassaworks.senseplanner.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sassaworks.senseplanner.FirebaseIdlingResource;
import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.adapter.CategoriesAdapter;
import com.sassaworks.senseplanner.data.ActivityRecord;
import com.sassaworks.senseplanner.data.Category;
import com.sassaworks.senseplanner.data.DayStatistics;
import com.sassaworks.senseplanner.firebaseutils.FirebaseDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateTaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateTaskFragment extends Fragment implements FirebaseDatabaseHelper.OnGetItem,
        FirebaseDatabaseHelper.OnSingleDataLoaded, FirebaseDatabaseHelper.OnRemoveFromStatistics {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ACTIVITY_RECORD = "activity_record";
    public static final int CALENDAR_PERMISSION = 4;
    public static final int UPDATE_CALENDAR_PERMISSION = 5;


    private static final String CATEGORY_POSITION = "category_position";
    private static final String APPEALING_POSITION = "appealing_position";
    private static final String MOOD_POSITION = "mood_position";
    public static final String STARTING_DAY = "starting_day";

    public static final int START_TODAY = 0;
    public static final int START_YESTERDAY = -1;
    public static final int START_TOMORROW = 1;

    // TODO: Rename and change types of parameters
    FirebaseUser user;
    FirebaseDatabase db;
    DatabaseReference refCategories;
    DatabaseReference statRef;


    @BindView(R.id.sp_activities)
    Spinner mCategorySpinner;
    @BindView(R.id.sp_appealing)
    Spinner mAppealingSpinner;
    @BindView(R.id.sp_mood)
    Spinner mMoodSpinner;

    HashMap<String, Integer> activities;
    HashMap<String, Integer> appealing;
    HashMap<String, Integer> mood;

    CategoriesAdapter mCategoryAdapter;
    CategoriesAdapter mAppealingAdapter;
    CategoriesAdapter mMoodAdapter;

    @BindView(R.id.et_name)
    EditText mNameText;
    @BindView(R.id.et_description)
    EditText mDescrText;

    @BindView(R.id.et_date)
    EditText mDateText;
    @BindView(R.id.et_time)
    EditText mTimeText;
    @BindView(R.id.et_date_f)
    EditText mDateTextF;
    @BindView(R.id.et_time_f)
    EditText mTimeTextF;
    @BindView(R.id.cb_notify)
    CheckBox mNotifyCheckbox;

    @BindView(R.id.bt_save_event)
    Button mSaveButton;

    int mYear, mYearF;
    int mMonth, mMonthF;
    int mDayOfMonth, mDayOfMonthF;
    int mHour, mHourF;
    int mMinute, mMinuteF;

    Calendar selectedTimestamp;
    Calendar selectedTimestampF;

    ActivityRecord activityRecord;
    String startingActivity;
    private long eventid = 0;

    private int mCategoryPosition=-1;
    private int mAppealingPosition=-1;
    private int mMoodPosition=-1;

    private long mDefaultTimestamp=-1;
    boolean mIsIdleSet = false;


    public CreateTaskFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CreateTaskFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateTaskFragment newInstance(ActivityRecord record) {
        CreateTaskFragment fragment = new CreateTaskFragment();
        Bundle args = new Bundle();
        if (record != null) {
            args.putParcelable(ACTIVITY_RECORD, record);
            fragment.setArguments(args);
        }
        return fragment;
    }

    public static CreateTaskFragment newInstance(String activity) {
        CreateTaskFragment fragment = new CreateTaskFragment();
        Bundle args = new Bundle();
        if (activity != "") {
            args.putString("activity", activity);
            fragment.setArguments(args);
        }

        return fragment;
    }

    public static CreateTaskFragment newInstance(int startingDate) {
        CreateTaskFragment fragment = new CreateTaskFragment();
        Bundle args = new Bundle();
        args.putInt(STARTING_DAY, startingDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            Log.d("TIMEZONE","TimeZone   "+tz.getDisplayName(false, TimeZone.SHORT)+" Timezon id :: " +tz.getID());
            calendar.setTimeZone(tz);
            if (getArguments().getParcelable(ACTIVITY_RECORD) != null) {
                activityRecord = getArguments().getParcelable(ACTIVITY_RECORD);
            }
            else if (getArguments().getString("activity") != null) {
                startingActivity = getArguments().getString("activity");
                mDefaultTimestamp = calendar.getTimeInMillis();
            }
            else if (getArguments().containsKey(STARTING_DAY))
            {

                int startDay = getArguments().getInt(STARTING_DAY);
                switch(startDay)
                {
                    case START_YESTERDAY:
                        calendar.add(Calendar.DAY_OF_MONTH,-1);
                        break;
                    case START_TOMORROW:
                        calendar.add(Calendar.DAY_OF_MONTH,1);
                        break;
                }
                mDefaultTimestamp = calendar.getTimeInMillis();
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_task, container, false);
        ButterKnife.bind(this, view);

        FirebaseIdlingResource.increment();
        mSaveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SaveEvent();
            }
        });

        mDateText.setOnClickListener(onDateClickListener);
        mDateTextF.setOnClickListener(onDateClickListener);

        mTimeText.setOnClickListener(onTimeClickListener);
        mTimeTextF.setOnClickListener(onTimeClickListener);

        activities = new HashMap<>();
        appealing = new HashMap<>();
        mood = new HashMap<>();

        mCategoryAdapter = new CategoriesAdapter(getActivity(), activities.keySet().toArray(new String[0]));
        mAppealingAdapter = new CategoriesAdapter(getActivity(), appealing.keySet().toArray(new String[0]));
        mMoodAdapter = new CategoriesAdapter(getActivity(), mood.keySet().toArray(new String[0]));

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseDatabase.getInstance();
        refCategories = db.getReference("planner").child(user.getUid());

        FirebaseDatabaseHelper helperActivities = new FirebaseDatabaseHelper(refCategories, this, "activities", "name");
        FirebaseDatabaseHelper helperAppealing = new FirebaseDatabaseHelper(refCategories, this, "appealing", "numValue");
        FirebaseDatabaseHelper helperMood = new FirebaseDatabaseHelper(refCategories, this, "mood", "numValue");

        mCategorySpinner.setAdapter(mCategoryAdapter);
        mAppealingSpinner.setAdapter(mAppealingAdapter);
        mMoodSpinner.setAdapter(mMoodAdapter);


        helperActivities.getCategoriesList();
        helperAppealing.getCategoriesList();
        helperMood.getCategoriesList();

        if (activityRecord != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getDefault());
            calendar.setTimeInMillis(activityRecord.getTimestamp());
            int recordYear = calendar.get(Calendar.YEAR);
            int recordMonth = calendar.get(Calendar.MONTH);
            int recordDay = calendar.get(Calendar.DAY_OF_MONTH);
            int recordHour = calendar.get(Calendar.HOUR_OF_DAY);
            int recordMinute = calendar.get(Calendar.MINUTE);
            mYear = recordYear;
            mMonth = recordMonth;
            mDayOfMonth = recordDay;
            mHour = recordHour;
            mMinute = recordMinute;

            mNameText.setText(activityRecord.getName());
            mDescrText.setText(activityRecord.getDesciption());

            mDateText.setText(getString(R.string.date_format, String.valueOf(recordDay), String.valueOf(recordMonth+1), String.valueOf(recordYear)));
            mTimeText.setText(getString(R.string.time_format, recordHour, recordMinute));

            /*End date*/
            calendar.setTimeInMillis(activityRecord.getTimestampF());
            recordYear = calendar.get(Calendar.YEAR);
            recordMonth = calendar.get(Calendar.MONTH);
            recordDay = calendar.get(Calendar.DAY_OF_MONTH);
            recordHour = calendar.get(Calendar.HOUR_OF_DAY);
            recordMinute = calendar.get(Calendar.MINUTE);
            mDateTextF.setText(getString(R.string.date_format, String.valueOf(recordDay), String.valueOf(recordMonth+1), String.valueOf(recordYear)));
            mTimeTextF.setText(getString(R.string.time_format, recordHour, recordMinute));

            mYearF = recordYear;
            mMonthF = recordMonth;
            mDayOfMonthF = recordDay;
            mHourF = recordHour;
            mMinuteF = recordMinute;
            eventid = activityRecord.getEventId();

            mNotifyCheckbox.setChecked(activityRecord.isWithNotify());
        }

        if (mDefaultTimestamp!=-1)
        {
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            System.out.println("TimeZone   "+tz.getDisplayName(false, TimeZone.SHORT)+" Timezon id :: " +tz.getID());
            calendar.setTimeZone(tz);

            calendar.setTimeInMillis(mDefaultTimestamp);
            int recordYear = calendar.get(Calendar.YEAR);
            int recordMonth = calendar.get(Calendar.MONTH);
            int recordDay = calendar.get(Calendar.DAY_OF_MONTH);
            int recordHour = calendar.get(Calendar.HOUR_OF_DAY);
            int recordMinute = calendar.get(Calendar.MINUTE);
            mDateText.setText(getString(R.string.date_format, String.valueOf(recordDay), String.valueOf(recordMonth+1), String.valueOf(recordYear)));
            mTimeText.setText(getString(R.string.time_format, recordHour, recordMinute));
            mYear = recordYear;
            mMonth = recordMonth;
            mDayOfMonth = recordDay;
            mHour = recordHour;
            mMinute = recordMinute;

            /*End date*/
            calendar.add(Calendar.HOUR_OF_DAY,1);
            recordYear = calendar.get(Calendar.YEAR);
            recordMonth = calendar.get(Calendar.MONTH);
            recordDay = calendar.get(Calendar.DAY_OF_MONTH);
            recordHour = calendar.get(Calendar.HOUR_OF_DAY);
            recordMinute = calendar.get(Calendar.MINUTE);
            mDateTextF.setText(getString(R.string.date_format, String.valueOf(recordDay), String.valueOf(recordMonth+1), String.valueOf(recordYear)));
            mTimeTextF.setText(getString(R.string.time_format, recordHour, recordMinute));

            mYearF = recordYear;
            mMonthF = recordMonth;
            mDayOfMonthF = recordDay;
            mHourF = recordHour;
            mMinuteF = recordMinute;
        }

        if (savedInstanceState!=null)
        {
            mCategoryPosition = savedInstanceState.getInt(CATEGORY_POSITION);
            mAppealingPosition = savedInstanceState.getInt(APPEALING_POSITION);
            mMoodPosition = savedInstanceState.getInt(MOOD_POSITION);
            startingActivity = null;
        }

        return view;
    }


    @Override
    public void getItem(Category category, String type) {

        if (type == "activities") {
            activities.put(category.getName(), category.getNumValue());
            String[] a = activities.keySet().toArray(new String[0]);
            Arrays.sort(a);
            mCategoryAdapter.updateData(a);
            if (activityRecord != null && category.getName().equals(activityRecord.getCategory())
                    || startingActivity != null || mCategoryPosition != -1) {
                int position;
                if (startingActivity != null) {
                    position = mCategoryAdapter.getPosition(startingActivity);
                }
                else if (mCategoryPosition != -1)
                {
                    position = mCategoryPosition;
                }
                else {
                    position = mCategoryAdapter.getPosition(category.getName());
                }
                mCategorySpinner.setSelection(position);
            }

            if (!mIsIdleSet)
            {
                FirebaseIdlingResource.decrement();
                mIsIdleSet = true;
            }

        } else if (type == "appealing") {
            appealing.put(category.getName(), category.getNumValue());
            appealing = sortHashByValue(appealing);
            mAppealingAdapter.updateData(appealing.keySet().toArray(new String[0]));
            if (activityRecord != null && category.getName().equals(activityRecord.getJobAddiction())
                    || mAppealingPosition != -1) {
                if (mAppealingPosition!=-1)
                {
                    mAppealingSpinner.setSelection(mAppealingPosition);
                }
                else
                {
                    int position = mAppealingAdapter.getPosition(category.getName());
                    mAppealingSpinner.setSelection(position);
                }
            }
        } else if (type == "mood") {
            mood.put(category.getName(), category.getNumValue());
            mood = sortHashByValue(mood);
            mMoodAdapter.updateData(mood.keySet().toArray(new String[0]));
            if (activityRecord != null && category.getName().equals(activityRecord.getMoodType())
                    || mMoodPosition != -1) {
                if (mMoodPosition!=-1)
                {
                    mMoodSpinner.setSelection(mMoodPosition);
                }
                else
                {
                    int position = mMoodAdapter.getPosition(category.getName());
                    mMoodSpinner.setSelection(position);
                }
            }
        }

    }

    public void SaveEvent() {

        if (selectedTimestamp == null || selectedTimestampF == null)
            setActivityDate();

        //Date date = new Date(selectedTimestamp.getTimeInMillis());
        String sdf = new SimpleDateFormat("dd-MM-yyyy").format(selectedTimestamp.getTime());
        statRef = db.getReference().child("daystatistics").child(user.getUid()).child(sdf);
        FirebaseDatabaseHelper fbh = new FirebaseDatabaseHelper(statRef, this);
        fbh.GetDayStat(sdf);

    }

    private void setActivityDate() {
        selectedTimestamp = Calendar.getInstance();
        selectedTimestampF = Calendar.getInstance();
        if (mYear == 0 && mMonth == 0 && mDayOfMonth == 0 && activityRecord != null) {
            selectedTimestamp.setTimeInMillis(activityRecord.getTimestamp());
        } else {
            selectedTimestamp.set(mYear, mMonth, mDayOfMonth);
        }

        if (mYearF == 0 && mMonthF == 0 && mDayOfMonthF == 0 && activityRecord != null) {
            selectedTimestampF.setTimeInMillis(activityRecord.getTimestampF());
        } else {
            selectedTimestampF.set(mYearF, mMonthF, mDayOfMonthF);
        }
    }

    @Override
    public void onDataLoaded(DayStatistics ds) {
        String name = mNameText.getText().toString();
        String desc = mDescrText.getText().toString();

        selectedTimestampF = Calendar.getInstance();

        if (mYear == 0 && mMonth == 0 && mDayOfMonth == 0 && activityRecord != null) {
            selectedTimestamp.setTimeInMillis(activityRecord.getTimestamp());
        } else {
            selectedTimestamp.set(mYear, mMonth, mDayOfMonth, mHour, mMinute);
        }
        Long timespan = selectedTimestamp.getTimeInMillis();

        if (mYearF == 0 && mMonthF == 0 && mDayOfMonthF == 0 && activityRecord != null) {
            selectedTimestampF.setTimeInMillis(activityRecord.getTimestampF());
        } else {
            selectedTimestampF.set(mYearF, mMonthF, mDayOfMonthF, mHourF, mMinuteF);
        }
        Long timespanF = selectedTimestampF.getTimeInMillis();


        String moodStr = ((TextView) mMoodSpinner.getSelectedView().findViewById(R.id.item_tv_category)).getText().toString();
        String appealingStr = ((TextView) mAppealingSpinner.getSelectedView().findViewById(R.id.item_tv_category)).getText().toString();
        String categoryStr = ((TextView) mCategorySpinner.getSelectedView().findViewById(R.id.item_tv_category)).getText().toString();

        boolean withNotify = mNotifyCheckbox.isChecked();



        String key;
        if (activityRecord == null) {
            key = refCategories.child("activity_records").push().getKey();
        } else {
            key = activityRecord.getKey();
        }

        ActivityRecord record = new ActivityRecord(name, timespan, categoryStr, moodStr, appealingStr, desc, withNotify, timespanF);

        DayStatistics newDs = new DayStatistics();
        if (ds == null) {
            newDs.setAppeal_avg(appealing.get(appealingStr));
            newDs.setMood_avg(mood.get(moodStr));
            newDs.setAppeal_num(1);
            newDs.setMood_num(1);

        } else {
            int newMoodNum = ds.getMood_num() + 1;
            int newAppealNum = ds.getAppeal_num() + 1;
            float oldTotalMood = ds.getMood_avg() * ds.getMood_num();
            float oldTotalAppeal = ds.getAppeal_avg() * ds.getAppeal_num();

            float newMoodAvg = (oldTotalMood + mood.get(moodStr)) / newMoodNum;
            float newAppealAvg = (oldTotalAppeal + appealing.get(appealingStr)) / newAppealNum;

            newDs.setMood_num(newMoodNum);
            newDs.setAppeal_num(newAppealNum);
            newDs.setMood_avg(newMoodAvg);
            newDs.setAppeal_avg(newAppealAvg);
        }

        if (mYear == 0 && mMonth == 0 && mDayOfMonth == 0 && activityRecord != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(activityRecord.getTimestamp());
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            selectedTimestamp.set(mYear, mMonth, mDayOfMonth);
        } else {
            selectedTimestamp.set(mYear, mMonth, mDayOfMonth);
        }

        newDs.setTimestamp(selectedTimestamp.getTimeInMillis());


        if (activityRecord!=null && activityRecord.getEventId()!=0)
        {
            //updateCalendarEvent(mNotifyCheckbox.isChecked());
            updateCalendarEventPermission();
        }
        else if ((activityRecord==null || activityRecord.getEventId()==0) && mNotifyCheckbox.isChecked())
        {
            createCalendarEvent();
        }

        record.setEventId(this.eventid);
        refCategories.child("activity_records").child(key).setValue(record);
        statRef.setValue(newDs);
        if (activityRecord != null) {
            removeFromStatistics(activityRecord);
        } else {
            if (!mNotifyCheckbox.isChecked() ||
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED)
            {
                getActivity().finish();
            }
        }

    }

    private void removeFromStatistics(ActivityRecord removeRecord) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(removeRecord.getTimestamp());
        String sdf = new SimpleDateFormat("dd-MM-yyyy").format(calendar.getTime());
        statRef = db.getReference().child("daystatistics").child(user.getUid()).child(sdf);
        FirebaseDatabaseHelper fbh = new FirebaseDatabaseHelper(statRef, this);
        fbh.removeRecordFromStat(statRef, removeRecord, appealing, mood, this);
    }

    //callback from firebase

    public void dateSelected(View v) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if (v.getId() == R.id.et_date) {
                    //mDateText.setText(dayOfMonth + "-" + (month + 1) + "-" + year);
                    mDateText.setText(getString(R.string.date_format, String.valueOf(dayOfMonth), String.valueOf(month + 1), String.valueOf(year)));
                    mYear = year;
                    mMonth = month;
                    mDayOfMonth = dayOfMonth;
                } else if (v.getId() == R.id.et_date_f) {
                    mDateTextF.setText(getString(R.string.date_format, String.valueOf(dayOfMonth), String.valueOf(month + 1), String.valueOf(year)));
                    mYearF = year;
                    mMonthF = month;
                    mDayOfMonthF = dayOfMonth;
                }
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    public void timeSelected(View v) {
        final Calendar calendar = Calendar.getInstance();

        TimePickerDialog dialog = new TimePickerDialog(getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (v.getId() == R.id.et_time) {
                            mHour = hourOfDay;
                            mMinute = minute;
                            mTimeText.setText(getString(R.string.time_format,hourOfDay,minute));
                        } else if (v.getId() == R.id.et_time_f) {
                            mHourF = hourOfDay;
                            mMinuteF = minute;
                            mTimeTextF.setText(getString(R.string.time_format,hourOfDay,minute));
                        }


                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private View.OnClickListener onDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dateSelected(v);
        }
    };

    private View.OnClickListener onTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            timeSelected(v);
        }
    };


    private void updateCalendarEvent(boolean isChecked) {
        Uri updateUri = null;
        ContentValues cv = new ContentValues();
        long eventID = activityRecord.getEventId();
        if (isChecked) {
            cv.put(CalendarContract.Events.DTSTART, selectedTimestamp.getTimeInMillis());
            cv.put(CalendarContract.Events.DTEND, selectedTimestampF.getTimeInMillis());
            cv.put(CalendarContract.Events.TITLE, mNameText.getText().toString());
            cv.put(CalendarContract.Events.DESCRIPTION, mDescrText.getText().toString());
            cv.put(CalendarContract.Events.EVENT_TIMEZONE, selectedTimestamp
                    .getTimeZone().getID());
            updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
            int rows = getActivity().getContentResolver().update(updateUri, cv, null, null);
        }
        else {
            Uri deleteUri = null;
            this.eventid = 0;
            deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
            int rows = getActivity().getContentResolver().delete(deleteUri, null, null);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CALENDAR_PERMISSION)
        {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                requestCalendarData();
                getActivity().finish();
            }
            else
            {
                mNotifyCheckbox.setChecked(false);
                getActivity().finish();

            }
        }
        else if (requestCode == UPDATE_CALENDAR_PERMISSION)
        {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                updateCalendarEvent(mNotifyCheckbox.isChecked());
                getActivity().finish();
            }
            else
            {
                mNotifyCheckbox.setChecked(false);
                getActivity().finish();
            }
        }
    }


    private void updateCalendarEventPermission()
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
            requestPermissions(new String[]{Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_CALENDAR},UPDATE_CALENDAR_PERMISSION);
        }
        else
        {
            updateCalendarEvent(mNotifyCheckbox.isChecked());
        }
    }


    private void createCalendarEvent() {
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
            requestCalendarData();
        }
    }

    private void requestCalendarData()
    {
        long calId = getCalendarId();
        if (selectedTimestamp == null || selectedTimestampF == null)
            setActivityDate();
        ContentResolver contentResolver = getActivity().getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(CalendarContract.Events.DTSTART, selectedTimestamp.getTimeInMillis());
        cv.put(CalendarContract.Events.DTEND, selectedTimestampF.getTimeInMillis());
        cv.put(CalendarContract.Events.TITLE, mNameText.getText().toString());
        cv.put(CalendarContract.Events.DESCRIPTION, mDescrText.getText().toString());
        cv.put(CalendarContract.Events.EVENT_TIMEZONE, selectedTimestamp
                .getTimeZone().getID());

        cv.put(CalendarContract.Events.CALENDAR_ID,calId);
        Uri uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, cv);
        long eventId = Long.parseLong(uri.getLastPathSegment());
        this.eventid = eventId;
        //inserting event reminder
        ContentValues reminderValues = new ContentValues();
        reminderValues.put(CalendarContract.Reminders.EVENT_ID,eventId);
        reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        reminderValues.put(CalendarContract.Reminders.MINUTES,2);
        uri = contentResolver.insert(CalendarContract.Reminders.CONTENT_URI,reminderValues);
    }


    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    private static final int PROJECTION_ID_INDEX = 0;

    public long getCalendarId()
    {
        long calId = 0;
        Cursor calCursor = getContext().getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, EVENT_PROJECTION, CalendarContract.Calendars.VISIBLE + " = 1 AND "  + CalendarContract.Calendars.IS_PRIMARY + "=1", null, CalendarContract.Calendars._ID + " ASC");
        if(calCursor.getCount() <= 0){
            calCursor = getContext().getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, EVENT_PROJECTION, CalendarContract.Calendars.VISIBLE + " = 1", null, CalendarContract.Calendars._ID + " ASC");
        }
        while (calCursor.moveToNext())
        {
            calId = calCursor.getLong(PROJECTION_ID_INDEX);
        }
        return calId;
    }

    public HashMap<String,Integer> sortHashByValue(HashMap<String,Integer> hash)
    {
        List<Map.Entry<String,Integer>> list = new LinkedList<Map.Entry<String, Integer>>(hash.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        HashMap<String,Integer> sortedHash = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String,Integer> entry : list)
        {
            sortedHash.put(entry.getKey(),entry.getValue());
        }
        return sortedHash;
    }

    @Override
    public void onRemoveStatisticsLoaded(DayStatistics ds) {
        if (!mNotifyCheckbox.isChecked() ||
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED)
        {
            getActivity().finish();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCategorySpinner!=null) {
            outState.putInt(CATEGORY_POSITION, mCategorySpinner.getSelectedItemPosition());
            outState.putInt(APPEALING_POSITION, mAppealingSpinner.getSelectedItemPosition());
            outState.putInt(MOOD_POSITION, mMoodSpinner.getSelectedItemPosition());
        }
    }
}