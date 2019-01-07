package com.ksider.mobile.android.adaptor;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import com.ksider.mobile.android.view.LoadImageView;
import com.ksider.mobile.android.view.viewpagerindicator.IconPagerAdapter;

import java.util.List;


public class AlbumAdaptor extends PagerAdapter implements IconPagerAdapter  {

	protected List<String> items;
	protected Context mContext;

	public AlbumAdaptor(Context context, List<String> data) {
		mContext = context;
		items = data;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object o) {
		return view == ((ImageView) o);
	}

	/**
	 * 载入图片进去，用当前的position 除以 图片数组长度取余数是关键
	 */
	@Override
	public Object instantiateItem(View container, int position) {
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		LoadImageView loader = new LoadImageView(mContext);
		loader.setLayoutParams(layoutParams);
		loader.setScaleType(ScaleType.CENTER_CROP);
		loader.setImageResource(items.get(position % items.size()));
		((ViewPager) container).addView(loader, 0);
		return loader;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((LoadImageView) object);
	}

	@Override
	public int getIconResId(int index) {
		// TODO Auto-generated method stub
		return 0;
	}
}

