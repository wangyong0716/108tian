package com.ksider.mobile.android.utils.net.ssl;

import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.HurlStack;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.utils.stat.RequestStat;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Created by wenkui on 4/20/15.
 */
public class StatStack extends HurlStack{
    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
//        RequestStat.stat.start(System.currentTimeMillis());
        HttpResponse res = super.performRequest(request, additionalHeaders);
//        RequestStat.stat.end(System.currentTimeMillis());
//        Log.v(Constants.LOG_TAG," \n image\n"+ RequestStat.stat.toString());
        return  res;
    }
}
