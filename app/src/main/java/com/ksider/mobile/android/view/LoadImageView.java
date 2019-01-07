package com.ksider.mobile.android.view;


import android.content.Context;
import android.util.AttributeSet;
import com.android.volley.toolbox.NetworkImageView;
import com.ksider.mobile.android.model.images.ImageCacheManager;
import com.ksider.mobile.android.utils.ImageUtils;

public class LoadImageView extends NetworkImageView {
	protected static int count=0;
	protected LoadImageView _this;
	public LoadImageView(Context context) {
		super(context);
	}
	
	public LoadImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public LoadImageView(Context context, AttributeSet attrs, int defStyle)  {
		super(context, attrs, defStyle);
	}
	
	public void setImageResource(String url){
		if(url == null)return;
		if(!url.equals(this.getTag())){
			setImageDrawable(null);
		}
		
		setTag(url);
		_this = this;	
		if(!url.startsWith("http://")&&!url.startsWith("https://")){
			if(url.startsWith("u_")){
				url = url.substring(2);
			}
			url = ImageUtils.formatImageUrl(url, ImageUtils.MOBILE);
		}
		setImageUrl(url,  ImageCacheManager.getInstance().getImageLoader());

	}
}
