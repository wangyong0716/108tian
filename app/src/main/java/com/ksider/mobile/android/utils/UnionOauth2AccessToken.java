package com.ksider.mobile.android.utils;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;

/**
 * Created by yong on 9/6/15.
 */
public class UnionOauth2AccessToken extends Oauth2AccessToken {
    private static final String KEY_UNION_ID = "unionid";

    private String mUnionid = "";

    public String getUnionid() {
        return mUnionid;
    }

    public void setUnionid(String mUnionid) {
        this.mUnionid = mUnionid;
    }
}
