package com.ksider.mobile.android.share;

import android.app.Activity;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

public class ShareFactory {
	protected static Map<ShareEntity, IShare> mEntity = new HashMap<ShareEntity, IShare>();

	public static IShare create(Activity context, ShareEntity entity) {
		IShare share = null;
		switch (entity) {
			case FRIEND_CIRCLE:
				break;
			case WECHAT:
				break;
			case WEIBO:
				share = new WeiboShare(context);
				mEntity.put(ShareEntity.WEIBO, share);
				break;
			case QZONE:
				break;
			default:
				break;
		}
		return share;
	}

	public static void notifiedResponse(Intent intent) {
		IShare share = mEntity.get(ShareEntity.WEIBO);
		if (share != null) {
			share.handleResponse(intent);
		}
	}

}
