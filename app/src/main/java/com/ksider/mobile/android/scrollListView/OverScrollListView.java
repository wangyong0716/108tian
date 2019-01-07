package com.ksider.mobile.android.scrollListView;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.ksider.mobile.android.WebView.R;

/**
 * Created by yong on 9/16/15.
 */
public class OverScrollListView extends ListView {
    private Context context;
    private LinearLayout headerLayout;
    private ViewGroup header;
    private LinearLayout footerLayout;
    private ViewGroup footer;

    private float scrollY = 0;

    private int downDistance;
    private int upDistance;

    private float ratio = 0.4f;

    private int pointIndex = -1;

    private TransAnimRunnable topAnim;
    private TransAnimRunnable bottomAnim;

    private int originHeaderHeight = 0;
    private int originFooterHeight = 0;

    private boolean canExpandHeader;
    private boolean canExpandFooter;

    private boolean moveUpAble = true;
    private boolean moveDownAble = true;

    public OverScrollListView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public OverScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    public OverScrollListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    public void initView() {
        setOverScrollMode(OVER_SCROLL_NEVER);
        headerLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.lv_header, null);
        header = (RelativeLayout) headerLayout.findViewById(R.id.header);

        footerLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.lv_footer, null);
        footer = (RelativeLayout) footerLayout.findViewById(R.id.footer);

        header.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, originHeaderHeight));
        footer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, originFooterHeight));
        addHeaderView(headerLayout);
        addFooterView(footerLayout);

        topAnim = new TransAnimRunnable(this, header);
        bottomAnim = new TransAnimRunnable(this, footer);
    }

    public void getFooterHeight() {
        if (getAdapter().getCount() - getHeaderViewsCount() - getFooterViewsCount() < 1) {
            originFooterHeight = 0;
        } else {
            int contentHeight = getChildAt(getChildCount() - 1).getBottom() - getChildAt(0).getTop() - footer.getLayoutParams().height + getPaddingTop();
            int height = getHeight() - contentHeight;
            originFooterHeight = height > 0 ? height : 0;
            footer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, originFooterHeight));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (topAnim.isFinished() && bottomAnim.isFinished()) {
                    getFooterHeight();
                }
                if (!topAnim.isFinished()) {
                    topAnim.abortAnimation();
                }
                if (!bottomAnim.isFinished()) {
                    bottomAnim.abortAnimation();
                }
                downDistance = header.getLayoutParams().height;//downDistance = header.getMeasuredHeight() cannot get the real height
                upDistance = footer.getLayoutParams().height;
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
                    if (canExpandHeader && (deltaY > 0 || downDistance > 0)) {
                        moveDown(deltaY);
                    } else if (canExpandFooter && (deltaY < 0 || upDistance > 0)) {
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
            smoothScrollToPosition(0);
        }
    }

    private void moveUp(float deltaY) {
        if (moveUpAble) {
            deltaY *= ratio;
            upDistance -= deltaY;
            setFooterPadding(upDistance);
//            smoothScrollToPosition(getAdapter().getCount()-2);
        }
    }

    private void setHeaderPadding(float padding) {
        if (header != null && header.getLayoutParams() != null) {
            ViewGroup.LayoutParams layoutParams = header.getLayoutParams();
            layoutParams.height = Math.round(padding);
            header.setLayoutParams(layoutParams);
        }
    }

    private void setFooterPadding(float padding) {
        if (footer != null && footer.getLayoutParams() != null) {
            ViewGroup.LayoutParams layoutParams = footer.getLayoutParams();
            layoutParams.height = Math.round(padding);
            footer.setLayoutParams(layoutParams);
        }
    }

    private void animateUp() {
        topAnim.startAnimation(originHeaderHeight);
    }

    private void animateDown() {
        bottomAnim.startAnimation(originFooterHeight);
    }
}
