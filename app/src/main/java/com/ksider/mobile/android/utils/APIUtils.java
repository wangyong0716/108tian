package com.ksider.mobile.android.utils;

import android.util.Log;
import com.ksider.mobile.android.WebView.Constants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class APIUtils {
            protected static final String mBaseUrl = "https://api.108tian.com/mobile/";
//    protected static final String mBaseUrl = "http://test.api.108tian.com/mobile/";

    protected static final String mVersion = "v3";
    protected static String mSessionId = null;
    protected static Boolean mHasReady = false;
    public static final String POI_ATTRACTIONS = "Scene";
    public static final String POI_FARMYARD = "Farm";
    public static final String POI_RESORT = "Resort";
    public static final String POI_PICK = "Pick";
    public static final String GUIDE = "Weekly";
    public static final String ACTIVITY = "Event";
    public static final String CHOINCENESS = "Recommend";
    public static final String CHOINCENESSDETAILIST = "RecommendDetailList";
    public static final String AUTHORIZE = "authorize";
    public static final String SEARCH = "Search";
    public static final String COMMENT = "Comment";
    public static final String ORDER = "Order";
    public static final String REGISTER = "Register";
    public static final String USER_CENTER = "UserCenter";
    public static final String SYNC_NOTIFY = "SyncNotify";
    public static final String COUPON = "Coupon";
    public static final String SIGN = "Sign";
    public static final String NEW_COMMENT = "NewComment";
    public static final String PRODUCT = "Product";
    public static final String MERCHANT = "Merchant";
    public static final String CITYREGIONINFO = "CityRegionInfo";
    public static final String THEMEITEMS = "ThemeItems";
    public static final String PROMOTION = "Promotion";
    public static final String EVALUATE = "Evaluate";

    public static void clearSession() {
        mHasReady = false;
        mSessionId = null;
    }


    public static String getUrl(String entity, Map<String, Object> params) {
        params = appendSid(params);
        String uri = extractParams(params);
        if (uri != null) {
            return mBaseUrl + mVersion + "/" + entity + "?" + uri;
        }
        return null;
    }

    public static String getUrl(String entity) {
        Log.v(Constants.LOG_TAG, mBaseUrl + mVersion + "/" + entity);
        return mBaseUrl + mVersion + "/" + entity;
    }

    public static String getTheme(String entity) {
        return getTheme(entity, null);
    }

    public static String getTheme(String entity, Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<String, Object>();
        }
        params.put("type", entity);
        String uri = extractParams(params);
        return mBaseUrl + mVersion + "/ThemeItems?" + uri;
    }

    public static String getUserCenter(Map<String, Object> params) {
        if (mSessionId == null) {
            mSessionId = Storage.sharedPref.getString(Storage.SESSION_ID, null);
        }
        if (mSessionId != null) {
            params.put("sid", mSessionId);
        } else {
            return null;
        }
        params.put("channel", DeviceUuid.getChannel());
        return mBaseUrl + mVersion + "/UserCenter?" + extractParams(params);
    }

    public static String getHome(double lng, double lat) {
        return mBaseUrl + mVersion + "/Home?lnglat=" + lng + "," + lat + "&uuid=" + DeviceUuid.getUuid() + "&channel=" + DeviceUuid.getChannel();
    }

    public static String getHome(int cityId) {
        return mBaseUrl + mVersion + "/Home?cityId=" + cityId + "&uuid=" + DeviceUuid.getUuid() + "&channel=" + DeviceUuid.getChannel();
    }

    public static String getPOIList(String entity, Map<String, Object> params) {
        params = appendSid(params);
        String uri = extractParams(params);
        if (uri != null) {
            Log.v(Constants.LOG_TAG, mBaseUrl + mVersion + "/" + entity + "List?" + uri);
            return mBaseUrl + mVersion + "/" + entity + "List?" + uri;
        }
        return null;
    }

    /**
     * 获取短信验证码
     *
     * @param phone
     * @param reset 是否重置
     * @return
     */

    public static String getCode(String phone, Boolean reset) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("phoneNumber", phone);
        if (reset) {
            params.put("action", "sendVerifyCodePasswordReset");
        } else {
            params.put("action", "sendVerifyCodeRegister");
        }
        String uri = extractParams(params);
        if (uri != null) {
            return mBaseUrl + mVersion + "/Register?" + uri;
        }
        return null;
    }

    /**
     * *
     * 验证验证码
     *
     * @param phone
     * @param code
     * @return
     */
    public static String verifyCode(String phone, String code, Boolean reset) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("phoneNumber", phone);
        if (reset) {
            params.put("action", "verifyCodePasswordReset");
        } else {
            params.put("action", "verifyCodeRegister");
        }
        params.put("code", code);
        String uri = extractParams(params);
        if (uri != null) {
            return mBaseUrl + mVersion + "/Register?" + uri;
        }
        return null;
    }

    /**
     * 获取短信验证码
     *
     * @param phone
     * @return
     */

    public static String getBindCode(String phone) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("phoneNumber", phone);
        params.put("action", "sendVerifyCodeBind");
        String uri = extractParams(appendSid(params));
        if (uri != null) {
            return mBaseUrl + mVersion + "/Register?" + uri;
        }
        return null;
    }

    /**
     * 获取短信验证码
     *
     * @param phone
     * @return
     */

    public static String bindPhone(String phone, String code) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("phoneNumber", phone);
        params.put("code", code);
        params.put("action", "verifyCodeBind");
        String uri = extractParams(appendSid(params));
        if (uri != null) {
            return mBaseUrl + mVersion + "/Register?" + uri;
        }
        return null;
    }

    /**
     * 重置密码
     *
     * @param sid      　验证码验证正确时获取的验证码
     * @param password
     * @return
     */
    public static String resetPassword(String sid, String password) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("sid", sid);
        params.put("action", "setPassword");
        params.put("newPassword", StringUtils.md5Hash(password));
        params.put("channel", DeviceUuid.getChannel());
        String uri = extractParams(params);
        if (uri != null) {
            return mBaseUrl + mVersion + "/Register?" + uri;
        }
        return null;
    }

    public static String login(String phone, String password) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "login");
        params.put("password", StringUtils.md5Hash(password));
        params.put("phoneNumber", phone);
        params = appendSid(params);
        String uri = extractParams(params);
        if (uri != null) {
            return mBaseUrl + mVersion + "/Register?" + uri;
        }
        return null;
    }

    public static String register(Map<String, Object> params) {
        params = appendSid(params);
        String uri = extractParams(params);
        if (uri != null) {
            return mBaseUrl + mVersion + "/Register?" + uri;
        }
        return null;
    }

    protected static Map<String, Object> appendSid(Map<String, Object> params) {
        if (!mHasReady) {
            mHasReady = true;
            mSessionId = Storage.sharedPref.getString(Storage.SESSION_ID, null);
        }
        if (mSessionId != null) {
            params.put("sid", mSessionId);
        }
        params.put("channel", DeviceUuid.getChannel());
        return params;
    }

    public static String getPOIDetail(String entity, Map<String, Object> params) {
        params = appendSid(params);
        String uri = extractParams(params);
        if (uri != null) {
            Log.v(Constants.LOG_TAG, mBaseUrl + mVersion + "/" + entity + "Detail?" + uri);
            return mBaseUrl + mVersion + "/" + entity + "Detail?" + uri;
        }
        return null;
    }

    protected static String extractParams(Map<String, Object> params) {
        String uri = null;
        if (params != null) {
            for (Iterator<String> it = params.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                if (uri == null) {
                    uri = key + "=" + params.get(key);
                } else {
                    uri += "&" + key + "=" + params.get(key);
                }
            }
        }
        return uri;
    }
}
