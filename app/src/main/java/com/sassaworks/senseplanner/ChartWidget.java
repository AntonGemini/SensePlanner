package com.sassaworks.senseplanner;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import com.sassaworks.senseplanner.service.ChartIntentService;

import java.lang.reflect.Method;

/**
 * Implementation of App Widget functionality.
 */
public class ChartWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Bitmap chartBitmap) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.chart_widget);
        views.setImageViewBitmap(R.id.appwidget_image,chartBitmap);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        ChartIntentService.startActionGetChart(context);

    }

    public static void updateChart(Context context,
                                   AppWidgetManager appWidgetManager, int[] appWidgetIds, Bitmap chartBitmap)
    {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, chartBitmap);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}
