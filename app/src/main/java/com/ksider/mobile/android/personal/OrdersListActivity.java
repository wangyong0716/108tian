package com.ksider.mobile.android.personal;

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
import com.ksider.mobile.android.WebView.BaseActivity;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.activity.fragment.OnScrollChanged;
import com.ksider.mobile.android.model.OrderModel;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.view.paging.PagingListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yong on 2015/5/22.
 */
public class OrdersListActivity extends BaseActivity {
    protected PagingListView mListView;
    protected OrderListAdaptor mAdaptor;
    protected int mPage = -1;
    protected int mMaxPageSize = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_orders);
        customActionBar("我的订单");

        mListView = (PagingListView) findViewById(R.id.content_list);
        mAdaptor = new OrderListAdaptor(this);
        mListView.setAdapter(mAdaptor);
        mListView.setPagingableListener(new PagingListView.Pagingable() {
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

        mListView.setOnScrollChanged(new OnScrollChanged() {
            @Override
            public void onScrollChanged(int x, int y, int oldX, int oldY) {
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parents, View view, int position, long id) {
                OrderModel base = mAdaptor.getItem(position);
                if (base != null && base.getSerialNumber() != 0) {
                    //if the order has not been payed, consumer must pay first before getting the detail info of order
                    Intent intent = new Intent(OrdersListActivity.this, OrderDetailActivity.class);
                    intent.putExtra("serialNumber", base.getSerialNumber());
                    startActivity(intent);
                } else {
                    Log.v("AAA", "无效点击！");
                }
            }
        });
        initLoadingView();
    }

    /**
     * show the loading view before getting response
     */
    public void initLoadingView() {
        mListView.deleteFooterView();
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
        mListView.setVisibility(View.VISIBLE);
        findViewById(R.id.empty_list_item).setVisibility(View.GONE);
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
        setResponseView();
        mListView.setVisibility(View.GONE);
        findViewById(R.id.empty_list_item).setVisibility(View.VISIBLE);
    }

    /**
     * show error message if the connection fails
     */
    public void setErrorView() {
        setEmptyView();
        ((TextView) findViewById(R.id.no_order_info1)).setText(R.string.net_acc_failed);
        findViewById(R.id.no_order_info2).setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        initLoadingView();
        refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPage = -1;
    }

    protected String getRequestUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "list");
        params.put("step", Constants.PAGING_STEP);
        params.put("page", mPage);
        return APIUtils.getUrl(APIUtils.ORDER, params);
    }

    protected void refresh() {
        mPage = 0;
        mAdaptor.removeAllItems();
        Network.getInstance().addToRequestQueue(getRequest());
    }

    protected void process(JSONArray array) {
        if (array.length() == 0) {
            setEmptyView();
            return;
        }
        List<OrderModel> items = new ArrayList<OrderModel>();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                if (o.getInt("status") == 7 || o.getInt("status") == 8) {
                    break;
                }
                OrderModel item = new OrderModel();
                try {
                    item.setProductName(o.getString("productName"));
                    item.setStatus(o.getInt("status"));
                    item.setSerialNumber(o.getLong("serialNumber"));
                    item.setTotalFee(o.getDouble("totalFee"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try{
                    item.setProductImg(o.getString("productImg"));
                }catch (JSONException js){
                    js.printStackTrace();
                }
                try{
                    item.setQuantity(o.getLong("quantity"));
                }catch (JSONException js){
                    js.printStackTrace();
                }
                try{
                    item.setConsumeTime(o.getLong("consumeTime"));
                }catch(JSONException js){
                    js.printStackTrace();
                }
                try{
                    item.setProductId(o.getLong("productId"));
                }catch(JSONException js){
                    js.printStackTrace();
                }
                try {
                    item.setCouponDiscount(o.getDouble("couponDiscount"));
                    item.setCoupons(o.getString("coupons"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                items.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mListView.onFinishLoading(items.size() == Constants.PAGING_STEP, items);
    }

    protected JsonObjectRequest getRequest() {
        JsonObjectRequest request = new JsonObjectRequest(getRequestUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        setResponseView();
                        mMaxPageSize = response.getJSONObject("data").getInt("number");
                        process(response.getJSONObject("data").getJSONArray("list"));
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
            }
        });
        request.setShouldCache(false);
        return request;
    }

}

