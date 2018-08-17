package com.sassaworks.senseplanner.data;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class Category implements Parcelable {

    private String name;

    private int numValue;

    public Category()
    {}


    protected Category(Parcel in) {
        name = in.readString();
        numValue = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(numValue);
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumValue() {
        return numValue;
    }

    public void setNumValue(int numValue) {
        this.numValue = numValue;
    }
}
