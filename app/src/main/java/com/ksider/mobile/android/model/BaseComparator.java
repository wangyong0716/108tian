package com.ksider.mobile.android.model;

import java.util.Comparator;

/**
 * Created by yong on 8/5/15.
 */
public abstract class BaseComparator implements Comparator {
    public static final int DESC_SORT = 1;
    public static final int ASC_SORT = 2;
    public static final int MULTI_SORT = 3;
    protected int sortType;

    public BaseComparator() {
        this(ASC_SORT);
    }

    public BaseComparator(int sortType) {
        this.sortType = sortType;
    }

    public void setSortType(int sortType) {
        this.sortType = sortType;
    }

    public int getSortType() {

        return sortType;
    }

    @Override
    public int compare(Object o1, Object o2) {
        return 0;
    }
}
