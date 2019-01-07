package com.ksider.mobile.android.WebView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.activity.fragment.OnScrollChanged;
import com.ksider.mobile.android.adaptor.AlbumAdaptor;
import com.ksider.mobile.android.adaptor.ChoicenessDetailListViewAdaptor;
import com.ksider.mobile.android.adaptor.ScrollAlbumListener;
import com.ksider.mobile.android.auth.Authorize;
import com.ksider.mobile.android.model.BaseDataModel;
import com.ksider.mobile.android.model.DetailHeaderDataModel;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.scrollListView.OverScrollPullToZoomListView;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.view.LoadImageView;
import com.ksider.mobile.android.view.viewpagerindicator.CirclePageIndicator;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChoicenessActivity extends ActionBarActivity {
    protected BaseDataModel mBaseModel;
    protected OverScrollPullToZoomListView mListView;
    protected Boolean mHasRequest = false;
    protected ChoicenessDetailListViewAdaptor mAdaptor;
    protected IWeiboShareAPI mWeiboShareAPI;
    protected Tencent mTencent;
    protected IWXAPI mWXapi;
    protected BroadcastReceiver mReceiver;
//    protected View mShareLayout;
//    protected View mCustomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SlidingLayout(this);
        setContentView(R.layout.activity_choiceness);
        mBaseModel = Utils.retrieveArgData(getIntent());
        mListView = (OverScrollPullToZoomListView) findViewById(R.id.listview);
        if (mBaseModel.title != null) {
            TextView textView = (TextView) findViewById(R.id.header_title);
            textView.setText(mBaseModel.title);
        }
        mAdaptor = new ChoicenessDetailListViewAdaptor(this);
        mListView.setAdapter(mAdaptor);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> params, View view, int position, long id) {
                if (position == 0) {
                    return;
                }
                BaseDataModel base = mAdaptor.getItem(position - 1);
                if (base != null) {
                    Intent intent = Utils.getLandingActivity(ChoicenessActivity.this, base.type);
                    if (intent != null) {
                        Utils.initDetailPageArg(intent, base);
                        StatHandle.increaseClick(StatHandle.CHOINCEDETAIL);
                        startActivity(intent);
                    }
                }
            }
        });
        mListView.setPagingableListener(new OverScrollPullToZoomListView.Pagingable() {
            @Override
            public void onLoadMoreItems() {
                mListView.onFinishLoading(false, null);
            }
        });
        customActionBar();
        initShare();
        initCollect();
    }

    protected void back() {
        onBackPressed();
    }

    protected void initCollect() {
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!UserInfo.isLogin()) {
                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                    startActivity(intent);
                    return;
                }
                View collected = findViewById(R.id.collected);
                if (collected.getVisibility() == View.VISIBLE) {
                    postFavorite(FavoriteActions.CANCEL_FAVORATOR);
                } else {
                    postFavorite(FavoriteActions.FAVORATOR);
                }
            }
        };

        View view = findViewById(R.id.collect_area);
        view.setOnClickListener(listener);
        view.setVisibility(View.VISIBLE);
    }

    protected void customActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
//        toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_background_color));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        findViewById(R.id.list_backbutton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });

        final OverScrollPullToZoomListView scroll = (OverScrollPullToZoomListView) findViewById(R.id.listview);
        if (scroll != null) {
            toolbar.getBackground().setAlpha(0);
            final ImageView background = (ImageView) findViewById(R.id.background);
            final ImageView backButton = (ImageView) findViewById(R.id.list_backbutton);
            final ImageView collection = (ImageView) findViewById(R.id.collect);
            final ImageView share = (ImageView) findViewById(R.id.share);
            final ImageView line = (ImageView) findViewById(R.id.line);

            background.setImageResource(R.color.toolbar_background_color);
            backButton.setImageResource(R.drawable.backbutton_white_icon);
            collection.setImageResource(R.drawable.collect_white_icon);
            share.setImageResource(R.drawable.share_white_icon);
            line.setImageResource(R.color.divider_line_color);
            background.getDrawable().setAlpha(0);
            backButton.getDrawable().setAlpha(255);
            collection.getDrawable().setAlpha(255);
            share.getDrawable().setAlpha(255);
            line.getDrawable().setAlpha(0);

            final float imgHeight = (int) getResources().getDimension(R.dimen.header_image_height);
            final float height = (int) getResources().getDimension(R.dimen.header_banner_height);
            final float ratio0 = 255 / getResources().getDimension(R.dimen.header_image_height);
            final float ratio1 = 255 / height;
            final float ratio2 = 255 / (getResources().getDimension(R.dimen.header_image_height) - getResources().getDimension(R.dimen.header_banner_height));

            scroll.setOnScrollChanged(new OnScrollChanged() {
                @Override
                public void onScrollChanged(int left, int top, int oldLeft, int oldTop) {
                    if (top >= 0) {
                        if (top < height) {
                            backButton.setImageResource(R.drawable.backbutton_white_icon);
                            collection.setImageResource(R.drawable.collect_white_icon);
                            share.setImageResource(R.drawable.share_white_icon);
                            int alpha = (int) ((height - top) * ratio1);
                            backButton.getDrawable().setAlpha(alpha);
                            collection.getDrawable().setAlpha(alpha);
                            share.getDrawable().setAlpha(alpha);
                        } else if (top <= imgHeight) {
                            backButton.setImageResource(R.drawable.backbutton_icon);
                            collection.setImageResource(R.drawable.collect_icon);
                            share.setImageResource(R.drawable.share_icon);
                            int alpha = (int) ((top - height) * ratio2);
                            alpha = alpha < 255 ? alpha : 255;
                            backButton.getDrawable().setAlpha(alpha);
                            collection.getDrawable().setAlpha(alpha);
                            share.getDrawable().setAlpha(alpha);
                        } else {
                            backButton.getDrawable().setAlpha(255);
                            collection.getDrawable().setAlpha(255);
                            share.getDrawable().setAlpha(255);
                        }
                        if (top < imgHeight) {
                            int alpha = (int) (top * ratio0);
                            alpha = alpha < 240 ? alpha : 240;
                            background.getDrawable().setAlpha(alpha);
                            line.getDrawable().setAlpha(alpha);
                        } else {
                            background.getDrawable().setAlpha(240);
                            line.getDrawable().setAlpha(240);
                        }
                    } else {
                        backButton.getDrawable().setAlpha(255);
                        collection.getDrawable().setAlpha(255);
                        share.getDrawable().setAlpha(255);
                        background.getDrawable().setAlpha(0);
                        line.getDrawable().setAlpha(0);
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mHasRequest) {
            mHasRequest = true;
            Network.getInstance().addToRequestQueue(getRequest(), "choiceness");
        }
    }

    protected JsonObjectRequest getRequest() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", mBaseModel.id);
        Log.v("AAA", "getChoiceness->url=" + APIUtils.getPOIDetail(APIUtils.CHOINCENESS, params));
        return new JsonObjectRequest(APIUtils.getPOIDetail(APIUtils.CHOINCENESS, params), null, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {

                        proccess(response.getJSONObject("data"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(Constants.LOG_TAG, "response:" + error.toString());
            }
        });
    }

    protected void proccess(JSONObject data) {
        renderHeader(data);
        List<DetailHeaderDataModel> items = new ArrayList<DetailHeaderDataModel>();
        JSONArray lists;
        try {
            lists = data.getJSONArray("items");
            for (int i = 0; i < lists.length(); i++) {
                try {
                    DetailHeaderDataModel item = new DetailHeaderDataModel();
                    JSONObject o = (JSONObject) lists.get(i);
                    item.title = o.getString("name");
                    item.imgUrl = o.getString("img");
                    item.description = o.getString("desc");
                    JSONObject recommend = o.getJSONObject("recommend");
                    item.id = recommend.getString("id");
                    item.type = recommend.getString("type");
                    item.collection = recommend.getString("fav");
                    try {
                        item.hasFavorator = recommend.getBoolean("isFav");
                    } catch (JSONException e) {
//                        e.printStackTrace();
                    }
                    try {
                        item.expire = recommend.getBoolean("expired");
                    } catch (JSONException js) {
                        item.expire = false;
                        js.printStackTrace();
                    }
                    try {
                        JSONArray list = recommend.getJSONArray("priceRange");
                        if (list.length() > 1) {
                            item.price = StringUtils.getPriceRange(list.getDouble(0), list.getDouble(1));
                        } else {
                            item.price = "";
                        }
                    } catch (JSONException js) {
                        item.price = "";
                        js.printStackTrace();
                    }
                    items.add(item);
                } catch (JSONException e) {
                    Log.v(Constants.LOG_TAG, " " + e.toString());
//                    e.printStackTrace();
                }
            }
            JSONArray imgs = data.getJSONArray("imgs");
            if (imgs != null && imgs.length() > 0) {
                List<String> album = new ArrayList<String>();
                for (int i = 0; i < imgs.length(); i++) {
                    album.add(imgs.getString(i));
                }
                ViewPager pager = (ViewPager) findViewById(R.id.header_album);
                AlbumAdaptor adaptor = new AlbumAdaptor(this, album);
                pager.setAdapter(adaptor);
                pager.setCurrentItem(0);
                final int count = album.size();
                CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indicator);
                indicator.setViewPager(pager);
                indicator.setOnPageChangeListener(new ScrollAlbumListener(pager, true));
            }
            String collection = null;
            try {
                collection = data.getString("fav");
            } catch (JSONException e1) {
//                e1.printStackTrace();
                collection = "0";
            }
            Boolean hasFavorator = false;
            try {
                hasFavorator = data.getBoolean("isFav");
            } catch (JSONException e) {

            }

            if (!hasFavorator) {
                View view = findViewById(R.id.collect);
                view.setVisibility(View.VISIBLE);
                view = findViewById(R.id.collected);
                view.setVisibility(View.INVISIBLE);
            } else {
                View view = findViewById(R.id.collect);
                view.setVisibility(View.INVISIBLE);
                view = findViewById(R.id.collected);
                view.setVisibility(View.VISIBLE);
            }
            TextView text = (TextView) findViewById(R.id.collection_count);
            if (text != null) {
                text.setText(collection);
            }
        } catch (JSONException e1) {
//            e1.printStackTrace();
        }
        mListView.onFinishLoading(items.size() > 0, items);
    }

    protected void renderHeader(JSONObject data) {
        TextView text = (TextView) findViewById(R.id.header_title);
        try {
            mBaseModel.title = data.getString("name");
            text.setText(mBaseModel.title);
        } catch (JSONException e) {
            mBaseModel.title = "";
            e.printStackTrace();
        }
        text = (TextView) findViewById(R.id.header_subtitle);
        try {
            text.setText(data.getString("subtitle"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected Bitmap getHeaderImage() {
        ViewPager pager = (ViewPager) findViewById(R.id.header_album);
        Bitmap bit = null;
        if (pager != null) {
            LoadImageView image = null;
            if (pager.getChildCount() > 0) {
                image = (LoadImageView) pager.getChildAt(0);
            }
            if (image == null) {
                return null;
            }
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
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        StatHandle.postImpclkEvent(this, StatHandle.CHOINCEDETAIL);
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
                    mWXapi = WXAPIFactory.createWXAPI(ChoicenessActivity.this, Authorize.WX_APP_KEY, true);
                    if (mWXapi.isWXAppInstalled()) {
                        mWXapi.registerApp(Authorize.WX_APP_KEY);
                    }
                }
                ShareDialog.Builder shareDialog = new ShareDialog.Builder(ChoicenessActivity.this);
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
            mTencent = Tencent.createInstance(Authorize.QQ_APP_KEY, ChoicenessActivity.this.getApplicationContext());
        }
        Bundle bundle = new Bundle();
        bundle.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        bundle.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, getShareUrl("qq"));
        bundle.putString(QzoneShare.SHARE_TO_QQ_TITLE, getDetailTitle());
        bundle.putString(QzoneShare.SHARE_TO_QQ_IMAGE_URL, ImageUtils.formatImageUrl(mBaseModel.imgUrl, ImageUtils.MOBILE));
        bundle.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, "这里不错，" + getDetailTitle() + "下载APP寻找更多出行地http://m.108tian.com/download?hmsr=androidApp_" + DeviceUuid.getVersion() + "&hmmd=qq");
        final Context activity = this;
        mTencent.shareToQQ((Activity) this, bundle, new IUiListener() {
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
            textObject.text = "#108天周边游#这里不错，" + getDetailTitle() + getShareUrl("weibo") + " 下载APP https://m.108tian.com/download?hmsr=androidApp_" +
                    DeviceUuid.getVersion() + "&hmmd=weibo 收获百分百周末！";
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

    protected String getDetailTitle() {
        if (mBaseModel != null) {
            return mBaseModel.title;
        }
        return "108天周边游";
    }

    protected String getShareUrl(String source) {
        String url = Constants.HOME_BASE_URL + "choice/" + mBaseModel.id + ".html?hmmd=androidApp_" + DeviceUuid.getVersion() + "&hmsr=" + source;
        return url;
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

        params.put("POIType", "recommend");
        params.put("POIId", mBaseModel.id);
        JsonObjectRequest request = new JsonObjectRequest(APIUtils.getUserCenter(params), null, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getInt("status") == 0) {
                        switchCollectView();
                        MessageUtils.eventBus.post(new MessageEvent(MessageEvent.NOTIFY_COLLECTED_CHANGE));
                    }
                } catch (JSONException e) {
//                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(Constants.LOG_TAG, error.toString());
            }
        });
        Network.getInstance().addToRequestQueue(request);
    }

    protected void switchCollectView() {
        TextView text = (TextView) findViewById(R.id.collection_count);
        String count = text.getText().toString();
        Integer update = Integer.parseInt(count);
        View view = (View) findViewById(R.id.collect);
        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
            view = (View) findViewById(R.id.collected);
            view.setVisibility(View.INVISIBLE);
            update -= 1;
        } else {
            view.setVisibility(View.INVISIBLE);
            view = (View) findViewById(R.id.collected);
            view.setVisibility(View.VISIBLE);
            update += 1;
        }
        if (update < 0)
            update = 0;
        text.setText(update.toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != mTencent)
            mTencent.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra("go_home", false)) {
            Intent mainIntent = new Intent(ChoicenessActivity.this, HomeActivity.class);
            ChoicenessActivity.this.startActivity(mainIntent);
            ChoicenessActivity.this.finish();
            return;
        }
        super.onBackPressed();
    }
}
