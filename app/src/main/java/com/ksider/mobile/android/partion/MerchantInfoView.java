package com.ksider.mobile.android.partion;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.ksider.mobile.android.WebView.*;
import com.ksider.mobile.android.model.ProductStockModel;
import com.ksider.mobile.android.model.TrafficInfoModel;
import com.ksider.mobile.android.utils.BasicCategory;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.Status;
import com.umeng.analytics.MobclickAgent;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by yong on 2015/6/2.
 */
public class MerchantInfoView extends LinearLayout {
    private Context context;
    private String productId;
    private String phone;
    private TrafficInfoModel trafficInfo;
    private BasicCategory category;
    private boolean hasProduct;
    private String brief;
    private int dataVersion;
    private int productType;
    private String productString;
    private ArrayList<ProductStockModel> stocks;

    public void setProductString(String productString) {
        this.productString = productString;
    }

    public void setStocks(ArrayList<ProductStockModel> stocks) {
        this.stocks = stocks;
    }

    public MerchantInfoView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MerchantInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public MerchantInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.partion_merchant_info_view, this);
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }

    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
    }

    public void setRefundAble(boolean refundAble) {
        if (refundAble) {
            ((ImageView) findViewById(R.id.refund_icon)).setImageResource(R.drawable.refund_icon);
            ((TextView) findViewById(R.id.refund_description)).setText("可退");
        } else {
            ((ImageView) findViewById(R.id.refund_icon)).setImageResource(R.drawable.unrefund_icon);
            ((TextView) findViewById(R.id.refund_description)).setText("不可退");
        }
    }

    public void setCategory(BasicCategory category) {
        this.category = category;
    }

    public void setHasProduct(boolean hasProduct) {
        this.hasProduct = hasProduct;
    }

    public void setDataVersion(int dataVersion) {
        this.dataVersion = dataVersion;
    }

    public void setProductType(int productType) {
        this.productType = productType;
    }

    public void hideProductState() {
        hideProductState(true);
    }

    public void hideProductState(boolean hide) {
        if (hide) {
            findViewById(R.id.product_state).setVisibility(GONE);
        } else {
            findViewById(R.id.product_state).setVisibility(VISIBLE);
        }
    }

    public void hideDateInfo() {
        hideDateInfo(true);
    }

    public void hideDateInfo(boolean hide) {
        if (hide) {
            findViewById(R.id.to_date_info).setVisibility(GONE);
        } else {
            findViewById(R.id.to_date_info).setVisibility(VISIBLE);
        }
    }

    public void hideMerchantInfo() {
        hideMerchantInfo(true);
    }

    public void hideMerchantInfo(boolean hide) {
        if (hide) {
            findViewById(R.id.to_merchant_info).setVisibility(GONE);
        } else {
            findViewById(R.id.to_merchant_info).setVisibility(VISIBLE);
        }
    }

    public void hideMoreButton() {
        findViewById(R.id.more).setVisibility(GONE);
    }

    public void setDateLabel(String label) {
        ((TextView) findViewById(R.id.time_label)).setText(label);
    }

    public void setDateLabel(int labelId) {
        setDateLabel(context.getResources().getString(labelId));
    }

    public void setMerchantLabel(String label) {
        ((TextView) findViewById(R.id.merchant_label)).setText(label);
    }

    public void setMerchantLabel(int labelId) {
        setMerchantLabel(context.getResources().getString(labelId));
    }

    public void setDate(String date) {
        ((TextView) findViewById(R.id.date)).setText(date);
    }

    public void setMerchant(String merchant) {
        ((TextView) findViewById(R.id.merchant)).setText(merchant);
    }

    public void setLocation(String location) {
        ((TextView) findViewById(R.id.location)).setText(location);
    }

    public void setDistance(String distance) {
        ((TextView) findViewById(R.id.distance)).setText(distance);
    }

    public void setDistance(double distance) {
        String dis = "";
        if (distance >= 100) {
            dis += Math.round(distance);
        } else {
            NumberFormat formatter = new DecimalFormat("#0.00");
            dis += formatter.format(distance);
        }
        dis += "km";
        ((TextView) findViewById(R.id.distance)).setText(dis);
    }

    public void setMoreText(String moreText) {
        ((TextView) findViewById(R.id.more)).setText(moreText);
    }

    public void setMoreText(int moreTextId) {
        setMoreText(getResources().getString(moreTextId));
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setTrafficInfo(TrafficInfoModel trafficInfo) {
        this.trafficInfo = trafficInfo;
    }

    public void setMoreMerchantInfo(String brief) {
        this.brief = brief;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setValues() {
        setViews();
        ImageRequest imageRequest = new ImageRequest(
                getGaodeMapUrl(),
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        ((ImageView) findViewById(R.id.location_map)).setImageBitmap(response);
                    }
                }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError paramVolleyError) {

            }
        });
        Network.getInstance().addToRequestQueue(imageRequest, "LoadImageView");
        findViewById(R.id.consult).setOnClickListener(listener);
        findViewById(R.id.to_location_info).setOnClickListener(listener);
        findViewById(R.id.more).setOnClickListener(listener);
        findViewById(R.id.to_date_info).setOnClickListener(listener);
    }

    public void setViews() {
        if (!hasProduct) {
            hideMoreButton();
        }
        if (productType != Status.PRODUCT_TYPE_TIME || stocks.size() < 1) {
            hideDateInfo();
        }
        if (dataVersion >= 2) {
            return;
        }
        if (category == null) {
            return;
        }

        switch (category) {
            case ACTIVITY:
                if (!hasProduct) {
                    hideProductState();
                    hideDateInfo();
                }
                break;
            case PICKINGPART:
                hideDateInfo();
                if (!hasProduct) {
                    hideProductState();
                    hideMerchantInfo();
                }
                break;
            case GUIDE:
                setVisibility(GONE);
                break;
            case ATTRACTIONS:
                if (!hasProduct) {
                    hideProductState();
                }
                break;
            case FARMYARD:
            case RESORT:
                hideDateInfo();
                if (!hasProduct) {
                    hideProductState();
                    hideMerchantInfo();
                }
                break;
            case UNKOWN:
                break;
            default:
                break;
        }
    }

    public String getBaiduMapUrl() {
        String url = "http://api.map.baidu.com/staticimage?center=";
        url = url + trafficInfo.lng + "," + trafficInfo.lat + "&width=800&height=200&zoom=17&markers=";
        url = url + trafficInfo.lng + "," + trafficInfo.lat + "&markerStyles=s";
        return url;
    }

    public String getGaodeMapUrl() {
        String url = "http://restapi.amap.com/v3/staticmap?";
        url = url + "markers=mid,0xFF0000,A:" + trafficInfo.lng + "," + trafficInfo.lat + "&";
        url = url + "size=800*200&zoom=17&key=a22c6eabfbafd37bfbadc7d8303a94c1";
        return url;
    }

    private OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.consult:
                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                    context.startActivity(phoneIntent);
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("productId", productId == null ? "" : productId);
                    if (hasProduct) {
                        map.put("hasProduct", "true");
                    } else {
                        map.put("hasProduct", "false");
                    }
                    switch (category) {
                        case ATTRACTIONS:
                            MobclickAgent.onEvent(context, Constants.UMENG_STATISTICS_TAG_SCENE, map);
                            break;
                        case RESORT:
                        case FARMYARD:
                            MobclickAgent.onEvent(context, Constants.UMENG_STATISTICS_TAG_RESORT, map);
                            break;
                        case PICKINGPART:
                            MobclickAgent.onEvent(context, Constants.UMENG_STATISTICS_TAG_PICK, map);
                            break;
                        case ACTIVITY:
                            MobclickAgent.onEvent(context, Constants.UMENG_STATISTICS_TAG_EVENT, map);
                            break;
                        case GUIDE:
                            MobclickAgent.onEvent(context, Constants.UMENG_STATISTICS_TAG_RECOMMEND, map);
                            break;
                        default:
                            break;
                    }
                    break;
                case R.id.to_location_info:
                    if (trafficInfo != null) {
                        Intent intent = new Intent(context.getApplicationContext(), TrafficMapActivity.class);
                        intent.putExtra("data", trafficInfo);
                        context.startActivity(intent);
                    }
                    break;
                case R.id.more:
                    if (dataVersion >= 2 || category == BasicCategory.ACTIVITY || category == BasicCategory.ATTRACTIONS || category == BasicCategory.FARMYARD || category == BasicCategory.RESORT) {
                        Intent intent = new Intent(context, MoreMerchantInfoActivity.class);
                        intent.putExtra("brief", brief);
                        context.startActivity(intent);
                        break;
                    } else if (category == BasicCategory.PICKINGPART) {
                        Intent intent = new Intent(context, MoreDetailInfoActivity.class);
                        intent.putExtra("type", MoreDetailInfoActivity.DETAIL_MERCHANT_INTO);
                        intent.putExtra("brief", brief);
                        context.startActivity(intent);
                    } else {
                        break;
                    }
                    break;
                case R.id.to_date_info:
                    if (stocks.size() > 0) {
                        Intent intent = new Intent(context, PurchaseAcitvity.class);
                        intent.putExtra("product", productString);
                        intent.putParcelableArrayListExtra("stocks", stocks);
                        context.startActivity(intent);
                    }
                    break;
                default:
                    break;
            }
        }
    };

}
