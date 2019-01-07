package com.ksider.mobile.android.personal;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import com.ksider.mobile.android.WebView.BaseActivity;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.adaptor.StorageTabIndicatorAdaptor;
import com.ksider.mobile.android.model.ThemeData;
import com.ksider.mobile.android.view.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yong on 8/15/15.
 */
public class StorageActivity extends BaseActivity {
    private StorageTabIndicatorAdaptor mTabAdaptor;
    private String[] filters = {"攻略", "活动", "景点", "住宿"};
    public final static String[] choser = {"weekly", "event", "scene", "farmResort"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        customActionBar("收藏");

        List<ThemeData> items = new ArrayList<ThemeData>();
        for (int i = 0; i < filters.length; i++) {
            ThemeData theme = new ThemeData();
            theme.id = String.valueOf(i);
            theme.name = filters[i];
            items.add(theme);
        }
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        mTabAdaptor = new StorageTabIndicatorAdaptor(getSupportFragmentManager(), items);
        pager.setAdapter(mTabAdaptor);
        TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(pager);
    }
}
