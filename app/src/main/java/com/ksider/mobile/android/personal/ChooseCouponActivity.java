package com.ksider.mobile.android.personal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.ksider.mobile.android.WebView.BaseActivity;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.WebView.WebViewLandingActivity;
import com.ksider.mobile.android.model.CouponModel;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by yong on 2015/6/1.
 */
public class ChooseCouponActivity extends BaseActivity {
    private long productId = 0;
    private int poiType = 0;
    private int quantity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_coupon);
        new SlidingLayout(this);
        customActionBar("查看优惠券");

        Intent intent = getIntent();
        productId = intent.getLongExtra("productId", 0);
        poiType = intent.getIntExtra("poiType", 0);
        quantity = intent.getIntExtra("quantity", 0);

        findViewById(R.id.coupon_use_rule).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent useRule = new Intent(ChooseCouponActivity.this, WebViewLandingActivity.class);
                useRule.putExtra("url", Constants.COUPON_USE_RULE);
                useRule.putExtra("share", false);
                startActivity(useRule);
            }
        });
    }

    /**
     * show the loading view before getting response
     */
    public void initLoadingView() {
        findViewById(R.id.empty_list_item).setVisibility(View.GONE);
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
        setResponseView();
        findViewById(R.id.empty_list_item).setVisibility(View.VISIBLE);
    }

    /**
     * show error message if the connection fails
     */
    public void setErrorView() {
        setEmptyView();
        ((TextView) findViewById(R.id.no_coupon_info)).setText(R.string.net_acc_failed);
    }

    @Override
    public void onResume() {
        initLoadingView();
        super.onResume();
        refresh();
    }

    protected String getRequestUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "list");
        params.put("type", "valid");
        params.put("productId", productId);
        params.put("poiType", poiType);
        params.put("quantity", quantity);
        return APIUtils.getUrl(APIUtils.COUPON, params);
    }

    protected void refresh() {
        JsonRequest jsonRequest = getRequest();
        jsonRequest.setShouldCache(false);
        Network.getInstance().addToRequestQueue(jsonRequest);
    }

    protected void process(JSONArray array) {
        if (array.length() == 0) {
            setEmptyView();
            return;
        }
        LinearLayout couponListLayout = (LinearLayout) findViewById(R.id.coupon_list_layout);
        couponListLayout.removeAllViews();
        List<CouponModel> items = new ArrayList<CouponModel>();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                addItem(couponListLayout, o);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected JsonObjectRequest getRequest() {
        Log.v("AAA", "ChooseCouponActivity->" + getRequestUrl());
        return new JsonObjectRequest(getRequestUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        setResponseView();
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
    }

    public void addItem(LinearLayout couponList, JSONObject o) {
        double worth = 0;
        long validTime = 0;
        String couponName = "";
        int status = 3;
        double feeConstraint = 0;
        try {
            couponName = o.getString("couponName");
            worth = o.getDouble("worth");
            validTime = o.getLong("validTime");
            status = o.getInt("status");
        } catch (JSONException js) {
            js.printStackTrace();
        }
        try {
            feeConstraint = o.getDouble("feeConstraint");
        } catch (JSONException js) {
            feeConstraint = 0;
            js.printStackTrace();
        }

        View view;
        if (status == Status.COUPON_NORMAL) {
            view = getLayoutInflater().inflate(R.layout.list_view_coupon_item, null);
            ((TextView) view.findViewById(R.id.use_state)).setText("未使用");
        } else if (status == Status.COUPON_CONSUMED) {
            view = getLayoutInflater().inflate(R.layout.list_view_coupon_consumed_item, null);
            ((TextView) view.findViewById(R.id.use_state)).setText("已使用");
        } else if (status == Status.COUPON_CANCEL) {
            view = getLayoutInflater().inflate(R.layout.list_view_coupon_consumed_item, null);
            ((TextView) view.findViewById(R.id.use_state)).setText("已注销");
        } else {
            return;
        }
        if (feeConstraint <= 0) {
            ((TextView) view.findViewById(R.id.coupon_name)).setText("无限制");
        } else {
            ((TextView) view.findViewById(R.id.coupon_name)).setText(getResources().getString(R.string.coupon_des, Math.round(feeConstraint)));
        }
        int intMoney = (int) Math.floor(worth);
        double floatMoney = worth - intMoney;
        if (floatMoney < 0.01) {
            ((TextView) view.findViewById(R.id.coupon_int_money)).setText(intMoney + "");
            ((TextView) view.findViewById(R.id.coupon_float_money)).setText("");
        } else {
            ((TextView) view.findViewById(R.id.coupon_int_money)).setText(intMoney + ".");
            DecimalFormat df = new DecimalFormat("#0.00");
            ((TextView) view.findViewById(R.id.coupon_float_money)).setText(df.format(floatMoney).substring(2) + "");
        }
//        ((TextView) view.findViewById(R.id.coupon_name)).setText(couponName);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        ((TextView) view.findViewById(R.id.coupon_vaild_time)).setText("截止日期：" + sdf.format(new Date(validTime)));
        if (status == Status.COUPON_NORMAL) {
            LinearLayout couponBackground = (LinearLayout) view.findViewById(R.id.coupon_background);
            couponBackground.setTag(o);
            couponBackground.setOnClickListener(listener);
        }
        couponList.addView(view);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtra("coupon_info", v.getTag().toString());
            ChooseCouponActivity.this.setResult(RESULT_OK, intent);
            ChooseCouponActivity.this.finish();
        }
    };
}
