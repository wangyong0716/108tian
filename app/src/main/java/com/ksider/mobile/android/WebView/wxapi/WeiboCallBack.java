package com.ksider.mobile.android.WebView.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.ksider.mobile.android.auth.Authorize;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.utils.MessageUtils;
import com.sina.weibo.sdk.api.share.*;
import com.sina.weibo.sdk.constant.WBConstants;
import com.tencent.mm.sdk.modelmsg.SendAuth;

/**
 * Created by yong on 10/31/15.
 */
public class WeiboCallBack extends Activity implements IWeiboHandler.Response, IWeiboHandler.Request {
    private IWeiboShareAPI mShareWeiboAPI = null;
    private BaseRequest mBaseRequest = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShareWeiboAPI = WeiboShareSDK.createWeiboAPI(this, Authorize.WB_APP_KEY);

//        mShareWeiboAPI.handleWeiboRequest(getIntent(), this);
        mShareWeiboAPI.handleWeiboResponse(getIntent(), this);
        handleIntent(getIntent());
        Log.i("AAA", "weiboCallBack->create");
    }

    /**
     * @see {@link Activity#onNewIntent}
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

//        mShareWeiboAPI.handleWeiboRequest(intent, this);
        mShareWeiboAPI.handleWeiboResponse(getIntent(), this);
        handleIntent(intent);
        Log.i("AAA", "weiboCallBack->onNewIntent");
    }

    private void handleIntent(Intent intent) {
        this.finish();
        SendAuth.Resp resp = new SendAuth.Resp(intent.getExtras());
        Intent i = new Intent();
        i.putExtra("errorCode", resp.errCode);
        MessageUtils.post(MessageUtils.NOTIFY_WEIBO_SHARE_RESULT, new MessageEvent(MessageEvent.NOTIFY_WEIBO_SHARE_RESULT, i.getExtras()));
    }

    /**
     * 接收微客户端博请求的数据。
     * 当微博客户端唤起当前应用并进行分享时，该方法被调用。
     *
     * @param baseRequest 微博请求数据对象
     * @see {@link IWeiboShareAPI#handleWeiboRequest}
     */
    @Override
    public void onRequest(BaseRequest baseRequest) {
        // 保存从微博客户端唤起第三方应用时，客户端发送过来的请求数据对象
        mBaseRequest = baseRequest;
        Log.i("AAA", "omRequest");
        Toast.makeText(this, "onrequest", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResponse(BaseResponse res) {
        Log.i("AAA", "weiboCallBack->errCode=" + res.errCode);
        switch (res.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
//                Toast.makeText(this, "分享成功", Toast.LENGTH_LONG).show();
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
//                Toast.makeText(this, "取消分享", Toast.LENGTH_LONG).show();
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
//                Toast.makeText(this, "分享失败: " + res.errMsg, Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }

    }

}
