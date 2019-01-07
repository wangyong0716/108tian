package com.ksider.mobile.android.view.paging.gridview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import com.ksider.mobile.android.view.paging.FinishView;
import com.ksider.mobile.android.view.paging.LoadingView;

import java.util.List;

public class PagingGridView extends GridViewWithHeaderAndFooter {

    public interface Pagingable {
        void onLoadMoreItems();
    }

    private boolean isLoading;
    private boolean hasMoreItems;
    private Pagingable pagingableListener;
    private LoadingView loadinView;
    private FinishView mFinishView;

    public PagingGridView(Context context) {
        super(context);
        init();
    }

    public PagingGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PagingGridView(Context context, AttributeSet attrs, int defStyle) {
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
//            setMoveUpAble(true);
        } else {
            removeFooterView(mFinishView);
        }
    }

    public boolean hasMoreItems() {
        return this.hasMoreItems;
    }

    public void onFinishLoading(boolean hasMoreItems,
                                List<? extends Object> newItems) {
        setHasMoreItems(hasMoreItems);
        setIsLoading(false);
        ListAdapter adapter = getAdapter();
        if (adapter instanceof HeaderViewGridAdapter) {
            adapter = ((HeaderViewGridAdapter) adapter).getWrappedAdapter();
        }
        if (newItems != null && newItems.size() > 0) {
            if (adapter instanceof PagingBaseAdapter) {
                ((PagingBaseAdapter) adapter).addMoreItems(newItems);
            }
        }
        if (!hasMoreItems) {
            try {
                removeFooterView(loadinView);
//                setMoveUpAble(true);
                mFinishView.show(adapter.getCount() > 0);
                removeFooterView(mFinishView);
                addFooterView(mFinishView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showFailed() {
        mFinishView.showFailed();
        try {
            removeFooterView(loadinView);
//            setMoveUpAble(true);
            removeFooterView(mFinishView);
            addFooterView(mFinishView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        isLoading = false;
        loadinView = new LoadingView(getContext());
        addFooterView(loadinView);
//        setMoveUpAble(false);
        mFinishView = new FinishView(getContext());
        setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // DO NOTHING...
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (totalItemCount > 0) {
                    int lastVisibleItem = firstVisibleItem + visibleItemCount;
                    if (!isLoading && hasMoreItems
                            && (lastVisibleItem == totalItemCount)) {
                        if (pagingableListener != null) {
                            isLoading = true;
                            pagingableListener.onLoadMoreItems();
                        }

                    }
                }
            }
        });
    }

}
