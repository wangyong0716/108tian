package com.ksider.mobile.android.view.paging;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.ksider.mobile.android.activity.fragment.OnScrollChanged;

import java.util.List;

public class PagingListView extends ListView {

    public interface Pagingable {
        void onLoadMoreItems();
    }

    private boolean isLoading;
    private boolean hasMoreItems;
    private Pagingable pagingableListener;
    protected LoadingView loadinView;
    protected FinishView mFinishView;
    private int mLoadIndex = 0;
    private OnScrollListener onScrollListener;
    protected OnScrollChanged onScrollChanged;

    public PagingListView(Context context) {
        super(context);
        init();
    }

    public PagingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PagingListView(Context context, AttributeSet attrs, int defStyle) {
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
        } else {
            removeFooterView(mFinishView);
        }
    }

    public boolean hasMoreItems() {
        return this.hasMoreItems;
    }

    public void onFinishLoading(boolean hasMoreItems, List<? extends Object> newItems) {
        if (getFooterViewsCount() > 0) {
            removeFooterView(mFinishView);
        }
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
            ListAdapter adapter = ((HeaderViewListAdapter) getAdapter()).getWrappedAdapter();
            mFinishView.show(adapter.getCount() > 0);
            addFooterView(mFinishView);
        }
    }

    /**
     * mainly used in resuming the list: delete the endView added
     * last time and then add loadingView because the last
     * loadingView has been deleted
     */
    public void deleteFooterView() {
        if (getFooterViewsCount() > 0) {
            removeFooterView(mFinishView);
        }
        if (getFooterViewsCount() == 0) {
            addFooterView(loadinView);
        }
    }

    public void removeFooterView(){
        if (getFooterViewsCount() > 0) {
            removeFooterView(mFinishView);
        }
    }

    public void showFailed() {
        mFinishView.showFailed();
        try {
            removeFooterView(loadinView);
            removeFooterView(mFinishView);
            addFooterView(mFinishView);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init() {
        isLoading = false;
        loadinView = new LoadingView(getContext());
        mFinishView = new FinishView(getContext());
        addFooterView(loadinView);

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
                            if (PagingListView.this instanceof PullToRefreshListView) {
                                try {
                                    if (hasMoreItems) {
                                        removeFooterView(loadinView);
                                        addFooterView(loadinView);
                                    }
                                } catch (Exception e) {

                                }
                            }
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
                int top = child.getTop();
                int position = getFirstVisiblePosition();
                int height = child.getHeight();
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
