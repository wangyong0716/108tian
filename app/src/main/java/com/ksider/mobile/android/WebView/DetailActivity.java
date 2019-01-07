package com.ksider.mobile.android.WebView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.activity.fragment.*;
import com.ksider.mobile.android.auth.Authorize;
import com.ksider.mobile.android.model.BaseDataModel;
import com.ksider.mobile.android.model.DetailHeaderDataModel;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.model.ProductStockModel;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.view.LoadImageView;
import com.ksider.mobile.android.view.ResponsiveScrollView;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.umeng.analytics.MobclickAgent;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yong on 9/2/15.
 */
public class DetailActivity extends BaseActivity {
    protected String mEntity;
    protected BaseDataModel mParams;
    protected Boolean mHasRequest = false;
    protected BasicCategory mCategory;
    protected IWeiboShareAPI mWeiboShareAPI;
    protected Tencent mTencent;
    protected IWXAPI mWXapi;
    protected BroadcastReceiver mReceiver;

    protected boolean hasProduct = false;
    protected boolean refundAble = false;
    protected String phone = "";
    protected double price = -1;

    protected int dataVersion;
//    private ConsultView consult;

    protected ArrayList<ProductStockModel> stocks = new ArrayList<ProductStockModel>();
    private ProductStockModel selectedStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        new SlidingLayout(this);
        customActionBar();
        if (savedInstanceState == null) {
            Bundle args = getIntent().getExtras();
            mParams = Utils.retrieveArgData(getIntent());
            String category = args.getString("category");
            if (category != null) {
                if (category.equals(BasicCategory.ACTIVITY.toString())) {
                    mCategory = BasicCategory.ACTIVITY;
                    mEntity = APIUtils.ACTIVITY;
                } else if (category.equals(BasicCategory.ATTRACTIONS.toString())) {
                    mCategory = BasicCategory.ATTRACTIONS;
                    mEntity = APIUtils.POI_ATTRACTIONS;
                } else if (category.equals(BasicCategory.FARMYARD.toString())) {
                    mCategory = BasicCategory.FARMYARD;
                    mEntity = APIUtils.POI_FARMYARD;
                } else if (category.equals(BasicCategory.GUIDE.toString())) {
                    mCategory = BasicCategory.GUIDE;
                    mEntity = APIUtils.GUIDE;
                } else if (category.equals(BasicCategory.PICKINGPART.toString())) {
                    mCategory = BasicCategory.PICKINGPART;
                    mEntity = APIUtils.POI_PICK;
                } else if (category.equals(BasicCategory.RESORT.toString())) {
                    mCategory = BasicCategory.RESORT;
                    mEntity = APIUtils.POI_RESORT;
                }
            }
            if (mCategory == null) {
                this.finish();
            }
            initShare();
            initLoadingView();
        } else {
            mParams = (BaseDataModel) savedInstanceState.getSerializable("params");
            mCategory = (BasicCategory) savedInstanceState.getSerializable("category");
            mEntity = savedInstanceState.getString("entity");
            if (mCategory == null) {
                this.finish();
            }
            initShare();
            initLoadingView();
        }

        MessageUtils.register(MessageUtils.NOTIFY_SHARE_RESULT, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("params", mParams);
        outState.putSerializable("category", mCategory);
        outState.putString("entity", mEntity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (findViewById(R.id.price_toolbar) != null) {
            findViewById(R.id.price_toolbar).setBackgroundColor(getResources().getColor(R.color.divider_line_color));
        }
        if (!mHasRequest) {
            mHasRequest = true;
            try {
                Network.getInstance().addToRequestQueue(getRequest(mEntity), "detail");
            } catch (Exception e) {
                Log.v(Constants.LOG_TAG, e.toString());
                e.printStackTrace();
            }
        }
//        if (consult != null) {
//            consult.refreshConsult();
//        }
        MobclickAgent.onPageStart(mEntity + "_detail"); //统计页面
        MobclickAgent.onResume(this);
        initCollect();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mEntity + "_detail");
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        MessageUtils.unregister(MessageUtils.NOTIFY_SHARE_RESULT, this);
    }

    public void onEventMainThread(MessageEvent event) {
        switch (event.getType()) {
            case MessageEvent.NOTIFY_SHARE_RESULT:
                //weixin share callback
                Bundle bundle = (Bundle) event.getData();
                break;
            default:
                break;
        }
    }

    protected void initCollect() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!UserInfo.isLogin()) {
                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                    startActivity(intent);
                    return;
                }
                View collected = (View) findViewById(R.id.collected);
                if (collected.getVisibility() == View.VISIBLE) {
                    postFavorite(FavoriteActions.CANCEL_FAVORATOR);
                } else {
                    postFavorite(FavoriteActions.FAVORATOR);
                }
            }
        };
        View view = (View) findViewById(R.id.collect_area);
        view.setOnClickListener(listener);
    }

    protected void postFavorite(FavoriteActions action) {
        Map<String, Object> params = new HashMap<String, Object>();
        switch (action) {
            case FAVORATOR:
                params.put("action", "setFav");
                break;
            case CANCEL_FAVORATOR:
                params.put("action", "delFav");
                break;
            case BEEN:
                params.put("action", "setBeen");
                break;
            case CANCEL_BEEN:
                params.put("action", "delBeen");
                break;
            default:
                return;
        }
        switch (mCategory) {
            case ATTRACTIONS:
                params.put("POIType", "scene");
                break;
            case FARMYARD:
                params.put("POIType", "farm");
                break;
            case PICKINGPART:
                params.put("POIType", "pick");
                break;
            case RESORT:
                params.put("POIType", "resort");
                break;
            case ACTIVITY:
                params.put("POIType", "events");
                break;
            case GUIDE:
                params.put("POIType", "weekly");
                break;
            default:
                break;
        }
        params.put("POIId", mParams.id);
        JsonObjectRequest request = new JsonObjectRequest(APIUtils.getUserCenter(params), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getInt("status") == 0) {
                        switchCollectView();
                        MessageUtils.eventBus.post(new MessageEvent(MessageEvent.NOTIFY_COLLECTED_CHANGE));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(Constants.LOG_TAG, error.toString());
            }
        });
        Network.getInstance().addToRequestQueue(request);
    }

    protected void switchCollectView() {
        View view = (View) findViewById(R.id.collect);
        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
            view = findViewById(R.id.collected);
            view.setVisibility(View.INVISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
            view = findViewById(R.id.collected);
            view.setVisibility(View.VISIBLE);
        }
    }

    public void initLoadingView() {
        findViewById(R.id.scrollView).setVisibility(View.INVISIBLE);
        findViewById(R.id.ptr_id_image).setVisibility(View.GONE);
        findViewById(R.id.video_item_image).setVisibility(View.VISIBLE);
        TextView tv = (TextView) findViewById(R.id.video_item_label);
        tv.setVisibility(View.VISIBLE);
        tv.setText(R.string.loading);
    }

    public Map<String, Object> getRequestParams() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", mParams.id);
        return params;
    }

    protected JsonObjectRequest getRequest(String entity) {
        Log.v("AAA", "DetailActivity->url=" + APIUtils.getPOIDetail(entity, getRequestParams()));
        return new JsonObjectRequest(APIUtils.getPOIDetail(entity, getRequestParams()), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        LinearLayout baseLinearLayout = (LinearLayout) findViewById(R.id.baseLinearLayout);
                        if (baseLinearLayout != null) {
                            baseLinearLayout.setVisibility(View.GONE);
                        }
                        ResponsiveScrollView responsiveScrollView = (ResponsiveScrollView) findViewById(R.id.scrollView);
                        if (responsiveScrollView != null) {
                            responsiveScrollView.setVisibility(View.VISIBLE);
                        }
                        process(response.getJSONObject("data"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LinearLayout baseLinearLayout = (LinearLayout) findViewById(R.id.baseLinearLayout);
                if (baseLinearLayout != null) {
                    findViewById(R.id.ptr_id_image).setVisibility(View.VISIBLE);
                    findViewById(R.id.video_item_image).setVisibility(View.GONE);
                    TextView tv = (TextView) findViewById(R.id.video_item_label);
                    tv.setVisibility(View.VISIBLE);
                    tv.setText(R.string.net_acc_failed);
                }
                ResponsiveScrollView responsiveScrollView = (ResponsiveScrollView) findViewById(R.id.scrollView);
                if (responsiveScrollView != null) {
                    responsiveScrollView.setVisibility(View.GONE);
                }
            }
        });
    }

    public void process(JSONObject data) {
        try {
            dataVersion = data.getInt("dataVersion");
        } catch (JSONException js) {
            dataVersion = 0;
            js.printStackTrace();
        }

        if (dataVersion >= 2 && !isFinishing()) {
            Fragment fragment = new DetailProductFragmentNew();
            Bundle args = new Bundle();
            args.putString("response", data.toString());
            args.putInt("dataVersion", dataVersion);
            args.putSerializable("category", mCategory);
            args.putString("entity", mEntity);
            fragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_content, fragment).commitAllowingStateLoss();
        } else {
            Fragment fragment = null;
            if (mEntity.equals(APIUtils.ACTIVITY)) {
                fragment = new ActivityDetailFragment();
            } else if (mEntity.equals(APIUtils.POI_ATTRACTIONS)) {
                fragment = new CategoryDetailFragment();
            } else if (mEntity.equals(APIUtils.POI_FARMYARD)) {
                fragment = new StayDetailFragment();
            } else if (mEntity.equals(APIUtils.GUIDE)) {
                fragment = new GuideDetailFragment();
            } else if (mEntity.equals(APIUtils.POI_PICK)) {
                fragment = new PickingPartDetailFragment();
            } else if (mEntity.equals(APIUtils.POI_RESORT)) {
                fragment = new StayDetailFragment();
            }

            if (fragment != null && !isFinishing()) {
                Bundle args = new Bundle();
                args.putString("response", data.toString());
                args.putSerializable("category", mCategory);
                args.putString("entity", mEntity);
                fragment.setArguments(args);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_content, fragment).commitAllowingStateLoss();
            }
        }
    }

    protected void renderHeader(DetailHeaderDataModel detail) {
        LoadImageView loadImage = (LoadImageView) findViewById(R.id.header_image);
        loadImage.setImageResource(detail.imgUrl);
        if (detail.hasFavorator != null) {
            if (!detail.hasFavorator) {
                View view = (View) findViewById(R.id.collect);
                view.setVisibility(View.VISIBLE);
                view = (View) findViewById(R.id.collected);
                view.setVisibility(View.INVISIBLE);
            } else {
                View view = (View) findViewById(R.id.collect);
                view.setVisibility(View.INVISIBLE);
                view = (View) findViewById(R.id.collected);
                view.setVisibility(View.VISIBLE);
            }
        }
        if (detail.collection == null) {
            detail.collection = "0";
        }
        setTextView(R.id.collection_count, detail.collection);
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
//                        IntentFilter intentFilter = new IntentFilter();
//                        intentFilter.addAction("com.sina.weibo.sdk.action.ACTION_SDK_REQ_ACTIVITY");
//                        mReceiver = new BroadcastReceiver() {
//                            @Override
//                            public void onReceive(Context context, Intent intent) {
//
//                            }
//                        };
//                        registerReceiver(mReceiver, intentFilter);
                    }
                }
                if (mWXapi == null) {
                    mWXapi = WXAPIFactory.createWXAPI(DetailActivity.this, Authorize.WX_APP_KEY, true);
                    if (mWXapi.isWXAppInstalled()) {
                        mWXapi.registerApp(Authorize.WX_APP_KEY);
                    }
                }
                ShareDialog.Builder shareDialog = new ShareDialog.Builder(DetailActivity.this);
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
        msg.description = "这里不错，" + getDetailTitle()
                + " 下载APP,收获百分百的周末！";
        Bitmap bit = getHeaderImage();
        if (bit == null) {
            bit = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
        }
        msg.thumbData = ImageUtils.bmpToByteArray(bit, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "share" + System.currentTimeMillis();
        req.message = msg;
        req.scene = friend ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        mWXapi.sendReq(req);
    }

    protected void shareToZone() {
        if (mTencent == null) {
            mTencent = Tencent.createInstance(Authorize.QQ_APP_KEY, DetailActivity.this.getApplicationContext());
        }
        Bundle bundle = new Bundle();
        bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, getShareUrl("qq"));
        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, getDetailTitle());
        bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, ImageUtils.formatImageUrl(mParams.imgUrl, ImageUtils.MOBILE));
        bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, "这里不错，" + getDetailTitle()
                + "下载APP 收获百分百的周末 https://m.108tian.com/download?hmmd=androidApp_" +
                DeviceUuid.getVersion() + "&hmsr=qq");
        mTencent.shareToQQ(DetailActivity.this, bundle, new IUiListener() {
            @Override
            public void onComplete(Object response) {
                Log.i("AAA", "onComplete");
//                Toast.makeText(DetailActivity.this,"分享成功！",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(UiError e) {
                Log.i("AAA", "onError");
//                Toast.makeText(DetailActivity.this,"分享失败！",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                Log.i("AAA", "onCancel");
//                Toast.makeText(DetailActivity.this,"取消分享！",Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void shareToWeibo() {
        if (mWeiboShareAPI.isWeiboAppInstalled()) {
            WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
            TextObject textObject = new TextObject();
            textObject.text = "#108天周边游#这里不错，" + getDetailTitle() + getShareUrl("weibo") + " 下载APP https://m.108tian.com/download?hmmd=androidApp_" +
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
            Toast.makeText(this, "未安装微博", Toast.LENGTH_LONG).show();
        }
    }

    protected String getShareUrl(String source) {
        String url = null;
        if (mCategory != null && mParams != null) {
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
            url += "/" + mParams.id + ".html?hmmd=androidApp_" +
                    DeviceUuid.getVersion() + "&hmsr=" + source;
        }
        return url;
    }

    protected String getDetailTitle() {
        if (mParams != null) {
            return mParams.title;
        }
        return "108天周边游";
    }

    public Bitmap getHeaderImage() {
        LoadImageView image = (LoadImageView) findViewById(R.id.header_image);
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

    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra("go_home", false)) {
            Intent mainIntent = new Intent(DetailActivity.this, HomeActivity.class);
            DetailActivity.this.startActivity(mainIntent);
            DetailActivity.this.finish();
            return;
        }
        super.onBackPressed();
    }
}
