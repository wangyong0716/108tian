package com.ksider.mobile.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.ksider.mobile.android.WebView.R;

/**
 * Created by wenkui on 3/18/15.
 */
public class CustomerViewPaper extends ViewPager {
    protected Boolean mScrollEnable = true;
    public CustomerViewPaper(Context context) {
        super(context);
    }

    public CustomerViewPaper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    protected void init(Context context, AttributeSet attrs){
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CustomerViewPaper, 0, 0);
        mScrollEnable = attributes.getBoolean(R.styleable.CustomerViewPaper_scrollEnable,true);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.mScrollEnable && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.mScrollEnable && super.onInterceptTouchEvent(event);
    }

    public void setScrollEnable(boolean b) {
        this.mScrollEnable = b;
    }

}
