package com.ksider.mobile.android.scrollListView;

import android.os.SystemClock;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

/**
 * Created by yong on 9/18/15.
 */
public class TransAnimRunnableGrid implements Runnable {
    private ViewGroup mRoot;
    private static final long DEFAULT_DURATION = 300;
    private long mDuration;
    private boolean mIsFinished = true;
    private long mStartTime;
    private int mDistance;
    private int direction;
    private int dis;
    private ViewGroup.MarginLayoutParams mlp;

//    private ViewGroup mViewGroup;


    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    private static final Interpolator sInterpolator = new Interpolator() {
        /**
         * get change rate by time retio
         * @param paramAnonymousFloat   range[0,1.0]
         * @return [1.0, 0], return 0 if paramAnonymousFloat=0;return 1 if paramAnonymousFloat=1;asc rate
         */
        public float getInterpolation(float paramAnonymousFloat) {
            float f = paramAnonymousFloat - 1.0F;
            return 1.0F + f * (f * f);
        }
    };

    public TransAnimRunnableGrid(ViewGroup root) {
        this.mRoot = root;
    }

    public int abortAnimation() {
        mIsFinished = true;
        return dis;
    }

    public boolean isFinished() {
        return mIsFinished;
    }

    public void run() {
        if (mRoot != null) {
            if ((!mIsFinished) && (mDistance != 0)) {
                float ratio = ((float) SystemClock.currentThreadTimeMillis() - (float) mStartTime) / (float) mDuration;
                if (ratio < 1) {
                    dis = mDistance - (int) (mDistance * sInterpolator.getInterpolation(ratio));
                    mlp = (ViewGroup.MarginLayoutParams) mRoot.getLayoutParams();
                    if (direction >= 0) {
                        mlp.setMargins(0, dis, 0, 0);
                    } else {
                        mlp.setMargins(0, 0, 0, dis);
                    }
                    mRoot.setLayoutParams(mlp);
                    mRoot.post(this);
//                    Log.v("AAA", "height=" + dis);
                } else {
                    dis = 0;
                    mlp = (ViewGroup.MarginLayoutParams) mRoot.getLayoutParams();
                    if (direction >= 0) {
                        mlp.setMargins(0, 0, 0, 0);
                    } else {
                        mlp.setMargins(0, 0, 0, 0);
                    }
                    mRoot.setLayoutParams(mlp);
                    mIsFinished = true;
                }
            }
        }
    }

    public void startAnimation(int distance, int direction) {
        startAnimation(DEFAULT_DURATION, distance, direction);
    }

    public void startAnimation(long duration, int distance, int direction) {
        if (mRoot != null) {
            mDuration = duration;
            mDistance = distance;
            this.direction = direction;
            mIsFinished = false;
            mStartTime = SystemClock.currentThreadTimeMillis();
//            Log.v("AAA", "startTime=" + mStartTime + "|direction=" + direction + "|distance=" + mDistance);
            mRoot.post(this);
        }
    }
}
