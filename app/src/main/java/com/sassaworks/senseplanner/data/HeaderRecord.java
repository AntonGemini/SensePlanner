package com.sassaworks.senseplanner.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HeaderRecord implements CollectionItem {

    private long formatDate;

    @Override
    public int getItemType() {
        return CollectionItem.TYPE_DATE;
    }

    @Override
    public String getFormattedDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(formatDate);
        return new SimpleDateFormat("dd-MM-yyyy").format(calendar.getTime());
    }

    public void setFormattedDate(long formatDate)
    {
        this.formatDate = formatDate;
    }
}