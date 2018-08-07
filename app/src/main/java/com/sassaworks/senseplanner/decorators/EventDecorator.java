package com.sassaworks.senseplanner.decorators;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.sassaworks.senseplanner.R;
import com.sassaworks.senseplanner.data.ActivityRecord;

import java.util.ArrayList;

public class EventDecorator implements DayViewDecorator {

    private ArrayList<CalendarDay> events;
    Context context;
    ColorDrawable drawable;

    public EventDecorator(ArrayList<CalendarDay> events, Context context, ColorDrawable color)
    {
        this.events = events;
        this.context = context;
        this.drawable = color;
    }

    @Override
    public boolean shouldDecorate(CalendarDay calendarDay) {
        return events.contains(calendarDay);
    }

    @Override
    public void decorate(DayViewFacade dayViewFacade) {
        dayViewFacade.setBackgroundDrawable(drawable);
    }
}
