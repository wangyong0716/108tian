package com.ksider.mobile.android.partion;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.WebView.SingleListActivity;
import com.ksider.mobile.android.model.TrafficInfoModel;
import com.ksider.mobile.android.utils.BasicCategory;

/**
 * Created by yong on 7/29/15.
 */
public class RecommendView extends LinearLayout {
    private Context context;
    private String title;
    private TrafficInfoModel trafficInfo;

    public RecommendView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public RecommendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public RecommendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.partion_recommend_view, this);
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }

    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
    }

    public void setValues(String title, TrafficInfoModel trafficInfo) {
        this.title = title;
        this.trafficInfo = trafficInfo;
        findViewById(R.id.recommend_accommodation).setOnClickListener(listener);
        findViewById(R.id.recommend_pickingpart).setOnClickListener(listener);
        findViewById(R.id.recommend_activity).setOnClickListener(listener);
    }

    private OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, SingleListActivity.class);
            intent.putExtra("data", trafficInfo);
            intent.putExtra("around", true);
            intent.putExtra("title", title);
            switch (view.getId()) {
                case R.id.recommend_accommodation:
                    intent.putExtra("category", BasicCategory.RESORT);
                    break;
                case R.id.recommend_pickingpart:
                    intent.putExtra("category", BasicCategory.PICKINGPART);
                    break;
                case R.id.recommend_activity:
                    intent.putExtra("category", BasicCategory.ACTIVITY);
                    break;
                default:
                    intent.putExtra("category", BasicCategory.UNKOWN);
                    break;
            }
            context.startActivity(intent);
        }
    };

}
