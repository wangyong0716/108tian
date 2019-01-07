package com.ksider.mobile.android;

import android.app.Application;
import android.graphics.Bitmap.CompressFormat;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mobstat.SendStrategyEnum;
import com.baidu.mobstat.StatService;
import com.ksider.mobile.android.comm.ShareDataPool;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.model.Position;
import com.ksider.mobile.android.model.images.ImageCacheManager;
import com.ksider.mobile.android.model.images.ImageCacheManager.CacheType;
import com.ksider.mobile.android.utils.DeviceUuid;
import com.ksider.mobile.android.utils.LocationsUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.Storage;
import com.umeng.analytics.MobclickAgent;
import de.greenrobot.event.EventBus;

public class MainApplication extends Application {
	private static int DISK_IMAGECACHE_SIZE = 1024*1024*5;
	private static CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = CompressFormat.PNG;
	private static int DISK_IMAGECACHE_QUALITY = 100;
	protected BDLocationListener mBDListener;
	@Override
	public void onCreate(){
		super.onCreate();
		onsetupStat();
		LocationsUtils.init(this);
		Network.init(this);
		DeviceUuid.init(getApplicationContext());
//		SDKInitializer.initialize(getApplicationContext());
		Storage.init(getApplicationContext());
		initLocation();
		createImageCache();
	}
	
	private void onsetupStat(){
		StatService.setSessionTimeOut(300);
		StatService.setLogSenderDelayed(10);
		StatService.setSendLogStrategy(this, SendStrategyEnum.SET_TIME_INTERVAL, 1);
		StatService.setDebugOn(false);
		MobclickAgent.setDebugMode(false);
		MobclickAgent.setSessionContinueMillis(300*1000);
	}

	@Override
	public void	onTerminate(){
		LocationsUtils.instance().stop();
	}
	
	private void createImageCache(){
		ImageCacheManager.getInstance().init(this,
				this.getPackageCodePath(),
				DISK_IMAGECACHE_SIZE,
				DISK_IMAGECACHE_COMPRESS_FORMAT,
				DISK_IMAGECACHE_QUALITY,
				CacheType.MEMORY);
	}
	
	public void initLocation() {
		mBDListener = new BDLocationListener() {
			@Override
			public void onReceiveLocation(BDLocation loc) {
				Position position = null;
				if (loc.getLocType() == 61 || loc.getLocType() == 65 || loc.getLocType() == 161) {
					LocationsUtils.instance().stop();
					LocationsUtils.instance().unRegisterLocationListener(mBDListener);
					position = new Position(loc.getLatitude(), loc.getLongitude());
					ShareDataPool.position = position;
					Storage.putString("position", loc.getLongitude()+","+loc.getLatitude());
					EventBus.getDefault().postSticky( new MessageEvent(MessageEvent.UPDATE_POSITION));
				} else {
					String coord = Storage.sharedPref.getString("position", "116.403875,39.915168");
					String[] coords = coord.split(",");
					position = new Position();
					if(coords.length == 2){
						position.longitude = Double.parseDouble(coords[0]);
						position.latitude = Double.parseDouble(coords[1]);
					}
				}
				if(position != null){
					ShareDataPool.position = position;
				}
			}
		};
		LocationsUtils.instance().registerLocationListener(mBDListener).start();
	}
}
