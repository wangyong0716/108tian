package com.ksider.mobile.android.WebView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.adaptor.SingleListPagingAdaptor;
import com.ksider.mobile.android.model.BaseDataModel;
import com.ksider.mobile.android.model.ListViewDataModel;
import com.ksider.mobile.android.model.TrafficInfoModel;
import com.ksider.mobile.android.scrollListView.OverScrollPagingListView;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleListActivity extends ActionBarActivity {
    protected OverScrollPagingListView mListView;
    protected Boolean mHasRequest = false;
    protected SingleListPagingAdaptor mAdaptor;
    protected BasicCategory mBasicCategory;
    protected Boolean mAround = false;
    protected int mPage = 0; // 页数
    protected int mCityId = 1; // 城市id
    protected int mMaxPageSize = -1;
    protected double mMinDistance = 0;
    protected TrafficInfoModel mTrafficInfo;
    protected Boolean mIsLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_list_layout);
        new SlidingLayout(this);
        Bundle args = getIntent().getExtras();
        if (args != null) {
            mBasicCategory = (BasicCategory) args.getSerializable("category");
            mAround = (Boolean) args.getBoolean("around");
            mAround = mAround == null ? false : mAround;
            mTrafficInfo = (TrafficInfoModel) args.getSerializable("data");
        }
        mBasicCategory = mBasicCategory == null ? BasicCategory.UNKOWN : mBasicCategory;
        mListView = (OverScrollPagingListView) findViewById(R.id.content_list);
        mAdaptor = new SingleListPagingAdaptor(this, mBasicCategory);
        mListView.setAdapter(mAdaptor);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> params, View view, int position, long id) {
                if (position < mListView.getHeaderViewsCount() || position >= mListView.getHeaderViewsCount() + mAdaptor.getCount()) {
                    return;
                }
                BaseDataModel base = mAdaptor.getItem(position - mListView.getHeaderViewsCount());
                if (base != null) {
                    if (!mAround) {
                        Intent intent = Utils.getLandingActivity(SingleListActivity.this, BasicCategory.GUIDE);
                        intent.putExtra("data", base);
                        startActivity(intent);
                    } else {
                        Intent intent = Utils.getLandingActivity(SingleListActivity.this, mBasicCategory);
                        Utils.initDetailPageArg(intent, base);
                        startActivity(intent);
                    }
                }
            }
        });
        mListView.setPagingableListener(new OverScrollPagingListView.Pagingable() {
            @Override
            public void onLoadMoreItems() {
                if (!mIsLoading) {
                    mIsLoading = true;
                    mPage++;
                    if (mPage * Constants.PAGING_STEP < mMaxPageSize) {
                        Network.getInstance().addToRequestQueue(getRequest());
                    } else {
                        mListView.onFinishLoading(false, null);
                    }
                }
            }
        });
        customActionBar(toGetTitle());
    }

    protected void customActionBar(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_background_color));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        TextView titleTextView = (TextView) findViewById(R.id.list_title);
        if (titleTextView != null) {
            titleTextView.setText(title);
        }
        findViewById(R.id.list_backbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });
    }

    protected void back() {
        super.onBackPressed();
    }

    public String toGetTitle() {
        String title = "玩点大全";
        if (mAround) {
            switch (mBasicCategory) {
                case FARMYARD:
                case RESORT:
                    title = "周边住宿";
                    break;
                case ATTRACTIONS:
                    title = "周边景点";
                    break;
                case PICKINGPART:
                    title = "采摘园";
                    break;
                case ACTIVITY:
                    title = "活动";
                    break;
                default:
                    break;
            }
        }
        return title;
    }

    @Override
    public void onResume() {
        super.onResume();
            mPage = 0;
            Network.getInstance().addToRequestQueue(getRequest(), "singleList");
    }

    public Map<String, Object> getRequestParams() {
        Map<String, Object> params = new HashMap<String, Object>();
        if (mAround) {
            params.put("step", Constants.PAGING_STEP);
            params.put("sort", "distance");
            params.put("minDistance", mMinDistance);
            params.put("lnglat", mTrafficInfo.lng + "," + mTrafficInfo.lat);
        } else {
            mCityId = Storage.sharedPref.getInt("cityId", 1);
            params.put("cityId", mCityId);
        }
        params.put("page", mPage);
        return params;
    }

    public String getRequestUrl() {
        String entry = null;
        switch (mBasicCategory) {
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

    public String getPoiType() {
        switch (mBasicCategory) {
            case ATTRACTIONS:
                return "scene";
            case FARMYARD:
                return "farm";
            case PICKINGPART:
                return "pick";
            case RESORT:
                return "resort";
            case ACTIVITY:
                return "events";
            case GUIDE:
                return "weekly";
            default:
                return "";
        }
    }

    protected JsonObjectRequest getRequest() {
        Log.v("AAA", "singleListActivity->url=" + getRequestUrl());
        return new JsonObjectRequest(getRequestUrl(), null, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    mIsLoading = false;
                    if (response != null && response.getInt("status") == 0) {
                        proccess(response.getJSONObject("data"), getPoiType());
                        mMaxPageSize = response.getJSONObject("data").getInt("number");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mIsLoading = false;
                Log.v(Constants.LOG_TAG, "response:" + error.toString());
            }
        });
    }

    protected void updateMiniDistance(JSONObject data) {
        if (mAround) {
            JSONArray lists;
            try {
                lists = data.getJSONArray("list");
                for (int i = 0; i < lists.length(); i++) {
                    JSONObject o = (JSONObject) lists.get(i);
                    mMinDistance = o.getDouble("dis");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

//    protected double getDistance(JSONObject item) {
//        Double dist = -1.0;
//        try {
//            dist = item.getDouble("dis");
//            dist = Math.round(dist * 100) * 1.0 / 100;
//            // 修正为0的不合理情况
//            if (dist == 0) {
//                dist = Math.random() * 0.4 + 0.1;
//                dist = Math.round(dist * 100) * 1.0 / 100;
//            }
//        } catch (JSONException e) {
//        } catch (Exception e) {
//            Log.v(Constants.LOG_TAG, e.toString());
//        }
//        return dist;
//    }

    protected void proccess(JSONObject data, String poiType) {
        try {
            updateMiniDistance(data);
            List<ListViewDataModel> items = new ArrayList<ListViewDataModel>();
            JSONArray lists = data.getJSONArray("list");
            if (mAround) {
                for (int i = 0; i < lists.length(); i++) {
                    ListViewDataModel item = new ListViewDataModel();
                    JSONObject o = (JSONObject) lists.get(i);
                    item.id = o.getString("id");
                    item.title = o.getString("name");
                    try {
                        item.imgUrl = o.getString("headImg");
                    } catch (JSONException e) {
                        Log.v(Constants.LOG_TAG, "JSONException:" + e.toString());
                    }
//                    Double dist = getDistance(o);
                    try {
                        item.location = o.getString("district");
                    } catch (JSONException e) {
                        item.location = "";
                        e.printStackTrace();
                    }
                    try {
                        item.distance = StringUtils.getDistance(o.getDouble("dis")) + "km";
                    } catch (Exception e) {
                        item.distance = "";
                        e.printStackTrace();
                    }
//                    if (dist >= 0) {
//                        item.distance = dist + "km";
//                    } else {
//                        item.distance = "";
//                    }
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

                    try {
                        item.collection = o.getString("fav");
                    } catch (JSONException e) {
                        Log.v(Constants.LOG_TAG, e.toString());
                        e.printStackTrace();
                    }
                    try {
                        item.isFav = o.getBoolean("isFav");
                    } catch (JSONException js) {
                        item.isFav = false;
                        js.printStackTrace();
                    }
                    item.type = poiType;
                    items.add(item);
                }
            } else {
                for (int i = 0; i < lists.length(); i++) {
                    ListViewDataModel item = new ListViewDataModel();
                    JSONObject o = (JSONObject) lists.get(i);
                    item.title = o.getString("name");
                    item.id = o.getString("id");
                    item.description = o.getString("lead");
                    String headImg = o.getString("headImg");
                    if (headImg.startsWith("u_")) {
                        headImg = headImg.substring(2);
                    }
                    item.imgUrl = headImg;
                    Double dist = -1.0;
                    items.add(item);
                }
            }
            mListView.onFinishLoading(true, items);
        } catch (JSONException e) {
            Log.v(Constants.LOG_TAG, " JSONException " + e.toString());
            e.printStackTrace();
        }
    }
}
