package com.ksider.mobile.android.scrollListView;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import com.ksider.mobile.android.view.paging.gridview.GridViewWithHeaderAndFooter;

/**
 * Created by yong on 9/10/15.
 */
public class OverGridView extends GridViewWithHeaderAndFooter {
    private Context context;
    private float MOVE_FACTOR = 0.4f;
    private int ANIM_TIME = 300;
    private float startY;
    private Rect originalRect = new Rect();
    private boolean canPullDown = false;
    private boolean canPullUp = false;
    private boolean isMoved = false;
    private int offset = 0;

    public OverGridView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public OverGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public OverGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    public void getOriginLocation() {
        originalRect.set(getLeft(), getTop(), getRight(), getBottom());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // handle outside
        boolean isTouchOutOfScrollView = ev.getY() >= this.getHeight() || ev.getY() <= 0;
        if (isTouchOutOfScrollView) {
            if (isMoved)
                boundBack();
            return true;
        }
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                getOriginLocation();
                offset = 0;
                canPullDown = !canScrollVertically(-1);
                canPullUp = !canScrollVertically(1);
                startY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                boundBack();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!canPullDown && !canPullUp) {
                    startY = ev.getY();
                    canPullDown = !canScrollVertically(-1);
                    canPullUp = !canScrollVertically(1);
                    break;
                }
                float nowY = ev.getY();
                int deltaY = (int) (nowY - startY);
                startY = nowY;
                //make listView scrollable when scroll from top to bottom or from bottom to top
                if (deltaY > 0) {
                    canPullUp = false;
                } else if (deltaY < 0) {
                    canPullDown = false;
                }
                boolean shouldMove = (canPullDown && deltaY > 0)    // pull down
                        || (canPullUp && deltaY < 0)    // pull up
                        || (canPullUp && canPullDown);  // content is shorter than viewHeight
                if (shouldMove) {
                    offset += (int) (deltaY * MOVE_FACTOR);
                    layout(originalRect.left, originalRect.top + offset, originalRect.right, originalRect.bottom + offset);
//                    Log.v("AAA","toY="+(originalRect.top + offset));
//                    scrollTo(100, -500);
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
        TranslateAnimation anim = new TranslateAnimation(0, 0, getTop(), originalRect.top);
        anim.setDuration(ANIM_TIME);
        anim.setInterpolator(new DecelerateInterpolator());
        startAnimation(anim);
        layout(originalRect.left, originalRect.top, originalRect.right, originalRect.bottom);
        canPullDown = false;
        canPullUp = false;
        isMoved = false;
    }
}
