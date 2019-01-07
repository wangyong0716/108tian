package com.ksider.mobile.android.scrollListView;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.ksider.mobile.android.view.paging.gridview.GridViewWithHeaderAndFooter;

/**
 * Created by yong on 9/16/15.
 */
public class OverScrollGridView extends GridViewWithHeaderAndFooter {
    private Context context;

    private float scrollY = 0;

    private int downDistance;
    private int upDistance;

    private float ratio = 0.4f;

    private int pointIndex = -1;

    private TransAnimRunnableGrid topAnim;
    private TransAnimRunnableGrid bottomAnim;

    private boolean canExpandHeader;
    private boolean canExpandFooter;

    private boolean moveUpAble = true;
    private boolean moveDownAble = true;

    public OverScrollGridView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public OverScrollGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    public OverScrollGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    public void initView() {
        setOverScrollMode(OVER_SCROLL_NEVER);


        topAnim = new TransAnimRunnableGrid(this);
        bottomAnim = new TransAnimRunnableGrid(this);
    }

//    public void getFooterHeight() {
//        Log.v("AAA", "getFooterHeight");
//        if (getChildCount() < 1) {
//            Log.v("AAA", "count=1");
//            upDistance = 0;
//        } else {
//            int contentHeight = getChildAt(getChildCount() - 1).getBottom() - getChildAt(0).getTop() + getPaddingTop() + getBottom();
//            upDistance = getHeight() - contentHeight;
//            Log.v("AAA", "count=" + getChildCount() + "|contentHeight=" + contentHeight + "|getHeight=" + getHeight());
//        }
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                if (topAnim.isFinished() && bottomAnim.isFinished()) {
//                    getFooterHeight();
//                }
                if (!topAnim.isFinished()) {
                    downDistance = topAnim.abortAnimation();
                } else {
                    downDistance = 0;
                }
                if (!bottomAnim.isFinished()) {
                    upDistance = bottomAnim.abortAnimation();
                } else {
                    upDistance = 0;
                }
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
                resetView();
                pointIndex = -1;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!canExpandFooter && !canExpandHeader) {
                    canExpandHeader = !canScrollVertically(-1);
                    canExpandFooter = !canScrollVertically(1);
                    pointIndex = event.getPointerCount() - 1;
                    scrollY = event.getY(pointIndex);
                    break;
                }
                if (event.getPointerCount() - 1 != pointIndex) {
                    pointIndex = event.getPointerCount() - 1;
                    scrollY = event.getY(pointIndex);
                } else {
                    float nowY = event.getY(pointIndex);
                    float deltaY = nowY - scrollY;
                    scrollY = nowY;
//                    Log.v("AAA","canExpandHeader="+canExpandHeader+"|canExpandFooter="+canExpandFooter);
//                    Log.v("AAA","deltaY="+deltaY+"|downDistance="+downDistance+"|upDistance="+upDistance);
                    if (canExpandHeader && (deltaY > 0 || downDistance > 0)&&Math.abs(deltaY)>3) {
                        moveDown(deltaY);
                    } else if (canExpandFooter && (deltaY < 0 || upDistance > 0)&&Math.abs(deltaY)>3) {
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
        if (downDistance > 0 && moveDownAble) {
            animateUp();
            downDistance = 0;
        }
        if (upDistance > 0 && moveUpAble) {
            animateDown();
            upDistance = 0;
        }
    }

    private void moveDown(float deltaY) {
        if (moveDownAble) {
            deltaY *= ratio;
            downDistance += deltaY;
            setHeaderPadding(downDistance);
//            Log.v("AAA", "downDistance=" + downDistance);
//            smoothScrollToPosition(0);
        }
    }

    private void moveUp(float deltaY) {
        if (moveUpAble) {
            deltaY *= ratio;
            upDistance -= deltaY;
            setFooterPadding(upDistance);
//            tryToScrollToBottomSmoothly();
//            smoothScrollToPosition(getAdapter().getCount()-2);
        }
    }

    private void setHeaderPadding(float padding) {
        MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        mlp.setMargins(0, Math.round(padding), 0, 0);
        setLayoutParams(mlp);
    }

    private void setFooterPadding(float padding) {
        MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        mlp.setMargins(0, 0, 0, Math.round(padding));
        setLayoutParams(mlp);
    }

    private void animateUp() {
        topAnim.startAnimation(downDistance, 1);
    }

    private void animateDown() {
        bottomAnim.startAnimation(upDistance, -1);
    }
}
