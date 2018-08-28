package com.sassaworks.senseplanner.service;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;

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
import com.sassaworks.senseplanner.ChartWidget;
import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.data.ActivityRecord;
import com.sassaworks.senseplanner.data.Mood;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import durdinapps.rxfirebase2.DataSnapshotMapper;
import durdinapps.rxfirebase2.RxFirebaseDatabase;

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

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.sassaworks.senseplanner.service.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.sassaworks.senseplanner.service.extra.PARAM2";
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
    public static void startActionGetChart(Context context) {
        Intent intent = new Intent(context, ChartIntentService.class);
        intent.setAction(ACTION_GET_CHART);
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
//    public static void startActionBaz(Context context, String param1, String param2) {
//        Intent intent = new Intent(context, MyIntentService.class);
//        intent.setAction(ACTION_BAZ);
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
//        context.startService(intent);
//    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_CHART.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            }
//            else if (ACTION_BAZ.equals(action)) {
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
//                handleActionBaz(param1, param2);
//            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        db = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        baseRef = db.getReference("planner").child(user.getUid());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH,-7);
        //SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Query query = baseRef.child("activity_records").orderByChild("timestamp")
                .startAt(calendar.getTimeInMillis());
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
        Map<String,Float> chartValues = new HashMap<>();
        float total = 0;
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
//        PieChart.LayoutParams parms = new BarChart.LayoutParams(300, 200);
//        mChart.setLayoutParams(parms);
//        mChart.setMinimumWidth(300);
//        mChart.setMinimumHeight(200);
//        mChart.getViewPortHandler().setChartDimens(300, 200);
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
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
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
