package com.ksider.mobile.android.model;

/**
 * Created by yong on 2015/6/18.
 */
public class AdModel {
    private String img;
    private String to;
    private String name;
    private String type;
    private long modified;
    private int holdTime;
    private boolean filled = false;

    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    public boolean isFilled() {

        return filled;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    public void setHoldTime(int holdTime) {
        this.holdTime = holdTime;
    }

    public String getImg() {

        return img;
    }

    public String getTo() {
        return to;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public long getModified() {
        return modified;
    }

    public int getHoldTime() {
        return holdTime;
    }
}
