package com.ksider.mobile.android.activity.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.auth.AccessTokenKeeper;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.utils.*;
import com.umeng.analytics.MobclickAgent;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeFramment extends BaseFragment {
    private View mRoot;
    private Boolean mLoginState = false;

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("meFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("meFragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MessageUtils.eventBus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MessageUtils.eventBus.unregister(this);
    }

    public void onEventMainThread(MessageEvent event) {
        switch (event.getType()) {
            case MessageEvent.NOTIFY_LOGIN:
                mLoginState = true;
                if (event.getData() == null) {
                    notifyLogin();
                } else {
                    switchContent();
                }
                break;
            case MessageEvent.NOTIFY_LOGOUT:
                mLoginState = false;
                notifyLogout();
                switchContent();
                break;

            default:
                break;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    protected void switchContent() {
        if (getActivity() == null) {
            return;
        }
        MessageUtils.eventBus.postSticky(new MessageEvent(MessageEvent.NOTIFY_COLLECTED_CHANGE));
        Fragment personal = Fragment.instantiate(getActivity(), PersonalInfoFragment.class.getName());
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_content, personal, "personal").commitAllowingStateLoss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_base, container, false);
        initView();
        return mRoot;
    }

    public void initView() {
        mLoginState = UserInfo.isLogin();
        switchContent();
    }

    protected void notifyLogout() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "logout");
        String url = APIUtils.getUserCenter(params);
        JsonObjectRequest req = new JsonObjectRequest(url, null, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(Constants.LOG_TAG, error.toString());
            }
        });
        Network.getInstance().addToRequestQueue(req);
        Storage.remove(Storage.USER_INFO);
        Storage.remove(Storage.SESSION_ID);
        APIUtils.clearSession();
    }

    protected void notifyLogin() {
        UnionOauth2AccessToken token = AccessTokenKeeper.readAccessToken(getActivity());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("access_token", token.getToken());
        params.put("expires_in", token.getExpiresTime());
        params.put("uid", token.getUid());
        params.put("type", token.getRefreshToken());
        params.put("unionid", token.getUnionid());
        String url = APIUtils.getUrl(APIUtils.AUTHORIZE, params);
        JsonObjectRequest req = new JsonObjectRequest(url, null, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getInt("status") == 0) {
                        Storage.putBoolean("refreshWebView", true);
                        UserInfo.store(response.getJSONObject("data"));
                        switchContent();
                        MessageUtils.postSticky(MessageUtils.NOTIFY_LOGIN_SUCCESS,new MessageEvent(MessageEvent.NOTIFY_LOGIN_SUCCESS));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(Constants.LOG_TAG, error.toString());
            }
        });
        Network.getInstance().addToRequestQueue(req);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Fragment login = getChildFragmentManager().findFragmentByTag("login");
//        if (login != null) {
//            login.onActivityResult(requestCode, resultCode, data);
//        }
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
