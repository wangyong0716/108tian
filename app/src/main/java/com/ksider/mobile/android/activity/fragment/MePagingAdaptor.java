package com.ksider.mobile.android.activity.fragment;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.ListViewDataModel;
import com.ksider.mobile.android.view.LoadImageView;
import com.ksider.mobile.android.view.paging.PagingBaseAdapter;

public class MePagingAdaptor extends PagingBaseAdapter<ListViewDataModel> {
    protected Activity mContext;

    public MePagingAdaptor(Activity context) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ListViewDataModel getItem(int postion) {
        if (0 <= postion && postion < items.size()) {
            return items.get(postion);
        }
        return null;
    }

    @Override
    public long getItemId(int postion) {
        return postion;
    }

    protected void setPrice(View rowView, String price, String type) {
        TextView textView = (TextView) rowView.findViewById(R.id.listview_price);
        if (price != null) {
            if (price.equals("-1")) {
                textView.setVisibility(View.GONE);
            } else if (price.equals("0")) {
                textView.setVisibility(View.VISIBLE);
                textView.setText("免费");
            } else {
                textView.setVisibility(View.VISIBLE);
                textView.setText("￥" + price);
            }
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    protected void setTitle(View rowView, String title) {
        TextView textView = (TextView) rowView.findViewById(R.id.list_title);
        if (title != null) {
            textView.setText(title);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    protected void setLocation(View rowView, String location) {
        if (location != null) {
            TextView textView = (TextView) rowView.findViewById(R.id.listview_location);
            textView.setText(location);
        } else {
            View view = rowView.findViewById(R.id.listview_location);
            view.setVisibility(View.GONE);
        }
    }

    @Override
    public View getView(int postion, View convertView, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = convertView;
        ViewHolder holder;
        ListViewDataModel data = getItem(postion);
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.list_view_search_result_item, viewGroup, false);
            holder = new ViewHolder();
            holder.title = (TextView) rowView.findViewById(R.id.list_title);
            holder.date = (TextView) rowView.findViewById(R.id.listview_date);
            holder.location = (TextView) rowView.findViewById(R.id.listview_location);
            holder.distance = (TextView) rowView.findViewById(R.id.listview_distance);
            holder.price = (TextView) rowView.findViewById(R.id.listview_price);
            holder.image = (LoadImageView) rowView.findViewById(R.id.listview_headImage);
            holder.collection = (TextView) rowView.findViewById(R.id.listview_collection);
            holder.dateDistance = (LinearLayout) rowView.findViewById(R.id.date_distance);
            holder.distanceIcon = (ImageView) rowView.findViewById(R.id.distance_icon);
            holder.locDisIcon = (ImageView) rowView.findViewById(R.id.loc_dis_icon);
            holder.cardTitle = (TextView) rowView.findViewById(R.id.card_title);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (data.type.equals("weekly")) {
            holder.cardTitle.setText("攻略");
        } else if (data.type.equals("resort")) {
            holder.cardTitle.setText("度假村");
        } else if (data.type.equals("farm")) {
            holder.cardTitle.setText("农家院");
        } else if (data.type.equals("scene")) {
            holder.cardTitle.setText("景点");
        } else if (data.type.equals("pick")) {
            holder.cardTitle.setText("采摘园");
        } else if (data.type.equals("event") || data.type.equals("events")) {
            holder.cardTitle.setText("活动");
        } else if (data.type.equals("recommend")) {
            holder.cardTitle.setText("精选");
        }

        if (data.title != null) {
            holder.title.setText(data.title);
        }

        if (data.imgUrl != null && data.imgUrl.length() > 4) {
            holder.image.setImageResource(data.imgUrl);
        }
        if ((data.startDate == null || data.startDate.equals("")) && (data.location == null || data.location.equals(""))) {
            holder.dateDistance.setVisibility(View.GONE);
        } else if ((data.startDate == null || data.startDate.equals("")) && !(data.location == null || data.location.equals(""))) {
            holder.dateDistance.setVisibility(View.VISIBLE);
            holder.distanceIcon.setVisibility(View.VISIBLE);
            holder.date.setVisibility(View.GONE);
            holder.location.setVisibility(View.VISIBLE);
            holder.location.setText(data.location);
        } else if (!(data.startDate == null || data.startDate.equals("")) && (data.location == null || data.location.equals(""))) {
            holder.dateDistance.setVisibility(View.VISIBLE);
            holder.distanceIcon.setVisibility(View.GONE);
            holder.date.setVisibility(View.VISIBLE);
            holder.date.setText(data.startDate);
            holder.location.setVisibility(View.GONE);
        } else {
            holder.distanceIcon.setVisibility(View.GONE);
            holder.dateDistance.setVisibility(View.VISIBLE);
            holder.date.setVisibility(View.VISIBLE);
            holder.location.setVisibility(View.VISIBLE);
            holder.date.setText(data.startDate);
            holder.location.setText(data.location);
        }
        if (data.location != null && !data.location.equals("") && data.distance != null && !data.distance.equals("")) {
            holder.locDisIcon.setVisibility(View.VISIBLE);
        } else {
            holder.locDisIcon.setVisibility(View.GONE);
        }
        if (data.distance != null && !data.distance.equals("")) {
            holder.distance.setVisibility(View.VISIBLE);
            holder.distance.setText(data.distance);
        } else {
            holder.distance.setVisibility(View.GONE);
        }
        if (data.collection == null || data.collection.equals("")) {
            holder.collection.setVisibility(View.GONE);
        } else {
            holder.collection.setVisibility(View.VISIBLE);
            holder.collection.setText(data.collection + "人");
        }
        if (data.price == null || data.price.equals("") || data.price.equals("-1")) {
            holder.price.setVisibility(View.GONE);
        } else if (data.price.equals("0")) {
            holder.price.setVisibility(View.VISIBLE);
            holder.price.setText("免费");
        } else {
            holder.price.setVisibility(View.VISIBLE);
            holder.price.setText(mContext.getResources().getString(R.string.toolbar_price, data.price));
        }
        return rowView;
    }

    /**
     * ViewHolder
     */
    protected static class ViewHolder {
        public TextView cardTitle;
        public TextView title;
        public TextView label;
        public TextView collection;
        public TextView date;
        public TextView location;
        public TextView distance;
        public TextView price;
        public ImageView distanceIcon;
        public ImageView locDisIcon;
        public LinearLayout dateDistance;
        public LoadImageView image;
    }
}
