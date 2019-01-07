/*
 * ******************************************************************************
 *   Copyright (c) 2014 J Withey.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */

package com.ksider.mobile.android.swipe;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import com.ksider.mobile.android.WebView.R;
import com.nineoldandroids.animation.AnimatorListenerAdapter;

import static com.nineoldandroids.view.ViewHelper.*;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * This is based on code by Roman Nurik.
 * See this link for original code https://github.com/romannurik/Android-SwipeToDismiss
 * </p>
 * <p/>
 * A {@link View.OnTouchListener} that makes the list items in a {@link ListView}
 * dismissable. {@link ListView} is given special treatment because by default it handles touches
 * for its list items... i.e. it's in charge of drawing the pressed state (the list selector),
 * handling list item clicks, etc.
 * <p/>
 * <p>After creating the listener, the caller should also call
 * {@link ListView#setOnScrollListener(AbsListView.OnScrollListener)}, passing
 * in the scroll listener returned by {@link #makeScrollListener()}. If a scroll listener is
 * already assigned, the caller should still pass scroll changes through to this listener. This will
 * ensure that this {@link SwipeDismissListViewTouchListener} is paused during list view
 * scrolling.</p>
 * <p/>
 * <p>This class Requires API level 8 or later </p>
 */
public class SwipeDismissListViewTouchListener implements View.OnTouchListener {
    // Cached ViewConfiguration and system-wide constant values
    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private long mAnimationTime;

    // Fixed properties
    private ListView mListView;
    private DismissCallbacks mCallbacks;
    private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero
    private boolean belowApi12 = false;
    private boolean belowApi16 = false;

    // Transient properties
    private float mDownX;
    private float mDownY;
    private boolean mSwiping;
    private int mSwipingSlop;
    private VelocityTracker mVelocityTracker;
    private int mDownPosition;
    private View mDownView;
    private boolean mPaused;
    private int firstSlop;
    private boolean firstSlopAlreadySet = false;
    private int animationCount = 0;
    private boolean currentlyScrolling = false;

    private Context mContext = null;

    /**
     * The callback interface used by {@link SwipeDismissListViewTouchListener} to inform its client
     * about a successful dismissal of one or more list item positions.
     */
    public interface DismissCallbacks {
        /**
         * Called to determine whether the given position can be dismissed.
         */
        boolean canDismiss(int position);

        /**
         * Called when the user has indicated they she would like to dismiss an item
         *
         * @param viewToDismiss The view to dismiss
         * @param adapterPos    The position within the adapter to dismiss
         */
        void onDismiss(View viewToDismiss, int adapterPos);

        /**
         * For disabling the adding of new items while the swiping/scroll animations are running, as
         * this can cause visual glitches if you were to quickly swipe and add new view without this
         * functionality. Disable/enable your 'New item' button/functionality in your activity here.
         */
        void currentlyAnimatingSwipeOrScroll(boolean isAnimating);
    }

    /**
     * Constructs a new swipe-to-dismiss touch listener for the given list view.
     *
     * @param listView  The list view whose items should be dismissable.
     * @param callbacks The callbacks to trigger at various points during the add/remove animation process
     */
    public SwipeDismissListViewTouchListener(ListView listView, DismissCallbacks callbacks) {
        mContext = listView.getContext();
        trashWidth = (int) mContext.getResources().getDimension(R.dimen.trash_width);
        ViewConfiguration vc = ViewConfiguration.get(mContext);
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = listView.getContext().getResources().getInteger(
                android.R.integer.config_mediumAnimTime);
        mListView = listView;
        mCallbacks = callbacks;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            belowApi12 = true;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            belowApi16 = true;
        }
    }

    /**
     * Enables or disables (pauses or resumes) watching for swipe-to-dismiss gestures.
     *
     * @param enabled Whether or not to watch for gestures.
     */
    public void setEnabled(boolean enabled) {
        mPaused = !enabled;
    }

    /**
     * Returns an {@link AbsListView.OnScrollListener} to be added to the {@link
     * ListView} using {@link ListView#setOnScrollListener(AbsListView.OnScrollListener)}.
     * If a scroll listener is already assigned, the caller should still pass scroll changes through
     * to this listener. This will ensure that this {@link SwipeDismissListViewTouchListener} is
     * paused during list view scrolling.</p>
     *
     * @see SwipeDismissListViewTouchListener
     */
    public AbsListView.OnScrollListener makeScrollListener() {
        return new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    mCallbacks.currentlyAnimatingSwipeOrScroll(true);
                    currentlyScrolling = true;
                } else {
                    mCallbacks.currentlyAnimatingSwipeOrScroll(false);
                    currentlyScrolling = false;
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        };
    }

    int trashWidth = 0;
    int translate = 0;
    int beginX = 0;
    int itemPosition = 0;

    @SuppressLint("NewApi")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mViewWidth < 2) {
            mViewWidth = mListView.getWidth();
        }


        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (mPaused || animationCount > 0) {
                    return false;
                }
                // Find the child view that was touched (perform a hit test)
                Rect rect = new Rect();
                int childCount = mListView.getChildCount();
                int[] listViewCoords = new int[2];
                mListView.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];

                View child = null;
                for (int i = 0; i < childCount; i++) {
                    child = mListView.getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x, y)) {
                        itemPosition = i;
                        mDownView = child.findViewById(R.id.listview_img_layout);
                        if (mDownView == null) {
                            return false;
                        }
                        beginX = (int) mDownView.getX();
                        break;
                    }
                }
                if (mDownView != null) {
                    try {
                        mDownX = motionEvent.getRawX();
                        mDownY = motionEvent.getRawY();
                        mDownPosition = mListView.getPositionForView(mDownView);
                        if (mCallbacks.canDismiss(mDownPosition)) {
                            mVelocityTracker = VelocityTracker.obtain();
                            mVelocityTracker.addMovement(motionEvent);
                        } else {
                            mDownView = null;
                        }
                    } catch (NullPointerException npe) {
                        npe.printStackTrace();
                    }
                }
                return false;
            }

            case MotionEvent.ACTION_UP: {
                if (mVelocityTracker == null) {
                    break;
                }
                if (!mSwiping) {
                    break;
                }
                mVelocityTracker.addMovement(motionEvent);
                mVelocityTracker.computeCurrentVelocity(1000);
                ++animationCount;
                float deltaX = motionEvent.getRawX() - mDownX;
                if (deltaX > 0) {
                    translate = 0;
                } else if (deltaX < 0) {
                    translate = -trashWidth;
                } else {
                    translate = beginX < 0 ? 0 : -trashWidth;
                }
                if (mDownView != null) {
                    mDownView.animate()
                            .translationX(translate)
                            .alpha(1)
                            .scaleX(1)
                            .scaleY(1)
                            .setInterpolator(new DecelerateInterpolator(1f))
                            .rotation(0)
                            .setDuration((long) ((mAnimationTime / 2f) + 20 * ((Math.abs(deltaX) / mViewWidth))));
                }
//                    .withEndAction(new Runnable() {
//                        @Override
//                        public void run() {
                --animationCount;
//                            if (!currentlyScrolling)
                mCallbacks.currentlyAnimatingSwipeOrScroll(false);
//                        }
//                    });
                ImageView trash = (ImageView) mListView.getChildAt(itemPosition).findViewById(R.id.trash);
                if (trash != null) {
                    if (translate >= 0) {
                        trash.setClickable(false);
                    } else if (translate < 0) {
                        int chiCount = mListView.getChildCount();
                        trash.setClickable(true);
                        View chi = null;
                        for (int i = 0; i < chiCount; i++) {
                            chi = mListView.getChildAt(i).findViewById(R.id.listview_img_layout);
                            if (chi != null && chi.getX() < 0 && i != itemPosition) {
                                ((ImageView) mListView.getChildAt(i).findViewById(R.id.trash)).setClickable(false);
                                chi.animate()
                                        .translationX(0)
                                        .alpha(1)
                                        .scaleX(1)
                                        .scaleY(1)
                                        .setInterpolator(new DecelerateInterpolator(1f))
                                        .rotation(0)
                                        .setDuration((long) ((mAnimationTime / 2f) + 20 * ((Math.abs(translate) / mViewWidth))));
//                                .withEndAction(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        if (!currentlyScrolling)
                                mCallbacks.currentlyAnimatingSwipeOrScroll(false);
//                                    }
//                                });
                            }
                        }
                    }
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                beginX = 0;
                beginX = 0;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                if (mVelocityTracker == null) {
                    break;
                }
                if (mSwiping) {
                    if (mDownView != null) {
                        // cancel
                        if (belowApi12) {
                            animate(mDownView)
                                    .translationX(0)
                                    .alpha(1)
                                    .scaleX(1)
                                    .scaleY(1)
                                    .rotation(0)
                                    .setDuration(mAnimationTime)
                                    .setListener(null);
                        } else {
                            mDownView.animate()
                                    .translationX(0)
                                    .alpha(1)
                                    .scaleX(1)
                                    .scaleY(1)
                                    .rotation(0)
                                    .setDuration(mAnimationTime)
                                    .setListener(null);
                        }
                    }
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                firstSlopAlreadySet = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mVelocityTracker == null) {
                    break;
                } else if (mPaused) {
                    break;
                }
                mVelocityTracker.addMovement(motionEvent);
                float deltaX = motionEvent.getRawX() - mDownX;
                float deltaY = motionEvent.getRawY() - mDownY;
                if (Math.abs(deltaX) > mSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
                    mSwiping = true;
                    mSwipingSlop = (deltaX > 0 ? mSlop : -mSlop);
                    if (!firstSlopAlreadySet) {
                        firstSlop = mSwipingSlop;
                        firstSlopAlreadySet = true;
                    }
                    mListView.requestDisallowInterceptTouchEvent(true);
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (motionEvent.getActionIndex()
                                    << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mListView.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }
                if (!mSwiping || deltaX < 0) {
                    int chiCount = mListView.getChildCount();
                    View chi = null;
                    for (int i = 0; i < chiCount; i++) {
                        chi = mListView.getChildAt(i).findViewById(R.id.listview_img_layout);
                        if (chi != null && chi.getX() < 0 && i != itemPosition) {
                            ((ImageView) mListView.getChildAt(i).findViewById(R.id.trash)).setClickable(false);
                            chi.animate()
                                    .translationX(0)
                                    .alpha(1)
                                    .scaleX(1)
                                    .scaleY(1)
                                    .setInterpolator(new DecelerateInterpolator(1f))
                                    .rotation(0)
                                    .setDuration((long) ((mAnimationTime / 2f) + 20 * ((Math.abs(translate) / mViewWidth))));
//                                .withEndAction(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        if (!currentlyScrolling)
                            mCallbacks.currentlyAnimatingSwipeOrScroll(false);
//                                    }
//                                });
                        }
                    }
                }
                if (mSwiping && mDownView != null) {
                    if (beginX + deltaX - firstSlop < 0) {
                        mDownView.setTranslationX(beginX + deltaX - firstSlop);
                    } else {
                        mDownView.setTranslationX(0);
                    }
                    return true;
                }
                break;
            }
        }
        return false;
    }

    @SuppressLint("NewApi")
    private void dismiss(final View view, final int position, boolean dismissRight, float absVelocityX, float deltaX) {
        if (view == null) {
            --animationCount;
            mCallbacks.currentlyAnimatingSwipeOrScroll(false);
            mCallbacks.onDismiss(view, position);
            return;
        }
        if (belowApi12) {
            animate(view)
                    .translationX(dismissRight ? mViewWidth : -mViewWidth)
                    .alpha(0)
                    .scaleX(0.25f)
                    .scaleY(0.25f)
                    .rotation(dismissRight ? 20 : -20)
                    .setInterpolator(new LinearInterpolator())
                    .setDuration((long) (mAnimationTime / 1.5))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(com.nineoldandroids.animation.Animator animator) {
                            // Restore animated values
                            setAlpha(view, 1);
                            setTranslationX(view, 0);
                            setRotation(view, 0);
                            setScaleX(view, 1);
                            setScaleY(view, 1);
                            --animationCount;
                            mCallbacks.currentlyAnimatingSwipeOrScroll(false);
                            mCallbacks.onDismiss(view, position);
                        }
                    });
        } else if (belowApi16) {
            view.animate()
                    .translationX(dismissRight ? mViewWidth : -mViewWidth)
                    .alpha(0)
                    .scaleX(0.25f)
                    .scaleY(0.25f)
                    .rotation(dismissRight ? 20 : -20)
                    .setInterpolator(new LinearInterpolator())
                    .setDuration((long) (mAnimationTime / 1.5))
                    .setListener(new AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.setAlpha(1);
                            view.setTranslationX(0);
                            view.setRotation(0);
                            view.setScaleX(1);
                            view.setScaleY(1);
                            --animationCount;
                            mCallbacks.currentlyAnimatingSwipeOrScroll(false);
                            mCallbacks.onDismiss(view, position);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    });
        } else {
            view.animate()
                    .translationX(dismissRight ? mViewWidth : -mViewWidth)
                    .alpha(0)
                    .scaleX(0.25f)
                    .scaleY(0.25f)
                    .rotation(dismissRight ? 20 : -20)
                    .setInterpolator(new LinearInterpolator())
                    .setDuration((long) (mAnimationTime / 1.5));
//                .withEndAction(new Runnable() {
//                    @Override
//                    public void run() {
//                        view.setAlpha(1);
//                        view.setTranslationX(0);
//                        view.setRotation(0);
//                        view.setScaleX(1);
//                        view.setScaleY(1);
//                        --animationCount;
//                        mCallbacks.currentlyAnimatingSwipeOrScroll(false);
//                        mCallbacks.onDismiss(view, position);
//                    }
//                });
        }
    }
}


















