package com.ksider.mobile.android.scrollListView;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

/**
 * Created by yong on 9/10/15.
 */
public class OverUpScrollView extends ScrollView {
    private Context context;
    private float MOVE_FACTOR = 0.4f;
    private int ANIM_TIME = 300;
    private View contentView;
    private float startY;
    private Rect originalRect = new Rect();
    private boolean canPullUp = false;
    private boolean isMoved = false;
    private int offset = 0;

    public OverUpScrollView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public OverUpScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public OverUpScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            contentView = getChildAt(0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (contentView == null)
            return;
        originalRect.set(contentView.getLeft(), contentView.getTop(), contentView.getRight(), contentView.getBottom());
    }

    public void init() {
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (contentView == null) {
            return super.dispatchTouchEvent(ev);
        }
//        boolean isTouchOutOfScrollView = ev.getY() >= this.getHeight() || ev.getY() <= 0;
//        if (isTouchOutOfScrollView) {
//            if (isMoved)
//                boundBack();
//            return true;
//        }
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                offset = 0;
                canPullUp = isCanPullUp();
                startY = ev.getY();
                break;
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_UP:
                boundBack();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!canPullUp) {
                    startY = ev.getY();
                    canPullUp = isCanPullUp();
                    break;
                }
                float nowY = ev.getY();
                int deltaY = (int) (nowY - startY);
                startY = nowY;
                if (deltaY > 0) {
                    canPullUp = false;
                }
                boolean shouldMove = (canPullUp && deltaY < 0);    // 可以上拉， 并且手指向上移动
                if (shouldMove) {
                    offset += (int) (deltaY * MOVE_FACTOR);
                    contentView.layout(originalRect.left, originalRect.top + offset, originalRect.right, originalRect.bottom + offset);
                    isMoved = true;
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void boundBack() {
        offset = 0;
        if (!isMoved)
            return;
        TranslateAnimation anim = new TranslateAnimation(0, 0, contentView.getTop(), originalRect.top);
        anim.setDuration(ANIM_TIME);
        anim.setInterpolator(new DecelerateInterpolator());
        contentView.startAnimation(anim);
        contentView.layout(originalRect.left, originalRect.top, originalRect.right, originalRect.bottom);
        canPullUp = false;
        isMoved = false;
    }

    private boolean isCanPullUp() {
        return contentView.getHeight() <= getHeight() + getScrollY();
    }
}
