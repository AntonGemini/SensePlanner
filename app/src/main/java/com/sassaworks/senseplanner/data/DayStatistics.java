package com.sassaworks.senseplanner.data;

public class DayStatistics {

  public float getMood_avg() {
    return mood_avg;
  }

  public void setMood_avg(float mood_avg) {
    this.mood_avg = mood_avg;
  }

  private float mood_avg;
  private int mood_num;
  private float appeal_avg;
  private int appeal_num;
  private long timestamp;

  public int getMood_num() {
    return mood_num;
  }

  public void setMood_num(int mood_num) {
    this.mood_num = mood_num;
  }

  public float getAppeal_avg() {
    return appeal_avg;
  }

  public void setAppeal_avg(float appeal_avg) {
    this.appeal_avg = appeal_avg;
  }

  public int getAppeal_num() {
    return appeal_num;
  }

  public void setAppeal_num(int appeal_num) {
    this.appeal_num = appeal_num;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}