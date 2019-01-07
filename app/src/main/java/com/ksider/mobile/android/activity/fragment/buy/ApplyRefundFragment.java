package com.ksider.mobile.android.activity.fragment.buy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.activity.fragment.FragmentCallback;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.Status;
import com.ksider.mobile.android.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yong on 2015/6/2.
 */
public class ApplyRefundFragment extends Fragment {
    private final int GET_CODES = 0;
    private final int REFUND_CODES = 1;
    private final int REFUND_ORDER = 2;
    protected View mRoot;
    private long serialNumber = -1;
    private double refundMoney = 0;
    private int codeNum = 0;
    private int refundNum = 0;
    private LinearLayout codeList;
    private String refundCodes = "";
    private int refundedNum = 0;
    private double totalFee = 0;
    private double couponDiscount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        codeNum = 0;
        refundMoney = 0;
        mRoot = inflater.inflate(R.layout.fragment_apply_refund, container, false);
        serialNumber = getArguments().getLong("serialNumber");
        ((TextView) mRoot.findViewById(R.id.refund_money)).setText(StringUtils.getPrice(refundMoney) + "元");
        getCodes();
        mRoot.findViewById(R.id.submit).setOnClickListener(submitListener);
        return mRoot;
    }

    private View.OnClickListener submitListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (refundNum < 1) {
                Toast.makeText(getActivity(), "请选择消费码！", Toast.LENGTH_LONG).show();
            } else {
                setSubmitClickable(false);
                refund();
            }
        }
    };

    protected String getRequestUrl(int type) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serialNumber", serialNumber);
        if (type == GET_CODES) {
            params.put("action", "orderDetail");
        } else if (type == REFUND_CODES) {
            params.put("action", "refundCodes");
            params.put("codes", refundCodes);
        } else if (type == REFUND_ORDER) {
            params.put("action", "refundOrder");
        }
        return APIUtils.getUrl(APIUtils.ORDER, params);
    }

    protected void getCodes() {
        Network.getInstance().addToRequestQueue(getRequest(GET_CODES));
    }

    protected void refund() {
        if (codeNum > refundNum) {
            Network.getInstance().addToRequestQueue(getRequest(REFUND_CODES));
        } else if (codeNum == refundNum) {
            Network.getInstance().addToRequestQueue(getRequest(REFUND_ORDER));
        }
    }

    public void setSubmitClickable(boolean clickable) {
        Button submit = (Button) mRoot.findViewById(R.id.submit);
        submit.setClickable(clickable);
//        if (!clickable) {
//            submit.setBackgroundResource(R.drawable.bg_unclickable_green);
//        } else {
//            submit.setBackgroundResource(R.drawable.bg_cornered_green);
//        }
    }

    protected JsonObjectRequest getRequest(final int type) {
        JsonObjectRequest request = new JsonObjectRequest(getRequestUrl(type), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        if (type == GET_CODES) {
                            process(response.getJSONObject("data"));
                            setSubmitClickable(true);
                        } else {
                            FragmentCallback callback = (FragmentCallback) getActivity();
                            callback.next(null);
                        }
                    } else if (type != GET_CODES) {
                        Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_LONG).show();
                        setSubmitClickable(true);
                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), "退款出现异常，请重新操作!", Toast.LENGTH_LONG).show();
                    setSubmitClickable(true);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), R.string.net_acc_failed, Toast.LENGTH_LONG).show();
                setSubmitClickable(true);
            }
        });
        request.setShouldCache(false);
        return request;
    }

    protected void process(JSONObject jsonObject) {
        codeList = (LinearLayout) mRoot.findViewById(R.id.code_list_layout);
        codeList.removeAllViews();
        try {
            JSONObject product = jsonObject.getJSONObject("product");
            String productName = product.getString("productName");
            if (productName != null) {
                ((TextView) mRoot.findViewById(R.id.consumeTitle)).setText(productName);
            }
            JSONObject order = jsonObject.getJSONObject("order");
            totalFee = order.getDouble("totalFee");
            try {
                couponDiscount = order.getDouble("couponDiscount");
            } catch (JSONException js) {
                js.printStackTrace();
            }
        } catch (JSONException js) {
            js.printStackTrace();
        }
        try {
            JSONArray codes = jsonObject.getJSONArray("codes");
            codeNum = codes.length();
            for (int i = 0; i < codes.length(); i++) {
                JSONObject code = codes.getJSONObject(i);
                int status = 0;
                status = code.getInt("status");
                switch (status) {
                    case Status.CODE_UNCONSUMED:
                    case Status.CODE_REFOUND_REJECTED:
                        addItem(codeList, code);
                        break;
                    case Status.CODE_REFOUND_REQUIRED:
                    case Status.CODE_REFOUND_APPROVED:
                    case Status.CODE_REFOUND_DONE:
                        refundedNum++;
                        break;
                    default:
                        break;
                }
            }
        } catch (JSONException js) {
            js.printStackTrace();
        }
        updateRefund(codeList);
    }

    public void addItem(ViewGroup codeList, JSONObject code) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.refund_code_item, null);
        try {
//            DecimalFormat df = new DecimalFormat("#00");
//            ((TextView) view.findViewById(R.id.code_title)).setText("消费码" + df.format(num) + "：");

            final CheckBox cb = (CheckBox) view.findViewById(R.id.refund_check);
            cb.setText(StringUtils.consumeCodeFormat(code.getString("code")));
            cb.setOnCheckedChangeListener(checkBoxListener);
            cb.setTag(code);

//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    cb.performClick();
//                }
//            });
        } catch (JSONException js) {
            js.printStackTrace();
        }
        codeList.addView(view);
    }

    private CompoundButton.OnCheckedChangeListener checkBoxListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            updateRefund(codeList);
        }
    };

    /**
     * update refund info after every change of checkBoxs select
     *
     * @param list
     * @throws JSONException
     */
    public void updateRefund(ViewGroup list) {
        refundMoney = 0;
        refundCodes = "";
        refundNum = 0;
        for (int i = 0; i < list.getChildCount(); i++) {
            View view = list.getChildAt(i);
            CheckBox cb = (CheckBox) view.findViewById(R.id.refund_check);
            JSONObject code = (JSONObject) cb.getTag();
            if (cb.isChecked()) {
                refundNum++;
                try {
                    refundMoney += code.getDouble("price");
                    refundCodes += (code.getString("code") + ",");
                } catch (JSONException js) {
                    js.printStackTrace();
                }

            }
        }
        if (refundCodes.length() > 0) {
            refundCodes = refundCodes.substring(0, refundCodes.length() - 1);
        }
        refundMoney = refundMoney > couponDiscount ? (refundMoney - couponDiscount) : 0;
        if (couponDiscount >= 0.01) {
            ((TextView) mRoot.findViewById(R.id.refund_money)).setText(getResources().getString(R.string.refund_tip, StringUtils.getPrice(refundMoney)));
        } else {
            ((TextView) mRoot.findViewById(R.id.refund_money)).setText(StringUtils.getPrice(refundMoney) + "元");
        }
    }

    @Override
    public void onResume() {
        setSubmitClickable(false);
        super.onResume();
    }

    /**
     * show the amount of money the consumer could get after refunding
     *
     * @param refunded  the number of codes that has been refunded
     * @param refunding the number of codes that will be refunded
     * @param unRefund  the number of codes that will not be refunded
     * @param price     single price of code
     * @param discount  coupon worth
     * @param payMoney  amount of money the consumer paid for the order
     * @return the money the consumer will get
     */
    public double getRefundMoney(int refunded, int refunding, int unRefund, double price, double discount, double payMoney) {
        double money = 0;
        double refundedMoney = refunded * price - discount;
        refundedMoney = refundedMoney > 0 ? refundedMoney : 0;
        int refundSum = refunded + refunding;
        double refundSumMoney = refundSum * price - discount;
        refundSumMoney = refundSumMoney > 0 ? refundSumMoney : 0;
        double toRefundMoney = refundSumMoney - refundedMoney;
        if (toRefundMoney < 0.01) {
            money = unRefund > 0 ? 0 : payMoney;
        } else {
            money = toRefundMoney;
        }
        return money;
    }
}
