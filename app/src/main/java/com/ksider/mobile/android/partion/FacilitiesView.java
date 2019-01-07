package com.ksider.mobile.android.partion;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;

/**
 * Created by yong on 7/29/15.
 */
public class FacilitiesView extends LinearLayout {
    private Context context;

    public FacilitiesView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public FacilitiesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public FacilitiesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.partion_facilities_view, this);
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }

    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
    }

    public void setHasProduct(boolean hasProduct){
        if (hasProduct){
            setVisibility(VISIBLE);
        }else {
            setVisibility(GONE);
        }
    }

    public void setMoreText(String moreText) {
        ((TextView) findViewById(R.id.more)).setText(moreText);
    }

    public void setMoreText(int moreTextId) {
        setMoreText(getResources().getString(moreTextId));
    }
}
