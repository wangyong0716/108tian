package com.ksider.mobile.android.partion;


import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.MoreDetailInfoActivity;
import com.ksider.mobile.android.WebView.R;

/**
 * Created by yong on 2015/6/2.
 */
public class DetailView extends LinearLayout {
    private Context context;
    private String title = "";

    public DetailView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public DetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public DetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.partion_fee_detail_view, this);
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.title)).setText(title);
        this.title = title;
    }

    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
    }

    public void setContent(String content) {
//        ((AlignTextView) findViewById(R.id.part_content)).setContent(content);
        ((TextView) findViewById(R.id.part_content)).setText(content);
    }

    public void setContent(int contentId) {
        setTitle(getResources().getString(contentId));
    }

    public void setMoreText(String moreText) {
        ((TextView) findViewById(R.id.more)).setText(moreText);
    }

    public void setMoreText(int moreTextId) {
        setMoreText(getResources().getString(moreTextId));
    }

    public void setMoreClickEvent(final String detail) {
        findViewById(R.id.more).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MoreDetailInfoActivity.class);
                intent.putExtra("type", MoreDetailInfoActivity.DETAIL_FEE_INFO);
                intent.putExtra("title", title);
                intent.putExtra("brief", detail);
                context.startActivity(intent);
            }
        });
    }
}
