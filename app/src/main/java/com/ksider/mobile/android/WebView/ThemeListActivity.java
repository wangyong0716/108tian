package com.ksider.mobile.android.WebView;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import com.ksider.mobile.android.adaptor.ListThemeTabIndicatorAdaptor;
import com.ksider.mobile.android.model.ThemeData;
import com.ksider.mobile.android.utils.BasicCategory;
import com.ksider.mobile.android.view.pulltozoomview.PullToZoomScrollView;
import com.ksider.mobile.android.view.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

public class ThemeListActivity extends ActionBarActivity {
    protected ListThemeTabIndicatorAdaptor mTabAdaptor;
    private String name;
    private String id;
    public static String[] filters = {"景点", "住宿", "采摘"};
    public static BasicCategory[] categories = {BasicCategory.ATTRACTIONS, BasicCategory.RESORT, BasicCategory.PICKINGPART};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_themes);
        if (getIntent() != null) {
            name = getIntent().getStringExtra("name");
            id = getIntent().getStringExtra("id");
        }
        customActionBar(name);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        List<ThemeData> items = new ArrayList<ThemeData>();
        for (int i = 0; i < filters.length; i++) {
            ThemeData theme = new ThemeData();
            theme.id = id;
            theme.name = filters[i];
            items.add(theme);
        }
        mTabAdaptor = new ListThemeTabIndicatorAdaptor(getSupportFragmentManager(), items);
        pager.setAdapter(mTabAdaptor);
        TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(pager);
    }

    protected void customActionBar(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_background_color));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        TextView titleTextView = (TextView) findViewById(R.id.list_title);
//        findViewById(R.id.toolbar_line).setVisibility(View.INVISIBLE);
        if (titleTextView != null) {
            titleTextView.setText(title);
        }

        findViewById(R.id.list_backbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThemeListActivity.super.onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageView backButton = (ImageView) findViewById(R.id.list_backbutton);
        backButton.setImageResource(R.drawable.backbutton_icon);
        backButton.getDrawable().setAlpha(255);
    }
}
