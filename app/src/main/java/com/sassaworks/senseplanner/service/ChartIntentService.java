package com.sassaworks.senseplanner.service;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.sassaworks.senseplanner.ChartWidget;
import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.data.ActivityRecord;
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

import durdinapps.rxfirebase2.DataSnapshotMapper;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import okhttp3.internal.connection.StreamAllocation;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ChartIntentService extends IntentService {
  // TODO: Rename actions, choose action names that describe tasks that this
  // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
  private static final String ACTION_GET_CHART = "com.sassaworks.senseplanner.service.action.GET_CHART";
  private static final String CHART_DATE = "chart_date";

  // TODO: Rename parameters
  private FirebaseDatabase db;
  private FirebaseUser user;
  private DatabaseReference baseRef;
  private ArrayList<ActivityRecord> eventsList;
  Map<String,Integer> moodMap = new HashMap<>();
  private PieChart pieChart;
  PieChart mChart;

  public ChartIntentService() {
    super("ChartIntentService");
  }

  /**
   * Starts this service to perform action Foo with the given parameters. If
   * the service is already performing a task this action will be queued.
   *
   * @see IntentService
   */
  // TODO: Customize helper method
  public static void startActionGetChart(Context context, Long chartDate) {
    Intent intent = new Intent(context, ChartIntentService.class);
    intent.putExtra(CHART_DATE,chartDate);
    intent.setAction(ACTION_GET_CHART);
    context.startService(intent);
  }

  /**
   * Starts this service to perform action Baz with the given parameters. If
   * the service is already performing a task this action will be queued.
   *
   * @see IntentService
   */

  @Override
  protected void onHandleIntent(Intent intent) {
    if (intent != null) {
      final String action = intent.getAction();
      if (ACTION_GET_CHART.equals(action)) {
        handleActionFoo(intent.getLongExtra(CHART_DATE,0));
      }
    }
  }

  /**
   * Handle action Foo in the provided background thread with the provided
   * parameters.
   */
  private void handleActionFoo(long chartDate) {


    db = FirebaseDatabase.getInstance();
    user = FirebaseAuth.getInstance().getCurrentUser();
    baseRef = db.getReference(getString(R.string.ref_planner)).child(user.getUid());

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(chartDate);
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    Query query = baseRef.child("activity_records").orderByChild("formattedDate")
            .equalTo(sdf.format(calendar.getTime()));
    DatabaseReference refMood = baseRef.child("mood");
    eventsList = new ArrayList<>();
    RxFirebaseDatabase.observeSingleValueEvent(query, DataSnapshotMapper.listOf(ActivityRecord.class))
            .concatMap(events -> {
              for (ActivityRecord ac : events)
              {
                eventsList.add(ac);
              }
              return RxFirebaseDatabase.observeSingleValueEvent(refMood,DataSnapshotMapper.listOf(Mood.class));
            }).subscribe(mood -> {
      for(Mood m:mood)
      {
        moodMap.put(m.getName(),m.getNumValue());
      }
      LoadDailyChart();
    });


  }

  private void LoadDailyChart() {
    Map<String,Float> chartValues = new LinkedHashMap<>();
    float total = 0;
    moodMap = sortHashByValue(moodMap);
    for (Map.Entry<String, Integer> entry : moodMap.entrySet())  {
      chartValues.put(entry.getKey(),0f);
      for (ActivityRecord event : eventsList)
      {
        String type = event.getMoodType();
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

    mChart = new PieChart(getApplicationContext());
    PieDataSet dataSet = new PieDataSet(entries,"Share");
    dataSet.setColors(new int[] { R.color.Bad, R.color.Average, R.color.High }, getApplicationContext());
    PieData data = new PieData(dataSet);
    mChart.setData(data);

    mChart.measure(View.MeasureSpec.makeMeasureSpec(400,View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(555,View.MeasureSpec.EXACTLY));
    mChart.layout(0, 0, mChart.getMeasuredWidth(), mChart.getMeasuredHeight());
    mChart.invalidate();
    //chartTask.execute();
    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        Bitmap chartBitmap = mChart.getChartBitmap();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(),ChartWidget.class));
        ChartWidget.updateChart(getApplicationContext(),appWidgetManager,appWidgetIds,chartBitmap);
      }
    },800);


  }

  /**
   * Handle action Baz in the provided background thread with the provided
   * parameters.
   */
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

  AsyncTask<Void,Void,PieChart> chartTask = new AsyncTask<Void, Void, PieChart>() {
    @Override
    protected PieChart doInBackground(Void... barCharts) {
      pieChart.invalidate();
      return pieChart;
    }

    @Override
    protected void onPostExecute(PieChart chart) {
      Bitmap chartBitmap = chart.getChartBitmap();
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
      int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(),ChartWidget.class));
      ChartWidget.updateChart(getApplicationContext(),appWidgetManager,appWidgetIds,chartBitmap);

    }
  };
}