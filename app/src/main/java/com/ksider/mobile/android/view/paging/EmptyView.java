package com.ksider.mobile.android.view.paging;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;

/**
 * Created by yong on 8/22/15.
 */
public class EmptyView extends LinearLayout {
    private Context context;

    public EmptyView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public EmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public EmptyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.list_empty_view, this);
    }

    public void setEmptyInfo(int infoId) {
        setEmptyInfo((String) context.getText(infoId));
    }

    public void setEmptyInfo(String info) {
        ((TextView) findViewById(R.id.empty_info)).setText(info);
    }
}