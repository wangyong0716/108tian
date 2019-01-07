package com.ksider.mobile.android.utils;

import android.util.Log;
import com.ksider.mobile.android.model.MessageEvent;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wenkui on 5/21/15.
 */
public class UserInfo {
    public static boolean isLogin() {
        return Storage.getSharedPref().getString(Storage.SESSION_ID, null) != null;
    }

    public static JSONObject getUserInfo() {
        String userInfo = Storage.getSharedPref().getString(Storage.USER_INFO, null);
        if (userInfo != null) {
            try {
                return new JSONObject(userInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getPhone() {
        JSONObject user = getUserInfo();
        if (user != null) {
            try {
                return user.getString("mobilePhone");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static String getUserId() {
        JSONObject user = getUserInfo();
        if (user != null) {
            try {
                return user.getString("uid");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static int getScore() {
        JSONObject user = getUserInfo();
        if (user != null) {
            try {
                return user.getInt("memberPoint");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static int getUsedScore() {
        JSONObject user = getUserInfo();
        if (user != null) {
            try {
                return user.getInt("memberPointUsed");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static void store(JSONObject data) {
        if (data != null) {
            try {
                Log.i("AAA", "store->data=" + data.toString());
                Storage.putString(Storage.USER_INFO, data.toString());
                Storage.putString(Storage.SESSION_ID, data.getString("sid"));
                APIUtils.clearSession();
                MessageUtils.eventBus.post(new MessageEvent(MessageEvent.NOTIFY_LOGIN, true));
                Utils.alisUser(data.getString("uid"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getCityId() {
        String cityInfo = Storage.getSharedPref().getString(Storage.CITY_ID, null);
        if (cityInfo != null) {
            try {
                JSONObject city = new JSONObject(cityInfo);
                return city.getInt("city_id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static String getCityName() {
        String cityInfo = Storage.getSharedPref().getString(Storage.CITY_ID, null);
        if (cityInfo != null) {
            try {
                JSONObject city = new JSONObject(cityInfo);
                return city.getString("city_name");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static void addDefaultCity(JSONObject city) {
        if (city == null) {
            Storage.putString(Storage.CITY_ID, city.toString());
            try {
                Storage.putString(Storage.CITY_ID, city.toString());
                Storage.putString(Storage.SESSION_ID, city.getString("sid"));
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
    }
}
