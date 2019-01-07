package com.ksider.mobile.android.activity.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.HashMap;
import java.util.Map;

public class FramentFactory {
	protected Map<String, Fragment> mFragments;


	public FramentFactory() {
		mFragments = new HashMap<String, Fragment>();
	}

	public static Fragment createFragment(String tabName) {
		Fragment fragment = null;
		if ("首页".equals(tabName)) {
			fragment = new HomePageFragment();
		} else if ("活动".equals(tabName)) {
			fragment = new ListFragment();
		} else if ("发现".equals(tabName)) {
			fragment = new ChoicenessFragment();
		} else if ("我的".equals(tabName)) {
			fragment = new MeFramment();
		} else {
			fragment = new BaseFragment();
		}
		
		Bundle b = new Bundle();
		b.putString(BaseFragment.ARG_POSITION, tabName);
		fragment.setArguments(b);
		return fragment;
	}

}
