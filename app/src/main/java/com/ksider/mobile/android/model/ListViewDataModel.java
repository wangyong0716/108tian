package com.ksider.mobile.android.model;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class ListViewDataModel extends BaseDataModel {
    private static final long serialVersionUID = 866467;
    public String location;
    public String price;
    public String collection;
    public String startDate;
    public Bitmap imageBitmap;
    public Drawable imageDrawable;
    public String issued;
    public String label;
    public String distance;
    public boolean isFav;

    public ListViewDataModel() {

    }

    public ListViewDataModel(String title, String location, String price, String collection) {
        this.title = title;
        this.location = location;
        this.price = price;
        this.collection = collection;
    }

    public ListViewDataModel(String title, String location, String price, String collection, Bitmap image) {
        this(title, location, price, collection);
        this.imageBitmap = image;
    }

    public ListViewDataModel(String title, String location, String price, String collection, Drawable image) {
        this(title, location, price, collection);
        this.imageDrawable = image;
    }
}
