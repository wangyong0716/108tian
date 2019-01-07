package com.ksider.mobile.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class Storage {
    public static final String USER_INFO = "userinfo";
    public static final String SESSION_ID = "sessionId";
    public static final String CITY_ID = "cityinfo";
    public static SharedPreferences sharedPref;

    public static void init(Context context) {
        sharedPref = context.getSharedPreferences(Storage.class.getName(), Context.MODE_PRIVATE);
    }

    public static SharedPreferences getSharedPref() {
        return sharedPref;
    }

    public static void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void putFloat(String key, float value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public static void putInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void putLong(String key, long value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void putStringSet(String key, Set<String> values) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(key, values);
        editor.commit();
    }

    public static void remove(String key) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(key);
        editor.commit();
    }
}
