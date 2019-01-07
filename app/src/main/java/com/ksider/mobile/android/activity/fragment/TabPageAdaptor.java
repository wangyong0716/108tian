package com.ksider.mobile.android.activity.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.view.slidingtab.ITabIconViewProvider;
import com.ksider.mobile.android.view.slidingtab.TabIconView;

import java.util.ArrayList;

public class TabPageAdaptor extends FragmentPagerAdapter implements ITabIconViewProvider{
	private  String[] mTitles = { "首页", "发现","活动", "我的"};
//	private  String[] mTitles = { "精选"};
	private  int[] mTabIcon = {R.drawable.tab_find_icon,
					  		R.drawable.tab_choiceness_unselect_icon,
						   R.drawable.tab_activity_unselect_icon,
						   R.drawable.tab_me_unselect_icon};
	private ArrayList<View> mTabIconViews;
	public TabPageAdaptor(FragmentManager fm, Context ct) {
		super(fm);
		mTabIconViews = new ArrayList<View>();
		mTabIconViews.add(new TabIconView(ct, R.drawable.tab_find_icon, "首页"));
		mTabIconViews.add(new TabIconView(ct, R.drawable.tab_choiceness_icon,"发现"));
		mTabIconViews.add(new TabIconView(ct, R.drawable.tab_activity_icon, "活动"));
		mTabIconViews.add(new TabIconView(ct, R.drawable.tab_me_icon, "我的"));
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		return mTitles[position];
	}

	@Override
	public int getCount() {
		return mTitles.length;
	}

	@Override
	public Fragment getItem(int position) {
		return FramentFactory.createFragment(mTitles[position]);
	}


	@Override
	public View getTabIcon(int position) {
		// TODO Auto-generated method stub
		return mTabIconViews.get(position);
	}

}
