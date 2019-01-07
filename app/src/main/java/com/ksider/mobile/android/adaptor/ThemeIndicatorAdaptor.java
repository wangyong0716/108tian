package com.ksider.mobile.android.adaptor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.ksider.mobile.android.activity.fragment.ThemeViewPagerFragment;

import java.util.List;


public class ThemeIndicatorAdaptor extends FragmentPagerAdapter {
    protected List<String> mItems;

    public ThemeIndicatorAdaptor(FragmentManager fm, List<String> items) {
        super(fm);
        mItems = items;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

//    public int getPostionByTheme(String theme) {
//        for (int i = 0; i < mItems.size(); i++) {
//            if (mItems.get(i).id.equals(String.valueOf(theme))) {
//                return i;
//            }
//        }
//        return -1;
//    }

    @Override
    public Fragment getItem(int postion) {
        if (0 <= postion && postion < mItems.size()) {
            Bundle args = new Bundle();
            args.putString("themes", mItems.get(postion));
            Fragment fg = new ThemeViewPagerFragment();
            fg.setArguments(args);
            return fg;
        }
        return null;
    }

//    @Override
//    public CharSequence getPageTitle(int position) {
//        return mItems.get(position % mItems.size()).name;
//    }

    @Override
    public long getItemId(int postion) {
        return postion;
    }
}
