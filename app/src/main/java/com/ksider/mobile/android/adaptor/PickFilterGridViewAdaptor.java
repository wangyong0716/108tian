package com.ksider.mobile.android.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.ThemeData;

import java.util.List;

public class PickFilterGridViewAdaptor extends BaseAdapter {
	protected Context mContext;
	protected List<ThemeData> mItems;
	protected int mBackgroundId;

	public PickFilterGridViewAdaptor(Context context, List<ThemeData> data, int resId) {
		mContext = context;
		mItems = data;
		mBackgroundId = resId;
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public ThemeData getItem(int position) {
		return mItems.get(position % mItems.size());
	}

	@Override
	public long getItemId(int position) {
		return Long.parseLong(mItems.get(position % mItems.size()).id);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		View gridView;
		if (convertView != null) {
			gridView = convertView;
		} else {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			gridView = inflater.inflate(R.layout.filter_pick_button, null);
		}
		TextView button = (TextView) gridView.findViewById(R.id.button);
		button.setBackgroundResource(mBackgroundId);
		button.setText(mItems.get(position % mItems.size()).name);
		return gridView;
	}

}
