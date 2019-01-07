package com.ksider.mobile.android.utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.Volley;
import com.ksider.mobile.android.utils.net.ssl.SslHttpStack;
import com.ksider.mobile.android.utils.net.ssl.StatStack;

public class Network {

    /**
     * Log or request TAG
     */
    public static final String TAG = "108tian";
    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;
    private RequestQueue mImageQueue;
    /**
     * A singleton instance of the application class for easy access in other places
     */
    private static Network mInstance;
    private static Context mContext;

    public Network(Context context) {
        mContext = context;
        BasicNetwork network = new BasicNetwork(new SslHttpStack(false, mContext));
        mRequestQueue = new RequestQueue(new NoCache(), network);
        mRequestQueue.start();
        mImageQueue = Volley.newRequestQueue(mContext, new StatStack());
    }


    public static String getUserAgent() {
        return "ksider/" + DeviceUuid.getVersion() + " (Android; Android " + Build.VERSION.RELEASE + "; " + Build.BRAND + " " + android.os.Build.MODEL + ")";
    }

    public static synchronized void init(Context context) {
        if (mInstance == null) {
            mInstance = new Network(context);
        }
    }

    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized Network getInstance() {
        return mInstance;
    }

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public RequestQueue getImageQueue() {
        return mImageQueue;
    }


    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setTag(TAG);

        getRequestQueue().add(req);
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
