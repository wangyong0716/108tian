package com.ksider.mobile.android.adaptor;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.ListViewDataModel;
import com.ksider.mobile.android.utils.StatHandle;
import com.ksider.mobile.android.view.LoadImageView;
import com.ksider.mobile.android.view.paging.PagingBaseAdapter;

public class MerchantProductListAdaptor extends PagingBaseAdapter<ListViewDataModel> {
    protected Activity mContext;

    public MerchantProductListAdaptor(Activity context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ListViewDataModel getItem(int position) {
        if (0 <= position && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.list_view_activity_divider_item, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.image = (LoadImageView) view.findViewById(R.id.listview_headImage);
            viewHolder.title = (TextView) view.findViewById(R.id.list_title);
            viewHolder.price = (TextView) view.findViewById(R.id.listview_price);
            viewHolder.location = (TextView) view.findViewById(R.id.listview_location);
            viewHolder.startDate = (TextView) view.findViewById(R.id.listview_StartDate);
            viewHolder.collection = (TextView) view.findViewById(R.id.listview_collection);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        ListViewDataModel data = getItem(position);
        if (data == null)
            return view;
        viewHolder.title.setText(data.title);
        if (data.price != null && !data.price.equals("")) {
            viewHolder.price.setText(mContext.getResources().getString(R.string.toolbar_price, data.price));
        } else {
            viewHolder.price.setVisibility(View.GONE);
        }
        if (data.location != null && !data.location.equals("")) {
            viewHolder.location.setText(data.location);
        } else {
            viewHolder.location.setVisibility(View.GONE);
        }
        if (data.startDate != null && viewHolder.startDate != null) {
            if (data.startDate.equals("")) {
                viewHolder.startDate.setVisibility(View.GONE);
            } else {
                viewHolder.startDate.setVisibility(View.VISIBLE);
                viewHolder.startDate.setText(data.startDate);
            }
        }
        if (data.collection.equals("")) {
            viewHolder.collection.setVisibility(View.GONE);
        } else {
            viewHolder.collection.setText(data.collection + "人");
        }
        if (data.imageBitmap != null) {
            viewHolder.image.setImageBitmap(data.imageBitmap);
        } else if (data.imageDrawable != null) {
            viewHolder.image.setImageDrawable(data.imageDrawable);
        } else if (data.imgUrl != null && data.imgUrl.length() > 4) {
            viewHolder.image.setImageResource(data.imgUrl);
        }
        StatHandle.increaseImpression(StatHandle.PRODUCT_LIST);
        return view;
    }

    /**
     * ViewHolder
     */
    static class ViewHolder {
        public TextView title;
        public TextView price;
        public TextView collection;
        public TextView location;
        public TextView startDate;
        public LoadImageView image;
    }
}
