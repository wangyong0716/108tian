
/**
 * Copyright 2013 Mani Selvaraj
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.ksider.mobile.android.utils.net.ssl;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.HttpStack;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.net.toolbox.MultiPartRequest;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom implementation of com.android.volley.toolboox.HttpStack
 * Uses apache HttpClient-4.2.5 jar to take care of . You can download it from here
 * http://hc.apache.org/downloads.cgi
 *
 * @author Mani Selvaraj
 */
public class SslHttpStack implements HttpStack {

    private boolean mIsConnectingToYourServer = false;
    private final static String HEADER_CONTENT_TYPE = "Content-Type";
    protected Context mContext;
    protected HttpClient mHttpClient;

    public SslHttpStack(boolean isYourServer, Context context) {
        mIsConnectingToYourServer = isYourServer;
        mContext = context;
    }

    private static void addHeaders(HttpUriRequest httpRequest, Map<String, String> headers) {
        for (String key : headers.keySet()) {
            httpRequest.setHeader(key, headers.get(key));
        }
    }

    @SuppressWarnings("unused")
    private static List<NameValuePair> getPostParameterPairs(Map<String, String> postParams) {
        List<NameValuePair> result = new ArrayList<NameValuePair>(postParams.size());
        for (String key : postParams.keySet()) {
            result.add(new BasicNameValuePair(key, postParams.get(key)));
        }
        return result;
    }

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {
//        RequestStat.stat.start(System.currentTimeMillis());
        HttpUriRequest httpRequest = createHttpRequest(request, additionalHeaders);
        httpRequest.setHeader("Connection", "keep-alive");
        httpRequest.setHeader("User-Agent", Network.getUserAgent());
        addHeaders(httpRequest, additionalHeaders);
        addHeaders(httpRequest, request.getHeaders());
        onPrepareRequest(httpRequest);
        if (mHttpClient == null) {
            HttpParams httpParams = httpRequest.getParams();
//            int timeoutMs = request.getTimeoutMs();
            // data collection and possibly different for wifi vs. 3G.
            //set connect timeout
            HttpConnectionParams.setConnectionTimeout(httpParams, Constants.CONNECTION_TIMEOUT);
            //set socket timeout, namely timeout gor getting response from server
            HttpConnectionParams.setSoTimeout(httpParams, Constants.SOCKET_TIMEOUT);
            /* Register schemes, HTTP and HTTPS */
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", new PlainSocketFactory(), 80));
            registry.register(new Scheme("https", new EasySSLSocketFactory(mContext, mIsConnectingToYourServer), 443));

            /* Make a thread safe connection manager for the client */
            ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(httpParams, registry);
            mHttpClient = new DefaultHttpClient(manager, httpParams);
        }
        HttpResponse res = mHttpClient.execute(httpRequest);
//        RequestStat.stat.end(System.currentTimeMillis());
//        Log.v(Constants.LOG_TAG, RequestStat.stat.toString());
        return res;
    }

    /**
     * Creates the appropriate subclass of HttpUriRequest for passed in request.
     */
    @SuppressWarnings("deprecation")
    /* protected */ static HttpUriRequest createHttpRequest(Request<?> request,
                                                            Map<String, String> additionalHeaders) throws AuthFailureError {
        switch (request.getMethod()) {
            case Method.DEPRECATED_GET_OR_POST: {
                // This is the deprecated way that needs to be handled for backwards compatibility.
                // If the request's post body is null, then the assumption is that the request is
                // GET.  Otherwise, it is assumed that the request is a POST.
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    HttpPost postRequest = new HttpPost(request.getUrl());
                    postRequest.addHeader(HEADER_CONTENT_TYPE, request.getPostBodyContentType());
                    HttpEntity entity;
                    entity = new ByteArrayEntity(postBody);
                    postRequest.setEntity(entity);
                    return postRequest;
                } else {
                    return new HttpGet(request.getUrl());
                }
            }
            case Method.GET:
                return new HttpGet(request.getUrl());
            case Method.DELETE:
                return new HttpDelete(request.getUrl());
            case Method.POST: {
                HttpPost postRequest = new HttpPost(request.getUrl());
                if (request instanceof MultiPartRequest) {
                    setMultiPartBody(postRequest, request);
                } else {
                    postRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                }
                setEntityIfNonEmptyBody(postRequest, request);
                return postRequest;
            }
            case Method.PUT: {
                HttpPut putRequest = new HttpPut(request.getUrl());
                if (request instanceof MultiPartRequest) {
                    setMultiPartBody(putRequest, request);

                } else {
                    putRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                }
                ;
                setEntityIfNonEmptyBody(putRequest, request);
                return putRequest;
            }
            // Added in source code of Volley libray.
//            case Method.PATCH: {
//            	HttpPatch patchRequest = new HttpPatch(request.getUrl());
//            	patchRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
//                setEntityIfNonEmptyBody(patchRequest, request);
//                return patchRequest;
//            }
            default:
                throw new IllegalStateException("Unknown request method.");
        }
    }

    private static void setEntityIfNonEmptyBody(HttpEntityEnclosingRequestBase httpRequest,
                                                Request<?> request) throws AuthFailureError {
        byte[] body = request.getBody();
        if (body != null) {
            HttpEntity entity = new ByteArrayEntity(body);
            httpRequest.setEntity(entity);
        }
    }

    /**
     * If Request is MultiPartRequest type, then set MultipartEntity in the httpRequest object.
     *
     * @param httpRequest
     * @param request
     * @throws AuthFailureError
     */
    private static void setMultiPartBody(HttpEntityEnclosingRequestBase httpRequest,
                                         Request<?> request) throws AuthFailureError {

        // Return if Request is not MultiPartRequest
        if (!(request instanceof MultiPartRequest)) {
            return;
        }

        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentType contentType = ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8);
        //Iterate the stringUploads
        Map<String, String> stringUpload = ((MultiPartRequest) request).getStringUploads();
        for (Map.Entry<String, String> entry : stringUpload.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            try {
                multipartEntity.addPart(((String) entry.getKey()), new StringBody((String) entry.getValue()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Map<String, File> fileUpload = ((MultiPartRequest) request).getFileUploads();
        for (Map.Entry<String, File> entry : fileUpload.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            multipartEntity.addPart(((String) entry.getKey()), new FileBody((File) entry.getValue()));
        }

        Map<String, byte[]> byteUpload = ((MultiPartRequest) request).getByteUploads();
        for (Map.Entry<String, byte[]> entry : byteUpload.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            multipartEntity.addPart(((String) entry.getKey()), new ByteArrayBody((byte[]) entry.getValue(), "image"));
        }
        httpRequest.setEntity(multipartEntity);
    }

    /**
     * Called before the request is executed using the underlying HttpClient.
     * <p/>
     * <p>Overwrite in subclasses to augment the request.</p>
     */
    protected void onPrepareRequest(HttpUriRequest request) throws IOException {
        // Nothing.
    }
}
 


