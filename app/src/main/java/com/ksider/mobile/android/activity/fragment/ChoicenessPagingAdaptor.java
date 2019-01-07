package com.ksider.mobile.android.activity.fragment;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.ListViewDataModel;
import com.ksider.mobile.android.utils.StatHandle;
import com.ksider.mobile.android.view.LoadImageView;
import com.ksider.mobile.android.view.paging.gridview.PagingBaseAdapter;

public class ChoicenessPagingAdaptor extends PagingBaseAdapter<ListViewDataModel> {
    protected Activity mContext;

    public ChoicenessPagingAdaptor(Activity context) {
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
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.choice_list_item, viewGroup, false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.list_title);
//			holder.title_firtLine = (TextView) convertView.findViewById(R.id.list_title_firstLine);
            holder.image = (LoadImageView) convertView.findViewById(R.id.listview_headImage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
//		holder.title_firtLine.setVisibility(View.INVISIBLE);
        ListViewDataModel data = getItem(position);
        if (data.title != null) {
//			if(data.title.length() < 10){
            holder.title.setText(data.title);
//			}else{
//				holder.title_firtLine.setVisibility(View.VISIBLE);
//				int end = data.title.length()>20?20:data.title.length();
//				holder.title.setText(data.title.substring(10,end));
//				holder.title.setText(data.title.substring(end-10,end));
//				holder.title_firtLine.setText(data.title.substring(0,end-10));
//			}
        }

        if (data.imageBitmap != null) {
            holder.image.setImageBitmap(data.imageBitmap);
        } else if (data.imageDrawable != null) {
            holder.image.setImageDrawable(data.imageDrawable);
        } else if (data.imgUrl != null && data.imgUrl.length() > 4) {
            holder.image.setImageResource(data.imgUrl);
        }
        StatHandle.increaseImpression(StatHandle.CHOINCELIST);
        if (position % 2 == 0) {
            convertView.setPadding((int) mContext.getResources().getDimension(R.dimen.margin_standard), 0, 0, 0);
        } else {
            convertView.setPadding(0, 0, (int) mContext.getResources().getDimension(R.dimen.margin_standard), 0);
        }
        return convertView;
    }

    static class ViewHolder {
        TextView title;
        //		TextView title_firtLine;
        LoadImageView image;

    }
}
