package com.ksider.mobile.android.partion;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.utils.HtmlWraper;

/**
 * Created by yong on 7/28/15.
 */
public class WebContentView extends LinearLayout{
    private Context context;

    public WebContentView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public WebContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public WebContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.partion_web_view, this);
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }

    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
    }

    public void setWebContent(String webContent){
        Log.v("AAA","content="+webContent);
        WebView content = (WebView) (ViewGroup) findViewById(R.id.web_content);
        content.getSettings().setJavaScriptEnabled(true);
        content.getSettings().setDomStorageEnabled(true);
        content.getSettings().setUseWideViewPort(true);
        content.getSettings().setLoadWithOverviewMode(true);
        content.getSettings().setBuiltInZoomControls(true);
        content.loadData(HtmlWraper.getHtmlDoc(webContent), "text/html; charset=UTF-8", null);
    }
}

