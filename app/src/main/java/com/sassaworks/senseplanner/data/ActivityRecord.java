package com.sassaworks.senseplanner.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ActivityRecord extends Category implements CollectionItem {

    @Exclude
    private String key;

    private long timestamp;
    private long timestampF;

    private String category;
    private String moodType;
    private String jobAddiction;
    private String desciption;

    private boolean withNotify;
    private long eventId;

    public ActivityRecord()
    {}

    public ActivityRecord(String name, long timestamp, String category, String moodType,
                          String jobAddiction, String desciption, boolean withNotify, long timestampF)
    {
        super.setName(name);
        this.timestamp = timestamp;
        this.category = category;
        this.moodType = moodType;
        this.jobAddiction = jobAddiction;
        this.desciption = desciption;
        this.withNotify = withNotify;
        this.timestampF = timestampF;
    }


    protected ActivityRecord(Parcel in) {
        super(in);
        key = in.readString();
        timestamp = in.readLong();
        category = in.readString();
        moodType = in.readString();
        jobAddiction = in.readString();
        desciption = in.readString();
        withNotify = in.readByte() != 0;
        timestampF = in.readLong();
        eventId = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeString(key);
        dest.writeLong(timestamp);
        dest.writeString(category);
        dest.writeString(moodType);
        dest.writeString(jobAddiction);
        dest.writeString(desciption);
        dest.writeByte((byte) (withNotify ? 1 : 0));
        dest.writeLong(timestampF);
        dest.writeLong(eventId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ActivityRecord> CREATOR = new Creator<ActivityRecord>() {
        @Override
        public ActivityRecord createFromParcel(Parcel in) {
            return new ActivityRecord(in);
        }

        @Override
        public ActivityRecord[] newArray(int size) {
            return new ActivityRecord[size];
        }
    };

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMoodType() {
        return moodType;
    }

    public void setMoodType(String moodType) {
        this.moodType = moodType;
    }

    public String getJobAddiction() {
        return jobAddiction;
    }

    public void setJobAddiction(String jobAddiction) {
        this.jobAddiction = jobAddiction;
    }

    public String getDesciption() {
        return desciption;
    }

    public void setDesciption(String desciption) {
        this.desciption = desciption;
    }

    public boolean isWithNotify() {
        return withNotify;
    }

    public void setWithNotify(boolean withNotify) {
        this.withNotify = withNotify;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getTimestampF() { return timestampF; }

    public void setTimestampF(long timestampF) {this.timestampF = timestampF; }

    public long getEventId() { return eventId; }

    public void setEventId(long eventId) {this.eventId = eventId; }



    @Override
    public int getItemType() {
        return CollectionItem.TYPE_DESC;
    }

    @Override
    public String getFormattedDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return new SimpleDateFormat("dd-MM-yyyy").format(calendar.getTime());
    }
}
