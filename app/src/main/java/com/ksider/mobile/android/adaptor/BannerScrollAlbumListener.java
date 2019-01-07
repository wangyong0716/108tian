package com.ksider.mobile.android.adaptor;

import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

public class BannerScrollAlbumListener implements OnPageChangeListener {
    protected ViewPager mPager;
    private int mPageCount = 0;
    protected OnPageChangeListener mListener;
    //banner scroll
//    private boolean bannerChanged = false;
//    private int bannerEnd = 0;
//    private int bannerStart = 0;
//    private int currentBanner = 0;

    public BannerScrollAlbumListener(ViewPager pager, Boolean swipe) {
        super();
        mPager = pager;
        mPageCount = mPager.getAdapter().getCount();
//        bannerStart = 1;
//        bannerEnd = mPageCount - 2;
//        currentBanner = bannerStart;

        if (mPageCount > 2 && swipe) {
            final Handler handler = new Handler();
            final Runnable delay = new Runnable() {
                @Override
                public void run() {
                    if (((BannerAlbumAdaptor)mPager.getAdapter()).getSize()>1) {
                        int item = mPager.getCurrentItem();
                        item = (item + 1) % mPageCount;
                        mPager.setCurrentItem(item);
                    }
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
//        bannerChanged = true;
//        if (position > bannerEnd) {
//            currentBanner = bannerStart;
//        } else if (position < bannerStart) {
//            currentBanner = bannerEnd;
//        } else {
//            currentBanner = position;
//        }
//
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
//        if (ViewPager.SCROLL_STATE_IDLE == state) {
//            if (bannerChanged) {
//                bannerChanged = false;
//                mPager.setCurrentItem(currentBanner, false);
//            }
//        }

        if (mListener != null) {
            mListener.onPageScrollStateChanged(state);
        }
    }
}
