package com.ksider.mobile.android.WebView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.*;
import android.widget.TextView;
import android.widget.Toast;
import com.ksider.mobile.android.auth.Authorize;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.view.LoadImageView;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.modelbase.BaseResp;
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
 * Created by wenkui on 3/20/15.
 */
public class WebViewLandingActivity extends BaseActivity {
    protected IWeiboShareAPI mWeiboShareAPI;
    protected Tencent mTencent;
    protected IWXAPI mWXapi;
    protected BroadcastReceiver mReceiver;
    protected String mTitle;
    protected String mUrl;
    protected String mFirstImage = null;
    protected WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_landing);
        new SlidingLayout(this);
        customActionBar();
        if (getIntent() != null) {
            mWebView = (WebView) findViewById(R.id.webview);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setLoadWithOverviewMode(true);
            mWebView.getSettings().setUseWideViewPort(true);
            mWebView.getSettings().setUserAgentString(getUserAgent());
            mWebView.clearCache(false);

            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onLoadResource(WebView view, String url) {
                    if (mFirstImage == null) {
                        if (url.indexOf(".jpg") > 0 || url.indexOf(".png") > 0) {
                            mFirstImage = url;
                            LoadImageView imageView = (LoadImageView) findViewById(R.id.header_image);
                            imageView.setImageResource(mFirstImage);
                        }
                    }
                }

                /**
                 *
                 * @param view
                 * @param url
                 * sinaweibo -> url=sinaweibo://u/2769332963
                 * weichat   -> url=weixin://dl/profile/a108tian
                 * mail      -> url=mailto:merchant@108tian.com
                 * qq        -> url=mqq://user/574963782
                 * @return
                 */
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.v("AAA", "OverrideUrlLoading->url=" + url);
                    if (url.startsWith("http")) {
                        view.loadUrl(url);
                    } else {
                        try {
                            startActivity(
                                    new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, final String url) {
                    return;
                }

            });
            mWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onReceivedTitle(WebView view, String sTitle) {
                    super.onReceivedTitle(view, sTitle);
                    mTitle = sTitle;
                    TextView title = (TextView) findViewById(R.id.list_title);
                    if (sTitle != null && sTitle.length() > 0) {
                        title.setText(sTitle);
                    } else {
                        title.setText("");
                    }
                }
            });
            String data = getIntent().getExtras().getString("data");
            if (data != null) {
                mWebView.loadData(HtmlWraper.getHtmlDoc(data), "text/html; charset=UTF-8", null);
            } else {

                mUrl = getIntent().getExtras().getString("url");
                Log.i("AAA", "WebViewLandingActivity->url=" + mUrl);
                loadUrl();
            }
        }
        try {
            if (getIntent().getBooleanExtra("share", true)) {
                initShare();
            }
        } catch (NullPointerException npe) {
            initShare();
            npe.printStackTrace();
        }
        findViewById(R.id.list_backbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        MessageUtils.register(MessageUtils.NOTIFY_SHARE_RESULT, this);
        MessageUtils.register(MessageUtils.NOTIFY_WEIBO_SHARE_RESULT, this);
    }

    public static String getUserAgent() {
        return "ksider/" + DeviceUuid.getVersion() + " (Android; Android " + Build.VERSION.RELEASE + "; " + Build.BRAND + " " + android.os.Build.MODEL + ")";
    }

    public void loadUrl() {
        String target = mUrl;
        if (target != null) {
            target = target.indexOf("?") > 0 ? target + "&" : target + "?";
            if (target.contains("108tian.com")) {
                target += "f=android&version=" + DeviceUuid.getVersion() + "&cityId=" + Storage.getSharedPref().getInt("cityId", 1);
                String sessions = Storage.sharedPref.getString(Storage.SESSION_ID, null);
                if (sessions != null) {
                    CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(mWebView.getContext());
                    cookieSyncManager.startSync();
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookieManager.removeSessionCookie();
                    cookieManager.setAcceptCookie(true);
                    cookieManager.setCookie(target, "sid=" + sessions + "; domain=108tian.com; httpOnly ");
                    cookieSyncManager.sync();
                    cookieSyncManager.stopSync();
                }
            }
            mWebView.loadUrl(target + "&random=" + System.currentTimeMillis());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Storage.getSharedPref().getBoolean("refreshWebView", false)) {
            loadUrl();
            Storage.putBoolean("refreshWebView", false);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (getIntent().getBooleanExtra("go_home", false)) {
                Intent mainIntent = new Intent(WebViewLandingActivity.this, HomeActivity.class);
                WebViewLandingActivity.this.startActivity(mainIntent);
                WebViewLandingActivity.this.finish();
                return;
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    protected void initShare() {
        findViewById(R.id.share_icon).setVisibility(View.VISIBLE);
        final Context _this = this;
        findViewById(R.id.share_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mWeiboShareAPI == null) {
                    mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(_this, Authorize.WB_APP_KEY);
                    if (mWeiboShareAPI.isWeiboAppInstalled()) {
                        mWeiboShareAPI.registerApp();
                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction("com.sina.weibo.sdk.action.ACTION_SDK_REQ_ACTIVITY");
                        mReceiver = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {

                            }
                        };
                        registerReceiver(mReceiver, intentFilter);
                    }
                }
                if (mWXapi == null) {
                    mWXapi = WXAPIFactory.createWXAPI(WebViewLandingActivity.this, Authorize.WX_APP_KEY, true);
                    if (mWXapi.isWXAppInstalled()) {
                        mWXapi.registerApp(Authorize.WX_APP_KEY);
                    }
                }
                ShareDialog.Builder shareDialog = new ShareDialog.Builder(WebViewLandingActivity.this);
                shareDialog.setShareToFriend(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareToWeixin(true);
                    }
                }).setShareToWeixin(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareToWeixin(false);
                    }
                }).setShareToWeibo(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareToWeibo();
                    }
                }).setShareToQQ(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareToZone();
                    }
                }).show();
            }
        });
    }

    protected void shareToWeixin(Boolean friend) {
        if (mWXapi == null || !mWXapi.isWXAppInstalled()) {
            Toast.makeText(this, "未安装微信！", Toast.LENGTH_LONG).show();
            return;
        }
        if (mWXapi.getWXAppSupportAPI() < 0x21020001)
            friend = false;
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = getShareUrl(friend ? "weixin_timeline" : "weixin");
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = getDetailTitle();
        msg.description = "下载108天APP,收获百分百的周末！";
        if (mFirstImage != null) {
            Bitmap bit = getHeaderImage();
            if (bit != null) {
                msg.thumbData = ImageUtils.bmpToByteArray(bit, true);
            }
        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "share" + System.currentTimeMillis();
        req.message = msg;
        req.scene = friend ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        mWXapi.sendReq(req);
    }

    protected void shareToZone() {
        if (mTencent == null) {
            mTencent = Tencent.createInstance(Authorize.QQ_APP_KEY, WebViewLandingActivity.this.getApplicationContext());
        }
        Bundle bundle = new Bundle();
        bundle.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        bundle.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, getShareUrl("qq"));
        bundle.putString(QzoneShare.SHARE_TO_QQ_TITLE, getDetailTitle());
        bundle.putString(QzoneShare.SHARE_TO_QQ_IMAGE_URL, ImageUtils.formatImageUrl(mFirstImage, ImageUtils.MOBILE));
        bundle.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, "推荐，" + getDetailTitle());
        final Context activity = this;
        mTencent.shareToQQ((Activity) this, bundle, new IUiListener() {
            @Override
            public void onCancel() {
//                Toast.makeText(activity, "取消分享", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onComplete(Object view) {
//                Toast.makeText(activity, "分享成功", Toast.LENGTH_LONG).show();
                Log.i("AAA", "QQ success!");
                handleShareResult(Constants.SHARE_RESULT_SUCCESS, Constants.SHARE_TYPE_QQ);
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
            textObject.text = "#108天周边游#" + getDetailTitle() + getShareUrl("weibo");
            weiboMessage.textObject = textObject;
            if (mFirstImage != null) {
                Bitmap image = getHeaderImage();
                if (image != null) {
                    ImageObject imageObject = new ImageObject();
                    imageObject.setImageObject(image);
                    weiboMessage.imageObject = imageObject;
                }
            }
            SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
            request.transaction = String.valueOf(System.currentTimeMillis());
            request.multiMessage = weiboMessage;
            mWeiboShareAPI.sendRequest(request);
        } else {
            Toast.makeText(this, "未安装微博", Toast.LENGTH_LONG).show();
        }
    }

    protected Bitmap getHeaderImage() {
        LoadImageView image = (LoadImageView) findViewById(R.id.header_image);
        image.buildDrawingCache(true);
        Bitmap bit = image.getDrawingCache(true);
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
        return bit;
    }

    protected String getDetailTitle() {
        if (mTitle != null) {
            return mTitle;
        }
        return "";
    }

    protected String getShareUrl(String source) {
        String url = mUrl;
        if (url != null) {
            url = url.indexOf("?") >= 0 ? url + "&" : url + "?";
            url += "hmmd=androidApp_" + DeviceUuid.getVersion() + "&hmsr=" + source;
        }
        return url;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != mTencent)
            mTencent.onActivityResult(requestCode, resultCode, data);
    }

    public void handleShareResult(String shareResult, String shareType) {
        Log.i("AAA", "load JS");
        mWebView.loadUrl("javascript:window.T.Events.emit('Share:" + shareResult + "','" + shareType + "')");
    }

    public void onEventMainThread(MessageEvent event) {
        switch (event.getType()) {
            //weixin share callback
            case MessageEvent.NOTIFY_SHARE_RESULT:
                Bundle bundle = (Bundle) event.getData();
                int errorCode = bundle.getInt("errorCode");
                switch (errorCode) {
                    case BaseResp.ErrCode.ERR_OK:
                        handleShareResult(Constants.SHARE_RESULT_SUCCESS, Constants.SHARE_TYPE_WEIXIN);
                        Log.i("AAA", "weixin share success");
                        break;
                    case BaseResp.ErrCode.ERR_USER_CANCEL:
                        Log.i("AAA", "weixin share cancel");
                        break;
                    case BaseResp.ErrCode.ERR_AUTH_DENIED:
                        Log.i("AAA", "weixin share denied");
                        break;
                    default:
                        break;
                }
                break;
            //weibo share callback
            case MessageEvent.NOTIFY_WEIBO_SHARE_RESULT:
                Bundle weiboBundle = (Bundle) event.getData();
                int weiboErrorCode = weiboBundle.getInt("errorCode");
                switch (weiboErrorCode) {
                    case WBConstants.ErrorCode.ERR_OK:
                        handleShareResult(Constants.SHARE_RESULT_SUCCESS, Constants.SHARE_TYPE_WEIBO);
                        Log.i("AAA", "weibo share success");
                        break;
                    case WBConstants.ErrorCode.ERR_CANCEL:
                        Log.i("AAA", "weibo share cancel");
                        break;
                    case WBConstants.ErrorCode.ERR_FAIL:
                        Log.i("AAA", "weibo share fail");
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        mWebView.destroy();
        MessageUtils.unregister(MessageUtils.NOTIFY_SHARE_RESULT, this);
        MessageUtils.unregister(MessageUtils.NOTIFY_WEIBO_SHARE_RESULT, this);
    }
}

