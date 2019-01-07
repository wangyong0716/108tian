package com.ksider.mobile.android.activity.fragment.buy.model;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.auth.Authorize;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.WePayMD5;
import com.ksider.mobile.android.utils.net.toolbox.FormJsonObjectRequest;
import com.ksider.mobile.android.utils.net.toolbox.StringRequestNoParams;
import com.sina.weibo.sdk.utils.MD5;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by wenkui on 5/12/15.
 */
public class WeChatOrderInfo {
    protected String mWeChatUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    protected String mOutTradeNo;
    protected String mNotifyUrl;
    protected String mTotalFee;
    protected String mSubject;
    protected CallBack mCallback;
    protected SortedMap mSignParams;

    public interface CallBack {
        void response(PayReq req);
    }

    protected interface SignCallback {
        void response(String sign);
    }

    public WeChatOrderInfo(String notifyUrl, String outTradeNo, String subject, String totalFee) {
        mNotifyUrl = notifyUrl;
        mOutTradeNo = outTradeNo;
        mSubject = subject;
        if (totalFee != null) {
            Double total = (Double.valueOf(totalFee) * 100);
            mTotalFee = new Integer(total.intValue()).toString();
        }
        mSignParams = new TreeMap();
        mSignParams.put("appid", Authorize.WX_APP_KEY);
        mSignParams.put("body", mSubject);
        mSignParams.put("mch_id", Authorize.WX_MCH_ID);
        mSignParams.put("nonce_str", genNonceStr());
        mSignParams.put("notify_url", mNotifyUrl);
        mSignParams.put("out_trade_no", mOutTradeNo);
        mSignParams.put("spbill_create_ip", "127.0.0.1");
        mSignParams.put("total_fee", mTotalFee);
        mSignParams.put("trade_type", "APP");
    }

    public void addCallbackListner(CallBack callBack) {
        mCallback = callBack;
    }

    protected void genSignContent(SortedMap params, final SignCallback callback) {
        final StringBuilder sb = new StringBuilder();
        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if(sb.length()>0){
                sb.append('&');
            }
            sb.append(key);
            sb.append('=');
            sb.append(params.get(key));
        }
        try {
            Map<String, Object> mParams = new HashMap<String, Object>();
            mParams.put("action", "signWeixin");
            mParams.put("content", URLEncoder.encode(sb.toString(), "utf-8"));
            JsonObjectRequest request = new JsonObjectRequest(APIUtils.getUrl(APIUtils.SIGN,mParams),null,
                    new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getInt("status") == 0) {
                        if (callback != null) {
                            callback.response(response.getString("data").toUpperCase());
                        }
                    }
                } catch (JSONException e) {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.v(Constants.LOG_TAG, volleyError.toString());
            }
        });
            Network.getInstance().addToRequestQueue(request);
            request.setShouldCache(false);
        }catch (UnsupportedEncodingException e){

        }
//        FormJsonObjectRequest request = new FormJsonObjectRequest(APIUtils.getUrl(APIUtils.SIGN), new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                try {
//                    if (response.getInt("status") == 0) {
//                        if (callback != null) {
//                            callback.response(response.getString("data").toUpperCase());
//                        }
//                    }
//                } catch (JSONException e) {
//
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//
//            }
//        });
//        try {
//            request.setShouldCache(false);
//            request.addFormParams("action", "signWeixin");
//            request.addFormParams("content", URLEncoder.encode(sb.toString(), "utf-8"));
//            Network.getInstance().addToRequestQueue(request);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

    }

    private String genProductArgs(String sign) {
        try {
            mSignParams.put("sign", sign);
            String xmlstring = toXml(mSignParams);
            return xmlstring;
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, "genProductArgs fail, ex = " + e.getMessage());
        }
        return null;
    }


    private String genNonceStr() {
        Random random = new Random();
        return WePayMD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }

    private String toXml(SortedMap params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            sb.append("<" + key + ">");
            sb.append(params.get(key));
            sb.append("</" + key + ">");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    public Map<String, String> decodeXml(String content) {

        try {
            Map<String, String> xml = new HashMap<String, String>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                String nodeName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:

                        break;
                    case XmlPullParser.START_TAG:

                        if ("xml".equals(nodeName) == false) {
                            //实例化student对象
                            xml.put(nodeName, parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }

            return xml;
        } catch (Exception e) {
        }
        return null;
    }

    public void run() {
        genSignContent(mSignParams, new SignCallback() {
            @Override
            public void response(String sign) {
                requestPayment(sign);
            }
        });
    }

    protected void requestPayment(String sign) {
        StringRequestNoParams request = new StringRequestNoParams(Request.Method.POST, mWeChatUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    proccess(new String(res.getBytes("ISO-8859-1"), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        });
        request.setBody(genProductArgs(sign));
        Network.getInstance().addToRequestQueue(request);
    }

    protected void proccess(String value) {
        Map<String, String> xmls = decodeXml(value);
        //校验微信支付
        if ("SUCCESS".equals(xmls.get("return_code"))) {
            final PayReq request = new PayReq();
            request.appId = Authorize.WX_APP_KEY;
            request.partnerId = Authorize.WX_MCH_ID;
            request.prepayId = xmls.get("prepay_id");
            request.nonceStr = genNonceStr();
            request.packageValue = "Sign=WXPay";
            request.timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            SortedMap signParams = new TreeMap();
            signParams.put("appid", request.appId);
            signParams.put("noncestr", request.nonceStr);
            signParams.put("package", request.packageValue);
            signParams.put("partnerid", request.partnerId);
            signParams.put("prepayid", request.prepayId);
            signParams.put("timestamp", request.timeStamp);
            genSignContent(signParams, new SignCallback() {
                @Override
                public void response(String sign) {
                    request.sign = sign;
                    if (mCallback != null) {
                        mCallback.response(request);
                    }
                }
            });
        }
    }

}
