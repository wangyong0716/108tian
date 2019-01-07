package com.ksider.mobile.android.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.ksider.mobile.android.WebView.Constants;

import java.util.UUID;

/**
 * Created by wenkui on 1/28/15.
 */
public class DeviceUuid {
    protected volatile static String uuid;
    protected  static String channel;
    protected static String version;
    public static void init(Context context){
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        uuid = deviceUuid.toString();
        initChannel(context);
        initVersion(context);
    }
    protected static  void initChannel(Context context){
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            channel = bundle.getString("BaiduMobAd_CHANNEL");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(Constants.LOG_TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(Constants.LOG_TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
    }
    protected static  void initVersion(Context context){
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        }
    }

    public static String getUuid(){
        return uuid==null?"":uuid;
    }
    public static  String getChannel(){
        return channel==null?"":channel;
    }
    public static String getVersion(){
        return version==null?"":version;
    }
}
