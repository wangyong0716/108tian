package com.ksider.mobile.android.utils.net.toolbox;

import android.util.Log;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.ksider.mobile.android.WebView.Constants;

/**
 * Created by wenkui on 5/12/15.
 */
public class StringRequestNoParams extends StringRequest {
    protected String mBody;
    public StringRequestNoParams(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public StringRequestNoParams(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }
    public void setBody(String body){
        mBody = body;
    }
    @Override
    public byte[] getBody(){
        return mBody==null?null:mBody.getBytes();
    }

}
