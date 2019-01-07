package com.ksider.mobile.android.utils;

import android.util.Log;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.model.MessageEvent;
import de.greenrobot.event.EventBus;

import java.util.HashMap;
import java.util.Map;

public class MessageUtils {
	public static final String UPDATE_POSITION = "updatePosition";
	public static final String UPDATE_CITYNAME = "updateCityName";
	public static final String NOTIFY_COLLECTED_CHANGE = "NotifyCollectedChange";
	public static final String NOTIFY_SHARE_RESULT = "NotifyShareResult";
	public static final String NOTIFY_LOGIN_RESULT = "NotifyLoginResult";
	public static final String NOTIFY_LOGIN_SUCCESS = "NotifyLoginSuccess";
	public static final String NOTIFY_GET_CITY = "NotifyGetCity";
	public static final String NOTIFY_WEIBO_SHARE_RESULT = "NotifyWeiboShareResult";
	public static final EventBus eventBus = new EventBus();
	protected static final Map<String, EventBus> stickyBus = new HashMap<String, EventBus>();
	public static  void post(String channel, MessageEvent event){
		EventBus bus = stickyBus.get(channel);
		if(bus  == null){
			bus = new EventBus();
			stickyBus.put(channel, bus);
		}
		bus.post(event);
	}
	public static synchronized void postSticky(String channel, MessageEvent event){
		
		EventBus bus = stickyBus.get(channel);
		Log.v(Constants.LOG_TAG, "postSticky:"+channel+" "+bus);
		if(bus  == null){
			bus = new EventBus();
			stickyBus.put(channel, bus);
		}
		bus.postSticky(event);
	}
	
	public static  void registerSticky(String channel, Object subscriber){
		EventBus bus = stickyBus.get(channel);
		if(bus == null){
			bus = new EventBus();
			stickyBus.put(channel, bus);
		}
		bus.registerSticky(subscriber);
	}
	
	public static  void register(String channel, Object subscriber){
		EventBus bus = stickyBus.get(channel);
		if(bus == null){
			bus = new EventBus();
			stickyBus.put(channel, bus);
		}
		bus.register(subscriber);
	}
	public static  void unregister(String channel, Object subscriber){
		EventBus bus = stickyBus.get(channel);
		if(bus == null){
			bus = new EventBus();
			stickyBus.put(channel, bus);
		}
		bus.unregister(subscriber);
	}
}
