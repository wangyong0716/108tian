package com.ksider.mobile.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by wenkui on 3/27/15.
 */
public class FakeBoldTextView extends TextView {
    public FakeBoldTextView(Context context) {
        super(context);
        setFakeBoldText();
    }

    public FakeBoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFakeBoldText();
    }

    public FakeBoldTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFakeBoldText();
    }
    protected void setFakeBoldText(){
        getPaint().setFakeBoldText(true);
    }
}
