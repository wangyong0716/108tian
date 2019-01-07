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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.OrderModel;
import com.ksider.mobile.android.personal.OrderDetailActivity;
import com.ksider.mobile.android.personal.OrderListAdaptor;
import com.ksider.mobile.android.scrollListView.OverScrollPullToRefreshListView;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.view.paging.EmptyView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderListViewPagerFragment extends Fragment {
    protected int mPage = 0;
    protected int mMaxPageSize = 0;
    protected View mRoot;
    protected OverScrollPullToRefreshListView mPagingListView;
    protected OrderListAdaptor mPagingAdaptor;

    private String type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString("type");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
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
            mRoot = inflater.inflate(R.layout.fragment_order_paging_listview, container, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        mPagingAdaptor = new OrderListAdaptor(getActivity());
        mPagingAdaptor.setType(type);
        mPagingListView = (OverScrollPullToRefreshListView) mRoot.findViewById(R.id.content_list);
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
        if (getParentFragment() instanceof OnScrollChanged) {
            mPagingListView.setOnScrollChanged((OnScrollChanged) getParentFragment());
        }
        mPagingListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                OrderModel base = mPagingAdaptor.getItem(position);
                if (base != null && base.getSerialNumber() != 0) {
                    //if the order has not been payed, consumer must pay first before getting the detail info of order
                    Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
                    intent.putExtra("serialNumber", base.getSerialNumber());
                    startActivity(intent);
                } else {
                    Log.v("AAA", "无效点击！");
                }
            }
        });
//        refresh();
        return mRoot;
    }

    protected String getRequestUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "list");
        params.put("step", Constants.PAGING_STEP);
        params.put("page", mPage);
        params.put("type", type);
        return APIUtils.getUrl(APIUtils.ORDER, params);
    }

    protected void refresh() {
        initEmptyView();
        if (mPagingListView != null && mPagingListView.getAdapter() != null && mPage >= 0) {
            mPage = 0;
            mMaxPageSize = 0;
        }
        mPagingAdaptor.removeAllItems();
        Network.getInstance().addToRequestQueue(getRequest());
    }

    protected void process(JSONArray array) {
        if (array.length() == 0) {
            setEmptyView();
            return;
        }
        mPagingListView.onRefreshComplete();
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
                try {
                    item.setProductImg(o.getString("productImg"));
                } catch (JSONException js) {
                    js.printStackTrace();
                }
                try {
                    item.setQuantity(o.getLong("quantity"));
                } catch (JSONException js) {
                    js.printStackTrace();
                }
                try {
                    item.setConsumeTime(o.getLong("consumeTime"));
                } catch (JSONException js) {
                    js.printStackTrace();
                }
                try {
                    item.setProductId(o.getLong("productId"));
                } catch (JSONException js) {
                    js.printStackTrace();
                }
                try {
                    item.setCouponDiscount(o.getDouble("couponDiscount"));
                    item.setCoupons(o.getString("coupons"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    item.setSellPrice(o.getDouble("sellPrice"));
                } catch (JSONException js) {
                    item.setSellPrice(0);
                    js.printStackTrace();
                }
                try {
                    item.setRefund(o.getInt("refund"));
                } catch (JSONException js) {
                    item.setRefund(0);
                    js.printStackTrace();
                }
                try {
                    item.setEvaluate(o.getInt("evaluate"));
                } catch (JSONException js) {
                    item.setEvaluate(0);
                    js.printStackTrace();
                }
                items.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mPagingListView.onFinishLoading(items.size() == Constants.PAGING_STEP, items);
    }

    protected JsonObjectRequest getRequest() {
        Log.v("AAA", "orderType=" + type + "->url=" + getRequestUrl());
        JsonObjectRequest request = new JsonObjectRequest(getRequestUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        mMaxPageSize = response.getJSONObject("data").getInt("number");
                        process(response.getJSONObject("data").getJSONArray("list"));
                    } else {
                        setEmptyView();
                    }
                } catch (JSONException e) {
                    setEmptyView();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setEmptyView();
                Log.v(Constants.LOG_TAG, "response:" + error.toString());
            }
        });
        request.setShouldCache(false);
        return request;
    }

    public void initEmptyView() {
        mRoot.findViewById(R.id.list_empty_view).setVisibility(View.GONE);
    }

    public void setEmptyView() {
        mPagingListView.setHasMoreItems(false);
        mPagingListView.onRefreshComplete();
        EmptyView emptyView = (EmptyView) mRoot.findViewById(R.id.list_empty_view);
        emptyView.setVisibility(View.VISIBLE);
    }
}
