package com.ksider.mobile.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.ksider.mobile.android.view.pulltozoomview.PullToZoomScrollView;

public class ResponsiveScrollView extends PullToZoomScrollView {
	public interface OnScrollEventListener {
		public void onEndScroll();
	}

	private boolean mIsFling;
	private OnScrollEventListener mOnEndScrollListener;
	private int mOriginBottom = -1; 
	public ResponsiveScrollView(Context context) {
		super(context);
	}

	public ResponsiveScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ResponsiveScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev){
		return super.onTouchEvent(ev);
	}


	@Override
	public void fling(int velocityY) {
		super.fling(velocityY);
		mIsFling = true;
	}

	@Override
	protected void onScrollChanged(int x, int y, int oldX, int oldY) {
		super.onScrollChanged(x, y, oldX, oldY);
		if (mIsFling) {
			if (Math.abs(y - oldY) < 40 || y >= getMeasuredHeight() || y == 0) {
				if (mOnEndScrollListener != null) {
					mOnEndScrollListener.onEndScroll();
				}
				if(mOriginBottom<0){
					mOriginBottom = getMeasuredHeight();
				}
				mIsFling = false;
			}
		}
	}

	public void setOnEndScrollListener(OnScrollEventListener mOnEndScrollListener) {
		this.mOnEndScrollListener = mOnEndScrollListener;
	}
}
