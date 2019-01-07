package com.ksider.mobile.android.WebView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ZoomControls;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.ksider.mobile.android.comm.ShareDataPool;
import com.ksider.mobile.android.model.TrafficInfoModel;
import com.ksider.mobile.android.utils.Utils;

import java.net.URISyntaxException;

public class TrafficMapActivity extends BaseActivity {
    protected MapView mMapView;
    TrafficInfoModel mInfo;

    private AMap aMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_info);
        customActionBar("交通信息");
        mInfo = (TrafficInfoModel) getIntent().getExtras().getSerializable("data");
        if (mInfo != null) {
            setTextView(R.id.address, mInfo.address);
            setTextView(R.id.name, mInfo.name);
            setTextView(R.id.distance, mInfo.distance);
            mMapView = (MapView) findViewById(R.id.bmapView);
            if (mInfo.lat == null || mInfo.lng == null) {
                mMapView.setVisibility(View.GONE);
                return;
            }
            mMapView.onCreate(savedInstanceState);// 必须要写
            aMap = mMapView.getMap();
            LatLng point = new LatLng(mInfo.lat, mInfo.lng);
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.gcoding_icon);
            MarkerOptions option = new MarkerOptions().position(point).icon(bitmap);
            mMapView.getMap().addMarker(option);
            aMap.moveCamera(CameraUpdateFactory.changeLatLng(point));
            aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        }
        // 隐藏缩放控件
        int childCount = mMapView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = mMapView.getChildAt(i);
            if (child instanceof ZoomControls) {
                child.setVisibility(View.GONE);
                break;
            }
        }
        findViewById(R.id.router).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri = null;
                Intent intent = null;
                if (Utils.checkPackage(TrafficMapActivity.this, "com.autonavi.minimap")) {
                    uri = "androidamap://route?sourceApplication=com.ksider.mobile.android.WebView&sname=启点&slat=" + ShareDataPool.position.latitude
                            + "&slon=" + ShareDataPool.position.longitude + "&dname=" + mInfo.name
                            + "&dlat=" + mInfo.lat + "&dlon=" + mInfo.lng + "&dev=1&m=0&t=2";
                    intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse(uri));
                    intent.setPackage("com.autonavi.minimap");
                } else if (Utils.checkPackage(TrafficMapActivity.this, "com.baidu.BaiduMap")) {
                    uri = "intent://map/direction?origin=latlng:"
                            + ShareDataPool.position.latitude
                            + ","
                            + ShareDataPool.position.longitude
                            + "|name:当前位置"
                            + "&destination=latlng:"
                            + mInfo.lat
                            + ","
                            + mInfo.lng
                            + "|name:"
                            + mInfo.name
                            + "&mode=driving&src=com.ksider.mobile.android.WebView#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end";
                    try {
                        intent = Intent.getIntent(uri);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(TrafficMapActivity.this, "未安装高德地图或者百度地图", Toast.LENGTH_LONG).show();
                    return;
                }
                if (intent != null) {
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        findViewById(R.id.phone).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mInfo.phone));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
}
