package com.sassaworks.senseplanner.data;

public class ActivityRecord extends Category {

    private long timestamp;

    private String category;
    private String moodType;
    private String jobAddiction;
    private String desciption;

    private boolean withNotify;

    public ActivityRecord()
    {}

    public ActivityRecord(String name, long timestamp, String category, String moodType,
                          String jobAddiction, String desciption, boolean withNotify)
    {
        super.setName(name);
        this.timestamp = timestamp;
        this.category = category;
        this.moodType = moodType;
        this.jobAddiction = jobAddiction;
        this.desciption = desciption;
        this.withNotify = withNotify;
    }


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
}
