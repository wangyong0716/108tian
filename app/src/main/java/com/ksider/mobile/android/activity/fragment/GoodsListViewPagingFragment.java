package com.ksider.mobile.android.activity.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.comm.ShareDataPool;
import com.ksider.mobile.android.model.BaseDataModel;
import com.ksider.mobile.android.model.ListViewDataModel;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.scrollListView.OverScrollPullToRefreshListView;
import com.ksider.mobile.android.utils.*;
import de.greenrobot.event.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoodsListViewPagingFragment extends Fragment {
    protected BasicCategory mBasicCategory;
    protected TabIndicator mTabIndicator;
    protected OverScrollPullToRefreshListView mPagingListView;
    protected GoodsListViewPagingAdaptor mPagingAdaptor;
    protected String TAG = "GoodsListViewPaging";
    protected View mRoot;
    protected int mPage = -1; // 页数
    protected int mCityId = 1; // 城市id
    protected int mTheme = -1; // 游玩主题
    protected int mRegionId = -1; // 地区id
    protected int mGroupId = -1; // 人群id
    protected int mSpecies = -1;
    protected double mMinDistance = 0;
    protected int mMaxPageSize = -1; // 最大页数
    protected Boolean LocationReady = false;
    protected String mWeekPeriod = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBasicCategory = (BasicCategory) getArguments().getSerializable("category");
            mTabIndicator = (TabIndicator) getArguments().getSerializable("indicator");
            mCityId = Storage.sharedPref.getInt("cityId", 1);
            mTheme = getArguments().getInt("theme", -1);
            mRegionId = getArguments().getInt("regionId", -1);
            mGroupId = getArguments().getInt("groupId", -1);
        }
        mBasicCategory = mBasicCategory == null ? BasicCategory.UNKOWN : mBasicCategory;
        mTabIndicator = mTabIndicator == null ? TabIndicator.UNKOWN : mTabIndicator;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        StatHandle.postImpclkEvent(getActivity(), StatHandle.EVENT_LIST);
        super.onDestroy();
    }

    public void onEventMainThread(MessageEvent event) {
        if (event.getType() == MessageEvent.UPDATE_DEFAULTCITY) {
            mCityId = Storage.sharedPref.getInt("cityId", 1);
            clearData();
            mPage = 0;
            Network.getInstance().addToRequestQueue(getRequest(), TAG);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPage < 0) {
            if (!LocationReady && mTabIndicator == TabIndicator.NEARBY) {
                String coord = Storage.sharedPref.getString("position", null);
                if (coord == null) {
                    return;
                }
            }
            mPage = 0;
            Network.getInstance().addToRequestQueue(getRequest(), TAG);
        }
    }

    private void clearData() {
        if (mPagingListView != null && mPagingListView.getAdapter() != null && mPage >= 0) {
            mPage = -1;
            mMaxPageSize = -1;
            mMinDistance = 0;
            mPagingAdaptor.removeAllItems();
            mPagingListView.setHasMoreItems(false);
        }
    }

    public void refrash(TabIndicator indicator) {
        if (indicator == null)
            return;
        mTabIndicator = indicator;
        if (mTabIndicator == TabIndicator.THISWEEK) {
            mWeekPeriod = "this";
        } else if (mTabIndicator == TabIndicator.NEXTWEEK) {
            mWeekPeriod = "next";
        } else {
            mWeekPeriod = null;
        }
        clearData();
        mPage = 0;
        Network.getInstance().addToRequestQueue(getRequest());
    }

    public void refrash(Integer theme) {
        if (theme == null)
            return;
        if (mBasicCategory == BasicCategory.PICKINGPART) {
            mSpecies = theme;
            mTheme = -1;
        } else {
            mTheme = theme;
        }
        clearData();
        mPage = 0;
        Network.getInstance().addToRequestQueue(getRequest());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        if (mRoot != null) {
            ViewGroup parent = (ViewGroup) mRoot.getParent();
            if (parent != null) {
                parent.removeView(mRoot);
                mPagingListView.onRefreshComplete();
                return mRoot;
            }
        }

        try {
            mRoot = inflater.inflate(R.layout.fragment_googs_paging_listview, container, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        mPagingAdaptor = new GoodsListViewPagingAdaptor(getActivity(), mBasicCategory);
        mPagingListView = (OverScrollPullToRefreshListView) mRoot.findViewById(R.id.content_list);
        mPagingListView.setOnRefreshListener(new OverScrollPullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearData();
                mPage = 0;
                Network.getInstance().addToRequestQueue(getRequest());
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
        if (getParentFragment() instanceof OnScrollChanged) {
            mPagingListView.setOnScrollChanged((OnScrollChanged) getParentFragment());
        }
        mPagingListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                container.performClick();
                if (mPagingAdaptor.getCount() > position) {
                    Intent intent = Utils.getLandingActivity(getActivity(), mBasicCategory);
                    BaseDataModel data = mPagingAdaptor.getItem(position);
                    if (data != null) {
                        Utils.initDetailPageArg(intent, data);
                        StatHandle.increaseClick(StatHandle.EVENT_LIST);
                        startActivity(intent);
                    }
                }
            }
        });
        EventBus.getDefault().registerSticky(this);
        return mRoot;
    }

    protected void proccessGuide(JSONObject data) {
        try {
            mMaxPageSize = data.getInt("number");
            List<ListViewDataModel> items = new ArrayList<ListViewDataModel>();
            JSONArray lists = data.getJSONArray("list");
            for (int i = 0; i < lists.length(); i++) {
                try {
                    ListViewDataModel item = new ListViewDataModel();
                    JSONObject o = (JSONObject) lists.get(i);
                    item.title = o.getString("name");
                    item.id = o.getString("id");
                    item.description = o.getString("lead");
                    String headImg = o.getString("headImg");
                    item.imgUrl = headImg;
                    try {
                        item.collection = o.getString("fav");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    items.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mPagingListView.onFinishLoading(items.size() > 0, items);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void proccessActivity(JSONObject data) {
        try {
            mMaxPageSize = data.getInt("number");
            List<ListViewDataModel> items = new ArrayList<ListViewDataModel>();
            JSONArray lists = data.getJSONArray("list");
            for (int i = 0; i < lists.length(); i++) {
                try {
                    ListViewDataModel item = new ListViewDataModel();
                    JSONObject o = (JSONObject) lists.get(i);
                    item.id = o.getString("id");
                    item.title = o.getString("name");
                    item.imgUrl = o.getString("headImg");
                    Double dist = getDistance(o);
                    if (dist >= 0) {
                        item.location = StringUtils.getDistance(dist) + "km";
                    } else {
                        item.location = "";
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

                    try {
                        item.collection = o.getString("fav");
                    } catch (JSONException e) {
                        item.collection = "";
                        Log.v(Constants.LOG_TAG, e.toString());
                        e.printStackTrace();
                    }
                    try {
                        item.isFav = o.getBoolean("isFav");
                    } catch (JSONException js) {
                        item.isFav = false;
                        js.printStackTrace();
                    }
                    try {
                        item.startDate = DateUtils.getRecentDate(o.getLong("startTime"));
                    } catch (JSONException js) {
                        js.printStackTrace();
                        item.startDate = "";
                    }
                    items.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mPagingListView.onFinishLoading(items.size() > 0, items);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void undateMiniDistance(JSONObject data) {
        if (mTabIndicator == TabIndicator.NEARBY) {
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

    protected void proccess(JSONObject data) {
        if (mPagingListView != null) {
            mPagingListView.onRefreshComplete();
        }
        undateMiniDistance(data);
        if (mBasicCategory == BasicCategory.GUIDE) {
            proccessGuide(data);
            return;
        } else if (mBasicCategory == BasicCategory.ACTIVITY) {
            proccessActivity(data);
            return;
        }
        try {
            mMaxPageSize = data.getInt("number");
            List<ListViewDataModel> items = new ArrayList<ListViewDataModel>();
            JSONArray lists = data.getJSONArray("list");
            for (int i = 0; i < lists.length(); i++) {
                try {
                    ListViewDataModel item = new ListViewDataModel();
                    JSONObject o = (JSONObject) lists.get(i);
                    item.id = o.getString("id");
                    item.title = o.getString("name");
                    item.imgUrl = o.getString("headImg");
                    item.location = o.getString("district");
                    Double dist = getDistance(o);
                    if (dist >= 0) {
                        item.location += "，" + dist + "km";
                    }
                    try {
                        item.price = o.getString("price");
                    } catch (JSONException e) {
                        item.price = "";
                        e.printStackTrace();
                    }
                    try {
                        item.collection = o.getString("fav");
                    } catch (JSONException e) {
                        item.collection = "";
                        Log.v(Constants.LOG_TAG, e.toString());
                        e.printStackTrace();
                    }
                    items.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mPagingListView.onFinishLoading(items.size() > 0, items);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected double getDistance(JSONObject item) {
        Double dist = -1.0;
        try {
            if (mTabIndicator == TabIndicator.NEARBY) {
                dist = item.getDouble("dis");
                dist = Math.round(dist * 100) * 1.0 / 100;
            } else {
                JSONArray lngLatitude = item.getJSONArray("lngLatitude");
                Double lng = lngLatitude.getDouble(0);
                Double lat = lngLatitude.getDouble(1);
                dist = Maths.getSelfDistance(lat, lng);
            }
            // fix 显示
            if (dist == 0.0) {
                dist = Math.random() * 5 / 10 + 0.1;
                dist = Math.round(dist * 100) * 1.0 / 100;
            }
        } catch (JSONException e) {
            dist = -1.0;
        } catch (Exception e) {
            Log.v(Constants.LOG_TAG, e.toString());
        }
        return dist;
    }

    public Map<String, Object> getRequestParams() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("page", mPage);
        mCityId = Storage.sharedPref.getInt("cityId", 1);
        params.put("cityId", mCityId);
        if (mRegionId > 0) {
            params.put("regionId", mRegionId);
        }

        if (mTheme > 0) {
            params.put("theme", mTheme);
        }

        if (mGroupId > 0) {
            params.put("group", mGroupId);
        }

        if (mSpecies > 0) {
            params.put("species", mSpecies);
        }
        if (mWeekPeriod != null) {
            params.put("weekPeriod", mWeekPeriod);
        }
        params.put("step", Constants.PAGING_STEP);
        if (mTabIndicator == TabIndicator.NEARBY) {
            if (ShareDataPool.position != null) {
                params.put("sort", "distance");
                params.put("minDistance", mMinDistance);
                params.put("lnglat", ShareDataPool.position.longitude + "," + ShareDataPool.position.latitude);
            }
        } else if (mTabIndicator == TabIndicator.PRICE_ASC) {
            params.put("sort", "priceAsc");
        } else if (mTabIndicator == TabIndicator.PRICE_DESC) {
            params.put("sort", "priceDesc");
        }
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

    protected JsonObjectRequest getRequest() {
        Log.v("AAA", "getActivityUrl=" + getRequestUrl());
        return new JsonObjectRequest(getRequestUrl(), null, new Listener<JSONObject>() {
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
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mPagingListView.onRefreshComplete();
                mPagingListView.showFailed();
                Log.v(Constants.LOG_TAG, "response:" + error.toString());
            }
        });
    }

}
