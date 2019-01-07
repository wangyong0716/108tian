package com.ksider.mobile.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import com.ksider.mobile.android.WebView.R;

public class LaunchCoverView extends RelativeLayout {

	public LaunchCoverView(Context context) {
		this(context, null);
	}
	public LaunchCoverView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public LaunchCoverView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	protected void init(){
		View view = findViewById(R.id.list_title);
	}
}
