package com.ksider.mobile.android.partion;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.ksider.mobile.android.WebView.MoreMerchantInfoActivity;
import com.ksider.mobile.android.WebView.PurchaseAcitvity;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.WebView.TrafficMapActivity;
import com.ksider.mobile.android.model.ProductStockModel;
import com.ksider.mobile.android.model.TrafficInfoModel;
import com.ksider.mobile.android.utils.Network;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by yong on 2015/6/2.
 */
public class SellerInfoView extends LinearLayout {
    private Context context;
    private TrafficInfoModel trafficInfo;
    private String brief;
    private String productString;
    private ArrayList<ProductStockModel> stocks;
    private boolean productExists = false;
    private boolean expired = false;

    public void setProductString(String productString) {
        this.productString = productString;
    }

    public void setStocks(ArrayList<ProductStockModel> stocks) {
        this.stocks = stocks;
    }

    public SellerInfoView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public SellerInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public SellerInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.partion_seller_info_view, this);
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }

    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
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

    public void setDate(boolean expired, String date) {
        this.expired = expired;
        if (!expired) {
            ((TextView) findViewById(R.id.date)).setText(date);
        }
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

    public void setTrafficInfo(TrafficInfoModel trafficInfo) {
        this.trafficInfo = trafficInfo;
    }

    public void setMoreMerchantInfo(String brief) {
        this.brief = brief;
    }

    public void setProductExists(boolean productExists) {
        this.productExists = productExists;
    }

    public void setValues() {
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
        findViewById(R.id.to_location_info).setOnClickListener(listener);
        findViewById(R.id.to_merchant_info).setOnClickListener(listener);
        findViewById(R.id.to_date_info).setOnClickListener(listener);
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
            if (!productExists) {
                return;
            }
            switch (view.getId()) {
                case R.id.to_location_info:
                    if (trafficInfo != null) {
                        Intent trafficIntent = new Intent(context.getApplicationContext(), TrafficMapActivity.class);
                        trafficIntent.putExtra("data", trafficInfo);
                        context.startActivity(trafficIntent);
                    }
                    break;
                case R.id.to_merchant_info:
                    if (brief != null && !brief.equals("")) {
                        Intent merchantIntent = new Intent(context, MoreMerchantInfoActivity.class);
                        merchantIntent.putExtra("brief", brief);
                        context.startActivity(merchantIntent);
                    }
                    break;
                case R.id.to_date_info:
                    if (stocks.size() > 0 && productString != null && !productString.equals("") && !expired) {
                        Intent purchaseIntent = new Intent(context, PurchaseAcitvity.class);
                        purchaseIntent.putExtra("product", productString);
                        purchaseIntent.putParcelableArrayListExtra("stocks", stocks);
                        context.startActivity(purchaseIntent);
                    }
                    break;
                default:
                    break;
            }
        }
    };

}
