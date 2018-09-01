package com.sassaworks.senseplanner.ui;


import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
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
import com.sassaworks.senseplanner.MainActivity;
import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.adapter.CategoriesAdapter;
import com.sassaworks.senseplanner.adapter.SectionsPageAdapter;
import com.sassaworks.senseplanner.data.ActivityRecord;
import com.sassaworks.senseplanner.data.Appealing;
import com.sassaworks.senseplanner.data.Mood;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import durdinapps.rxfirebase2.DataSnapshotMapper;
import durdinapps.rxfirebase2.RxFirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChartFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String START_DATE = "start_date";
    private static final String FINISH_DATE = "finish_date";
    private static final String APPEALING_POSITION = "app_position";
    private static final String MOOD_POSITION = "mood_position";
    private static final String SELECTED_CATEGORY = "selected_category";

    FirebaseDatabase db;
    DatabaseReference baseRef;
    FirebaseUser user;
    Map<String,Integer> moodMap = new HashMap<>();
    Map<String,Integer> appealingMap = new HashMap<>();
    int mYear;
    int mMonth;
    int mDayOfMonth;
    int mYearF;
    int mMonthF;
    int mDayOfMonthF;
    String selectedCategory = "";
    boolean isDrawing = false;
    public static SectionsPageAdapter.nextFragmentListener chartListener;

    @BindView(R.id.sp_chartName) Spinner mChartName;
    @BindView(R.id.sp_mood) Spinner mMoodType;
    @BindView(R.id.sp_appealing) Spinner mAppealingType;
    @BindView(R.id.rb_appealing) RadioButton mAppealingRadio;
    @BindView(R.id.rb_mood) RadioButton mMoodRadio;
    @BindView(R.id.et_dateS) EditText mDateS;
    @BindView(R.id.et_dateF) EditText mDateF;
    @BindView(R.id.chart) BarChart mChart;
    @BindView(R.id.rg_typeGroup) RadioGroup mTypeGroup;

    private long mStartTimeInMillis = 0;
    private long mFinishTimeInMillis = 0;
    private int mAddictionPosition;
    private int mMoodPosition;

    public ChartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ChartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChartFragment newInstance(SectionsPageAdapter.nextFragmentListener listener) {
        chartListener = listener;
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
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
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        ButterKnife.bind(this,view);

        db = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        baseRef = db.getReference("planner").child(user.getUid());

        ArrayAdapter<CharSequence> chartAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.charts_name,android.R.layout.simple_spinner_item);
        chartAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mChartName.setAdapter(chartAdapter);
        mChartName.setOnItemSelectedListener(onChartItemSelected);

        mDateS.setOnClickListener(onDateClickListener);
        mDateF.setOnClickListener(onDateClickListener);

        if (savedInstanceState != null)
        {
            mStartTimeInMillis = savedInstanceState.getLong(START_DATE);
            mFinishTimeInMillis = savedInstanceState.getLong(FINISH_DATE);
            mAddictionPosition = savedInstanceState.getInt(APPEALING_POSITION);
            mMoodPosition = savedInstanceState.getInt(MOOD_POSITION);
            selectedCategory = savedInstanceState.getString(SELECTED_CATEGORY);
        }


        mTypeGroup.setOnCheckedChangeListener(onCheckedListener);
//        mAppealingRadio.setOnCheckedChangeListener(onCheckedListener);
//        mMoodRadio.setOnCheckedChangeListener(onCheckedListener);
        isDrawing = true;
        return view;
    }


    public AdapterView.OnItemSelectedListener onChartItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (view != null) {
                String selectedMenu = ((TextView) view).getText().toString();
                if (selectedMenu == getString(R.string.best_appealing_chart)) {
                    LoadBestAppealingChart();
                } else if (selectedMenu == getString(R.string.hour_chart)) {
                    chartListener.fragment0Changed("Hour");
                }
                else
                {
                    chartListener.fragment0Changed("Daily");
                }
            }
            else {
                LoadBestAppealingChart();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    };




    public AdapterView.OnItemSelectedListener onAppealingItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (!isDrawing) {
                if (pos > 0) {
                    String selection = getKeyByValue(appealingMap, pos);
                    selectedCategory = selection;
                } else if (pos == 0) {
                    selectedCategory = "";
                }
                GetDataBestAppealingChart();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    };

    public AdapterView.OnItemSelectedListener onMoodItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (!isDrawing) {
                if (pos > 0) {
                    String selection = getKeyByValue(moodMap, pos);
                    selectedCategory = selection;
                } else if (pos == 0) {
                    selectedCategory = "";
                }
                GetDataBestAppealingChart();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    };

    private RadioGroup.OnCheckedChangeListener onCheckedListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.rb_appealing && mAppealingRadio.isChecked())
            {
                mAppealingType.setEnabled(true);
                mMoodType.setEnabled(false);
            }
            else if (checkedId == R.id.rb_mood && mMoodRadio.isChecked()) {
                mAppealingType.setEnabled(false);
                mMoodType.setEnabled(true);
            }

            if (mAppealingType.isEnabled() && appealingMap.size()!=0) {
                selectedCategory = getKeyByValue(appealingMap, mAppealingType.getSelectedItemPosition());
            } else if (mMoodType.isEnabled() && moodMap.size()!=0) {
                selectedCategory = getKeyByValue(moodMap, mMoodType.getSelectedItemPosition());
            }

            if (selectedCategory.equals("All")) selectedCategory = "";
            GetDataBestAppealingChart();
        }
    };



    private void LoadBestAppealingChart() {

        //selectedCategory = "";

        DatabaseReference referAppealing = db.getReference().child("planner").child(user.getUid()).child("appealing");
        DatabaseReference referMood = db.getReference().child("planner").child(user.getUid()).child("mood");
        mAppealingType.setOnItemSelectedListener(onAppealingItemSelected);
        mMoodType.setOnItemSelectedListener(onMoodItemSelected);
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
            fillAppMoodSpinners();

        });
    }

    private void fillAppMoodSpinners() {
        mAppealingType.setVisibility(View.VISIBLE);
        mMoodType.setVisibility(View.VISIBLE);
        appealingMap.put(getString(R.string.all_types),0);
        moodMap.put(getString(R.string.all_types),0);


        //Sorting values
        appealingMap = sortHashByValue(appealingMap);
        moodMap = sortHashByValue(moodMap);

        CategoriesAdapter mAppealingAdapter = new CategoriesAdapter(getActivity(), appealingMap.keySet().toArray(new String[0]));
        CategoriesAdapter mMoodAdapter = new CategoriesAdapter(getActivity(), moodMap.keySet().toArray(new String[0]));
        mAppealingAdapter.setDropDownViewResource(R.layout.item_category);
        mMoodAdapter.setDropDownViewResource(R.layout.item_category);

        mAppealingType.setAdapter(mAppealingAdapter);
        mMoodType.setAdapter(mMoodAdapter);
        if (mAddictionPosition!=0)
        {
            mAppealingType.setSelection(mAddictionPosition);
        }
        if (mMoodPosition!=0)
        {
            mMoodType.setSelection(mMoodPosition);
        }

        if (mAppealingRadio.isChecked())
        {
            mAppealingType.setEnabled(true);
            mMoodType.setEnabled(false);
        }
        else if (mMoodRadio.isChecked())
        {
            mAppealingType.setEnabled(false);
            mMoodType.setEnabled(true);
        }
        GetDataBestAppealingChart();

    }

    private void GetDataBestAppealingChart() {

        isDrawing = true;
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

        if (mStartTimeInMillis!= 0)
        {
            cS.setTimeInMillis(mStartTimeInMillis);
            mYear = cS.get(Calendar.YEAR);
            mMonth = cS.get(Calendar.MONTH);
            mDayOfMonth = cS.get(Calendar.DAY_OF_MONTH);
            mDateS.setText(getResources().getString(R.string.date_format,String.valueOf(mYear),
                    String.valueOf(mMonth+1),String.valueOf(mDayOfMonth)));

        }
        else if (!mDateS.getText().toString().equals(""))
        {
            cS.set(mYear,mMonth,mDayOfMonth);
        }
        else {
            cS.set(cS.get(Calendar.YEAR),cS.get(Calendar.MONTH)-1,cS.get(Calendar.DAY_OF_MONTH));
            mYear = cS.get(Calendar.YEAR);
            mMonth = cS.get(Calendar.MONTH);
            mDayOfMonth = cS.get(Calendar.DAY_OF_MONTH);
            mDateS.setText(getResources().getString(R.string.date_format,String.valueOf(mYear),
                    String.valueOf(mMonth+1),String.valueOf(mDayOfMonth)));
        }


        if(mFinishTimeInMillis!=0)
        {
            cF.setTimeInMillis(mFinishTimeInMillis);
            mYearF = cF.get(Calendar.YEAR);
            mMonthF = cF.get(Calendar.MONTH);
            mDayOfMonthF = cF.get(Calendar.DAY_OF_MONTH);
            mDateF.setText(getResources().getString(R.string.date_format,String.valueOf(mYearF),
                    String.valueOf(mMonthF+1),String.valueOf(mDayOfMonthF)));
        }
        else if (!mDateF.getText().toString().equals(""))
        {
            cF.set(mYearF,mMonthF,mDayOfMonthF);
        }
        else {
            cF.set(cF.get(Calendar.YEAR),cF.get(Calendar.MONTH),cF.get(Calendar.DAY_OF_MONTH));
            mYearF = cF.get(Calendar.YEAR);
            mMonthF = cF.get(Calendar.MONTH);
            mDayOfMonthF = cF.get(Calendar.DAY_OF_MONTH);
            mDateF.setText(getResources().getString(R.string.date_format,String.valueOf(mYearF),
                    String.valueOf(mMonthF+1),String.valueOf(mDayOfMonthF)));
        }

        mStartTimeInMillis = cS.getTimeInMillis();
        mFinishTimeInMillis = cF.getTimeInMillis();

        DatabaseReference ref = baseRef.child("activity_records");
        Query query = ref.orderByChild(order);
        Map<String,Float> rawEntries = new HashMap<>();

        if (selectedCategory!=null && selectedCategory!="")
        {
            query = query.equalTo(selectedCategory);
        }

        RxFirebaseDatabase.observeSingleValueEvent(query,DataSnapshotMapper.listOf(ActivityRecord.class))
                .subscribe(events -> {
                    for (ActivityRecord ac : events)
                    {
                        if (ac.getTimestamp()>=cS.getTimeInMillis() && ac.getTimestamp()<=cF.getTimeInMillis()) {
                            if (rawEntries.containsKey(ac.getCategory())) {
                                rawEntries.put(ac.getCategory(), rawEntries.get(ac.getCategory()) + 1f);
                            } else {
                                rawEntries.put(ac.getCategory(), 1f);
                            }
                        }
                    }
                    DrawBestAppealingChart(rawEntries);
                });
    }

    private void DrawBestAppealingChart(Map<String, Float> rawEntries) {
        rawEntries = descSortHashByValue(rawEntries);
        Map<Float,String> lablesMap = new HashMap<>();
        List<BarEntry> barEntries = new ArrayList<>();
        float cnt = 0;
        for(Map.Entry<String,Float> e : rawEntries.entrySet())
        {
            BarEntry barEntry = new BarEntry(cnt,e.getValue());
            barEntries.add(barEntry);
            lablesMap.put(cnt,e.getKey());
            cnt++;
        }
        BarDataSet dataSet = new BarDataSet(barEntries,"Categories");
        dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);

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

    public HashMap<String,Integer> sortHashByValue(Map<String,Integer> hash)
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

    public  HashMap<String,Float> descSortHashByValue(Map<String,Float> hash)
    {
        List<Map.Entry<String,Float>> list = new LinkedList<>(hash.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                return o1.getValue().compareTo(o2.getValue())*-1;
            }
        });
        HashMap<String,Float> sortedHash = new LinkedHashMap<>();
        for (Map.Entry<String,Float> entry : list)
        {
            sortedHash.put(entry.getKey(),entry.getValue());
        }
        return sortedHash;
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
                if (v.getId() == R.id.et_dateS) {

                    mDateS.setText(getString(R.string.date_format,String.valueOf(dayOfMonth),String.valueOf(month+1),String.valueOf(year)));
                    mYear = year;
                    mMonth = month;
                    mDayOfMonth = dayOfMonth;
                    mStartTimeInMillis = 0;
                }
                else if (v.getId() == R.id.et_dateF) {
                    mDateF.setText(getString(R.string.date_format,String.valueOf(dayOfMonth),String.valueOf(month+1),String.valueOf(year)));
                    mYearF = year;
                    mMonthF = month;
                    mDayOfMonthF = dayOfMonth;
                    mFinishTimeInMillis = 0;
                }
                GetDataBestAppealingChart();
            }
        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    public <K,V> K getKeyByValue(Map<K,V> map, V value)
    {
        for (Map.Entry<K,V> entry : map.entrySet())
        {
            if (entry.getValue().equals(value))
            {
                return entry.getKey();
            }
        }
        return null;
    }



    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(START_DATE,mStartTimeInMillis);
        outState.putLong(FINISH_DATE,mFinishTimeInMillis);
        outState.putInt(APPEALING_POSITION,mAppealingType.getSelectedItemPosition());
        outState.putInt(MOOD_POSITION,mMoodType.getSelectedItemPosition());
        outState.putString(SELECTED_CATEGORY,selectedCategory);
    }
}
