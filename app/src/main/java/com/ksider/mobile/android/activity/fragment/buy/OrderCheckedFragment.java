package com.ksider.mobile.android.activity.fragment.buy;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.alipay.sdk.app.PayTask;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.PurchaseAcitvity;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.activity.fragment.FragmentCallback;
import com.ksider.mobile.android.activity.fragment.buy.model.AliPayResult;
import com.ksider.mobile.android.activity.fragment.buy.model.AlipayOrderInfo;
import com.ksider.mobile.android.activity.fragment.buy.model.WeChatOrderInfo;
import com.ksider.mobile.android.auth.Authorize;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.model.OrderModel;
import com.ksider.mobile.android.utils.*;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wenkui on 4/28/15.
 */
public class OrderCheckedFragment extends Fragment {
    private View view;
    private OrderModel mOrderModel;
    private double couponWorth = 0;

    protected JSONObject mParams;
    protected JSONObject mNotify;
    protected IWXAPI mWXapi;
    protected static final int PAYMENT_MODEL_ALIPAY = 0;
    protected static final int PAYMENT_MODEL_WECHAT = 1;
    protected int mPayment = PAYMENT_MODEL_ALIPAY;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView more = (TextView) getActivity().findViewById(R.id.more_choice);
        if (more != null) {
            more.setVisibility(View.INVISIBLE);
        }
        view = inflater.inflate(R.layout.fragment_buy_order_checked, container, false);
        ((TextView) view.findViewById(R.id.submit)).setText("确认支付");
        if (savedInstanceState == null) {
            mOrderModel = new OrderModel();
            Bundle args = getArguments();
            if (args != null) {
                try {
                    mParams = new JSONObject(args.getString("order"));
//                    mOrderModel.loadJson(mParams.getJSONObject("order"));
                    mOrderModel.loadJson(mParams);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            mOrderModel = (OrderModel) savedInstanceState.get("OrderModel");
        }
        initOrder(view);
        RadioGroup payment_channel = (RadioGroup) view.findViewById(R.id.payment_channel);
        payment_channel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.alipay:
                        mPayment = PAYMENT_MODEL_ALIPAY;
                        break;
                    case R.id.weixinpay:
                        mPayment = PAYMENT_MODEL_WECHAT;
                        break;
                }
            }
        });
        Network.getInstance().addToRequestQueue(getRequest());
        view.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setClickable(false);
                submit();
                MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_PURCHASE_PAY);
            }
        });
        MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_PURCHASE_CHECK);
        return view;
    }

    protected void initOrder(final View baseview) {
        if (baseview != null && mOrderModel != null) {
            TextView textView = (TextView) baseview.findViewById(R.id.order_title);
            textView.setText(mOrderModel.getProductName());

            if (mOrderModel.getConsumeTime() > 0) {
                textView = (TextView) baseview.findViewById(R.id.use_date);
                textView.setText(DateUtils.getFormatDateWithWeek(mOrderModel.getConsumeTime()));
            } else {
                baseview.findViewById(R.id.date_clickarea).setVisibility(View.GONE);
            }
            textView = (TextView) baseview.findViewById(R.id.quantity);
            textView.setText(String.valueOf(mOrderModel.getQuantity()));
//            textView = (TextView) baseview.findViewById(R.id.conclude_product);
//            textView.setText(getResources().getString(R.string.purchase_order_product, mOrderModel.getQuantity()));

            couponWorth = mOrderModel.getCouponDiscount();
            textView = (TextView) baseview.findViewById(R.id.coupon_worth);
            if (couponWorth < 0.01) {
                baseview.findViewById(R.id.coupon_use_state).setVisibility(View.GONE);
            } else {
                baseview.findViewById(R.id.coupon_use_state).setVisibility(View.VISIBLE);
                textView.setText(getResources().getString(R.string.coupon_worth, StringUtils.getPrice(couponWorth)));
            }
//            TextView text = (TextView) baseview.findViewById(R.id.amount_to_pay);
//            text.setText(formatter.format(mOrderModel.getTotalFee()) + "元");
            ((TextView) baseview.findViewById(R.id.product_price)).setText(StringUtils.getPrice(mOrderModel.getSellPrice()) + "元");
            ((TextView) baseview.findViewById(R.id.to_pay)).setText(StringUtils.getPrice(mOrderModel.getTotalFee()) + "元");
            ((TextView) baseview.findViewById(R.id.price)).setText(StringUtils.getPrice(mOrderModel.getTotalFee()) + "元");

            if (mOrderModel.getRefund() == 1) {
                ((TextView) baseview.findViewById(R.id.refundAble)).setText(R.string.support_refund);
            } else {
                ((TextView) baseview.findViewById(R.id.refundAble)).setText(R.string.no_support_refund);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("OrderModel", mOrderModel);
    }

    public JsonObjectRequest getRequest() {
        Map<String, Object> params = new HashMap<String, Object>();
//        try {
        params.put("action", "getPaymentInfo");
//            JSONObject order = mParams.getJSONObject("order");
        params.put("serialNumber", mOrderModel.getSerialNumber());
        return new JsonObjectRequest(APIUtils.getUrl(APIUtils.ORDER, params), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject res) {
                try {
                    if (res.getInt("status") == 0) {
                        mNotify = res.getJSONObject("data").getJSONObject("notify");
                    } else {
                        Toast.makeText(getActivity(), res.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), "服务器异常", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(Constants.LOG_TAG, volleyError.toString());
                Toast.makeText(getActivity(), "服务器异常", Toast.LENGTH_LONG).show();
            }
        });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return null;
    }

    public void submit() {
        if (mNotify == null) {
            view.findViewById(R.id.submit).setClickable(true);
            Network.getInstance().addToRequestQueue(getRequest());
            return;
        }
        try {
//            JSONObject order = mParams.getJSONObject("order");
            if (mOrderModel.getTotalFee() < 0.01) {
                Toast.makeText(getActivity(), "订单信息有误，请重新下单！", Toast.LENGTH_LONG).show();
                return;
            }
            switch (mPayment) {
                case PAYMENT_MODEL_ALIPAY:
                    AliPayTask aliPayTask = new AliPayTask();
                    AlipayOrderInfo orderInfo = new AlipayOrderInfo(mNotify.getString("ali"),
                            String.valueOf(mOrderModel.getSerialNumber()),
                            mOrderModel.getProductName(),
                            String.valueOf(mOrderModel.getTotalFee()));
                    aliPayTask.execute(orderInfo);
                    break;
                case PAYMENT_MODEL_WECHAT:
                    if (mWXapi == null) {
                        mWXapi = WXAPIFactory.createWXAPI(getActivity(), Authorize.WX_APP_KEY, true);
                    }
                    if (mWXapi.isWXAppInstalled()) {
                        mWXapi.registerApp(Authorize.WX_APP_KEY);
                    } else {
                        Toast.makeText(getActivity(), "未安装微信", Toast.LENGTH_LONG).show();
                        return;
                    }
                    WeChatOrderInfo weChatOrderInfo = new WeChatOrderInfo(mNotify.getString("weixin"),
                            String.valueOf(mOrderModel.getSerialNumber()),
                            mOrderModel.getProductName(),
                            String.valueOf(mOrderModel.getTotalFee()));
                    weChatOrderInfo.addCallbackListner(new WeChatOrderInfo.CallBack() {
                        @Override
                        public void response(PayReq req) {
                            mWXapi.sendReq(req);
                        }
                    });
                    weChatOrderInfo.run();
                    break;
            }
        } catch (JSONException e) {
            view.findViewById(R.id.submit).setClickable(true);
            e.printStackTrace();
        }
    }

    protected void showResult(String result) {
        if (getActivity() instanceof FragmentCallback) {
            FragmentCallback callback = (FragmentCallback) getActivity();
            JSONObject data = new JSONObject();
            try {
                data.put(PurchaseAcitvity.STAGE, PurchaseAcitvity.PAYMENT);
                data.put("out_trade_no", mOrderModel.getSerialNumber());
                if (result != null) {
                    data.put("result", result);
                }
                data.put("payment", mPayment);
                callback.next(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class AliPayTask extends AsyncTask<AlipayOrderInfo, Integer, String> {

        @Override
        protected String doInBackground(AlipayOrderInfo... alipayOrderInfos) {
            PayTask payTask = new PayTask(getActivity());

            if (alipayOrderInfos.length < 1) {
                return null;
            }
            String result = null;
            try {
                AlipayOrderInfo orderInfo = alipayOrderInfos[0];
                if (orderInfo == null) {
                    return null;
                }
                result = payTask.pay(orderInfo.getOrderInfo());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            AliPayResult ali = new AliPayResult(result);
            if ("9000".equals(ali.getResultStatus())) {
                showResult(ali.getResult());
            } else {
                String tips = "";
                if ("4000".equals(ali.getResultStatus())) {
                    tips = "订单支付失败";
                } else if ("6001".equals(ali.getResultStatus())) {
                    tips = "取消支付";
                } else if ("6002".equals(ali.getResultStatus())) {
                    tips = "取消支付";
                } else if ("8000".equals(ali.getResultStatus())) {
                    tips = "正在处理中";
                }
                Toast.makeText(getActivity(), tips, Toast.LENGTH_LONG).show();
            }
            view.findViewById(R.id.submit).setClickable(true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MessageUtils.eventBus.register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (view.findViewById(R.id.purchase_bottom_bar) != null) {
            view.findViewById(R.id.purchase_bottom_bar).setBackgroundColor(getResources().getColor(R.color.divider_line_color));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MessageUtils.eventBus.unregister(this);
    }

    public void onEventMainThread(MessageEvent event) {
        if (event.getType() == MessageEvent.NOTIFY_WECHAT_PAY_FINISH) {
            showResult(null);
        } else if (event.getType() == MessageEvent.NOTIFY_WECHAT_PAY_ERROR) {
            Toast.makeText(getActivity(), "微信支付失败！", Toast.LENGTH_LONG).show();
        } else if (event.getType() == MessageEvent.NOTIFY_WECHAT_PAY_CANEL) {
            Toast.makeText(getActivity(), "取消支付", Toast.LENGTH_LONG).show();
        }
        view.findViewById(R.id.submit).setClickable(true);
    }

}
