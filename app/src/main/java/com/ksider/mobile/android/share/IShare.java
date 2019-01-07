package com.ksider.mobile.android.share;

import android.content.Intent;
import android.graphics.Bitmap;

public interface IShare {
	Boolean share(String id, String desc, Bitmap thumb);
	ShareEntity getEntity();
	void handleResponse(Intent intent);
}
