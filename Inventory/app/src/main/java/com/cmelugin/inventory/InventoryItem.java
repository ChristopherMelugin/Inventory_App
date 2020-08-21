package com.cmelugin.inventory;

public class InventoryItem {

    private int mId;
    private String mTitle;
    private int mQuantity;
    private int mNotifyOnLow;
    private String mUsername;

    // public getter methods
    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public int getNotifyOnLow() {
        return mNotifyOnLow;
    }

    public String getUsername() {
        return mUsername;
    }




    // public setter methods
    public void setId(int id) {mId = id;}

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setQuantity(int quantity) {
        mQuantity = quantity;
    }

    public void setNotifyOnLow(int notifyOnLow) {
        mNotifyOnLow = notifyOnLow;
    }

    public void setUsername(String username) {
        mUsername = username;
    }
}
