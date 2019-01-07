package com.ksider.mobile.android.WebView.wxapi;


import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.auth.Authorize;


import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.utils.MessageUtils;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{
	

    private IWXAPI mWxApi;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		mWxApi = WXAPIFactory.createWXAPI(this, Authorize.WX_APP_KEY);
		mWxApi.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		mWxApi.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {

		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			if(resp.errCode == 0){
				MessageUtils.eventBus.postSticky(new MessageEvent(MessageEvent.NOTIFY_WECHAT_PAY_FINISH));
			}else if(resp.errCode == -1){
				MessageUtils.eventBus.postSticky(new MessageEvent(MessageEvent.NOTIFY_WECHAT_PAY_ERROR));
			}else if(resp.errCode == -2){
				MessageUtils.eventBus.postSticky(new MessageEvent(MessageEvent.NOTIFY_WECHAT_PAY_CANEL));
			}
		}
		this.finish();
	}
}