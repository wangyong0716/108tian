package com.ksider.mobile.android.partion;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.auth.Authorize;
import com.ksider.mobile.android.utils.BasicCategory;
import com.ksider.mobile.android.utils.DeviceUuid;
import com.ksider.mobile.android.utils.ImageUtils;
import com.ksider.mobile.android.view.LoadImageView;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.io.ByteArrayOutputStream;

/**
 * Created by yong on 2015/6/2.
 */
public class ShareView extends LinearLayout {
    private Context context;
    protected IWeiboShareAPI mWeiboShareAPI;
    protected Tencent mTencent;
    protected IWXAPI mWXapi;
    protected BroadcastReceiver mReceiver;
    private String mTitle;
    protected BasicCategory mCategory;
    private String id;
    private String imgUrl;
//    private Bitmap bitmap;

    public ShareView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ShareView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public ShareView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.partion_share_view, this);
        initShare();
    }

    public void initShare() {
        if (mWeiboShareAPI == null) {
            mWeiboShareAPI = WeiboShareSDK.createWeiboAPI((Activity) context, Authorize.WB_APP_KEY);
            if (mWeiboShareAPI.isWeiboAppInstalled()) {
                mWeiboShareAPI.registerApp();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("com.sina.weibo.sdk.action.ACTION_SDK_REQ_ACTIVITY");
//                mReceiver = new BroadcastReceiver() {
//                    @Override
//                    public void onReceive(Context context, Intent intent) {
//
//                    }
//                };
//                context.registerReceiver(mReceiver, intentFilter);
            }
        }
        if (mWXapi == null) {
            mWXapi = WXAPIFactory.createWXAPI((Activity) context, Authorize.WX_APP_KEY, true);
            if (mWXapi.isWXAppInstalled()) {
                mWXapi.registerApp(Authorize.WX_APP_KEY);
            }
        }
        findViewById(R.id.friends).setOnClickListener(listener);
        findViewById(R.id.wechat).setOnClickListener(listener);
        findViewById(R.id.weibo).setOnClickListener(listener);
        findViewById(R.id.qq).setOnClickListener(listener);
    }

    public void setValues(String title, BasicCategory category, String id, String imgUrl) {
        this.mTitle = title;
        this.mCategory = category;
        this.id = id;
        this.imgUrl = imgUrl;
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }

    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
    }

    public void hideTitle(boolean hide) {
        if (hide) {
            findViewById(R.id.share_title_layout).setVisibility(GONE);
        } else {
            findViewById(R.id.share_title_layout).setVisibility(VISIBLE);
        }
    }

    public Bitmap getHeaderImage() {
        LoadImageView image = (LoadImageView) ((Activity) context).findViewById(R.id.header_image);
        Bitmap bit = null;
        if (image != null) {
            image.setDrawingCacheEnabled(true);
            image.buildDrawingCache(true);
            bit = image.getDrawingCache(true);
            if (bit != null) {
                if (bit.getWidth() > 0 && bit.getHeight() > 0) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bit.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                    byte[] b = baos.toByteArray();
                    bit = BitmapFactory.decodeByteArray(b, 0, b.length);
                    if (b.length >= 32768) {
                        float scale = (float) (32768.0 / (b.length + 1));
                        Matrix matrix = new Matrix();
                        matrix.postScale(scale, scale);
                        bit = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
                    }
                } else {
                    bit = null;
                }
            }
        }
        return bit;
    }

    protected String getShareUrl(String source) {
        String url = null;
        if (mCategory != null) {
            url = Constants.HOME_BASE_URL;
            switch (mCategory) {
                case ATTRACTIONS:
                    url += "scene";
                    break;
                case FARMYARD:
                    url += "farm";
                    break;
                case PICKINGPART:
                    url += "pick";
                    break;
                case RESORT:
                    url += "resort";
                    break;
                case ACTIVITY:
                    url += "event";
                    break;
                case GUIDE:
                    url += "weekly";
                default:
                    break;
            }
            url += "/" + id + ".html?hmmd=androidApp_" +
                    DeviceUuid.getVersion() + "&hmsr=" + source;
        }
        return url;
    }

    protected void shareToWeixin(Boolean friend) {
        if (mWXapi == null || !mWXapi.isWXAppInstalled()) {
            Toast.makeText(context, "未安装微信！", Toast.LENGTH_LONG).show();
            return;
        }
        if (mWXapi.getWXAppSupportAPI() < 0x21020001)
            friend = false;
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = getShareUrl(friend ? "weixin_timeline" : "weixin");
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = mTitle;
        msg.description = "这里不错，" + mTitle
                + " 下载APP,收获百分百的周末！";
        Bitmap bit = getHeaderImage();
        if (bit == null) {
            bit = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
        }
        msg.thumbData = ImageUtils.bmpToByteArray(bit, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "108tian" + System.currentTimeMillis();
        req.message = msg;
        req.scene = friend ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        mWXapi.sendReq(req);
    }


    protected void shareToZone() {
        Bundle bundle = new Bundle();
        bundle.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        bundle.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, getShareUrl("qq"));
        bundle.putString(QzoneShare.SHARE_TO_QQ_TITLE, mTitle);
        bundle.putString(QzoneShare.SHARE_TO_QQ_IMAGE_URL, ImageUtils.formatImageUrl(imgUrl, ImageUtils.MOBILE));
        bundle.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, "这里不错，" + mTitle
                + "下载APP 收获百分百的周末 https://m.108tian.com/download?hmmd=androidApp_" +
                DeviceUuid.getVersion() + "&hmsr=qq");
        final Context activity = context;
        mTencent.shareToQQ((Activity) context, bundle, new IUiListener() {
            @Override
            public void onCancel() {
//                Toast.makeText(activity, "取消分享", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onComplete(Object view) {
//                Toast.makeText(activity, "分享成功", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(UiError error) {
//                Toast.makeText(activity, "分享错误:" + error.errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void shareToWeibo() {
        if (mWeiboShareAPI.isWeiboAppInstalled()) {
            WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
            TextObject textObject = new TextObject();
            textObject.text = "#108天周边游#这里不错，" + mTitle + getShareUrl("weibo") + " 下载APP https://m.108tian.com/download?hmmd=androidApp_" +
                    DeviceUuid.getVersion() + "&hmsr=weibo 收获100分周末";
            weiboMessage.textObject = textObject;
            Bitmap image = getHeaderImage();
            if (image != null) {
                ImageObject imageObject = new ImageObject();
                imageObject.setImageObject(image);
                weiboMessage.imageObject = imageObject;
            }
            SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
            request.transaction = String.valueOf(System.currentTimeMillis());
            request.multiMessage = weiboMessage;
            mWeiboShareAPI.sendRequest(request);
        } else {
            Toast.makeText(context, "未安装微博", Toast.LENGTH_LONG).show();
        }
    }

    private OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.friends:
                    shareToWeixin(true);
                    break;
                case R.id.wechat:
                    shareToWeixin(false);
                    break;
                case R.id.weibo:
                    shareToWeibo();
                    break;
                case R.id.qq:
                    if (mTencent == null) {
                        mTencent = Tencent.createInstance(Authorize.QQ_APP_KEY, context.getApplicationContext());
                    }
                    shareToZone();
                    break;
                default:
                    break;
            }
        }
    };
}
