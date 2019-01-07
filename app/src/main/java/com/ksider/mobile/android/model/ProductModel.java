package com.ksider.mobile.android.model;

/**
 * Created by yong on 8/6/15.
 */
public class ProductModel {
    private String id;
    private double price;
    private String headImg;
    private String name;
    private String type;
    private double distance;
    private long startTime;

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {

        return startTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getId() {

        return id;
    }

    public double getPrice() {
        return price;
    }

    public String getHeadImg() {
        return headImg;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getDistance() {
        return distance;
    }
}
