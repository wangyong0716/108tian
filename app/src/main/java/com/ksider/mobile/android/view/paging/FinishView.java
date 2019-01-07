package com.ksider.mobile.android.view.paging;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.ksider.mobile.android.WebView.R;

public class FinishView extends LinearLayout {

	public FinishView(Context context) {
		super(context);
		init();
	}

	public FinishView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FinishView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		inflate(getContext(), R.layout.finish_view, this);
	}
	public void showFailed() {
		findViewById(R.id.failed_tips).setVisibility(View.VISIBLE);
		findViewById(R.id.end_view).setVisibility(View.GONE);
		findViewById(R.id.empty_view).setVisibility(View.GONE);
	}
	public void show(Boolean empty) {
		findViewById(R.id.failed_tips).setVisibility(View.GONE);
		View end_view = findViewById(R.id.end_view);
		View empty_view = findViewById(R.id.empty_view);
		if(empty){
			end_view.setVisibility(View.VISIBLE);
			empty_view.setVisibility(View.INVISIBLE);
		}else{
			end_view.setVisibility(View.INVISIBLE);
			empty_view.setVisibility(View.VISIBLE);
		}
	}
}
