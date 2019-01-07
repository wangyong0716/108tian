package com.ksider.mobile.android.scrollListView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import com.ksider.mobile.android.activity.fragment.OnScrollChanged;
import com.ksider.mobile.android.view.paging.LoadingView;
import com.ksider.mobile.android.view.paging.PagingBaseAdapter;

import java.util.List;

public class OverScrollPagingListView extends OverScrollListView {

    public interface Pagingable {
        void onLoadMoreItems();
    }

    private boolean isLoading;
    private boolean hasMoreItems;
    private Pagingable pagingableListener;
    protected LoadingView loadinView;
    private int mLoadIndex = 0;
    private OnScrollListener onScrollListener;
    protected OnScrollChanged onScrollChanged;

    public OverScrollPagingListView(Context context) {
        super(context);
        init();
    }

    public OverScrollPagingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverScrollPagingListView(Context context, AttributeSet attrs, int defStyle) {
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
            setMoveUpAble(true);
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
            setMoveUpAble(true);
        }
    }

    /**
     * mainly used in resuming the list: delete the endView added
     * last time and then add loadingView because the last
     * loadingView has been deleted
     */
    public void deleteFooterView() {
        if (getFooterViewsCount() == 1) {
            addFooterView(loadinView);
            setMoveUpAble(false);
        }
    }

    public void showFailed() {
        try {
            removeFooterView(loadinView);
            setMoveUpAble(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init() {
        isLoading = false;
        loadinView = new LoadingView(getContext());
        addFooterView(loadinView);
        setMoveUpAble(false);

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
                        }
                    }
                }
            }
        });
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
