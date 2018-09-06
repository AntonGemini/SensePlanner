package com.sassaworks.senseplanner.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.sassaworks.senseplanner.CreateTaskActivity;
import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.data.ActivityRecord;
import com.sassaworks.senseplanner.data.DayStatistics;
import com.sassaworks.senseplanner.decorators.EventDecorator;
import com.sassaworks.senseplanner.firebaseutils.FirebaseDatabaseHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CalendarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class CalendarFragment extends Fragment implements FirebaseDatabaseHelper.OnEventsGetCompleted {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FirebaseUser user;
    FirebaseDatabase db;
    DatabaseReference refStat;

    @BindView(R.id.calendar) MaterialCalendarView mCalendarView;
    @BindView(R.id.rb_appealing) RadioButton mAppealingRadio;
    @BindView(R.id.rb_mood) RadioButton mMoodRadio;
    @BindView(R.id.fabToday) FloatingActionButton fabToday;
    @BindView(R.id.fabYesterday) FloatingActionButton mFabYesterday;
    @BindView(R.id.fabTomorrow) FloatingActionButton mFabTomorrow;

    private OnFragmentInteractionListener mListener;
    private String selectedType = "appealing";
    private ArrayList<DayStatistics> mEvents;

    public CalendarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment CalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalendarFragment newInstance() {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
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
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        ButterKnife.bind(this,view);

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

        mAppealingRadio.setOnCheckedChangeListener(onCheckedListener);
        mMoodRadio.setOnCheckedChangeListener(onCheckedListener);
        //mCalendarView = view.findViewById(R.id.calendar);

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


        loadCalendarData();
        //Inflate the layout for this fragment
        return view;
    }

    private void loadCalendarData()
    {
        db = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        refStat = db.getReference("daystatistics").child(user.getUid());
        FirebaseDatabaseHelper helper = new FirebaseDatabaseHelper(this);
        helper.getActivityRecords(refStat);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onEventsGet(ArrayList<DayStatistics> events) {

        //events.stream().filter(s -> s.getJobAddiction().equals("Low")).collect(Collectors.toList());
        //i equals number of job appealings of user
        mEvents = events;
        fillCalendar();
    }

    private void fillCalendar()
    {
        for (int i=0; i<1; i++)
        {
            //query ActivityRecords where jobAppealing equals i

            //convert all this days to CalendarDay list
            //get colour for this type of jobAppealing
            //create an EventDecorator and add it to the calendar
            if (mEvents!=null) {
                mCalendarView.addDecorator(new EventDecorator(getDaysByType(1, 1.66f, mEvents), getContext(),
                        new ColorDrawable(getContext().getResources().getColor(R.color.low_mood))));
                mCalendarView.addDecorator(new EventDecorator(getDaysByType(1.66f, 2.34f, mEvents), getContext(),
                        new ColorDrawable(getContext().getResources().getColor(R.color.avg_mood))));
                mCalendarView.addDecorator(new EventDecorator(getDaysByType(2.34f, 3f, mEvents), getContext(),
                        new ColorDrawable(getContext().getResources().getColor(R.color.high_mood))));
            }
            //calendar.addDecorator();
        }
        //ArrayList<DayStatistics> userEvents = mEvents;
    }

    public ArrayList<CalendarDay> getDaysByType(float lowBound, float upperBound, ArrayList<DayStatistics> events)
    {
        ArrayList<CalendarDay> days = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        if (upperBound == 3)
        {
            upperBound = 3.00001f;
        }
        for (DayStatistics ac :events) {
            float value = selectedType.equals("appealing") ? ac.getAppeal_avg() : ac.getMood_avg();
            if (value >= lowBound && value < upperBound)
            {
                calendar.setTimeInMillis(ac.getTimestamp());
                CalendarDay cd = CalendarDay.from(calendar);
                days.add(cd);
            }
        }
        return days;
    }


    private CompoundButton.OnCheckedChangeListener onCheckedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (mAppealingRadio.isChecked())
            {
                selectedType = "appealing";
            }
            else if (mMoodRadio.isChecked())
            {
                selectedType = "mood";
            }
            fillCalendar();
        }
    };

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
