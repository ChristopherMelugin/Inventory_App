package com.cmelugin.inventory;

public class Tag {

    private int mId;
    private String mTag;


    // public getter methods
    public int getId() {
        return mId;
    }

    public String getTag() {
        return mTag;
    }


    // public setter methods
    public void setId(int id) {
        mId = id;
    }

    public void setTag(String tag) {
        mTag = tag;
    }


    // Overriding the toString() function so that it displays correctly in the spinner
    @Override
    public String toString() {
        return mTag;
    }
}