package com.sassaworks.senseplanner.ui;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.adapter.CategoriesAdapter;
import com.sassaworks.senseplanner.adapter.SectionsPageAdapter;
import com.sassaworks.senseplanner.data.ActivityRecord;
import com.sassaworks.senseplanner.data.Appealing;
import com.sassaworks.senseplanner.data.Category;
import com.sassaworks.senseplanner.data.Mood;

import java.lang.reflect.MalformedParameterizedTypeException;
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
 * Use the {@link HourChartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HourChartFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    FirebaseDatabase db;
    DatabaseReference baseRef;
    FirebaseUser user;

    int mYear;
    int mMonth;
    int mDayOfMonth;
    String selectedCategory;
    String selectedType;
    private boolean isDrawing = true;
    List<String> activitiesList;
    List<ActivityRecord> eventsList;
    Map<String,Integer> moodMap = new HashMap<>();
    Map<String,Integer> appealingMap = new HashMap<>();
    Map<Float, String> lables;

    @BindView(R.id.sp_chartName) Spinner mChartName;
    @BindView(R.id.sp_categories) Spinner mActivityType;
    @BindView(R.id.rb_appealing) RadioButton mAppealingRadio;
    @BindView(R.id.rb_mood) RadioButton mMoodRadio;
    @BindView(R.id.et_dateS) EditText mDateS;
    @BindView(R.id.chart) BarChart mChart;

    public static SectionsPageAdapter.nextFragmentListener chartListener;



    public HourChartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HourChartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HourChartFragment newInstance(SectionsPageAdapter.nextFragmentListener listener) {
        HourChartFragment fragment = new HourChartFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        chartListener = listener;
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
        View view = inflater.inflate(R.layout.fragment_hour_chart, container, false);

        ButterKnife.bind(this,view);

        db = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        baseRef = db.getReference("planner").child(user.getUid());

        ArrayAdapter<CharSequence> chartAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.charts_name,android.R.layout.simple_spinner_item);
        chartAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mChartName.setAdapter(chartAdapter);
        mChartName.setOnItemSelectedListener(onChartItemSelected);
        mChartName.setSelection(1,false);

        mDateS.setOnClickListener(onDateClickListener);
        mAppealingRadio.setOnCheckedChangeListener(onCheckedListener);
        mMoodRadio.setOnCheckedChangeListener(onCheckedListener);

        return view;
    }


    public AdapterView.OnItemSelectedListener onChartItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (view != null) {
                String selectedMenu = ((TextView) view).getText().toString();
                if (selectedMenu == getString(R.string.best_appealing_chart)) {
                    chartListener.fragment0Changed("Chart");
                } else if (selectedMenu == getString(R.string.hour_chart)) {
                    LoadHourChart();
                }
                else {
                    chartListener.fragment0Changed("Daily");
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    };

    private void LoadHourChart() {

        selectedCategory = "";

        DatabaseReference referActivities = db.getReference().child("planner").child(user.getUid()).child("activities");
        activitiesList = new ArrayList<>();
        activitiesList.add(getString(R.string.all_types));
        RxFirebaseDatabase.observeSingleValueEvent(referActivities, DataSnapshotMapper.listOf(com.sassaworks.senseplanner.data.Activity.class))
                .subscribe(activities -> {
                    for (Category c : activities) {
                        activitiesList.add(c.getName());
                        //moodMap.put(m.getName(), m.getNumValue());
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
    }

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
                //mDateText.setText(dayOfMonth + "-" + (month + 1) + "-" + year);
                mDateS.setText(getString(R.string.date_format,String.valueOf(dayOfMonth),String.valueOf(month+1),String.valueOf(year)));
                mYear = year;
                mMonth = month;
                mDayOfMonth = dayOfMonth;
                GetDataHourChart();
            }
        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    public AdapterView.OnItemSelectedListener onActivityItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
//            if (!isDrawing) {
                if (pos > 0) {
                    TextView tv = view.findViewById(R.id.item_tv_category);
                    //String selection = getKeyByValue(appealingMap, pos);
                    selectedCategory = tv.getText().toString();
                } else if (pos == 0) {
                    selectedCategory = "";
                }
                GetDataHourChart();
//            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    };

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
            GetDataHourChart();
        }
    };

    private void GetDataHourChart() {
        String order;
        if (mAppealingRadio.isChecked())
        {
            order = "jobAddiction";
        }
        else {
            order = "moodType";
        }
        Calendar cS = Calendar.getInstance();
        Calendar cF = Calendar.getInstance();

        if (!mDateS.getText().toString().equals(""))
        {
            cS.set(mYear,mMonth,mDayOfMonth);
            cF.set(cF.get(Calendar.YEAR)+10,cF.get(Calendar.MONTH),cF.get(Calendar.DAY_OF_MONTH));
        }
        else {
            cS.set(cS.get(Calendar.YEAR),cS.get(Calendar.MONTH),cS.get(Calendar.DAY_OF_MONTH));
            cS.add(Calendar.DAY_OF_MONTH,-10);
            cF.set(cF.get(Calendar.YEAR),cF.get(Calendar.MONTH),cF.get(Calendar.DAY_OF_MONTH));
            mYear = cS.get(Calendar.YEAR);
            mMonth = cS.get(Calendar.MONTH);
            mDayOfMonth = cS.get(Calendar.DAY_OF_MONTH);
            mDateS.setText(getResources().getString(R.string.date_format,String.valueOf(mYear),
                    String.valueOf(mMonth+1),String.valueOf(mDayOfMonth)));

        }



        DatabaseReference ref = baseRef.child("activity_records");
        Query query = ref.orderByChild(order);

        if (selectedCategory!=null && selectedCategory!="")
        {
            query = query.equalTo(selectedCategory);
        }
        eventsList = new ArrayList<>();
        RxFirebaseDatabase.observeSingleValueEvent(query,DataSnapshotMapper.listOf(ActivityRecord.class))
                .subscribe(events -> {
                    for (ActivityRecord ac : events)
                    {
                        if (ac.getTimestamp()>=cS.getTimeInMillis() && ac.getTimestamp()<= cF.getTimeInMillis()) {
                            eventsList.add(ac);
                        }
                    }
                    BreakListIntoDatesMap();

                });
    }

    private void BreakListIntoDatesMap() {
        lables = new HashMap<>();
        Map<String,List<ActivityRecord>> mappedEvents = new HashMap<>();
        Calendar cl = Calendar.getInstance();
        cl.set(mYear,mMonth,mDayOfMonth);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        for (int i = 0; i < 10; i++) {
            lables.put(Float.valueOf(i), sdf.format(cl.getTime()));
            for (ActivityRecord ac : eventsList) {
                if (ac.getFormattedDate().equals(sdf.format(cl.getTime()))) {
                    if (mappedEvents.containsKey(sdf.format(cl.getTime()))) {
                        mappedEvents.get(sdf.format(cl.getTime())).add(ac);
                    } else {
                        List<ActivityRecord> list = new ArrayList<>();
                        list.add(ac);
                        mappedEvents.put(sdf.format(cl.getTime()), list);
                    }
                }
            }
            cl.add(Calendar.DAY_OF_MONTH,1);
        }
        mappedEvents.size();

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
            distributeForStackedBarValues(mappedEvents);

        });

    }

    private void distributeForStackedBarValues(Map<String,List<ActivityRecord>> mappedEvents) {

        Calendar cal = Calendar.getInstance();
        Map<String,float[]> finishedValues = new HashMap<>();
        Map<String,Integer> categoryMap = mAppealingRadio.isChecked() ? appealingMap : moodMap;

        for (Map.Entry<String, List<ActivityRecord>> entry : mappedEvents.entrySet())  {
            float[] barValues = new float[categoryMap.size()];
            finishedValues.put(entry.getKey(),barValues);
            for (Map.Entry<String,Integer> entryAppealing : categoryMap.entrySet()){
                barValues[entryAppealing.getValue()-1] = 0f;
                for (ActivityRecord ac : entry.getValue()) {
                    String type = mAppealingRadio.isChecked() ? ac.getJobAddiction() : ac.getMoodType();
                    if (type.equals(entryAppealing.getKey())) {
                        cal.setTimeInMillis(ac.getTimestampF() - ac.getTimestamp());
                        Float hour = Float.valueOf(cal.get(Calendar.HOUR));
                        Float minute = Float.valueOf(cal.get(Calendar.MINUTE));
                        Float minuteToHour = minute / 60;
                        Float totalHour = hour + minuteToHour;
                        Float val = finishedValues.get(entry.getKey())[entryAppealing.getValue()-1];
                        finishedValues.get(entry.getKey())[entryAppealing.getValue()-1] = val + totalHour;
                    }
                }
            }
        }

        drawHourChart(finishedValues);
    }

    private void drawHourChart(Map<String, float[]> finishedValues) {
        List<BarEntry> barEntries = new ArrayList<>();

        for (Map.Entry<Float,String> lable : lables.entrySet())
        {
            if (finishedValues.containsKey(lable.getValue()))
            {
                float[] vals = finishedValues.get(lable.getValue());
                BarEntry barEntry = new BarEntry(Float.valueOf(lable.getKey()),vals);
                barEntries.add(barEntry);
            }
        }
        BarDataSet dataSet = new BarDataSet(barEntries,"Type");
        dataSet.setColors(new int[] { R.color.Bad, R.color.Average, R.color.High }, getActivity());

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);
        mChart.setFitBars(true);
        mChart.setData(data);
        mChart.getDescription().setEnabled(false);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(false);
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);


        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        mChart.setDrawGridBackground(false);
        mChart.invalidate();
        isDrawing = false;
    }

}
