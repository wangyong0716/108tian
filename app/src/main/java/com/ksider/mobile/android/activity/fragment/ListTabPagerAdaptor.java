package com.ksider.mobile.android.activity.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ListTabPagerAdaptor extends FragmentPagerAdapter {
	private  String[] mTitles = { "首页", "周边", "主题"};
	public ListTabPagerAdaptor(FragmentManager fm) {
		super(fm);
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

}
