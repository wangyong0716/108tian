package com.ksider.mobile.android.scrollListView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.activity.fragment.OnScrollChanged;
import com.ksider.mobile.android.view.paging.LoadingView;
import com.ksider.mobile.android.view.paging.PagingBaseAdapter;

import java.util.List;

public class BasePagingListView extends ListView {
    private LinearLayout footerLayout;
    private ViewGroup footer;
    private float scrollY = 0;
    private int upDistance;
    private float ratio = 0.4f;
    private int pointIndex = -1;
    private TransAnimRunnable bottomAnim;
    private int originFooterHeight = 0;
    private boolean canExpandFooter;
    private boolean moveUpAble = true;

    public interface Pagingable {
        void onLoadMoreItems();
    }

    private boolean isLoading;
    private boolean hasMoreItems;
    private Pagingable pagingableListener;
    protected LoadingView loadinView;
    private OnScrollListener onScrollListener;
    protected OnScrollChanged onScrollChanged;

    public BasePagingListView(Context context) {
        super(context);
        init();
    }

    public BasePagingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BasePagingListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public boolean isLoading() {
        return this.isLoading;
    }

    public void setIsLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public void setPagingableListener(Pagingable pagingableListener) {
        this.pagingableListener = pagingableListener;
    }

    public void setHasMoreItems(boolean hasMoreItems) {
        this.hasMoreItems = hasMoreItems;
        if (!this.hasMoreItems) {
            removeFooterView(loadinView);
            moveUpAble = true;
        }
    }

    public boolean hasMoreItems() {
        return this.hasMoreItems;
    }

    public void onFinishLoading(boolean hasMoreItems, List<? extends Object> newItems) {
        setHasMoreItems(hasMoreItems);
        setIsLoading(false);
        if (newItems != null && newItems.size() > 0) {
            ListAdapter adapter = ((HeaderViewListAdapter) getAdapter()).getWrappedAdapter();
            if (adapter instanceof PagingBaseAdapter) {
                ((PagingBaseAdapter) adapter).addMoreItems(newItems);
            }
        }
        if (!hasMoreItems) {
            removeFooterView(loadinView);
            moveUpAble = true;
        }
    }

    /**
     * mainly used in resuming the list: delete the endView added
     * last time and then add loadingView because the last
     * loadingView has been deleted
     */
    public void deleteFooterView() {
        if (getFooterViewsCount() == 0) {
            addFooterView(loadinView);
            moveUpAble = false;
        }
    }

    public void showFailed() {
        try {
            removeFooterView(loadinView);
            moveUpAble = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init() {
        isLoading = false;
        loadinView = new LoadingView(getContext());
        addFooterView(loadinView);
        moveUpAble = false;

        super.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // Dispatch to child OnScrollListener
                if (onScrollListener != null) {
                    onScrollListener.onScrollStateChanged(view, scrollState);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                // Dispatch to child OnScrollListener
                if (onScrollListener != null) {
                    onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                }
                if (totalItemCount > 0) {
                    int lastVisibleItem = firstVisibleItem + visibleItemCount;
                    ListAdapter adapter = ((HeaderViewListAdapter) getAdapter()).getWrappedAdapter();
                    //check has requested
                    if (!isLoading && hasMoreItems && (lastVisibleItem == totalItemCount) && (adapter != null && adapter.getCount() > 0)) {
                        if (pagingableListener != null) {
                            isLoading = true;
                            pagingableListener.onLoadMoreItems();
                            if (BasePagingListView.this instanceof OverScrollPullToRefreshListView) {
                                try {
                                    if (hasMoreItems) {
                                        removeFooterView(loadinView);
                                        addFooterView(loadinView);
                                        moveUpAble = false;
                                    }
                                } catch (Exception e) {

                                }
                            }
                        }
                    }
                }
            }
        });

        ////////////////
        footerLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.lv_footer, null);
        footer = (RelativeLayout) footerLayout.findViewById(R.id.footer);

        footer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, originFooterHeight));
        addFooterView(footerLayout);

        bottomAnim = new TransAnimRunnable(this, footer);
        ///////////////////
    }

    public void getFooterHeight() {
        if (getAdapter().getCount() - getHeaderViewsCount() - getFooterViewsCount() < 1) {
            originFooterHeight = 0;
        } else {
            int contentHeight = getChildAt(getChildCount() - 1).getBottom() - getChildAt(0).getTop() - footer.getLayoutParams().height;//+getPaddingTop()
            int height = getHeight() - contentHeight;
            originFooterHeight = height > 0 ? height : 0;
            footer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, originFooterHeight));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //////////////////
                if (bottomAnim.isFinished() && moveUpAble) {
                    getFooterHeight();
                }
                if (!bottomAnim.isFinished()) {
                    bottomAnim.abortAnimation();
                }
                upDistance = footer.getLayoutParams().height;
                canExpandFooter = !canScrollVertically(1);
                ////////////
                break;

            case MotionEvent.ACTION_UP:
                ///////////////
                resetView();
                pointIndex = -1;
                ////////////////////

                break;

            case MotionEvent.ACTION_MOVE:

                ////////////////////////
                if (!canExpandFooter) {
                    canExpandFooter = !canScrollVertically(-1);
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
                    if (canExpandFooter && (deltaY < 0 || upDistance > 0)) {
                        moveUp(deltaY);
                    }
                }
                ////////////////////////////////
                break;
        }

        return super.onTouchEvent(event);
    }

    public void resetView() {
        pointIndex = -1;
        canExpandFooter = false;
        if (upDistance > 0 && moveUpAble) {
            animateDown();
            upDistance = 0;
        }
    }

    private void moveUp(float deltaY) {
        if (moveUpAble) {
            deltaY *= ratio;
            upDistance -= deltaY;
            setFooterPadding(upDistance);
//            smoothScrollToPosition(0);
        }
    }

    private void setFooterPadding(float padding) {
        if (footer != null && footer.getLayoutParams() != null) {
            ViewGroup.LayoutParams layoutParams = footer.getLayoutParams();
            layoutParams.height = Math.round(padding);
            footer.setLayoutParams(layoutParams);
        }
    }

    private void animateDown() {
        bottomAnim.startAnimation(originFooterHeight);
    }

    @Override
    public void setOnScrollListener(OnScrollListener listener) {
        onScrollListener = listener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (onScrollChanged != null) {

            View child = getChildAt(0);
            if (child != null) {
                int scrolly = 0;
                if (child != null) {
                    scrolly = -child.getTop() + getFirstVisiblePosition() * child.getHeight();
                }
                onScrollChanged.onScrollChanged(x, scrolly, oldx, oldy);
            }
        }
    }

    public void setOnScrollChanged(OnScrollChanged listener) {
        onScrollChanged = listener;
    }

}
