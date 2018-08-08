package com.sassaworks.senseplanner.data;

public abstract class Category {

    private String name;

    private int numValue;

    public Category()
    {}


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
