package com.ksider.mobile.android.WebView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import com.ksider.mobile.android.auth.AccessTokenKeeper;
import com.ksider.mobile.android.auth.Authorize;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.MessageUtils;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.LogoutAPI;
import com.tencent.tauth.Tencent;
import org.json.JSONException;
import org.json.JSONObject;

public class SettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        new SlidingLayout(this);
        customActionBar("设置");
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.logout:
                        logout();
                        break;
                    case R.id.contact:
                        Intent contact = new Intent(SettingActivity.this, WebViewLandingActivity.class);
                        contact.putExtra("url", Constants.CONTACT_METHOD);
                        contact.putExtra("share", false);
                        startActivity(contact);
                        break;
                    case R.id.protocol:
                        Intent protocol = new Intent(SettingActivity.this, WebViewLandingActivity.class);
                        protocol.putExtra("url", Constants.CONSUMER_PROTOCOL);
                        protocol.putExtra("share", false);
                        startActivity(protocol);
                        break;
                    case R.id.feedback:
                        Intent feedback = new Intent(SettingActivity.this, FeedBackActivity.class);
                        startActivity(feedback);
                        break;
                    case R.id.review:
                        try {
                            Intent review = new Intent(Intent.ACTION_VIEW);
                            review.setData(Uri.parse("market://details?id=com.ksider.mobile.android.WebView"));
                            startActivity(review);
                        } catch (Exception e) {

                        }
                        break;
                    default:
                        break;
                }
            }
        };
        View view = findViewById(R.id.logout);
        view.setOnClickListener(listener);
        view = findViewById(R.id.contact);
        view.setOnClickListener(listener);
        view = findViewById(R.id.protocol);
        view.setOnClickListener(listener);
        view = findViewById(R.id.feedback);
        view.setOnClickListener(listener);
        view = findViewById(R.id.review);
        view.setOnClickListener(listener);
    }

    protected void logout() {
        Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(this);
        if ("weibo".equals(token.getRefreshToken())) {
            LogoutAPI api = new LogoutAPI(token);
            api.logout(new RequestListener() {
                @Override
                public void onWeiboException(WeiboException e) {

                }

                @Override
                public void onComplete(String resonse) {
                    if (!TextUtils.isEmpty(resonse)) {
                        try {
                            JSONObject json = new JSONObject(resonse);
                            if (json.isNull("error")) {
                                String value = json.getString("result");
                                if ("true".equalsIgnoreCase(value)) {
                                    AccessTokenKeeper.clear(SettingActivity.this);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else if ("qq".equals(token.getRefreshToken())) {
            Tencent mt = Tencent.createInstance(Authorize.QQ_APP_KEY, this.getApplicationContext());
            mt.logout(this);
            AccessTokenKeeper.clear(SettingActivity.this);
        }
        MessageUtils.eventBus.post(new MessageEvent(MessageEvent.NOTIFY_LOGOUT));
        SettingActivity.this.finish();
    }

    protected void onBack() {
        super.onBackPressed();
    }
}
