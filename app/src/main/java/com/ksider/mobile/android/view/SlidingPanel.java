package com.ksider.mobile.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;
import com.ksider.mobile.android.WebView.Constants;

public class SlidingPanel extends RelativeLayout {
	protected float mFadeDegree = (float) 0.5;
	protected Paint mFadePaint;
	public SlidingPanel(Context context) {
		this(context, null);
	}
	
	public SlidingPanel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public SlidingPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mFadePaint = new Paint();
	}
	public void drawFade(Canvas canvas, float openPercent, int left, int right) {
		Log.v(Constants.LOG_TAG, "drawFade:left"+left+"right:"+right);
		final int alpha = (int) (mFadeDegree * 255 * Math.abs(openPercent));
		mFadePaint.setColor(Color.argb(alpha, 0, 0, 0));
		canvas.drawRect(left, 0, right, getHeight(), mFadePaint);
	}
}
