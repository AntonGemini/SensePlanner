package com.sassaworks.senseplanner.ui;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.adapter.CategoriesAdapter;
import com.sassaworks.senseplanner.data.ActivityRecord;
import com.sassaworks.senseplanner.data.Category;
import com.sassaworks.senseplanner.data.DayStatistics;
import com.sassaworks.senseplanner.firebaseutils.FirebaseDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateTaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateTaskFragment extends Fragment implements FirebaseDatabaseHelper.OnGetItem, FirebaseDatabaseHelper.OnSingleDataLoaded {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    FirebaseUser user;
    FirebaseDatabase db;
    DatabaseReference refCategories;
    DatabaseReference statRef;

    Spinner mCategorySpinner;
    Spinner mAppealingSpinner;
    Spinner mMoodSpinner;

    HashMap<String, Integer> activities;
    HashMap<String, Integer> appealing;
    HashMap<String, Integer> mood;

    CategoriesAdapter mCategoryAdapter;
    CategoriesAdapter mAppealingAdapter;
    CategoriesAdapter mMoodAdapter;

    EditText mNameText;
    EditText mDescrText;

    EditText mDateText;
    EditText mTimeText;
    CheckBox mNotifyCheckbox;

    Button mSaveButton;


    int mYear;
    int mMonth;
    int mDayOfMonth;
    int mHour;
    int mMinute;

    Calendar selectedTimestamp;


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
    public static CreateTaskFragment newInstance() {
        CreateTaskFragment fragment = new CreateTaskFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        Log.d("RSC2","instance");
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
        View view = inflater.inflate(R.layout.fragment_create_task, container, false);
        mCategorySpinner = view.findViewById(R.id.sp_activities);
        mAppealingSpinner = view.findViewById(R.id.sp_appealing);
        mMoodSpinner = view.findViewById(R.id.sp_mood);

        mDateText = view.findViewById(R.id.et_date);
        mTimeText = view.findViewById(R.id.et_time);
        mNameText = view.findViewById(R.id.et_name);
        mDescrText = view.findViewById(R.id.et_description);

        mNotifyCheckbox = view.findViewById(R.id.cb_notify);
        mSaveButton = view.findViewById(R.id.bt_save_event);
        mSaveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SaveEvent();
            }
        });


        mDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                EditText mDateText = getActivity().findViewById(R.id.et_date);
                DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mDateText.setText(dayOfMonth + "-" + (month +1)+ "-"+year);
                        mYear = year;
                        mMonth = month;
                        mDayOfMonth = dayOfMonth;
                    }
                },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));

                dialog.show();
            }
        });

        mTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();

                EditText mDateText = getActivity().findViewById(R.id.et_date);
                TimePickerDialog dialog = new TimePickerDialog(getActivity(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                mTimeText.setText(hourOfDay + ":" + minute);
                                mHour = hourOfDay;
                                mMinute = minute;
                            }
                        },calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true);
                dialog.show();
            }
        });


        activities = new HashMap<>();
        appealing = new HashMap<>();
        mood = new HashMap<>();

        mCategoryAdapter = new CategoriesAdapter(getActivity(), activities.keySet().toArray(new String[0]));
        mAppealingAdapter = new CategoriesAdapter(getActivity(), appealing.keySet().toArray(new String[0]));
        mMoodAdapter = new CategoriesAdapter(getActivity(), mood.keySet().toArray(new String[0]));

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseDatabase.getInstance();
        refCategories = db.getReference("planner").child(user.getUid());

        FirebaseDatabaseHelper helperActivities = new FirebaseDatabaseHelper(refCategories,this, "activities");
        FirebaseDatabaseHelper helperAppealing = new FirebaseDatabaseHelper(refCategories,this, "appealing");
        FirebaseDatabaseHelper helperMood = new FirebaseDatabaseHelper(refCategories,this, "mood");

        mCategorySpinner.setAdapter(mCategoryAdapter);
        mAppealingSpinner.setAdapter(mAppealingAdapter);
        mMoodSpinner.setAdapter(mMoodAdapter);


        helperActivities.getCategoriesList();
        helperAppealing.getCategoriesList();
        helperMood.getCategoriesList();

        // Inflate the layout for this fragment
        return view;
    }




    @Override
    public void getItem(Category category, String type) {
        //String st = category.getName();

        Log.d("CATEG",String.valueOf(activities.size()));
        if (type=="activities") {

            activities.put(category.getName(),category.getNumValue());
            mCategoryAdapter.updateData(activities.keySet().toArray(new String[0]));
        }
        else if (type=="appealing") {
            appealing.put(category.getName(),category.getNumValue());
            mAppealingAdapter.updateData(appealing.keySet().toArray(new String[0]));
        }
        else if (type=="mood") {
            mood.put(category.getName(),category.getNumValue());
            mMoodAdapter.updateData(mood.keySet().toArray(new String[0]));
        }
    }

    public void SaveEvent()
    {



        selectedTimestamp = Calendar.getInstance();
        selectedTimestamp.set(mYear,mMonth,mDayOfMonth);

        //Date date = new Date(selectedTimestamp.getTimeInMillis());
        String sdf = new SimpleDateFormat("dd-MM-yyyy").format(selectedTimestamp.getTime());
        statRef = db.getReference().child("daystatistics").child(user.getUid()).child(sdf);
        FirebaseDatabaseHelper fbh = new FirebaseDatabaseHelper(statRef, this);
        fbh.GetDayStat(sdf);

        //Query from statistics where date = sfd
        //if got nothing put new item
        //else



    }

    @Override
    public void onDataLoaded(DayStatistics ds) {
        String name = mNameText.getText().toString();
        String desc = mDescrText.getText().toString();

        selectedTimestamp.set(mYear,mMonth,mDayOfMonth,mHour,mMinute);
        Long timespan = selectedTimestamp.getTimeInMillis();

//        Date date = new Date(timespan);
//        String sdf = new SimpleDateFormat("dd-MM-yyyy").format(date);
//        Calendar c = Calendar.getInstance();



        String moodStr = ((TextView)mMoodSpinner.getSelectedView().findViewById(R.id.item_tv_category)).getText().toString();
        String appealingStr = ((TextView)mAppealingSpinner.getSelectedView().findViewById(R.id.item_tv_category)).getText().toString();
        String categoryStr = ((TextView)mCategorySpinner.getSelectedView().findViewById(R.id.item_tv_category)).getText().toString();

        boolean withNotify = mNotifyCheckbox.isChecked();
        String key = refCategories.child("activity_records").push().getKey();
        ActivityRecord record = new ActivityRecord(name,timespan,categoryStr,moodStr,appealingStr,desc,withNotify);

        DayStatistics newDs = new DayStatistics();
        if (ds == null)
        {
            newDs.setAppeal_avg(appealing.get(appealingStr));
            newDs.setMood_avg(mood.get(moodStr));
            newDs.setAppeal_num(1);
            newDs.setMood_num(1);

        }
        else {
            int newMoodNum = ds.getMood_num()+1;
            int newAppealNum = ds.getAppeal_num() + 1;
            float oldTotalMood = ds.getMood_avg() * ds.getMood_num();
            float oldTotalAppeal = ds.getAppeal_avg() * ds.getAppeal_num();

            float newMoodAvg = (oldTotalMood + mood.get(moodStr))/newMoodNum;
            float newAppealAvg = (oldTotalAppeal + appealing.get(appealingStr))/newAppealNum;

            newDs.setMood_num(newMoodNum);
            newDs.setAppeal_num(newAppealNum);
            newDs.setMood_avg(newMoodAvg);
            newDs.setAppeal_avg(newAppealAvg);
        }
        selectedTimestamp.set(mYear,mMonth,mDayOfMonth);
        newDs.setTimestamp(selectedTimestamp.getTimeInMillis());

        refCategories.child("activity_records").child(key).setValue(record);
        statRef.setValue(newDs);
        getActivity().finish();
    }
}
