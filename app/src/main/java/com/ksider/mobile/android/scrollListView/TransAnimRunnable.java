package com.ksider.mobile.android.scrollListView;

import android.os.SystemClock;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

/**
 * Created by yong on 9/18/15.
 */
public class TransAnimRunnable implements Runnable {
    private ViewGroup mRoot;
    private static final long DEFAULT_DURATION = 230;
    private long mDuration;
    private boolean mIsFinished = true;
    private long mStartTime;
    private int mDistance;

    private int height;
    private ViewGroup mViewGroup;

    private static final Interpolator sInterpolator = new Interpolator() {
        /**
         * get change rate by time retio
         * @param paramAnonymousFloat   range[0,1.0]
         * @return [1.0, 0], return 0 if paramAnonymousFloat=0;return 1 if paramAnonymousFloat=1;asc rate
         */
        public float getInterpolation(float paramAnonymousFloat) {
            float f = paramAnonymousFloat - 1.0F;
            return 1.0F + f * (f * (f * (f * f)));
        }
    };

    public TransAnimRunnable(ViewGroup root, ViewGroup mViewGroup) {
        this.mRoot = root;
        this.mViewGroup = mViewGroup;
    }

    public void abortAnimation() {
        mIsFinished = true;
    }

    public boolean isFinished() {
        return mIsFinished;
    }

    public void run() {
        if (mViewGroup != null) {
            int dis;
            ViewGroup.LayoutParams localLayoutParams;
            if ((!mIsFinished) && (mDistance != 0)) {
                float ratio = ((float) SystemClock.currentThreadTimeMillis() - (float) mStartTime) / (float) mDuration;
                if (ratio < 1) {
                    dis = (int) (mDistance * sInterpolator.getInterpolation(ratio));
                    localLayoutParams = mViewGroup.getLayoutParams();
                    localLayoutParams.height = height + dis;
                    mViewGroup.setLayoutParams(localLayoutParams);
                    mRoot.post(this);
//                    Log.v("AAA","height="+height+dis);
                } else {
                    dis = (int) (mDistance * sInterpolator.getInterpolation(1));
                    localLayoutParams = mViewGroup.getLayoutParams();
                    localLayoutParams.height = height + dis;
                    mViewGroup.setLayoutParams(localLayoutParams);
                    mIsFinished = true;
                }
            }
        }
    }

    public void startAnimation(int toHeight) {
        startAnimation(DEFAULT_DURATION, toHeight);
    }

    public void startAnimation(long duration, int toHeight) {
        if (mRoot != null && mViewGroup != null) {
            ViewGroup.LayoutParams localLayoutParams = mViewGroup.getLayoutParams();
            height = localLayoutParams.height;
            mDuration = duration;
            mDistance = toHeight - height;
            mIsFinished = false;
            mStartTime = SystemClock.currentThreadTimeMillis();
//            Log.v("AAA", "startTime=" + mStartTime + "|height=" + height + "|distance=" + mDistance);
            mRoot.post(this);
        }
    }
}
