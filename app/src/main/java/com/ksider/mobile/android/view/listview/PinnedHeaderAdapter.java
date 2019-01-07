// @author Bhavya Mehta
package com.ksider.mobile.android.view.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import com.ksider.mobile.android.WebView.CitySelectedActivity;
import com.ksider.mobile.android.WebView.CitySelectedActivity.CityItem;
import com.ksider.mobile.android.WebView.R;

import java.util.ArrayList;
import java.util.Map;

// Customized adaptor to populate data in PinnedHeaderListView
public class PinnedHeaderAdapter extends BaseAdapter implements OnScrollListener, IPinnedHeader, Filterable {

    public static final int TYPE_ITEM = 0;
    public static final int TYPE_SECTION = 1;
    private static final int TYPE_MAX_COUNT = TYPE_SECTION + 1;

    LayoutInflater mLayoutInflater;
    int mCurrentSectionPosition = 0, mNextSectionPostion = 0;

    // array list to store section positions
    ArrayList<Integer> mListSectionPos;
    Map<Integer, Integer> mSectionIndex;

    // array list to store list view data
    ArrayList<CityItem> mListItems;

    // context object
    Context mContext;

    public PinnedHeaderAdapter(Context context, ArrayList<CityItem> listItems, ArrayList<Integer> listSectionPos,
                               Map<Integer, Integer> sectionIndex) {
        this.mContext = context;
        this.mListItems = listItems;
        this.mListSectionPos = listSectionPos;
        this.mSectionIndex = sectionIndex;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mListItems.size();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return !mListSectionPos.contains(position);
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }


    @Override
    public int getItemViewType(int position) {
        return mListSectionPos.contains(position) ? TYPE_SECTION : TYPE_ITEM;
    }

    @Override
    public CityItem getItem(int position) {
        if (0 <= position && position < mListItems.size()) {
            return mListItems.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < mListItems.size()) {
            return ((Object) mListItems.get(position)).hashCode();
        }
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        CityItem item = mListItems.get(position);
        if (convertView == null || ((ViewHolder) convertView.getTag()).expand != item.expand) {
            holder = new ViewHolder();
            int type = item.type;
            switch (type) {
                case TYPE_ITEM:
                    convertView = mLayoutInflater.inflate(R.layout.row_view, null);
                    break;
                case TYPE_SECTION:
                    holder.expand = item.expand;
                    convertView = mLayoutInflater.inflate(item.expand ? R.layout.section_expand_row_view : R.layout.section_row_view, null);
                    break;
            }
            holder.textView = (TextView) convertView.findViewById(R.id.row_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(mListItems.get(position).chname);
        return convertView;
    }

    @Override
    public int getPinnedHeaderState(int position) {
        // hide pinned header when items count is zero OR position is less than
        // zero OR
        // there is already a header in list view
        if (getCount() == 0 || position < 0 || mListSectionPos.indexOf(position) != -1) {
            return PINNED_HEADER_GONE;
        }
        mCurrentSectionPosition = getCurrentSectionPosition(position);
        mNextSectionPostion = getNextSectionPosition(mCurrentSectionPosition);
        if (mNextSectionPostion != -1 && position == mNextSectionPostion - 1) {
            return PINNED_HEADER_PUSHED_UP;
        }
        if (mNextSectionPostion == -1) {
            return PINNED_HEADER_GONE;
        }
        return PINNED_HEADER_VISIBLE;
    }

    public int getCurrentSectionPosition(int position) {
        return mSectionIndex.get(position) == null ? -1 : mSectionIndex.get(position);
    }

    public int getNextSectionPosition(int currentSectionPosition) {
        int index = mListSectionPos.indexOf(currentSectionPosition);
        if (index < 0) {
            return -1;
        }
        if ((index + 1) < mListSectionPos.size()) {
            return mListSectionPos.get(index + 1);
        }
        return mListSectionPos.get(index);
    }

    @Override
    public void configurePinnedHeader(View v, int position) {
        // set text in pinned header
        TextView header = (TextView) v;
        mCurrentSectionPosition = getCurrentSectionPosition(position);
        if (mCurrentSectionPosition >= 0 && mCurrentSectionPosition < mListItems.size()) {
            header.setText(mListItems.get(mCurrentSectionPosition).chname);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (view instanceof PinnedHeaderListView) {
            ((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public Filter getFilter() {
        return ((CitySelectedActivity) mContext).new ListFilter();
    }

    public void setListItems(ArrayList<CityItem> listItems) {
        this.mListItems = listItems;
    }

    public static class ViewHolder {
        public TextView textView;
        public Boolean expand = false;
    }

}
