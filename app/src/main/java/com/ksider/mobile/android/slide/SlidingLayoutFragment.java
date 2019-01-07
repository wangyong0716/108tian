package com.ksider.mobile.android.slide;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class SlidingLayoutFragment extends FrameLayout {

    /*可根据手势滑动的View*/
    private SlidingView mSlidingView;
    /*需要绑定的Activity*/
    private Activity mActivity;
    /*滑动View 的滑动监听*/
    private SlidingView.OnPageChangeListener mPageChangeListener;
    /*这个是当Activity 滑动到可以结束的时候用到的常量*/
    private final int POSITION_FINISH = 1;
    /*页面的View*/
    private View mContextView;
    /*用于定制自己的动画效果的接口*/
    private OnAnimListener mAnimListener;

    private Fragment mFragment;

    public SlidingLayoutFragment(Context context, Fragment fragment) {
        this(context, null, fragment);
    }

    public SlidingLayoutFragment(Context context, AttributeSet attrs, Fragment fragment) {
        this(context, attrs, 0, fragment);
    }

    public SlidingLayoutFragment(Context context, AttributeSet attrs, int defStyleAttr, Fragment fragment) {
        super(context, attrs, defStyleAttr);
        /*初始化可滑动的View*/
        mSlidingView = new SlidingView(context);
        /*吧可滑动的View 添加到当前Layout 中*/
        addView(mSlidingView);
        /*设置滑动监听*/
        mPageChangeListener = new SlidingOnPageChangeListener();
        mSlidingView.setOnPageChangeListener(mPageChangeListener);
        mActivity = (Activity) context;

        mFragment = fragment;
//        /*绑定Activity 到可滑动的View 上面*/
//        bindActivity(mActivity);
        bindFragment(mFragment);
    }

    /**
     * 侧滑View 和Fragment 绑定
     *
     * @param fragment
     */
    private void bindFragment(Fragment fragment) {
        /*获取Activity 的最顶级ViewGroup*/
        ViewGroup decorView = (ViewGroup) fragment.getView();
        /*获取Activity 显示内容区域的ViewGroup，包行ActionBar*/

        ViewGroup child = (ViewGroup) decorView.getChildAt(0);
        Log.v("AAA", "count=" + decorView.getChildCount() + "|childCount=" + child.getChildCount());
        decorView.removeView(child);
        setContentView(child);
        decorView.addView(this);
    }

    /**
     * 侧滑View 和Activity 绑定
     *
     * @param activity
     */
    private void bindActivity(Activity activity) {
        /*获取Activity 的最顶级ViewGroup*/
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        /*获取Activity 显示内容区域的ViewGroup，包行ActionBar*/
        ViewGroup child = (ViewGroup) decorView.getChildAt(0);
        decorView.removeView(child);
        setContentView(child);
        decorView.addView(this);
    }

    private void setContentView(View view) {
        mContextView = view;
        mSlidingView.setContent(view);

    }

    private class SlidingOnPageChangeListener implements SlidingView.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            /*到达了结束Activity 的条件*/
            if (position == POSITION_FINISH) {
                /*结束当前 */
//                mActivity.finish();
                mFragment.getFragmentManager().popBackStack();
            }
            if (mAnimListener != null) {
                mAnimListener.onAnimationSet(mContextView, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageSelected(int position) {

        }
    }

    /**
     * 设置自己定义的动画
     *
     * @param listener
     */
    public void setOnAnimListener(OnAnimListener listener) {
        mAnimListener = listener;
        mSlidingView.setShouldDraw(false);
    }

    /**
     * 定制动画的接口
     */
    public interface OnAnimListener {
        /**
         * 重写这个方法来定制动画
         *
         * @param view
         * @param offSet
         * @param offSetPix
         */
        void onAnimationSet(View view, float offSet, int offSetPix);
    }

    /**
     * 默认的动画效果
     */
    public static class SimpleAnimImpl implements OnAnimListener {
        private final int MAX_ANGLE = 25;

        @Override
        public void onAnimationSet(View view, float offSet, int offSetPix) {
            view.setPivotX(view.getWidth() / 2.0F);
            view.setPivotY(view.getHeight());
            View.ROTATION.set(view, MAX_ANGLE * offSet);
        }
    }

}
