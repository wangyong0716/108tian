package com.ksider.mobile.android.utils.net.toolbox;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by wenkui on 5/5/15.
 */
public class FormJsonObjectRequest extends JsonObjectRequest {
    private Map<String, String> mHeaders = new HashMap<String, String>();
    private Priority mPriority = null;
    protected  Map<String,String> mParams = new HashMap<String,String>();

    public FormJsonObjectRequest(int method, String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, null, listener, errorListener);
    }

    public FormJsonObjectRequest(String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, null, listener, errorListener);
    }
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders;
    }

    public void setHeader(String title, String content) {
        mHeaders.put(title, content);
    }

    public void setPriority(Priority priority) {
        this.mPriority = priority;
    }

    public void addFormParams(String key, String value) {
        this.mParams.put(key, value);
    }
    @Override
    protected Map<String, String> getParams(){
        return mParams;
    }
    @Override
    public byte[] getBody() {
        Map params = this.getParams();
        return params != null && params.size() > 0?encodeParameters(params, this.getParamsEncoding()):null;
    }

    @Override
    public String getBodyContentType() {
        return "application/x-www-form-urlencoded";
    }

    protected byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            Iterator var5 = params.entrySet().iterator();
            while(var5.hasNext()) {
                java.util.Map.Entry uee = (java.util.Map.Entry)var5.next();
                if(encodedParams.length()>0){
                    encodedParams.append('&');
                }
                encodedParams.append(URLEncoder.encode((String) uee.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode((String)uee.getValue(), paramsEncoding));
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException var6) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, var6);
        }
    }
    /*
     * If prioirty set use it,else returned NORMAL
     * @see com.android.volley.Request#getPriority()
     */
    public Priority getPriority() {
        if( this.mPriority != null) {
            return mPriority;
        } else {
            return Priority.NORMAL;
        }
    }
}
