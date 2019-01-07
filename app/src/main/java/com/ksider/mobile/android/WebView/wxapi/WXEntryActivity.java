package com.ksider.mobile.android.WebView.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.ksider.mobile.android.auth.Authorize;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.utils.MessageUtils;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        api = WXAPIFactory.createWXAPI(WXEntryActivity.this, Authorize.WX_APP_KEY, true);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        this.finish();
        SendAuth.Resp resp = new SendAuth.Resp(intent.getExtras());

        if (resp.transaction != null) {
            if (resp.transaction.startsWith("share")) {
                Intent i = new Intent();
                i.putExtra("errorCode", resp.errCode);
                MessageUtils.post(MessageUtils.NOTIFY_SHARE_RESULT, new MessageEvent(MessageEvent.NOTIFY_SHARE_RESULT, i.getExtras()));
            } else if (resp.transaction.startsWith("login")) {
                MessageUtils.post(MessageUtils.NOTIFY_LOGIN_RESULT, new MessageEvent(MessageEvent.NOTIFY_LOGIN_RESULT, intent.getExtras()));
            }
        }

    }


    @Override
    public void onReq(BaseReq baseReq) {
        Log.i("AAA", "request->code=" + baseReq.toString());
        return;
    }

    @Override
    public void onResp(BaseResp resp) {
        String result = "";

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = "share/login success";
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = "share/login canceled";
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = "share/login denied";
                break;
            default:
                result = "share/login share";
                break;
        }

//        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        this.finish();
    }
}
