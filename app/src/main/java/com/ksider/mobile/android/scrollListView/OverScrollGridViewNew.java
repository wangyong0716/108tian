package com.ksider.mobile.android.scrollListView;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import com.ksider.mobile.android.view.paging.gridview.GridViewWithHeaderAndFooter;

/**
 * Created by yong on 9/16/15.
 */
public class OverScrollGridViewNew extends GridViewWithHeaderAndFooter {
    private Context context;

    private float scrollY = 0;

    private int downDistance;
    private int upDistance;

    private float ratio = 0.4f;

    private int pointIndex = -1;

    private boolean canExpandHeader;
    private boolean canExpandFooter;

    private boolean moveUpAble = true;
    private boolean moveDownAble = true;

    private int originTop = 0;
    private int originBottom = 0;
    private int ANIM_TIME = 300;

    public OverScrollGridViewNew(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public OverScrollGridViewNew(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    public OverScrollGridViewNew(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    public void initView() {
        setOverScrollMode(OVER_SCROLL_NEVER);
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    public void getOriginLocation() {
        originTop = getTop();
        originBottom = getBottom();
        Log.v("AAA", "originTop=" + originTop + "|originBottom=" + originBottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.v("SSS", "ACTION_DOWN");
                getOriginLocation();
//                if (topAnim.isFinished() && bottomAnim.isFinished()) {
//                    getOriginLocation();
//                }
//
//                if (!topAnim.isFinished()) {
//                    topAnim.abortAnimation();
//                }
//                if (!bottomAnim.isFinished()) {
//                    bottomAnim.abortAnimation();
//                }
//                downDistance = header.getLayoutParams().height;//downDistance = header.getMeasuredHeight() cannot get the real height
//                upDistance = footer.getLayoutParams().height;
                canExpandHeader = !canScrollVertically(-1);
                canExpandFooter = !canScrollVertically(1);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                Log.v("SSS", "ACTION_UP");
                resetView();
                pointIndex = -1;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.v("SSS", "ACTION_MOVE");
                if (!canExpandFooter && !canExpandHeader) {
                    canExpandHeader = !canScrollVertically(-1);
                    canExpandFooter = !canScrollVertically(1);
                    pointIndex = event.getPointerCount() - 1;
                    scrollY = event.getY(pointIndex);
                    break;
                }
                if (event.getPointerCount() - 1 != pointIndex) {
                    Log.v("SSS", "change point");
                    pointIndex = event.getPointerCount() - 1;
                    scrollY = event.getY(pointIndex);
                } else {
                    float nowY = event.getY(pointIndex);
                    float deltaY = nowY - scrollY;
                    scrollY = nowY;
                    Log.v("SSS", "nowY=" + nowY);
//                    Log.v("AAA","canExpandHeader="+canExpandHeader+"|canExpandFooter="+canExpandFooter);
//                    Log.v("AAA","deltaY="+deltaY+"|downDistance="+downDistance+"|upDistance="+upDistance);
                    if (canExpandHeader && (deltaY > 0 || downDistance > 0)) {
                        Log.v("SSS", "down");
                        moveDown(deltaY);
                    } else if (canExpandFooter && (deltaY < 0 || upDistance > 0)) {
                        Log.v("SSS", "up");
                        moveUp(deltaY);
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setMoveUpAble(boolean moveUpAble) {
        this.moveUpAble = moveUpAble;
    }

    public void setMoveDownAble(boolean moveDownAble) {
        this.moveDownAble = moveDownAble;
    }

    public void resetView() {
        pointIndex = -1;
        canExpandFooter = false;
        canExpandHeader = false;
        if (downDistance > 0 && moveDownAble && originBottom > 0) {
//            animateUp();
            Log.v("AAA", "reset->downDistance=" + downDistance);
            boundBack(downDistance);
            downDistance = 0;
        }
        if (upDistance > 0 && moveUpAble && originBottom > 0) {
//            animateDown();
            Log.v("AAA", "reset->upDistance=" + upDistance);
            boundBack(-upDistance);
            upDistance = 0;
        }
    }

    private void moveDown(float deltaY) {
        if (moveDownAble && originBottom > 0) {
            deltaY *= ratio;
            downDistance += deltaY;
            setHeaderPadding(downDistance);
//            smoothScrollToPosition(0);
//            Log.v("CCC","scrollBy="+downDistance);
//            scrollTo(0, -downDistance);
//            postInvalidate();
        }
    }

    private void moveUp(float deltaY) {
        if (moveUpAble && originBottom > 0) {
            deltaY *= ratio;
            upDistance -= deltaY;
            setFooterPadding(upDistance);
//            tryToScrollToBottomSmoothly();
        }
    }

    private void setHeaderPadding(float padding) {
//        if (header != null && header.getLayoutParams() != null) {
//            ViewGroup.LayoutParams layoutParams = header.getLayoutParams();
//            layoutParams.height = Math.round(padding);
//            header.setMinimumHeight(Math.round(padding));
//            header.setLayoutParams(layoutParams);
        Log.v("AAA", "downDistance=" + downDistance + "|originTop=" + originTop + "|getTop=" + getTop());
        this.layout(getLeft(), downDistance + originTop, getRight(), downDistance + originBottom);
//        }
    }

    private void setFooterPadding(float padding) {
//        if (footer != null && footer.getLayoutParams() != null) {
//            ViewGroup.LayoutParams layoutParams = footer.getLayoutParams();
//            layoutParams.height = Math.round(padding);
//            footer.setLayoutParams(layoutParams);
        Log.v("AAA", "upDistance=" + upDistance);
        this.layout(getLeft(), originTop - upDistance, getRight(), originBottom - upDistance);
//        }
    }

//    private void animateUp() {
//        topAnim.startAnimation(originHeaderHeight);
//    }
//
//    private void animateDown() {
//        bottomAnim.startAnimation(originFooterHeight);
//    }

    private void boundBack(int top) {
        Log.v("AAA", "top=" + top + "|getTop=" + getTop() + "|originTop=" + originTop + "|originBottom=" + originBottom);
        TranslateAnimation anim = new TranslateAnimation(0, 0, top, 0);
        anim.setDuration(ANIM_TIME);
        anim.setInterpolator(new DecelerateInterpolator());
        startAnimation(anim);
        this.layout(getLeft(), originTop, getRight(), originBottom);
    }
}
