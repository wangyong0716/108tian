package com.ksider.mobile.android.WebView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.activity.fragment.MePagingAdaptor;
import com.ksider.mobile.android.model.BaseDataModel;
import com.ksider.mobile.android.model.ListViewDataModel;
import com.ksider.mobile.android.scrollListView.OverScrollPagingListView;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.DateUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResultActivity extends BaseActivity {
    protected int mPage = -1;
    protected OverScrollPagingListView mListView;
    protected MePagingAdaptor mAdaptor;
    protected int mMaxPageSize = -1;
    protected String mKeyWord;
    private int cityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        new SlidingLayout(this);
        mKeyWord = getIntent().getStringExtra("keyword");
        cityId = getIntent().getIntExtra("cityId", 1);
        customActionBar(mKeyWord);
        mListView = (OverScrollPagingListView) findViewById(R.id.result);
        mAdaptor = new MePagingAdaptor(this);
        mListView.setAdapter(mAdaptor);
        mListView.setPagingableListener(new OverScrollPagingListView.Pagingable() {
            @Override
            public void onLoadMoreItems() {
                mPage++;
                if (mPage * Constants.PAGING_STEP < mMaxPageSize) {
                    Network.getInstance().addToRequestQueue(getRequest());
                } else {
                    mListView.onFinishLoading(false, null);
                }
            }
        });
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parents, View view, int position, long id) {
                if (position < mListView.getHeaderViewsCount() || position >= mListView.getHeaderViewsCount() + mAdaptor.getCount()) {
                    return;
                }
                BaseDataModel base = mAdaptor.getItem(position - mListView.getHeaderViewsCount());
                if (base != null) {
                    Intent intent = Utils.getLandingActivity(SearchResultActivity.this, base.type);
                    if (intent != null) {
                        Utils.initDetailPageArg(intent, base);
                        startActivity(intent);
                    }
                }
            }
        });
        Network.getInstance().addToRequestQueue(getRequest());
    }

    protected JsonObjectRequest getRequest() {
        Map<String, Object> params = new HashMap<String, Object>();
        try {
            params.put("keyword", URLEncoder.encode(mKeyWord, "UTF-8"));
            params.put("cityId", cityId);
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return null;
        }
        Log.v("AAA", "searchResultUrl=" + APIUtils.getUrl(APIUtils.SEARCH, params));
        return new JsonObjectRequest(APIUtils.getUrl(APIUtils.SEARCH, params), null, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        proccess(response.getJSONObject("data"));
                    }
                } catch (JSONException e) {
                    mListView.onFinishLoading(false, null);
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(Constants.LOG_TAG, "Me response:" + error.toString());
            }
        });
    }

    protected void proccess(JSONObject data) {
        List<ListViewDataModel> items = new ArrayList<ListViewDataModel>();
        try {
            JSONArray entities = data.getJSONArray("list");
            for (int i = 0; i < entities.length(); i++) {
                JSONObject o = entities.getJSONObject(i);
                ListViewDataModel item = new ListViewDataModel();
                item.type = o.getString("type");
                item.id = o.getString("id");
                item.title = o.getString("name");
                try {
                    item.imgUrl = o.getString("headImg");
                } catch (JSONException js) {
                    js.printStackTrace();
                }
                Double dist = Utils.getDistance(o);
                try {
                    item.location = o.getString("district");
                } catch (JSONException e) {
                }
                if (dist > 0) {
                    item.distance = dist + "km";
                } else {
                    item.distance = "";
                }
                try {
                    item.price = o.getString("price");
                } catch (JSONException e) {
                    item.price = "";
                    e.printStackTrace();
                }
                try {
                    JSONObject recommend = o.getJSONObject("recommend");
                    try {
                        item.collection = recommend.getString("fav");
                    } catch (JSONException js) {
                        js.printStackTrace();
                        item.collection = "";
                    }
                    try {
                        item.startDate = DateUtils.getRecentDate(recommend.getLong("startTime"));
                    } catch (JSONException js) {
                        js.printStackTrace();
                        item.startDate = "";
                    }
                } catch (JSONException js) {
                    js.printStackTrace();
                }
                items.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mListView.onFinishLoading(items.size() > 0, items);
    }
}
