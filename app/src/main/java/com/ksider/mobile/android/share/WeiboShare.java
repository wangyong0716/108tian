package com.ksider.mobile.android.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.auth.Authorize;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.*;
import com.sina.weibo.sdk.constant.WBConstants;

public class WeiboShare implements IShare {
    protected IWeiboShareAPI mWeiboShareAPI;
    protected IWeiboHandler.Response mResponse;
    private Context context;

    public WeiboShare(Activity context) {
        this.context = context;
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(context, Authorize.WB_APP_KEY);
        mWeiboShareAPI.registerApp();
        mResponse = new IWeiboHandler.Response() {
            @Override
            public void onResponse(BaseResponse baseResp) {
                Log.v(Constants.LOG_TAG, "BaseResponse:" + baseResp.toString());
                switch (baseResp.errCode) {
                    case WBConstants.ErrorCode.ERR_OK:
                        break;
                    case WBConstants.ErrorCode.ERR_CANCEL:
                        break;
                    case WBConstants.ErrorCode.ERR_FAIL:
                        break;
                    default:
                        break;
                }
            }
        };
        mWeiboShareAPI.handleWeiboResponse(context.getIntent(), mResponse);
    }

    @Override
    public Boolean share(String url, String desc, Bitmap thumb) {
        // WebpageObject mediaObject = new WebpageObject();
        // mediaObject.identify = Utility.generateGUID();
        // mediaObject.title = "108天周边游";
        // mediaObject.description = desc;
        // if(thumb != null){
        // mediaObject.setThumbImage(thumb);
        // }
        // mediaObject.actionUrl = url;
        // mediaObject.defaultText = "thumb";
        TextObject textObject = new TextObject();
        textObject.text = "测试 http://108tian.com";
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        weiboMessage.textObject = textObject;

        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;
        Boolean response = mWeiboShareAPI.sendRequest(request);
        Log.v(Constants.LOG_TAG, "response:" + response);
        return response;
    }

    @Override
    public void handleResponse(Intent intent) {
        mWeiboShareAPI.handleWeiboResponse(intent, mResponse);
    }

    @Override
    public ShareEntity getEntity() {
        return ShareEntity.WEIBO;
    }

}
