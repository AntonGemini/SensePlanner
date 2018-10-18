package com.sassaworks.senseplanner;

import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.sassaworks.senseplanner.service.ChartIntentService;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConfigChartWidgetActivity extends AppCompatActivity {

    @BindView(R.id.et_date_config) EditText mDateEditText;
    @BindView(R.id.bt_config) Button mButton;

    private static final String PREFS_NAME = "com.sassaworks.senseplanner.ChartWidget";
    public static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    int mYear;
    int mMonth;
    int mDayOfMonth;
    long mStartTimeInMillis;

    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_config_chart_widget);

        ButterKnife.bind(this);
        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        calendar = Calendar.getInstance();

        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        mDateEditText.setText(getString(R.string.date_format,String.valueOf(mDayOfMonth),String.valueOf(mMonth+1),String.valueOf(mYear)));

        mDateEditText.setOnClickListener(onDateClickListener);
        mButton.setOnClickListener(onButtonClickListener);
    }

    private View.OnClickListener onButtonClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            calendar.set(mYear,mMonth,mDayOfMonth,0,0);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            sharedPreferences.edit().putLong(PREF_PREFIX_KEY + " " + mAppWidgetId,calendar.getTimeInMillis()).apply();
            //AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getBaseContext());
            ChartIntentService.startActionGetChart(getBaseContext(),calendar.getTimeInMillis());
            //IngredientsWidget.updateAppWidget(getBaseContext(),appWidgetManager,mAppWidgetId);

            Intent resultIntent = new Intent();
            resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,mAppWidgetId);
            setResult(RESULT_OK,resultIntent);
            finish();
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

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if (v.getId() == R.id.et_date_config) {

                    mDateEditText.setText(getString(R.string.date_format,String.valueOf(dayOfMonth),String.valueOf(month+1),String.valueOf(year)));
                    mYear = year;
                    mMonth = month;
                    mDayOfMonth = dayOfMonth;
                }
            }
        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }
}
