package com.sassaworks.senseplanner.ui;


import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
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

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import durdinapps.rxfirebase2.DataSnapshotMapper;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.SingleSource;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DailyChartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DailyChartFragment extends Fragment {
  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String START_DATE = "start_date";
  private static final String CATEGORY_POSITION = "cat_position";
  private static final String SELECTED_CATEGORY = "selected_category";

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
  @BindView(R.id.onboardingDailyChart) ConstraintLayout mOnboardingLayout;

  public static SectionsPageAdapter.nextFragmentListener chartListener;

  private long mStartTimeInMillis = 0;
  private int mCategoryPosition;

  private ChartFragment.OnChartNameSelected mCallback;


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
  public static DailyChartFragment newInstance() {
    DailyChartFragment fragment = new DailyChartFragment();
    Bundle args = new Bundle();
    //chartListener = listener;
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_daily_chart, container, false);
    ButterKnife.bind(this,view);

    //mChart.setNoDataText(getString(R.string.empty_chart_text));
    ArrayAdapter<CharSequence> chartAdapter = ArrayAdapter.createFromResource(getContext(),
            R.array.charts_name,android.R.layout.simple_spinner_item);
    chartAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mChartName.setAdapter(chartAdapter);
    mChartName.setOnItemSelectedListener(onChartItemSelected);
    mChartName.setSelection(2,false);

    db = FirebaseDatabase.getInstance();
    user = FirebaseAuth.getInstance().getCurrentUser();
    baseRef = db.getReference(getString(R.string.ref_planner)).child(user.getUid());

    mDateS.setOnClickListener(onDateClickListener);
    mAppealingRadio.setOnCheckedChangeListener(onCheckedListener);
    mMoodRadio.setOnCheckedChangeListener(onCheckedListener);

    if (savedInstanceState != null)
    {
      mStartTimeInMillis = savedInstanceState.getLong(START_DATE);
      mCategoryPosition = savedInstanceState.getInt(CATEGORY_POSITION);
      selectedCategory = savedInstanceState.getString(SELECTED_CATEGORY);
    }

    return view;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    try {
      mCallback = (ChartFragment.OnChartNameSelected)context;
    } catch(ClassCastException ex)
    {
      throw new ClassCastException(context.toString());
    }

  }


  public AdapterView.OnItemSelectedListener onChartItemSelected = new AdapterView.OnItemSelectedListener() {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
      if (view != null) {
        String selectedMenu = ((TextView) view).getText().toString();
        if (position == 0) {
          mCallback.onChartNameSelected("Chart");
          //chartListener.fragment0Changed("Chart", getActivity().getSupportFragmentManager());
        } else if (position == 1) {
          mCallback.onChartNameSelected("Hour");
          //chartListener.fragment0Changed("Hour", getActivity().getSupportFragmentManager());
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
        mStartTimeInMillis = 0;
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

      GetDataDailyChart();
    }
  };

  private void LoadDailyChart() {

    DatabaseReference referActivities = db.getReference().child(getString(R.string.ref_planner)).child(user.getUid()).child(getString(R.string.ref_activities));
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
    mActivityType.setSelection(mCategoryPosition);

  }

  private void GetDataDailyChart() {

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
      cF.set(cF.get(Calendar.YEAR),cF.get(Calendar.MONTH),cF.get(Calendar.DAY_OF_MONTH));
    }
    else {
      cS.set(cS.get(Calendar.YEAR),cS.get(Calendar.MONTH),cS.get(Calendar.DAY_OF_MONTH));

      mYear = cS.get(Calendar.YEAR);
      mMonth = cS.get(Calendar.MONTH);
      mDayOfMonth = cS.get(Calendar.DAY_OF_MONTH);
      mDateS.setText(getResources().getString(R.string.date_format,String.valueOf(mYear),
              String.valueOf(mMonth+1),String.valueOf(mDayOfMonth)));

    }

    mStartTimeInMillis = cS.getTimeInMillis();

    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    Query query = baseRef.child(getString(R.string.activity_records)).orderByChild("formattedDate")
            .equalTo(sdf.format(cS.getTime()));

    eventsList = new ArrayList<>();
    RxFirebaseDatabase.observeSingleValueEvent(query,DataSnapshotMapper.listOf(ActivityRecord.class))
            .switchIfEmpty((SingleSource<? extends List<ActivityRecord>>) empty -> {
              showEmptyImage();
            })
            .subscribe(events -> {
              for (ActivityRecord ac : events)
              {
                if (selectedCategory == "" || ac.getCategory().equals(selectedCategory))
                {
                  eventsList.add(ac);
                }
              }

              if (eventsList.size()==0)
              {
                showEmptyImage();
              }
              else {
                hideEmptyImage();
              }
              loadMoodAddictionData();
            });
  }

  private void showEmptyImage()
  {
    mOnboardingLayout.setVisibility(View.VISIBLE);
  }

  private void hideEmptyImage()
  {
    mOnboardingLayout.setVisibility(View.GONE);
  }

  private void loadMoodAddictionData() {
    Query referAppealing = db.getReference().child(getString(R.string.ref_planner)).child(user.getUid()).child(getString(R.string.ref_appealing)).orderByChild("numValue");
    Query referMood = db.getReference().child(getString(R.string.ref_planner)).child(user.getUid()).child(getString(R.string.ref_mood)).orderByChild("numValue");

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
    Map<String,Float> chartValues = new LinkedHashMap<>();
    float total = 0;
    categoryMap = sortHashByValue(categoryMap);
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
    dataSet.setValueFormatter(labelFormatter);
    dataSet.setColors(new int[] {R.color.Bad , R.color.Average, R.color.High }, getActivity());
    PieData data = new PieData(dataSet);
    mChart.getDescription().setEnabled(false);
    mChart.setData(data);
    mChart.invalidate();

  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putLong(START_DATE,mStartTimeInMillis);
    outState.putInt(CATEGORY_POSITION,mActivityType.getSelectedItemPosition());
    outState.putString(SELECTED_CATEGORY,selectedCategory);
  }

  public HashMap<String,Integer> sortHashByValue(Map<String,Integer> hash) {
    List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(hash.entrySet());

    Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
      @Override
      public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
        return o1.getValue().compareTo(o2.getValue());
      }
    });
    HashMap<String, Integer> sortedHash = new LinkedHashMap<String, Integer>();
    for (Map.Entry<String, Integer> entry : list) {
      sortedHash.put(entry.getKey(), entry.getValue());
    }
    return sortedHash;
  }


  IValueFormatter labelFormatter = new IValueFormatter() {

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
      return String.valueOf((int)value)+"%";
    }
  };



}