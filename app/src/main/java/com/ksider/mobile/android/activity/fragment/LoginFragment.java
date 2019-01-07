package com.ksider.mobile.android.activity.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.WebView.SignupActivity;
import com.ksider.mobile.android.activity.fragment.signup.NetworkFragment;
import com.ksider.mobile.android.auth.AccessTokenKeeper;
import com.ksider.mobile.android.auth.Authorize;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.utils.*;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginFragment extends NetworkFragment {
    protected View mRoot;
    protected SsoHandler mWeiboHander;
    protected Tencent mTencent;
    protected Boolean mThird = false;
    protected IWXAPI mWXapi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.login_fragment, container, false);
        initLogin();
        MessageUtils.register(MessageUtils.NOTIFY_LOGIN_RESULT, this);
        MessageUtils.register(MessageUtils.NOTIFY_LOGIN_SUCCESS, this);
        return mRoot;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mThird = getArguments().getBoolean("third");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MessageUtils.unregister(MessageUtils.NOTIFY_LOGIN_RESULT, this);
        MessageUtils.unregister(MessageUtils.NOTIFY_LOGIN_SUCCESS, this);
    }

    public void onEventMainThread(MessageEvent event) {
        if (event.getType() == MessageEvent.NOTIFY_LOGIN_RESULT) {
            SendAuth.Resp resp = new SendAuth.Resp((Bundle) event.getData());
            if (resp != null) {
                String errStr = "";
                switch (resp.errCode) {
                    case BaseResp.ErrCode.ERR_AUTH_DENIED:
                        errStr = "认证被否决";
                        break;
                    case BaseResp.ErrCode.ERR_OK:
                        errStr = "成功";
                        getWeChatToken(resp.code);
                        return;
                    case BaseResp.ErrCode.ERR_COMM:
                        errStr = "未知错误";
                        break;
                    case BaseResp.ErrCode.ERR_SENT_FAILED:
                        errStr = "发送失败";
                        break;
                    case BaseResp.ErrCode.ERR_UNSUPPORT:
                        errStr = "不支持错误";
                        break;
                    case BaseResp.ErrCode.ERR_USER_CANCEL:
                        errStr = "用户取消";
                        break;
                }
            }
        } else if (event.getType() == MessageEvent.NOTIFY_LOGIN_SUCCESS) {
            getActivity().finish();
        }
    }

    protected void getWeChatToken(String code) {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?secret=" + Authorize.WX_APP_SECRET + "&grant_type=authorization_code&appid="
                + Authorize.WX_APP_KEY + "&code=" + code;
        JsonObjectRequest request = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject res) {
//                Oauth2AccessToken token = new Oauth2AccessToken();
//                try {
//                    token.setExpiresTime(res.getLong("expires_in"));
//                    token.setUid(res.getString("openid"));
//                    token.setToken(res.getString("access_token"));
//                    token.setRefreshToken("weixin");
//                    String unionId;
//                    try {
//                        unionId = res.getString("unionid");
//                    } catch (JSONException js) {
//                        unionId = "";
//                    }
//                    AccessTokenKeeper.writeAccessToken(getActivity(), token, unionId);
                UnionOauth2AccessToken token = new UnionOauth2AccessToken();
                try {
                    token.setExpiresTime(res.getLong("expires_in"));
                    token.setUid(res.getString("openid"));
                    token.setToken(res.getString("access_token"));
                    token.setRefreshToken("weixin");
                    try {
                        token.setUnionid(res.getString("unionid"));
                    } catch (JSONException js) {
                        js.printStackTrace();
                    }

                    AccessTokenKeeper.writeAccessToken(getActivity(), token);
                    MessageUtils.eventBus.postSticky(new MessageEvent(MessageEvent.NOTIFY_LOGIN));
                    if (mThird) {
//                        getActivity().finish();
//                        MessageUtils.eventBus.postSticky(new MessageEvent(MessageEvent.NOTIFY_LOGIN_SUCCESS));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        Network.getInstance().addToRequestQueue(request);
    }

    protected void initLogin() {
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                switch (view.getId()) {
                    case R.id.weibo_button:
                        weiboAuth();
                        break;
                    case R.id.qq_button:
                        createQQAuth();
                        break;
                    case R.id.wechat_button:
                        wechatAuth();
                        break;
                    case R.id.signup_button:
                    case R.id.forget_button:
                        intent = new Intent(getActivity(), SignupActivity.class);
                        intent.putExtra("reset", R.id.forget_button == view.getId());
                        startActivityForResult(intent, 1357);
                        break;
                    default:
                        break;
                }
            }
        };
        View view = mRoot.findViewById(R.id.weibo_button);
        view.setOnClickListener(listener);
        view = mRoot.findViewById(R.id.qq_button);
        view.setOnClickListener(listener);
        mRoot.findViewById(R.id.signup_button).setOnClickListener(listener);
        mRoot.findViewById(R.id.forget_button).setOnClickListener(listener);
        mRoot.findViewById(R.id.wechat_button).setOnClickListener(listener);
        // login
        final EditText phone_edit = (EditText) mRoot.findViewById(R.id.phone_edit);
        phone_edit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                phone_edit.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        final EditText password_edit = (EditText) mRoot.findViewById(R.id.password_edit);
        password_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                password_edit.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Button login_button = (Button) mRoot.findViewById(R.id.login_button);
        login_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!StringUtils.checkMobilePhone(phone_edit.getText().toString())) {
                    StringUtils.setError(phone_edit, "电话格式不合法");
                    return;
                }
                if (password_edit.getText() == null || password_edit.getText().toString().length() == 0) {
                    StringUtils.setError(password_edit, "密码不能为空！");
                    return;
                }
                sendLogin(phone_edit.getText().toString(), password_edit.getText().toString());
            }
        });
    }

    public void sendLogin(String phone, String password) {
        Network.getInstance().addToRequestQueue(getRequest(APIUtils.login(phone, password)));
    }

    @Override
    public void proccess(JSONObject response) {
        try {
            if (response != null && response.getInt("status") == 0) {
                Storage.putBoolean("refreshWebView", true);
                UserInfo.store(response.getJSONObject("data"));
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

                if (mThird) {
                    getActivity().finish();
                }
            } else {
                setError(response.getString("msg"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void createQQAuth() {
        mTencent = Tencent.createInstance(Authorize.QQ_APP_KEY, getActivity());
        if (!mTencent.isSessionValid()) {
            mTencent.login(getActivity(), Authorize.WB_SCOPE, new IUiListener() {
                @Override
                public void onCancel() {

                }

                @Override
                public void onComplete(Object response) {
                    Oauth2AccessToken token = new Oauth2AccessToken();
                    JSONObject res = (JSONObject) response;
                    try {
                        token.setExpiresTime(res.getLong("expires_in"));
                        token.setUid(res.getString("openid"));
                        token.setToken(res.getString("access_token"));
                        token.setRefreshToken("qq");
                        AccessTokenKeeper.writeAccessToken(getActivity(), token); // 保存Token
                        MessageUtils.eventBus.post(new MessageEvent(MessageEvent.NOTIFY_LOGIN));
//                        getActivity().finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(UiError response) {
                    Log.v(Constants.LOG_TAG, "onError:" + response.toString());
                }
            });
        }
    }

    protected void weiboAuth() {
        WeiboAuth auth = new WeiboAuth(getActivity(), Authorize.WB_APP_KEY, Authorize.wB_REDIRECT_URL, Authorize.WB_SCOPE);
        mWeiboHander = new SsoHandler(getActivity(), auth);
        mWeiboHander.authorize(new WeiboAuthListener() {
            @Override
            public void onCancel() {
                Log.v("AAA", "onCancel");
                Log.v(Constants.LOG_TAG, "onCancel:");
            }

            @Override
            public void onComplete(Bundle values) {
                Log.v("AAA", "onComplete");
                proccessToken(values);
            }

            @Override
            public void onWeiboException(WeiboException e) {
                Log.v("AAA", "onWeiboException");
                e.printStackTrace();
                Log.v(Constants.LOG_TAG, "code:" + e.toString());
            }
        });
    }

    protected void wechatAuth() {
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test";
        req.transaction = "login" + System.currentTimeMillis();
        mWXapi = WXAPIFactory.createWXAPI(getActivity(), Authorize.WX_APP_KEY, true);
        if (!mWXapi.isWXAppInstalled()) {
            Toast.makeText(getActivity(), R.string.wechat_not_install, Toast.LENGTH_LONG).show();
        }
        mWXapi.registerApp(Authorize.WX_APP_KEY);
        mWXapi.sendReq(req);
    }

    public void proccessToken(Bundle values) {
        Oauth2AccessToken aToken = Oauth2AccessToken.parseAccessToken(values);
        if (aToken.isSessionValid()) {
            aToken.setRefreshToken("weibo");
            AccessTokenKeeper.writeAccessToken(getActivity(), aToken); //保存Token
            MessageUtils.eventBus.postSticky(new MessageEvent(MessageEvent.NOTIFY_LOGIN));
            if (mThird) {
//                getActivity().finish();
            }
        } else {
            // 当您注册的应用程序签名不正确时,就会收到错误Code,请确保签名正确
            String code = values.getString("code", "");
            Log.v(Constants.LOG_TAG, "code:" + code);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 32973) {
            if (mWeiboHander != null && data != null) {
                mWeiboHander.authorizeCallBack(requestCode, resultCode, data);
                proccessToken(data.getExtras());
            }
        } else if (requestCode == 1357 && resultCode == 1359) {
            getActivity().finish();
        }
    }
}
