package com.ksider.mobile.android.activity.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.adaptor.TabIndicatorAdaptor;
import com.ksider.mobile.android.model.ThemeData;
import com.ksider.mobile.android.scrollListView.OverScrollPullToRefreshListView;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.SelectDialog;
import com.ksider.mobile.android.utils.TabIndicator;
import com.ksider.mobile.android.view.viewpagerindicator.TabPageIndicator;
import com.umeng.analytics.MobclickAgent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends BaseFragment {
    private View mRoot;
    private TabIndicatorAdaptor mTabAdaptor;
    private String THEME_TAG = "activity_theme";
    private TabIndicator mTabIndicator = TabIndicator.DEFAULT;

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("event_list");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("event_list");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRoot != null) {
            ViewGroup parent = (ViewGroup) mRoot.getParent();
            if (parent != null) {
                parent.removeView(mRoot);
                return mRoot;
            }
        }
        mRoot = inflater.inflate(R.layout.list_fragment, container, false);
        initMask();
        TextView title = (TextView) mRoot.findViewById(R.id.list_title);
        mRoot.findViewById(R.id.header_banner).setBackgroundColor(getResources().getColor(R.color.toolbar_background_color));
        title.setText("活动");
        mRoot.findViewById(R.id.filter_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectDialog.Builder builder = new SelectDialog.Builder(getActivity());
                builder.setItem1("本周出发", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refresh(TabIndicator.THISWEEK);
                    }
                }).setItem2("下周出发", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refresh(TabIndicator.NEXTWEEK);
                    }
                }).setItem3("价格升序", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refresh(TabIndicator.PRICE_ASC);
                    }
                }).setItem4("价格降序", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refresh(TabIndicator.PRICE_DESC);
                    }
                }).setItem5("离我最近", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refresh(TabIndicator.NEARBY);
                    }
                }).show();
            }
        });
        Network.getInstance().addToRequestQueue(getRequest(), THEME_TAG);
        return mRoot;
    }

    public void initMask() {
        OverScrollPullToRefreshListView errorMask = (OverScrollPullToRefreshListView) mRoot.findViewById(R.id.error_mask);
        errorMask.setAdapter(new ArrayAdapter(getActivity(), 0));
        errorMask.onRefreshComplete();
        errorMask.setHasMoreItems(false);
        errorMask.setVisibility(View.GONE);
        errorMask.setOnRefreshListener(new OverScrollPullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Network.getInstance().addToRequestQueue(getRequest(), THEME_TAG);
            }
        });
    }

    public void showMask() {
        OverScrollPullToRefreshListView errorMask = (OverScrollPullToRefreshListView) mRoot.findViewById(R.id.error_mask);
        errorMask.onRefreshComplete();
        errorMask.setVisibility(View.VISIBLE);
    }

    public void hideMask() {
        OverScrollPullToRefreshListView errorMask = (OverScrollPullToRefreshListView) mRoot.findViewById(R.id.error_mask);
        errorMask.onRefreshComplete();
        errorMask.setVisibility(View.GONE);
    }

    protected void refresh(TabIndicator indicator) {
        ViewPager pager = (ViewPager) mRoot.findViewById(R.id.pager);
        Fragment page = getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + pager.getCurrentItem());
        if (page != null) {
            ((GoodsListViewPagingFragment) page).refrash(indicator);
        }
        mTabIndicator = indicator;
        if (mTabAdaptor != null) {
            mTabAdaptor.setIndicator(indicator);
        }
    }

    protected void process(JSONArray data) {
        hideMask();
        List<ThemeData> items = new ArrayList<ThemeData>();
        for (int i = 1; i < data.length(); i++) {
            try {
                ThemeData theme = new ThemeData();
                JSONObject item = data.getJSONObject(i);
                theme.id = String.valueOf(item.getInt("_id"));
                theme.name = item.getString("name");
                items.add(theme);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ViewPager pager = (ViewPager) mRoot.findViewById(R.id.pager);
        mTabAdaptor = new TabIndicatorAdaptor(getChildFragmentManager(), items);
        mTabAdaptor.setIndicator(mTabIndicator);
        pager.setAdapter(mTabAdaptor);
        TabPageIndicator indicator = (TabPageIndicator) mRoot.findViewById(R.id.indicator);
        indicator.setViewPager(pager);
    }

    protected JsonObjectRequest getRequest() {
        return new JsonObjectRequest(APIUtils.getTheme(APIUtils.ACTIVITY), null, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        process(response.getJSONArray("data"));
                    } else {
                        showMask();
                    }
                } catch (JSONException e) {
                    showMask();
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showMask();
            }
        });
    }

}
