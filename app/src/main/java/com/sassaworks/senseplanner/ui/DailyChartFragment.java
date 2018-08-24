package com.sassaworks.senseplanner.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sassaworks.senseplanner.MainActivity;
import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.adapter.SectionsPageAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DailyChartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DailyChartFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    FirebaseDatabase db;
    DatabaseReference baseRef;
    FirebaseUser user;

    @BindView(R.id.sp_chartName) Spinner mChartName;

    public static SectionsPageAdapter.nextFragmentListener chartListener;


    public DailyChartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DailyChartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DailyChartFragment newInstance(SectionsPageAdapter.nextFragmentListener listener) {
        DailyChartFragment fragment = new DailyChartFragment();
        Bundle args = new Bundle();
        chartListener = listener;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_daily_chart, container, false);
        ButterKnife.bind(this,view);

        ArrayAdapter<CharSequence> chartAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.charts_name,android.R.layout.simple_spinner_item);
        chartAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mChartName.setAdapter(chartAdapter);
        mChartName.setOnItemSelectedListener(onChartItemSelected);
        mChartName.setSelection(2,false);


        return view;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
    }

    public AdapterView.OnItemSelectedListener onChartItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (view != null) {
                String selectedMenu = ((TextView) view).getText().toString();
                if (selectedMenu == getString(R.string.best_appealing_chart)) {
                    chartListener.fragment0Changed("Chart");
                } else if (selectedMenu == getString(R.string.hour_chart)) {
                    chartListener.fragment0Changed("Hour");
                }
                else
                {

                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    };

}
