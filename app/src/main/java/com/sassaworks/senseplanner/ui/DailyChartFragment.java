package com.sassaworks.senseplanner.ui;


import android.app.DatePickerDialog;
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
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.sassaworks.senseplanner.MainActivity;
import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.adapter.CategoriesAdapter;
import com.sassaworks.senseplanner.adapter.SectionsPageAdapter;
import com.sassaworks.senseplanner.data.Activity;
import com.sassaworks.senseplanner.data.ActivityRecord;
import com.sassaworks.senseplanner.data.Appealing;
import com.sassaworks.senseplanner.data.Category;
import com.sassaworks.senseplanner.data.Mood;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import durdinapps.rxfirebase2.DataSnapshotMapper;
import durdinapps.rxfirebase2.RxFirebaseDatabase;

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

    int mYear;
    int mMonth;
    int mDayOfMonth;
    String selectedCategory;
    List<String> activitiesList;
    List<ActivityRecord> eventsList;
    Map<String,Integer> moodMap = new HashMap<>();
    Map<String,Integer> appealingMap = new HashMap<>();

    @BindView(R.id.sp_chartName) Spinner mChartName;
    @BindView(R.id.sp_categories) Spinner mActivityType;
    @BindView(R.id.rb_appealing) RadioButton mAppealingRadio;
    @BindView(R.id.rb_mood) RadioButton mMoodRadio;
    @BindView(R.id.et_dateS) EditText mDateS;
    @BindView(R.id.chart) PieChart mChart;

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

        db = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        baseRef = db.getReference("planner").child(user.getUid());

        mDateS.setOnClickListener(onDateClickListener);
        mAppealingRadio.setOnCheckedChangeListener(onCheckedListener);
        mMoodRadio.setOnCheckedChangeListener(onCheckedListener);



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
                    LoadDailyChart();
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    };

    private View.OnClickListener onDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dateSelected(v);
        }
    };

    public void dateSelected(View v)
    {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                mDateS.setText(getString(R.string.date_format,String.valueOf(dayOfMonth),String.valueOf(month+1),String.valueOf(year)));
                mYear = year;
                mMonth = month;
                mDayOfMonth = dayOfMonth;
                GetDataDailyChart();
            }
        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    public AdapterView.OnItemSelectedListener onActivityItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (pos > 0) {
                TextView tv = view.findViewById(R.id.item_tv_category);
                //String selection = getKeyByValue(appealingMap, pos);
                selectedCategory = tv.getText().toString();
            } else if (pos == 0) {
                selectedCategory = "";
            }
            GetDataDailyChart();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    };

    private CompoundButton.OnCheckedChangeListener onCheckedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

//            if (mAppealingRadio.isChecked())
//            {
//                selectedType = "appealing";
//            }
//            else if (mMoodRadio.isChecked())
//            {
//                selectedType = "mood";
//            }
            GetDataDailyChart();
        }
    };

    private void LoadDailyChart() {
        selectedCategory = "";

        DatabaseReference referActivities = db.getReference().child("planner").child(user.getUid()).child("activities");
        mActivityType.setOnItemSelectedListener(onActivityItemSelected);
        activitiesList = new ArrayList<>();
        activitiesList.add(getString(R.string.all_types));
        RxFirebaseDatabase.observeSingleValueEvent(referActivities, DataSnapshotMapper.listOf(com.sassaworks.senseplanner.data.Activity.class))
                .subscribe(activities -> {
                    for (Category c : activities) {
                        activitiesList.add(c.getName());
                    }
                    fillCategorySpinner();
                    GetDataDailyChart();
                });
    }

    private void fillCategorySpinner()
    {
        CategoriesAdapter mActivitiesAdapter = new CategoriesAdapter(getActivity(), activitiesList.toArray(new String[0]));
        mActivitiesAdapter.setDropDownViewResource(R.layout.item_category);
        mActivityType.setAdapter(mActivitiesAdapter);
        mActivityType.setOnItemSelectedListener(onActivityItemSelected);


    }

    private void GetDataDailyChart() {

        Calendar cS = Calendar.getInstance();
        Calendar cF = Calendar.getInstance();

        if (!mDateS.getText().toString().equals(""))
        {
            cS.set(mYear,mMonth,mDayOfMonth);
            cF.set(cF.get(Calendar.YEAR),cF.get(Calendar.MONTH),cF.get(Calendar.DAY_OF_MONTH));
        }
        else {
            cS.set(cS.get(Calendar.YEAR),cS.get(Calendar.MONTH),cS.get(Calendar.DAY_OF_MONTH));

            //cF.set(cF.get(Calendar.YEAR),cF.get(Calendar.MONTH),cF.get(Calendar.DAY_OF_MONTH));
            mYear = cS.get(Calendar.YEAR);
            mMonth = cS.get(Calendar.MONTH);
            mDayOfMonth = cS.get(Calendar.DAY_OF_MONTH);
            mDateS.setText(getResources().getString(R.string.date_format,String.valueOf(mYear),
                    String.valueOf(mMonth+1),String.valueOf(mDayOfMonth)));

        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        Query query = baseRef.child("activity_records").orderByChild("formattedDate")
                .equalTo(sdf.format(cS.getTime()));

        eventsList = new ArrayList<>();
        RxFirebaseDatabase.observeSingleValueEvent(query,DataSnapshotMapper.listOf(ActivityRecord.class))
                .subscribe(events -> {
                    for (ActivityRecord ac : events)
                    {
                            eventsList.add(ac);
                    }
                    loadMoodAddictionData();
                });
    }

    private void loadMoodAddictionData() {
        Query referAppealing = db.getReference().child("planner").child(user.getUid()).child("appealing").orderByChild("numValue");
        Query referMood = db.getReference().child("planner").child(user.getUid()).child("mood").orderByChild("numValue");

        RxFirebaseDatabase.observeSingleValueEvent(referMood, DataSnapshotMapper.listOf(Mood.class))
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
            aggregateByType();

        });
    }

    private void aggregateByType() {
        Map<String,Integer> categoryMap = mAppealingRadio.isChecked() ? appealingMap : moodMap;
        Map<String,Float> chartValues = new HashMap<>();
        float total = 0;
        for (Map.Entry<String, Integer> entry : categoryMap.entrySet())  {
            chartValues.put(entry.getKey(),0f);
            for (ActivityRecord event : eventsList)
            {
                String type = mAppealingRadio.isChecked() ? event.getJobAddiction() : event.getMoodType();
                if (type.equals(entry.getKey()))
                {
                    float val = chartValues.get(entry.getKey());
                    chartValues.put(entry.getKey(),val+1f);
                }
            }
            total = total + chartValues.get(entry.getKey());
        }
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String,Float> pieValue : chartValues.entrySet())
        {
            float share = (pieValue.getValue()/total)*100f;
            PieEntry pieEntry = new PieEntry(share,pieValue.getKey());
            entries.add(pieEntry);
        }

        PieDataSet dataSet = new PieDataSet(entries,"Share");
        dataSet.setColors(new int[] { R.color.Bad, R.color.Average, R.color.High }, getActivity());
        PieData data = new PieData(dataSet);
        mChart.setData(data);
        mChart.invalidate();

    }


}
