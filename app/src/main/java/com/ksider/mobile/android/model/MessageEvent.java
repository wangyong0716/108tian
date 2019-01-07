package com.ksider.mobile.android.model;

public class MessageEvent {
    public static final int UPDATE_POSITION = 0;
    public static final int UPDATE_DEFAULTCITY = 1;
    public static final int START_REQUEST = 2;
    public static final int REFLASH_CONTENT = 3;
    public static final int UPDATE_CITYNAME = 4;
    public static final int NOTIFY_LOGIN = 5;
    public static final int NOTIFY_LOGOUT = 6;
    public static final int NOTIFY_COLLECTED_CHANGE = 7;
    public static final int NOTIFY_ChOINESS_SELECTED = 8;
    public static final int NOTIFY_PERSONAL_INFO_CHANGE = 10;
    public static final int NOTIFY_SHARE_RESULT = 11;
    public static final int NOTIFY_LOGIN_RESULT = 12;
    public static final int NOTIFY_WECHAT_PAY_FINISH = 13;
    public static final int NOTIFY_WECHAT_PAY_ERROR = 14;
    public static final int NOTIFY_WECHAT_PAY_CANEL = 15;
    public static final int NOTIFY_GET_CITY = 16;
    public static final int NOTIFY_LOGIN_SUCCESS = 17;
    public static final int NOTIFY_WEIBO_SHARE_RESULT = 18;
    protected int mType;
    protected Object mData;

    public MessageEvent(int type) {
        mType = type;
    }

    public MessageEvent(int type, Object data) {
        mType = type;
        mData = data;
    }

    public int getType() {
        return mType;
    }

    public Object getData() {
        return mData;
    }
}
