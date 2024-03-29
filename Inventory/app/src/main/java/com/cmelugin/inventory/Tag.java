package com.cmelugin.inventory;

public class Tag {

    private int mId;
    private String mTag;
    private String mUsername;


    // public getter methods
    public int getId() {
        return mId;
    }

    public String getTag() {
        return mTag;
    }

    public String getUsername() {
        return mUsername;
    }


    // public setter methods
    public void setId(int id) {
        mId = id;
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public void setUsername(String username) {
        mUsername = username;
    }


    // Overriding the toString() function so that it displays correctly in the spinner
    @Override
    public String toString() {
        return mTag;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null || obj.getClass()!= this.getClass()) {
            return false;
        }
        Tag tag = (Tag) obj;

        return (tag.mTag.equals(this.mTag) && tag.mId == this.mId);
    }

    @Override
    public int hashCode() {
        return this.mId;
    }
}
