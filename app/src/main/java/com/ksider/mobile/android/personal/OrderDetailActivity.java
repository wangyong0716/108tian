package com.ksider.mobile.android.personal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.BaseActivity;
import com.ksider.mobile.android.WebView.PurchaseAcitvity;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.WebView.RefundActivity;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.view.LoadImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yong on 2015/5/23.
 */
public class OrderDetailActivity extends BaseActivity {
    private final int TOPAY = 0;
    private final int TOREBUY = 1;
    private long serialNumber = -1;
    private int poiType = 0;
    private String productName = "";
    private double totalFee = 0;
    private double originTotalFee = 0;
    private String poiId = "";
    private double couponDiscount = 0;
    private String coupons = "";
    protected JSONObject order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consume_code_detail);
        new SlidingLayout(this);
        customActionBar();
        TextView title = (TextView) findViewById(R.id.list_title);
        title.setText("订单详情");
        TextView refund = (TextView) findViewById(R.id.more_choice);
        refund.setText(getResources().getString(R.string.apply_refund));
        refund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderDetailActivity.this, RefundActivity.class);
                intent.putExtra("serialNumber", serialNumber);
                startActivity(intent);
            }
        });
        serialNumber = getIntent().getLongExtra("serialNumber", -1);
        findViewById(R.id.consume_code_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toProduct(poiType, poiId);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        findViewById(R.id.pay_or_rebuy).setVisibility(View.GONE);
        refresh();
    }

    public void fillData(int sellCount, long consumeTime) {
        if (productName != null) {
            ((TextView) findViewById(R.id.consumeTitle)).setText(productName);
            ((TextView) findViewById(R.id.productName)).setText(productName);
        }
        if (consumeTime != 0) {
            ((TextView) findViewById(R.id.consume_date)).setText(getDateByLongTime(consumeTime));
            ((TextView) findViewById(R.id.consume_time)).setText(getTimeByLongTime(consumeTime));
        }
        ((TextView) findViewById(R.id.sellCount)).setText(sellCount + "");
        totalFee = totalFee > 0 ? totalFee : 0;
        ((TextView) findViewById(R.id.total_fee)).setText(StringUtils.getPrice(originTotalFee) + "元");
        ((TextView) findViewById(R.id.coupon_worth)).setText(StringUtils.getPrice(couponDiscount) + "元");
        ((TextView) findViewById(R.id.to_pay)).setText(StringUtils.getPrice(totalFee) + "元");
        ((TextView) findViewById(R.id.serial_number)).setText(StringUtils.serialNumberFormat(serialNumber + ""));
    }

    public String getDateByLongTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        return sdf.format(new Date(time));
    }

    public String getTimeByLongTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date(time));
    }

    public String formatMoney(double money) {
        return String.format("%.2f", money) + "元";
    }

    protected String getRequestUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "orderDetail");
        params.put("serialNumber", serialNumber);
        return APIUtils.getUrl(APIUtils.ORDER, params);
    }

    protected void refresh() {
        Network.getInstance().addToRequestQueue(getRequest());
    }

    protected JsonObjectRequest getRequest() {
        Log.v("AAA", "url=" + getRequestUrl());
        return new JsonObjectRequest(getRequestUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        process(response.getJSONObject("data"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }

    protected void process(JSONObject jsonObject) throws JSONException {
        JSONObject product = jsonObject.getJSONObject("product");
        productName = product.getString("productName");
        poiType = product.getInt("poiType");
        poiId = product.getString("poiId");
        String productImg = "";
        double price = 0;
        int refund = 0;
        try {
            productImg = product.getString("productImg");
        } catch (JSONException js) {
            js.printStackTrace();
        }
        try {
            price = product.getDouble("sellPrice");
            refund = product.getInt("refund");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (productImg != "" && !productImg.equals("")) {
            ((LoadImageView) findViewById(R.id.listview_headImage)).setImageResource(productImg);
        }
        ((TextView) findViewById(R.id.price)).setText(StringUtils.getPrice(price));
        if (refund == 1) {
            ((TextView) findViewById(R.id.refundAble)).setText(R.string.refundable);
            ((ImageView) findViewById(R.id.refund_icon)).setImageResource(R.drawable.refund_icon);
        } else {
            ((TextView) findViewById(R.id.refundAble)).setText(R.string.unrefundable);
            ((ImageView) findViewById(R.id.refund_icon)).setImageResource(R.drawable.unrefund_icon);
        }

        order = jsonObject.getJSONObject("order");
        order.put("refund", product.getInt("refund"));
        order.put("sellPrice", product.getDouble("sellPrice"));
        totalFee = order.getDouble("totalFee");
        try {
            int quantity = order.getInt("quantity");
            originTotalFee = quantity * price;
            coupons = order.getString("coupons");
            couponDiscount = order.getDouble("couponDiscount");
        } catch (JSONException js) {
            js.printStackTrace();
        }

        int sellCount = order.getInt("quantity");
        long consumeTime = 0;
        try {
            consumeTime = order.getLong("consumeTime");
        } catch (JSONException js) {
            js.printStackTrace();
        }
        fillData(sellCount, consumeTime);

        JSONArray codes = jsonObject.getJSONArray("codes");
        int refundedNum = 0;
        int unConsumeNum = 0;
        LinearLayout codeList = (LinearLayout) findViewById(R.id.code_list);
        codeList.removeAllViews();
        if (codes.length() > 0) {
            findViewById(R.id.code_list_layout).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.code_list_layout).setVisibility(View.GONE);
        }
        for (int i = 0; i < codes.length(); i++) {
            JSONObject code = codes.getJSONObject(i);
            addItem(codeList, code);
            if (code.getInt("status") == 6) {
                refundedNum++;
            }
            if (code.getInt("status") == 1) {
                unConsumeNum++;
            }
        }
        if (unConsumeNum == 0 || refund != 1) {
            findViewById(R.id.more_choice).setVisibility(View.INVISIBLE);
        }
        if (codes.length() == 0) {
            addPayButton(TOPAY);
            return;
        } else if (refundedNum == codes.length()) {
            addPayButton(TOREBUY);
            return;
        }
    }

    public void addPayButton(final int type) {
//        LinearLayout view = (LinearLayout) findViewById(R.id.get_coupon_code_button);
//        view.setVisibility(View.VISIBLE);
        TextView view = (TextView) findViewById(R.id.pay_or_rebuy);
        view.setVisibility(View.VISIBLE);
        if (type == TOPAY) {
            view.setText("付款");
        } else if (type == TOREBUY) {
            view.setText("重新购买");
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == TOPAY) {
                    if (order != null) {
                        Intent intent = new Intent(OrderDetailActivity.this, PurchaseAcitvity.class);
                        intent.putExtra("order", order.toString());
                        intent.putExtra("payment", true);
                        startActivity(intent);
                    }
                } else if (type == TOREBUY) {
                    toProduct(poiType, poiId);
                }
            }
        });
    }

    public void addItem(LinearLayout codeList, JSONObject code) {
        View view = getLayoutInflater().inflate(R.layout.consume_code_item_new, null);
        try {
            ((TextView) view.findViewById(R.id.code)).setText(StringUtils.consumeCodeFormat(code.getString("code")));
            getConsumeState((TextView) view.findViewById(R.id.code_status), code.getInt("status"));
        } catch (JSONException js) {
            js.printStackTrace();
        }
        codeList.addView(view);
    }

    public void getConsumeState(TextView tv, int status) {
        switch ((byte) status) {
            case Status.CODE_UNCONSUMED:
            case Status.CODE_REFOUND_REJECTED:
                tv.setText("未消费");
                tv.setTextColor(getResources().getColor(R.color.toolbar_text_color));
                break;
            case Status.CODE_CONSUMED:
            case Status.CODE_SETTLED:
                tv.setText("已消费");
//                tv.setTextColor(getResources().getColor(R.color.black));
                break;
            case Status.CODE_REFOUND_REQUIRED:
            case Status.CODE_REFOUND_APPROVED:
                tv.setText("退款中");
//                tv.setTextColor(getResources().getColor(R.color.black));
                break;
            case Status.CODE_REFOUND_DONE:
                tv.setText("已退款");
//                tv.setTextColor(getResources().getColor(R.color.black));
                break;
            default:
                tv.setText("使用时出示消费码");
//                tv.setTextColor(getResources().getColor(R.color.black));
                break;
        }
    }

    public void toProduct(int pType, String pId) {
        String type = "";
        switch (pType) {
            case 1:
                type = "scene";
                break;
            case 2:
                type = "farm";
                break;
            case 3:
                type = "resort";
                break;
            case 4:
                type = "pick";
                break;
            case 5:
                type = "event";
                break;
            default:
                return;
        }
        Intent intent = Utils.getLandingActivity(OrderDetailActivity.this, type);
        if (intent != null) {
            intent.putExtra("BaseData_id", pId);
            startActivity(intent);
        }
    }
}
