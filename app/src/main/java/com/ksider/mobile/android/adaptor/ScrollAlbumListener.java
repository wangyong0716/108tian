package com.ksider.mobile.android.adaptor;

import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

public class ScrollAlbumListener implements OnPageChangeListener {
    protected ViewPager mPager;
    private int mPageCount = 0;
    protected OnPageChangeListener mListener;

    public ScrollAlbumListener(ViewPager pager, Boolean swipe) {
        super();
        mPager = pager;
        mPageCount = mPager.getAdapter().getCount();

        if (mPageCount > 2 && swipe) {
            final Handler handler = new Handler();
            final Runnable delay = new Runnable() {
                @Override
                public void run() {
                    int item = mPager.getCurrentItem();
                    item = (item + 1) % mPageCount;
                    mPager.setCurrentItem(item);
                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(this, 6000);
                }
            };
            handler.postDelayed(delay, 5000);
        }
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void onPageSelected(int position) {
        if (mListener != null) {
            mListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mListener != null) {
            mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
        if (mListener != null) {
            mListener.onPageScrollStateChanged(state);
        }
    }
}
