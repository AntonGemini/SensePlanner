package com.sassaworks.senseplanner.ui;

import android.content.Context;
import android.content.Intent;
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

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.sassaworks.senseplanner.CreateTaskActivity;
import com.sassaworks.senseplanner.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CalendarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class CalendarFragment extends Fragment implements OnDateSelectedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //@BindView(R.id.calendar)
    MaterialCalendarView calendar;

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


        calendar = view.findViewById(R.id.calendar);

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
    public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {

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
