package com.ksider.mobile.android.personal;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import com.ksider.mobile.android.WebView.BaseActivity;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.adaptor.OrderTabIndicatorAdaptor;
import com.ksider.mobile.android.model.ThemeData;
import com.ksider.mobile.android.view.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yong on 8/15/15.
 */
public class OrderActivity extends BaseActivity {
    private OrderTabIndicatorAdaptor mTabAdaptor;
    private String[] filters = {"待付款", "待消费", "已退款", "已消费"};
    public final static String[] choser = {"unPaid", "unConsumed", "refund", "consumed"};
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        customActionBar("我的订单");
        if (getIntent() != null) {
            index = getIntent().getIntExtra("index", 0);
        }

        List<ThemeData> items = new ArrayList<ThemeData>();
        for (int i = 0; i < filters.length; i++) {
            ThemeData theme = new ThemeData();
            theme.id = String.valueOf(i);
            theme.name = filters[i];
            items.add(theme);
        }
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        mTabAdaptor = new OrderTabIndicatorAdaptor(getSupportFragmentManager(), items);
        pager.setAdapter(mTabAdaptor);
        TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        if (index < items.size()) {
            indicator.setCurrentItem(index);
        }
    }
}
