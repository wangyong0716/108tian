package com.ksider.mobile.android.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import com.ksider.mobile.android.WebView.Constants;

import java.io.ByteArrayOutputStream;

public class ImageUtils {
	public static final String NORMAL = "n_";
	public static final String THUMBNAIL = "t_";
	public static final String LIST = "l_";
	public static final String ORIGINAL = "u_";
	public static final String MOBILE = "m_";

	public static String formatImageUrl(String url, String type) {
		String source = url;
		if (source == null || source.startsWith("http://")) {
			return source;
		}

		if (source.indexOf("_") == 1) {
			source = source.substring(2);
		}
		source = Constants.IMAGE_BASE_URL + type + source;
		return source;
	}
	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		if(bmp == null){
			return null;
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.JPEG, 60, output);
		if (needRecycle) {
			bmp.recycle();
		}
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
