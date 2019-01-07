package com.ksider.mobile.android.view.paging;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.ksider.mobile.android.WebView.R;



public class LoadingView extends LinearLayout {

	public LoadingView(Context context) {
		super(context);
		init();
	}

	public LoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public LoadingView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		init();
	}
	private void init() {
		inflate(getContext(), R.layout.loading_view, this);
	}

}
