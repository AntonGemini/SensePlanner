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
import android.widget.LinearLayout;

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
import com.sassaworks.senseplanner.decorators.EventDecorator;
import com.sassaworks.senseplanner.firebaseutils.FirebaseDatabaseHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.stream.Collectors;
import java8.util.stream.IntStreams;

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
    DatabaseReference refEvents;

    //@BindView(R.id.calendar)
    MaterialCalendarView mCalendarView;

    private OnFragmentInteractionListener mListener;

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
        //ButterKnife.bind(this.getActivity());
        //calendar.setOnDateChangedListener(this);
        FloatingActionButton fab = view.findViewById(R.id.fabMenu);
        //FloatingActionButton fabYesreday= view.findViewById(R.id.fabYesterday);
        FloatingActionButton fabToday= view.findViewById(R.id.fabToday);
        LinearLayout todayLayout = view.findViewById(R.id.todayLayout);
        LinearLayout yesterdayLayout = view.findViewById(R.id.yesterdayLayout);
        todayLayout.setVisibility(View.INVISIBLE);
        yesterdayLayout.setVisibility(View.INVISIBLE);


        Animation mShowButton = AnimationUtils.loadAnimation(getActivity(),R.anim.show_button);
        Animation mHideButton = AnimationUtils.loadAnimation(getActivity(),R.anim.hide_button);
        Animation mShowLayout = AnimationUtils.loadAnimation(getActivity(),R.anim.show_layout);


        Animation mHideLayout = AnimationUtils.loadAnimation(getActivity(),R.anim.hide_layout);


        mCalendarView = view.findViewById(R.id.calendar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (todayLayout.getVisibility() == View.VISIBLE)
                {
                    fab.startAnimation(mHideButton);
                    todayLayout.setVisibility(View.INVISIBLE);
                    yesterdayLayout.setVisibility(View.INVISIBLE);
                    yesterdayLayout.startAnimation(mHideLayout);
                    todayLayout.startAnimation(mHideLayout);

                }
                else {
                    todayLayout.setVisibility(View.VISIBLE);
                    yesterdayLayout.setVisibility(View.VISIBLE);
                    fab.startAnimation(mShowButton);
                    yesterdayLayout.startAnimation(mShowLayout);
                    todayLayout.startAnimation(mShowLayout);

                }
            }
        });

        fabToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),CreateTaskActivity.class);
                startActivity(intent);
            }
        });
        db = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        refEvents = db.getReference("planner").child(user.getUid()).child("activity_records");
        FirebaseDatabaseHelper helper = new FirebaseDatabaseHelper(refEvents,this);
        helper.getActivityRecords(refEvents);

        //Inflate the layout for this fragment
        return view;
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
    public void onEventsGet(ArrayList<ActivityRecord> events) {

        //events.stream().filter(s -> s.getJobAddiction().equals("Low")).collect(Collectors.toList());
        //i equals number of job appealings of user

        for (int i=0; i<1; i++)
        {
            //query ActivityRecords where jobAppealing equals i

            //convert all this days to CalendarDay list
            //get colour for this type of jobAppealing
            //create an EventDecorator and add it to the calendar

            mCalendarView.addDecorator(new EventDecorator(getDaysByMood("Bad",events),getContext(),
                    new ColorDrawable(getContext().getResources().getColor(R.color.low_mood))));
            mCalendarView.addDecorator(new EventDecorator(getDaysByMood("Good",events),getContext(),
                    new ColorDrawable(getContext().getResources().getColor(R.color.high_mood))));
            //calendar.addDecorator();
        }
        ArrayList<ActivityRecord> userEvents = events;
    }

    public ArrayList<CalendarDay> getDaysByMood(String type, ArrayList<ActivityRecord> events)
    {
        ArrayList<CalendarDay> days = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (ActivityRecord ac :events) {

            if (ac.getMoodType().equals(type))
            {
                calendar.setTimeInMillis(ac.getTimestamp());
                //calendar.
                CalendarDay cd = CalendarDay.from(calendar);
                days.add(cd);
            }
        }
        return days;
    }

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
