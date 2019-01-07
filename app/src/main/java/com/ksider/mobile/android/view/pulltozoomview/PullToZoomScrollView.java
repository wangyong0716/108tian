package com.ksider.mobile.android.view.pulltozoomview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.scrollListView.OverUpScrollView;

/**
 * Author: ZhuWenWu Version V1.0 Date: 2014/9/1 10:50. Description: Modification
 * History: Date Author Version Description
 * --------------------------------------
 * --------------------------------------------- 2014/9/1 ZhuWenWu 1.0 1.0 Why &
 * What is modified:
 */
public class PullToZoomScrollView extends OverUpScrollView {
    private static final String TAG = Constants.LOG_TAG;

    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float paramAnonymousFloat) {
            float f = paramAnonymousFloat - 1.0F;
            return 1.0F + f * (f * (f * (f * f)));
        }
    };
    // private static final Interpolator sInterpolator = new
    // DecelerateInterpolator();
    private View mContentView;// 中间View
    private View mHeadView;// 头部View
    private View mZoomView;// 缩放拉伸View

    private FrameLayout mContentContainer;
    private FrameLayout mHeaderContainer;
    // private FrameLayout mZoomContainer;
    private LinearLayout mRootContainer;

    private OnScrollViewChangedListener mOnScrollListener;
    private OnScrollViewZoomListener onScrollViewZoomListener;
    private ScalingRunnable mScalingRunnable;

    private int mScreenHeight;
    private int mZoomHeight;
    private int mZoomWidth;

    private int mActivePointerId = -1;
    private float mLastMotionY = -1.0F;
    private float mLastScale = -1.0F;
    private float mMaxScale = -1.0F;
    private boolean isHeaderTop = true;
    private boolean isEnableZoom = true;
    private boolean isParallax = false;

    public PullToZoomScrollView(Context context) {
        this(context, null);
    }

    public PullToZoomScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToZoomScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mHeaderContainer = new FrameLayout(getContext());
        mContentContainer = new FrameLayout(getContext());

        mRootContainer = new LinearLayout(getContext());
        mRootContainer.setOrientation(LinearLayout.VERTICAL);

        if (attrs != null) {
            LayoutInflater mLayoutInflater = LayoutInflater.from(getContext());
            // 初始化状态View
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PullToZoomScrollView);
            int height = (int) a.getDimension(R.styleable.PullToZoomScrollView_headerHeight, 200);
            mHeaderContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, height));

            int zoomViewResId = a.getResourceId(R.styleable.PullToZoomScrollView_scrollZoomView, 0);
            if (zoomViewResId > 0) {
                mZoomView = mLayoutInflater.inflate(zoomViewResId, mHeaderContainer, false);
                mHeaderContainer.addView(mZoomView);
            }
            int headViewResId = a.getResourceId(R.styleable.PullToZoomScrollView_scrollHeadView, 0);
            if (headViewResId > 0) {
                mHeadView = mLayoutInflater.inflate(headViewResId, mHeaderContainer, false);
                mHeaderContainer.addView(mHeadView);
            }
            int contentViewResId = a.getResourceId(R.styleable.PullToZoomScrollView_scrollContentView, 0);
            if (contentViewResId > 0) {
                mContentView = mLayoutInflater.inflate(contentViewResId, null, false);
                mContentContainer.addView(mContentView);
            }
            a.recycle();
        }

        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        mScreenHeight = localDisplayMetrics.heightPixels;
        mZoomWidth = localDisplayMetrics.widthPixels;
        mScalingRunnable = new ScalingRunnable();

        mRootContainer.addView(mHeaderContainer);
        mRootContainer.addView(mContentContainer);

        mRootContainer.setClipChildren(false);
        mHeaderContainer.setClipChildren(false);

        addView(mRootContainer);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setEnableZoom(boolean isEnableZoom) {
        this.isEnableZoom = isEnableZoom;
    }

    public void setParallax(boolean isParallax) {
        this.isParallax = isParallax;
    }

    public void setOnScrollListener(OnScrollViewChangedListener mOnScrollListener) {
        this.mOnScrollListener = mOnScrollListener;
    }

    public void setOnScrollViewZoomListener(OnScrollViewZoomListener onScrollViewZoomListener) {
        this.onScrollViewZoomListener = onScrollViewZoomListener;
    }

    public void setContentContainerView(View view) {
        if (mContentContainer != null) {
            mContentContainer.removeAllViews();
            mContentView = view;
            mContentContainer.addView(view);
        }
    }

    public void setHeaderContainer(View view) {
        if (mHeaderContainer != null && view != null) {
            mHeaderContainer.removeAllViews();
            mHeadView = view;
            if (mZoomView != null) {
                mHeaderContainer.addView(mZoomView);
            }
            mHeaderContainer.addView(mHeadView);
        }
    }

    public void showHeaderView() {
        if (mZoomView != null || mHeadView != null) {
            mHeaderContainer.setVisibility(VISIBLE);
        }
    }

    public void hideHeaderView() {
        if (mZoomView != null || mHeadView != null) {
            mHeaderContainer.setVisibility(GONE);
        }
    }


    public FrameLayout getHeaderContainer() {
        return mHeaderContainer;
    }

    public View getZoomView() {
        return mZoomView;
    }

    public View getContentView() {
        return mContentView;
    }

    public View getHeadView() {
        return mHeadView;
    }

    public void setZoomHeight(int mZoomHeight) {
        this.mZoomHeight = mZoomHeight;
    }

    public LinearLayout getRootContainer() {
        return mRootContainer;
    }

    private void endScaling() {
        if (mHeaderContainer.getBottom() >= mZoomHeight) {
            Log.d(TAG, "endScaling");
        }
        mScalingRunnable.startAnimation(230L);
    }

    // public boolean onInterceptTouchEvent(MotionEvent event) {
    // return super.onInterceptTouchEvent(event);
    // }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mZoomHeight == 0) {
            if (mZoomView != null) {
                mZoomHeight = mHeaderContainer.getHeight();
                mZoomWidth = mHeaderContainer.getWidth();
            }
        }
    }

    @Override
    protected void onScrollChanged(int left, int top, int oldLeft, int oldTop) {
        super.onScrollChanged(left, top, oldLeft, oldTop);
        if (isEnableZoom) {
            isHeaderTop = getScrollY() <= 0;
            if (isParallax) {
                float f = mZoomHeight - mZoomView.getBottom() + getScrollY();
                if ((f > 0.0F) && (f < mZoomHeight)) {
                    int i = (int) (0.65D * f);
                    mHeaderContainer.scrollTo(0, -i);
                } else if (mHeaderContainer.getScrollY() != 0) {
                    mHeaderContainer.scrollTo(0, 0);
                }
            }
        }
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollChanged(left, top, oldLeft, oldTop);
        }
    }

    public
    @Override
    boolean onTouchEvent(MotionEvent ev) {
        if (isHeaderTop && isEnableZoom) {
            switch (0xFF & ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    int j = ev.findPointerIndex(this.mActivePointerId);
                    if (j == -1) {
                        Log.e(TAG, "Invalid pointerId = " + mActivePointerId + " in onTouchEvent");
                    } else {
                        if (mLastMotionY == -1.0F) {
                            mLastMotionY = ev.getY(j);
                        }
                        if (mHeaderContainer.getBottom() >= mZoomHeight) {
                            ViewGroup.LayoutParams headLayoutParams = mHeaderContainer.getLayoutParams();
                            float f = ((ev.getY(j) - mLastMotionY + mHeaderContainer.getBottom()) / mZoomHeight - mLastScale)
                                    / 2.0F + mLastScale;
                            if ((mLastScale <= 1.0D) && (f < mLastScale)) {
                                headLayoutParams.height = mZoomHeight;
                                mHeaderContainer.setLayoutParams(headLayoutParams);
                                return super.onTouchEvent(ev);
                            }
                            mLastScale = Math.min(Math.max(f, 1.0F), mMaxScale);
                            headLayoutParams.height = ((int) (mZoomHeight * mLastScale));
                            if (headLayoutParams.height < mScreenHeight * 0.7) {
                                mHeaderContainer.setLayoutParams(headLayoutParams);
                            }
                            mLastMotionY = ev.getY(j);
                            return true;
                        }
                        this.mLastMotionY = ev.getY(j);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    reset();
                    endScaling();
                    if (onScrollViewZoomListener != null) {
                        onScrollViewZoomListener.onFinish();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    int i = ev.getActionIndex();
                    Log.v(TAG, "ACTION_CANCEL");
                    mLastMotionY = ev.getY(i);
                    mActivePointerId = ev.getPointerId(i);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    Log.v(TAG, "ACTION_POINTER_DOWN");
                    onSecondaryPointerUp(ev);
                    try {
                        mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId));
                    } catch (Exception e) {

                    }
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isHeaderTop && isEnableZoom) {
            switch (0xFF & ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_OUTSIDE:
                    if (!mScalingRunnable.isFinished()) {
                        mScalingRunnable.abortAnimation();
                    }
                    mLastMotionY = ev.getY();
                    mActivePointerId = ev.getPointerId(0);
                    mMaxScale = (mScreenHeight / mZoomHeight);
                    mLastScale = (mZoomView.getBottom() / mZoomHeight);
                    if (onScrollViewZoomListener != null) {
                        onScrollViewZoomListener.onStart();
                    }
                    Log.v(Constants.LOG_TAG, "ACTION_DOWN");
                    break;

                case MotionEvent.ACTION_UP:
                    reset();
                    endScaling();
                    if (onScrollViewZoomListener != null) {
                        onScrollViewZoomListener.onFinish();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    int i = ev.getActionIndex();
                    Log.v(TAG, "ACTION_CANCEL");
                    mLastMotionY = ev.getY(i);
                    mActivePointerId = ev.getPointerId(i);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    Log.v(TAG, "ACTION_POINTER_DOWN");
                    onSecondaryPointerUp(ev);
                    try {
                        mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId));
                    } catch (Exception e) {
                        //数组越界
                        e.printStackTrace();
                    }
                    break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void onSecondaryPointerUp(MotionEvent paramMotionEvent) {
        int i = (paramMotionEvent.getAction()) >> 8;
        if (paramMotionEvent.getPointerId(i) == mActivePointerId)
            if (i != 0) {
                mLastMotionY = paramMotionEvent.getY(0);
                mActivePointerId = paramMotionEvent.getPointerId(0);
            }
    }

    private void reset() {
        this.mActivePointerId = -1;
        this.mLastMotionY = -1.0F;
        this.mMaxScale = -1.0F;
        this.mLastScale = -1.0F;
    }

    class ScalingRunnable implements Runnable {
        long mDuration;
        boolean mIsFinished = true;
        float mScale;
        long mStartTime;

        ScalingRunnable() {
        }

        public void abortAnimation() {
            mIsFinished = true;
        }

        public boolean isFinished() {
            return mIsFinished;
        }

        public void run() {
            if ((!mIsFinished) && (mScale > 1.0D)) {
                float f1 = ((float) SystemClock.currentThreadTimeMillis() - (float) mStartTime) / (float) mDuration;
                if (f1 < 0 || f1 > 1.0D) {
                    return;
                }
                float f2 = mScale - (mScale - 1.0F) * sInterpolator.getInterpolation(f1);
                f2 = Math.min(mScale, f2);
                ViewGroup.LayoutParams headLayoutParams;
                headLayoutParams = mHeaderContainer.getLayoutParams();
                if (f2 > 1.0F) {
                    headLayoutParams.height = ((int) (f2 * mZoomHeight));
                    mHeaderContainer.setLayoutParams(headLayoutParams);
                    post(this);
                    return;
                }
                mIsFinished = true;
            }
        }

        public void startAnimation(long paramLong) {
            mStartTime = SystemClock.currentThreadTimeMillis();
            mDuration = paramLong;
            mScale = ((float) (mZoomView.getBottom()) / mZoomHeight);
            mIsFinished = false;
            post(this);
        }
    }

    public interface OnScrollViewChangedListener {
        public void onScrollChanged(int left, int top, int oldLeft, int oldTop);
    }

    public interface OnScrollViewZoomListener {
        public void onStart();

        public void onFinish();
    }
}
