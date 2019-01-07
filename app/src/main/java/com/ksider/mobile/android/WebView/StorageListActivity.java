package com.ksider.mobile.android.WebView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.activity.fragment.OnScrollChanged;
import com.ksider.mobile.android.activity.fragment.PersonalStorageAdaptor;
import com.ksider.mobile.android.model.BaseDataModel;
import com.ksider.mobile.android.model.ListViewDataModel;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.scrollListView.OverScrollPullToRefreshListView;
import com.ksider.mobile.android.swipe.SwipeDismissListViewTouchListener;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.view.paging.PagingListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by yong on 2015/5/9.
 */
public class StorageListActivity extends BaseActivity {
    public static final String PERSONAL_STORAGE_STRATEGY = "weekly";
    public static final String PERSONAL_STORAGE_ACTIVITY = "event";
    public static final String PERSONAL_STORAGE_SCENERY = "scene";
    public static final String PERSONAL_STORAGE_ACCOMMODATION = "farmResort";

    private String choice = "";
    protected OverScrollPullToRefreshListView mListView;
    protected PersonalStorageAdaptor mAdaptor;
    private String info = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_storage);
        TextView text = (TextView) findViewById(R.id.list_title);
        choice = getIntent().getStringExtra("choice");
        if (text != null) {
            if (PERSONAL_STORAGE_STRATEGY.equals(choice)) {
                customActionBar("我的攻略");
                info = "攻略";
            } else if (PERSONAL_STORAGE_ACTIVITY.equals(choice)) {
                customActionBar("我的活动");
                info = "活动";
            } else if (PERSONAL_STORAGE_SCENERY.equals(choice)) {
                customActionBar("我的景点");
                info = "景点";
            } else if (PERSONAL_STORAGE_ACCOMMODATION.equals(choice)) {
                customActionBar("我的住宿");
                info = "住宿";
            }
        }
        ((TextView) findViewById(R.id.no_storage_info1)).setText(getResources().getString(R.string.storage_info_none1, info));
        mListView = (OverScrollPullToRefreshListView) findViewById(R.id.content_list);
        mAdaptor = new PersonalStorageAdaptor(this);
        mListView.setAdapter(mAdaptor);
        mAdaptor.setMListView(mListView);

        mListView.setOnScrollChanged(new OnScrollChanged() {
            @Override
            public void onScrollChanged(int x, int y, int oldX, int oldY) {
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parents, View view, int position, long id) {
                BaseDataModel base = mAdaptor.getItem(position);
                if (base != null) {
                    Intent intent = Utils.getLandingActivity(StorageListActivity.this, base.type);
                    if (intent != null) {
                        Utils.initDetailPageArg(intent, base);
                        startActivity(intent);
                    }
                }
            }
        });

        SwipeDismissListViewTouchListener mTouchListener = new SwipeDismissListViewTouchListener(
                mListView,
                new SwipeDismissListViewTouchListener.DismissCallbacks() {
                    @Override
                    public void currentlyAnimatingSwipeOrScroll(boolean isAnimating) {

                    }

                    @Override
                    public void onDismiss(View viewToRemove, int position) {
                    }

                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }
                });

        mListView.setOnTouchListener(mTouchListener);
        mListView.setOnScrollListener(mTouchListener.makeScrollListener());

        initLoadingView();
        refresh();
    }

    public void refreshList(String type, String id) {
        mAdaptor.notifyDataSetChanged();
        deleteStorage(type, id);
    }

    public void deleteStorage(String type, String id) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "delFav");
        params.put("POIType", type);
        params.put("POIId", id);

        JsonObjectRequest request = new JsonObjectRequest(APIUtils.getUserCenter(params), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getInt("status") == 0) {
                        MessageUtils.eventBus.post(new MessageEvent(MessageEvent.NOTIFY_COLLECTED_CHANGE));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(Constants.LOG_TAG, error.toString());
            }
        });
        Network.getInstance().addToRequestQueue(request);
    }

    /**
     * show the loading view before getting response
     */
    public void initLoadingView() {
        findViewById(R.id.empty_list_item).setVisibility(View.INVISIBLE);
        findViewById(R.id.ptr_id_image).setVisibility(View.GONE);
        findViewById(R.id.video_item_image).setVisibility(View.VISIBLE);
        TextView tv = (TextView) findViewById(R.id.video_item_label);
        tv.setVisibility(View.VISIBLE);
        tv.setText(R.string.loading);
    }

    /**
     * hide the loading view after getting response
     */
    public void setResponseView() {
        LinearLayout baseLinearLayout = (LinearLayout) findViewById(R.id.baseLinearLayout);
        if (baseLinearLayout != null) {
            baseLinearLayout.setVisibility(View.GONE);
        }
    }

    /**
     * show the empty view if the data from the response is empty
     */
    public void setEmptyView() {
        mListView.onFinishLoading(false, null);
        mListView.setVisibility(View.GONE);
        setResponseView();
        findViewById(R.id.empty_list_item).setVisibility(View.VISIBLE);
    }

    /**
     * show error message if the connection fails
     */
    public void setErrorView() {
        setEmptyView();
        findViewById(R.id.empty_list_item).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.no_storage_info1)).setText(R.string.net_acc_failed);
        findViewById(R.id.no_storage_info2).setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MessageUtils.eventBus.unregister(this);
    }

    protected String getRequestUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "getTypeFav");
        params.put("favType", choice);
        return APIUtils.getUserCenter(params);
    }

    protected void refresh() {
        mAdaptor.removeAllItems();
        Network.getInstance().addToRequestQueue(getRequest());
    }

    protected void proccess(JSONArray array) {
        if (array.length() == 0) {
            setEmptyView();
            return;
        }
        List<ListViewDataModel> items = new ArrayList<ListViewDataModel>();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                ListViewDataModel item = new ListViewDataModel();
                item.type = o.getString("type");
                item.id = o.getString("id");
                item.title = o.getString("name");
                try {
                    item.imgUrl = o.getString("headImg");
                } catch (JSONException e) {
                }
                items.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mListView.onFinishLoading(false, items);
    }

    protected JsonObjectRequest getRequest() {
        return new JsonObjectRequest(getRequestUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        setResponseView();
                        proccess(response.getJSONArray("data"));
                    } else {
                        setEmptyView();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setErrorView();
                mListView.onFinishLoading(false, null);
            }
        });
    }
}
