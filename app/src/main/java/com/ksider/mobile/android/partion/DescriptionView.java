package com.ksider.mobile.android.partion;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;

/**
 * Created by yong on 2015/6/2.
 */
public class DescriptionView extends LinearLayout {
    public DescriptionView(Context context) {
        super(context);
        init();
    }

    public DescriptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DescriptionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.partion_description_view, this);
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }

    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
    }

    public void setContent(String content) {
//        ((AlignTextView)findViewById(R.id.part_content)).setContent(content);
        ((TextView)findViewById(R.id.part_content)).setText(content);
    }

    public void setContent(int contentId) {
        setTitle(getResources().getString(contentId));
    }
}
