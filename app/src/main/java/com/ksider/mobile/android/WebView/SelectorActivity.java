package com.ksider.mobile.android.WebView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.activity.fragment.ListViewPagingAdaptor;
import com.ksider.mobile.android.activity.fragment.SelectorArrayFragment;
import com.ksider.mobile.android.activity.fragment.SelectorItemsFragment;
import com.ksider.mobile.android.activity.fragment.SelectorPriceFragment;
import com.ksider.mobile.android.comm.ShareDataPool;
import com.ksider.mobile.android.model.BaseDataModel;
import com.ksider.mobile.android.model.ListViewDataModel;
import com.ksider.mobile.android.scrollListView.OverScrollPullToRefreshListView;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.*;
import com.umeng.analytics.MobclickAgent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by yong on 9/6/15.
 */
public class SelectorActivity extends BaseActivity {
    public static final int TYPE_THEME = 1;
    public static final int TYPE_LOCATION = 2;
    public static final int TYPE_PRICE = 3;

    private BasicCategory mCategory;
    private OverScrollPullToRefreshListView mPagingListView;
    private ListViewPagingAdaptor mPagingAdaptor;
    private int mPage = 0;           // 页数
    private int mMaxPageSize = -1;    // 最大页数

    private int themeId = -1;          // 游玩主题
    private int regionId = -1;       // 地区id
    private int sortId = 0;
    protected double mMinDistance = 0;

    private String themeString;
    private String locationString;

    private boolean showTheme = false;
    private boolean showLocation = false;
    private boolean showPrice = false;

    protected String mStatType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);
        new SlidingLayout(this);
        Intent intent = getIntent();
        if (intent != null) {
            mCategory = (BasicCategory) intent.getSerializableExtra("category");
        }
        mCategory = mCategory == null ? BasicCategory.UNKOWN : mCategory;
        switch (mCategory) {
            case ATTRACTIONS:
                customActionBar("周边");
                break;
            case RESORT:
            case FARMYARD:
                customActionBar("住宿");
                break;
            case PICKINGPART:
                customActionBar("采摘");
                break;
            default:
                customActionBar("筛选");
                break;
        }
        mPagingAdaptor = new ListViewPagingAdaptor(SelectorActivity.this, mCategory);
        mPagingListView = (OverScrollPullToRefreshListView) findViewById(R.id.content_list);
        mPagingListView.setOnRefreshListener(new OverScrollPullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        mPagingListView.setAdapter(mPagingAdaptor);
        mPagingListView.setPagingableListener(new OverScrollPullToRefreshListView.Pagingable() {
            @Override
            public void onLoadMoreItems() {
                mPage++;
                if (mPage * Constants.PAGING_STEP < mMaxPageSize) {
                    Network.getInstance().addToRequestQueue(getRequest());
                } else {
                    mPagingListView.onFinishLoading(false, null);
                }
            }
        });

        mPagingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                if (mPagingAdaptor.getCount() > position) {
                    Intent intent = Utils.getLandingActivity(SelectorActivity.this, mCategory);
                    BaseDataModel data = mPagingAdaptor.getItem(position);
                    if (data != null) {
                        if (mStatType != null) {
                            StatHandle.increaseClick(mStatType);
                        }
                        Utils.initDetailPageArg(intent, data);
                        startActivity(intent);
                    }
                }
            }
        });
        initStat();

        findViewById(R.id.theme_layout).setOnClickListener(listener);
        findViewById(R.id.location_layout).setOnClickListener(listener);
        findViewById(R.id.price_layout).setOnClickListener(listener);

        findViewById(R.id.mask).setOnClickListener(listener);
        findViewById(R.id.fragment_content).setOnClickListener(listener);
        refreshView();
        Network.getInstance().addToRequestQueue(getRequest());
        Network.getInstance().addToRequestQueue(getLocationsRequest());
        Network.getInstance().addToRequestQueue(getThemeRequest());
    }

    public void refresh() {
        mPagingListView.deleteFooterView();
        clearData();
        mPage = 0;
        Network.getInstance().addToRequestQueue(getRequest());
    }

    private void clearData() {
        if (mPagingListView != null && mPagingListView.getAdapter() != null) {
            mMaxPageSize = -1;
            mMinDistance = 0;
            ListViewPagingAdaptor adapter = (ListViewPagingAdaptor) ((HeaderViewListAdapter) mPagingListView.getAdapter()).getWrappedAdapter();
            adapter.removeAllItems();
        }
    }

    public Map<String, Object> getRequestParams() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("page", mPage);
        params.put("cityId", Storage.sharedPref.getInt("cityId", 1));
        if (regionId > 0) {
            params.put("regionId", regionId);
        }
        if (mCategory != BasicCategory.PICKINGPART) {
            if (themeId > 0) {
                params.put("theme", themeId);
            }
        } else {
            if (themeId > 0) {
                params.put("species", themeId);
            }
        }
        params.put("step", Constants.PAGING_STEP);

        if (sortId == 1) {
            params.put("sort", "priceAsc");
        } else if (sortId == 2) {
            params.put("sort", "priceDesc");
        } else if (sortId == 3) {
            params.put("sort", "distance");
            params.put("minDistance", mMinDistance);
            if (ShareDataPool.position != null) {
                params.put("lnglat", ShareDataPool.position.longitude + "," + ShareDataPool.position.latitude);
            }
        }
        return params;
    }

    public void setThemeId(int themeId) {
        if (this.themeId == themeId) {
            return;
        } else {
            this.themeId = themeId;
            refresh();
        }
    }

    public void setRegionId(int regionId) {
        if (this.regionId == regionId) {
            return;
        } else {
            this.regionId = regionId;
            refresh();
        }
    }

    public void setSortId(int sortId) {
        if (this.sortId == sortId) {
            return;
        } else {
            this.sortId = sortId;
            refresh();
        }
    }

    public void updateMinDistance(JSONArray array) {
        for (int i = array.length(); i > 0; i--) {
            try {
                JSONObject o = array.getJSONObject(i - 1);
                mMinDistance = o.getDouble("dis");
                return;
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
    }

    public String getLocationsRequestUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("cityId", Storage.sharedPref.getInt("cityId", 1));
        return APIUtils.getUrl(APIUtils.CITYREGIONINFO, params);
    }

    public String getRequestUrl() {
        String entry = null;
        switch (mCategory) {
            case ATTRACTIONS:
                entry = APIUtils.POI_ATTRACTIONS;
                break;
            case RESORT:
                entry = APIUtils.POI_RESORT;
                break;
            case FARMYARD:
                entry = APIUtils.POI_FARMYARD;
                break;
            case PICKINGPART:
                entry = APIUtils.POI_PICK;
                break;
            case ACTIVITY:
                entry = APIUtils.ACTIVITY;
                break;
            case GUIDE:
                entry = APIUtils.GUIDE;
                break;
            default:
                break;
        }
        if (entry != null) {
            return APIUtils.getPOIList(entry, getRequestParams());
        }
        return null;
    }

    protected JsonObjectRequest getLocationsRequest() {
        Log.v("AAA", "selectorActivity-->getLocationsRequest=" + getLocationsRequestUrl());
        return new JsonObjectRequest(getLocationsRequestUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        proccessLocationInfo(response.getJSONObject("data"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mPagingListView.onRefreshComplete();
                mPagingListView.showFailed();
                Log.v(Constants.LOG_TAG, "response:" + error.toString());
            }
        });
    }

    public void proccessLocationInfo(JSONObject data) {
        JSONArray array = new JSONArray();
        try {
            JSONObject outskirts = new JSONObject();
            outskirts.put("title", "近郊");
            outskirts.put("themes", data.getJSONArray("outskirts"));
            array.put(outskirts);
        } catch (JSONException js) {
            js.printStackTrace();
        }
        try {
            JSONObject incity = new JSONObject();
            incity.put("title", "市区");
            incity.put("themes", data.getJSONArray("incity"));
            array.put(incity);
        } catch (JSONException js) {
            js.printStackTrace();
        }
        try {
            JSONObject outer = new JSONObject();
            outer.put("title", "远郊");
            outer.put("themes", data.getJSONArray("outer"));
            array.put(outer);
        } catch (JSONException js) {
            js.printStackTrace();
        }
        locationString = array.toString();
    }

    protected JsonObjectRequest getRequest() {
        Log.v("AAA", "selectorActivity-->getRequest=" + getRequestUrl());
        return new JsonObjectRequest(getRequestUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        proccess(response.getJSONObject("data"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mPagingListView.onRefreshComplete();
                mPagingListView.showFailed();
                Log.v(Constants.LOG_TAG, "response:" + error.toString());
            }
        });
    }

    protected void proccess(JSONObject data) {
        if (mPagingListView != null) {
            mPagingListView.onRefreshComplete();
        }
        try {
            mMaxPageSize = data.getInt("number");
            List<ListViewDataModel> items = new ArrayList<ListViewDataModel>();
            JSONArray lists = data.getJSONArray("list");
            if (sortId == 3) {
                updateMinDistance(lists);
            }
            for (int i = 0; i < lists.length(); i++) {
                try {
                    ListViewDataModel item = new ListViewDataModel();
                    JSONObject o = (JSONObject) lists.get(i);
                    item.id = o.getString("id");
                    item.title = o.getString("name");
                    item.imgUrl = o.getString("headImg");
                    item.location = o.getString("district");
//                    Double dist = getDistance(o);
                    try {
                        item.distance = StringUtils.getDistance(o.getJSONArray("lngLatitude").getDouble(1), o.getJSONArray("lngLatitude").getDouble(0)) + "km";
                    } catch (Exception e) {
                        item.distance = "";
                        e.printStackTrace();
                    }

                    try {
                        JSONArray list = o.getJSONArray("priceRange");
                        if (list.length() > 1) {
                            item.price = StringUtils.getPriceRange(list.getDouble(0), list.getDouble(1));
                        } else {
                            item.price = "";
                        }
                    } catch (JSONException js) {
                        item.price = "";
                        js.printStackTrace();
                    }

//                    try {
//                        item.price = StringUtils.getPrice(o.getString("price"));
////                        item.price = StringUtils.getPrice(o.getDouble("price"));
//                    } catch (JSONException e) {
////                        e.printStackTrace();
//                    }
                    try {
                        item.collection = o.getString("fav");
                    } catch (JSONException e) {
                        Log.v(Constants.LOG_TAG, e.toString());
//                        e.printStackTrace();
                    }
                    try {
                        item.isFav = o.getBoolean("isFav");
                    } catch (JSONException js) {
                        item.isFav = false;
                        js.printStackTrace();
                    }

                    items.add(item);
                } catch (JSONException e) {
//                    e.printStackTrace();
                }
            }
            if (mPagingListView != null) {
                mPagingListView.onFinishLoading(items.size() > 0, items);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected double getDistance(JSONObject item) {
        Double dist = -1.0;
        try {
            JSONArray lngLatitude = item.getJSONArray("lngLatitude");
            Double lng = lngLatitude.getDouble(0);
            Double lat = lngLatitude.getDouble(1);
            dist = Maths.getSelfDistance(lat, lng);
            //fix 显示
            if (dist == 0.0) {
                dist = Math.random() * 5 / 10 + 0.1;
                dist = Math.round(dist * 100) * 1.0 / 100;
            }
        } catch (JSONException e) {
        } catch (Exception e) {
            Log.v(Constants.LOG_TAG, e.toString());
        }
        return dist;
    }

    public String getThemeRequestUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        switch (mCategory) {
            case ATTRACTIONS:
                params.put("type", "scene");
                break;
            case RESORT:
            case FARMYARD:
                params.put("type", "resort");
                break;
            case PICKINGPART:
                params.put("type", "pickNew");
                break;
            default:
                params.put("type", "scene");
                break;
        }
        params.put("cityId", Storage.sharedPref.getInt("cityId", 1));
        return APIUtils.getUrl(APIUtils.THEMEITEMS, params);
    }

    protected JsonObjectRequest getThemeRequest() {
        Log.v("AAA", "selectorActivity-->getThemeRequest=" + getThemeRequestUrl());
        return new JsonObjectRequest(getThemeRequestUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        proccessThemeInfo(response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mPagingListView.onRefreshComplete();
                mPagingListView.showFailed();
                Log.v(Constants.LOG_TAG, "response:" + error.toString());
            }
        });
    }

    public void proccessThemeInfo(JSONObject response) {
        try {
            switch (mCategory) {
                case ATTRACTIONS:
                    processSceneThemeInfo(response.getJSONArray("data"));
                    break;
                case RESORT:
                case FARMYARD:
                    processSceneThemeInfo(response.getJSONArray("data"));
                    break;
                case PICKINGPART:
                    processPickThemeInfo(response.getJSONObject("data"));
                    break;
                default:
                    break;
            }
        } catch (JSONException js) {
            js.printStackTrace();
        }

    }

    public void processPickThemeInfo(JSONObject data) {
        JSONArray array = new JSONArray();
        Iterator<String> keys = data.keys();

        while (keys.hasNext()) {
            try {
                String key = keys.next();
                JSONArray theme = getThemeArray(data.getJSONArray(key));
                if (theme.length() < 1) {
                    continue;
                }
                JSONObject object = new JSONObject();
                object.put("title", key);
                object.put("themes", theme);
                array.put(object);
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
        themeString = array.toString();
    }

    public JSONArray getThemeArray(JSONArray data) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject object = data.getJSONObject(i);
                JSONObject theme = new JSONObject();
                theme.put("id", object.getInt("_id"));
                theme.put("name", object.getString("name"));
                array.put(theme);
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
        return array;
    }

    public void processSceneThemeInfo(JSONArray array) {
        themeString = array.toString();
    }

    public void onResume() {
        super.onResume();
        ImageView backButton = (ImageView) findViewById(R.id.list_backbutton);
        backButton.setImageResource(R.drawable.backbutton_icon);
        backButton.getDrawable().setAlpha(255);
        MobclickAgent.onPageStart(mCategory.toString() + "_list"); //统计页面
        MobclickAgent.onResume(this);          //统计时长
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mCategory.toString() + "_list"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mStatType != null) {
            StatHandle.postImpclkEvent(SelectorActivity.this, mStatType);
        }
    }

    /**
     * update the status of mask
     */
    public void refreshView() {
        if (showTheme || showLocation || showPrice) {
            findViewById(R.id.mask).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.mask).setVisibility(View.GONE);
        }
    }

    public void refreshTheme(boolean showTheme) {
        if (this.showTheme == showTheme) {
            return;
        } else {
            this.showTheme = showTheme;
            if (showTheme) {
                ((TextView) findViewById(R.id.theme)).setTextColor(getResources().getColor(R.color.main_dark_green));
                ((ImageView) findViewById(R.id.theme_status_tag)).setImageResource(R.drawable.selector_icon_selected_up);
            } else {
                ((TextView) findViewById(R.id.theme)).setTextColor(getResources().getColor(R.color.personal_info_text_color));
                ((ImageView) findViewById(R.id.theme_status_tag)).setImageResource(R.drawable.selector_icon_unselect_down);
                getSupportFragmentManager().popBackStack();
            }
            refreshView();
        }
    }

    /**
     * update theme status and themeName
     *
     * @param showTheme whether the theme is chosed
     * @param themeName selector content
     */
    public void refreshTheme(boolean showTheme, String themeName) {
        ((TextView) findViewById(R.id.theme)).setText(themeName);
        refreshTheme(showTheme);
    }

    public void refreshLocation(boolean showLocation) {
        if (this.showLocation == showLocation) {
            return;
        } else {
            this.showLocation = showLocation;
            if (showLocation) {
                ((TextView) findViewById(R.id.location)).setTextColor(getResources().getColor(R.color.main_dark_green));
                ((ImageView) findViewById(R.id.location_status_tag)).setImageResource(R.drawable.selector_icon_selected_up);
            } else {
                ((TextView) findViewById(R.id.location)).setTextColor(getResources().getColor(R.color.personal_info_text_color));
                ((ImageView) findViewById(R.id.location_status_tag)).setImageResource(R.drawable.selector_icon_unselect_down);
                getSupportFragmentManager().popBackStack();
            }
            refreshView();
        }
    }

    public void refreshLocation(boolean showLocation, String locationName) {
        ((TextView) findViewById(R.id.location)).setText(locationName);
        refreshLocation(showLocation);
    }

    public void refreshPrice(boolean showPrice) {
        if (this.showPrice == showPrice) {
            return;
        } else {
            this.showPrice = showPrice;
            if (showPrice) {
                ((TextView) findViewById(R.id.price)).setTextColor(getResources().getColor(R.color.main_dark_green));
                ((ImageView) findViewById(R.id.price_status_tag)).setImageResource(R.drawable.selector_icon_selected_up);
            } else {
                ((TextView) findViewById(R.id.price)).setTextColor(getResources().getColor(R.color.personal_info_text_color));
                ((ImageView) findViewById(R.id.price_status_tag)).setImageResource(R.drawable.selector_icon_unselect_down);
                getSupportFragmentManager().popBackStack();
            }
            refreshView();
        }
    }

    public void refreshPrice(boolean showPrice, String priceName) {
        ((TextView) findViewById(R.id.price)).setText(priceName);
        refreshPrice(showPrice);
    }

    public void close() {
        refreshTheme(false);
        refreshLocation(false);
        refreshPrice(false);
    }

    @Override
    public void onBackPressed() {
        if (showTheme || showLocation || showPrice) {
            refreshTheme(false);
            refreshLocation(false);
            refreshPrice(false);
            refreshView();
            return;
        }
        super.onBackPressed();
    }

    public void initStat() {
        switch (mCategory) {
            case ATTRACTIONS:
                mStatType = StatHandle.ATTRACTION_LIST;
                break;
            case ACTIVITY:
                mStatType = StatHandle.EVENT_LIST;
                break;
            case RESORT:
                mStatType = StatHandle.RESORT_LIST;
                break;
            case FARMYARD:
                mStatType = StatHandle.FARM_LIST;
                break;
            case GUIDE:
                mStatType = StatHandle.GUIDE_LIST;
                break;
            case PICKINGPART:
                mStatType = StatHandle.PICK_LIST;
                break;
            default:
                break;
        }
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.theme_layout:
                    if (themeString == null || themeString.equals("")) {
                        Network.getInstance().addToRequestQueue(getThemeRequest());
                        return;
                    }
                    if (!showTheme) {
                        refreshLocation(false);
                        refreshPrice(false);
                        refreshTheme(true);

                        Fragment fragment;
                        if (mCategory == BasicCategory.PICKINGPART) {
                            fragment = new SelectorArrayFragment();
                        } else {
                            fragment = new SelectorItemsFragment();
                        }
                        Bundle args = new Bundle();
                        args.putInt("type", TYPE_THEME);
                        args.putString("data", themeString);
                        args.putInt("selectedId", themeId);
                        fragment.setArguments(args);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN | FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                        transaction.setCustomAnimations(R.anim.fragment_slide_down, R.anim.fragment_slide_up);
                        transaction.replace(R.id.fragment_content, fragment).addToBackStack(null).commitAllowingStateLoss();
                    } else {
                        refreshTheme(false);
                    }
                    break;
                case R.id.location_layout:
                    if (locationString == null || locationString.equals("")) {
                        Network.getInstance().addToRequestQueue(getLocationsRequest());
                        return;
                    }
                    if (!showLocation) {
                        refreshTheme(false);
                        refreshPrice(false);
                        refreshLocation(true);

                        Fragment fragment = new SelectorArrayFragment();
                        Bundle args = new Bundle();
                        args.putInt("type", TYPE_LOCATION);
                        args.putString("data", locationString);
                        args.putInt("selectedId", regionId);
                        fragment.setArguments(args);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN | FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                        transaction.setCustomAnimations(R.anim.fragment_slide_down, R.anim.fragment_slide_up);
                        transaction.replace(R.id.fragment_content, fragment).addToBackStack(null).commitAllowingStateLoss();
                    } else {
                        refreshLocation(false);
                    }
                    break;
                case R.id.price_layout:
                    if (!showPrice) {
                        refreshTheme(false);
                        refreshLocation(false);
                        refreshPrice(true);

                        Fragment fragment = new SelectorPriceFragment();
                        Bundle args = new Bundle();
                        args.putInt("selectedId", sortId);
                        fragment.setArguments(args);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN | FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                        transaction.setCustomAnimations(R.anim.fragment_slide_down, R.anim.fragment_slide_up);
                        transaction.replace(R.id.fragment_content, fragment).addToBackStack(null).commitAllowingStateLoss();
                    } else {
                        refreshPrice(false);
                    }
                    break;
                case R.id.mask:
                    close();
                default:
                    break;
            }
        }
    };
}
