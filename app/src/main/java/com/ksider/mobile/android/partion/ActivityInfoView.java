package com.ksider.mobile.android.partion;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;

/**
 * Created by yong on 2015/6/2.
 */
public class ActivityInfoView extends LinearLayout {
    public ActivityInfoView(Context context) {
        super(context);
        init();
    }

    public ActivityInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ActivityInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.partion_activity_info_view, this);
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }

    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
    }

    public void setDuration(String duration) {
        ((TextView) findViewById(R.id.duration)).setText(duration);
    }

    public void setTime(String time) {
        ((TextView) findViewById(R.id.time)).setText(time);
    }

    public void setLocation(String location) {
        ((TextView) findViewById(R.id.location)).setText(location);
    }
}
