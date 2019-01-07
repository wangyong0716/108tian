package com.ksider.mobile.android.activity.fragment.buy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.personal.ConsumeCodeListActivity;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.DateUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.StringUtils;
import com.ksider.mobile.android.utils.net.toolbox.FormJsonObjectRequest;
import com.umeng.analytics.MobclickAgent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by wenkui on 5/11/15.
 */
public class OrderDetailFragment extends Fragment {
    private View view;
    protected LayoutInflater mInflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView more = (TextView) getActivity().findViewById(R.id.more_choice);
        if (more != null) {
            more.setVisibility(View.INVISIBLE);
        }
        view = inflater.inflate(R.layout.fragment_buy_order_detail, container, false);
        view.findViewById(R.id.price).setVisibility(View.GONE);
        TextView submit = (TextView) view.findViewById(R.id.submit);
        submit.setText("继续购物");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        view.findViewById(R.id.check_coupon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ConsumeCodeListActivity.class);
                startActivity(intent);
            }
        });
        view.findViewById(R.id.purchase_bottom_bar).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.scrollView).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.loading_codes_anim).setVisibility(View.VISIBLE);
        Network.getInstance().addToRequestQueue(getRequest());
        MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_PURCHASE_CONFIRM);
        return view;
    }

    protected void proccess(JSONObject data) {
        view.findViewById(R.id.purchase_bottom_bar).setVisibility(View.VISIBLE);
        view.findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
        view.findViewById(R.id.loading_codes_anim).setVisibility(View.INVISIBLE);
        if (data != null) {
            try {
                JSONObject order = data.getJSONObject("order");
                TextView textView = (TextView) getView().findViewById(R.id.product_name);
                textView.setText(order.getString("productName"));
                long consumeTime = order.getLong("consumeTime");
                if (consumeTime > 0) {
                    textView = (TextView) getView().findViewById(R.id.use_date);
                    textView.setText(DateUtils.getFormatDateWithWeek(consumeTime));
                } else {
                    getView().findViewById(R.id.date_clickarea).setVisibility(View.GONE);
                }

                textView = (TextView) getView().findViewById(R.id.quantity);
                textView.setText(String.valueOf(order.getLong("quantity")));

                textView = (TextView) getView().findViewById(R.id.total);
                textView.setText(StringUtils.getPrice(order.getDouble("totalFee")) + "元");
                String serialNumber = null;
                if (order.getInt("status") == 2) {//付款成功
                    try {
                        JSONArray codes = data.getJSONArray("codes");
                        ViewGroup codeContainer = (ViewGroup) getView().findViewById(R.id.codeList);
                        for (int i = 0; i < codes.length(); i++) {
                            View item = getActivity().getLayoutInflater().inflate(R.layout.consume_code_item_number, null, false);
                            JSONObject code = codes.getJSONObject(i);

                            if (serialNumber == null) {
                                serialNumber = code.getString("serialNumber");
                            }
                            TextView text = (TextView) item.findViewById(R.id.code);
                            text.setText(StringUtils.consumeCodeFormat(code.getString("code")));
                            codeContainer.addView(item);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    getView().findViewById(R.id.tips_proccess).setVisibility(View.VISIBLE);
                }
                if (serialNumber != null) {
                    ((TextView) getView().findViewById(R.id.serial_number)).setText(StringUtils.serialNumberFormat(serialNumber));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (view.findViewById(R.id.purchase_bottom_bar) != null) {
            view.findViewById(R.id.purchase_bottom_bar).setBackgroundColor(getResources().getColor(R.color.divider_line_color));
        }
    }

    public JsonObjectRequest getRequest() {

        FormJsonObjectRequest request = new FormJsonObjectRequest(APIUtils.getUrl(APIUtils.SYNC_NOTIFY),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getInt("status") == 0) {
                                proccess(response.getJSONObject("data"));
                            } else {
                                Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), "服务器异常！", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "服务器异常！", Toast.LENGTH_LONG).show();
                Log.v(Constants.LOG_TAG, error.toString());
            }
        });
        request.setShouldCache(false);
        switch (getArguments().getInt("payment")) {
            case OrderCheckedFragment.PAYMENT_MODEL_WECHAT:
                request.addFormParams("platform", "weixin");
                request.addFormParams("out_trade_no", getArguments().getString("out_trade_no"));
                request.addFormParams("transaction_id", "unkown");
                break;
            case OrderCheckedFragment.PAYMENT_MODEL_ALIPAY:
                request.addFormParams("content", getArguments().getString("result"));
                request.addFormParams("platform", "ali");
                break;
        }
        return request;
    }
}
