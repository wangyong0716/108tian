package com.ksider.mobile.android.personal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.BaseActivity;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.WebView.WebViewLandingActivity;
import com.ksider.mobile.android.activity.fragment.signup.VerifyCodeDialogFragment;
import com.ksider.mobile.android.model.CouponModel;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.Status;
import com.ksider.mobile.android.utils.UserInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by yong on 2015/5/26.
 */
public class CouponActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_coupon);
        new SlidingLayout(this);
        customActionBar("我的优惠券");

        findViewById(R.id.get_coupon_code_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManger = (InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                inputManger.hideSoftInputFromWindow(v.getWindowToken(), 0);
                EditText coupon = (EditText) findViewById(R.id.coupon_id_input);
                String couponPassword = coupon.getText().toString().trim();
                if (couponPassword == null || couponPassword.equals("")) {
                    Toast.makeText(CouponActivity.this, R.string.input_coupon_password, Toast.LENGTH_LONG).show();
                    return;
                }
                if (UserInfo.getPhone() == null || UserInfo.getPhone().equals("")) {
                    showDialog();
                }
                Network.getInstance().addToRequestQueue(fetchCouponRequest(couponPassword));
                coupon.setText(null);
            }
        });

        findViewById(R.id.coupon_use_rule).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManger.hideSoftInputFromWindow(v.getWindowToken(), 0);
                Intent useRule = new Intent(CouponActivity.this, WebViewLandingActivity.class);
                useRule.putExtra("url", Constants.COUPON_USE_RULE);
                useRule.putExtra("share", false);
                startActivity(useRule);
            }
        });

        ((ScrollView) findViewById(R.id.coupon_scroll_view)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager inputManger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManger.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
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

    protected String getListRequestUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "listNew");
//        params.put("type", "unExpired");
        params.put("type", "valid");
        if (getIntent() != null && getIntent().getIntExtra("poiType", 0) != 0) {
            params.put("poiType", getIntent().getIntExtra("poiType", 0));
            params.put("productId", getIntent().getIntExtra("productId", 0));
        }
        return APIUtils.getUrl(APIUtils.COUPON, params);
    }

    protected void refresh() {
        Network.getInstance().addToRequestQueue(getCouponsRequest());
    }

    protected void process(JSONArray array) {
        if (array.length() == 0) {
            setEmptyView();
            return;
        }
        LinearLayout couponListLayout = (LinearLayout) findViewById(R.id.coupon_list_layout);
        couponListLayout.removeAllViews();
//        List<CouponModel> items = new ArrayList<CouponModel>();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                addItem(couponListLayout, o);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected JsonObjectRequest getCouponsRequest() {
        Log.v("AAA", "getListRequestUrl=" + getListRequestUrl());
        JsonObjectRequest request = new JsonObjectRequest(getListRequestUrl(), null, new Response.Listener<JSONObject>() {
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
        request.setShouldCache(false);
        return request;
    }

    public void addItem(LinearLayout couponList, JSONObject o) {
        if (couponList.getChildCount() == 0) {
            findViewById(R.id.empty_list_item).setVisibility(View.GONE);
        }
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
//        ((TextView) view.findViewById(R.id.coupon_name)).setText(couponName);
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        ((TextView) view.findViewById(R.id.coupon_vaild_time)).setText("截止日期：" + sdf.format(new Date(validTime)));
        couponList.addView(view);
    }

    protected String getCouponRequestUrl(String password) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "fetch");
        params.put("password", password);
        return APIUtils.getUrl(APIUtils.COUPON, params);
    }

    protected JsonObjectRequest fetchCouponRequest(String password) {
        JsonObjectRequest request = new JsonObjectRequest(getCouponRequestUrl(password), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        LinearLayout couponListLayout = (LinearLayout) findViewById(R.id.coupon_list_layout);
                        addItem(couponListLayout, response.getJSONObject("data"));
                        Toast.makeText(CouponActivity.this, R.string.fetch_coupon_success_info, Toast.LENGTH_LONG).show();
                    } else {
                        String message = response.getString("msg");
                        if (message == null || message.equals("")) {
                            message = getResources().getString(R.string.fetch_coupon_failed_info);
                        }
                        Toast.makeText(CouponActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(CouponActivity.this, R.string.net_acc_failed, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CouponActivity.this, R.string.server_acc_failed, Toast.LENGTH_LONG).show();
            }
        });
        request.setShouldCache(false);
        return request;
    }

    protected void showDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        VerifyCodeDialogFragment newFragment = new VerifyCodeDialogFragment();
        Bundle args = new Bundle();
        args.putString("action", "bind");
        args.putString("title", getResources().getString(R.string.edit_profile_bing_phone));
        newFragment.setArguments(args);
        newFragment.show(ft, "dialog");
        newFragment.setOnHide(new VerifyCodeDialogFragment.OnDialogHide() {
            @Override
            public void onHide() {

            }
        });
    }
}
