package com.ksider.mobile.android.utils;

import android.content.Context;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

public class LocationsUtils {
	protected LocationClient mLocationClient = null;
	protected static LocationsUtils mInstance;
	public LocationsUtils(Context context){
		mLocationClient = new LocationClient(context);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式
		option.setScanSpan(5000);							//设置发起定位请求的间隔时间为5000ms
		option.setCoorType("bd09ll");
		option.setIsNeedAddress(true);						//返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);					//返回的定位结果包含手机机头的方向
		mLocationClient.setLocOption(option);
	}
	
	public LocationsUtils registerLocationListener(BDLocationListener listener){
		mLocationClient.registerLocationListener(listener);
		return this;
	}
	
	public LocationsUtils unRegisterLocationListener(BDLocationListener listener){
		mLocationClient.unRegisterLocationListener(listener);
		return this;
	}
	
	public static void init(Context context){
		if(mInstance == null){
			mInstance = new LocationsUtils(context);
		}
	}
	
	public static LocationsUtils instance(){
		return mInstance;
	}
	
	public void stop(){
		mLocationClient.stop();
	}
	
	public LocationsUtils start(){
		mLocationClient.start();
		return this;
	}

}
