/*
 * Copyright (C) 2010-2013 The SINA WEIBO Open Source Project
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

package com.ksider.mobile.android.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.ksider.mobile.android.utils.UnionOauth2AccessToken;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

/**
 * 该类定义了微博授权时所需要的参数。
 *
 * @author SINA
 * @since 2013-10-07
 */
public class AccessTokenKeeper {
    private static final String PREFERENCES_NAME = "com_weibo_sdk_android";

    private static final String KEY_UID = "uid";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_EXPIRES_IN = "expires_in";
    private static final String LOGIN_TYPE = "login_type";
    private static final String IS_LOGIN = "is_login";
    private static final String UNION_ID = "unionid";

    /**
     * 保存 Token 对象到 SharedPreferences。
     *
     * @param context 应用程序上下文环境
     * @param token   Token 对象
     */
    public static void writeAccessToken(Context context, Oauth2AccessToken token) {
        writeAccessToken(context, token, "");
    }

    public static void writeAccessToken(Context context, UnionOauth2AccessToken token) {
        writeAccessToken(context, token, token.getUnionid());
    }

    /**
     * 保存Token和unionid到 SharedPreferences。
     *
     * @param context
     * @param token
     * @param unionId
     */
    public static void writeAccessToken(Context context, Oauth2AccessToken token, String unionId) {
        if (null == context || null == token) {
            return;
        }

        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.putString(KEY_UID, token.getUid());
        editor.putString(KEY_ACCESS_TOKEN, token.getToken());
        editor.putLong(KEY_EXPIRES_IN, token.getExpiresTime());
        editor.putString(LOGIN_TYPE, token.getRefreshToken());
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(UNION_ID, unionId);
        editor.commit();
    }

//    /**
//     * 从 SharedPreferences 读取 Token 信息。
//     *
//     * @param context 应用程序上下文环境
//     * @return 返回 Token 对象
//     */
//    public static Oauth2AccessToken readAccessToken(Context context) {
//        if (null == context) {
//            return null;
//        }
//
//        Oauth2AccessToken token = new Oauth2AccessToken();
//        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
//        token.setUid(pref.getString(KEY_UID, ""));
//        token.setToken(pref.getString(KEY_ACCESS_TOKEN, ""));
//        token.setExpiresTime(pref.getLong(KEY_EXPIRES_IN, 0));
//        token.setRefreshToken(pref.getString(LOGIN_TYPE, null));
//        return token;
//    }

    public static UnionOauth2AccessToken readAccessToken(Context context) {
        if (null == context) {
            return null;
        }

        UnionOauth2AccessToken token = new UnionOauth2AccessToken();
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        token.setUid(pref.getString(KEY_UID, ""));
        token.setToken(pref.getString(KEY_ACCESS_TOKEN, ""));
        token.setExpiresTime(pref.getLong(KEY_EXPIRES_IN, 0));
        token.setRefreshToken(pref.getString(LOGIN_TYPE, null));
        token.setUnionid(pref.getString(UNION_ID, ""));
        return token;
    }
//    /**
//     * 从 SharedPreferences 读取unionId
//     *
//     * @param context
//     * @return 返回unionId
//     */
//    public static String readUnionId(Context context) {
//        if (null == context) {
//            return "";
//        }
//        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
//        return pref.getString(UNION_ID, "");
//    }


    /**
     * 清空 SharedPreferences 中 Token信息。
     *
     * @param context 应用程序上下文环境
     */
    public static void clear(Context context) {
        if (null == context) {
            return;
        }

        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
}
