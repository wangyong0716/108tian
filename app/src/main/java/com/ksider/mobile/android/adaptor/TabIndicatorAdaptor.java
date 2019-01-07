package com.ksider.mobile.android.adaptor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.ksider.mobile.android.activity.fragment.GoodsListViewPagingFragment;
import com.ksider.mobile.android.model.ThemeData;
import com.ksider.mobile.android.utils.BasicCategory;
import com.ksider.mobile.android.utils.TabIndicator;

import java.util.List;

/**
 * Created by wenkui on 3/17/15.
 */
public class TabIndicatorAdaptor  extends FragmentPagerAdapter {
    protected List<ThemeData> mItems;
    protected TabIndicator mIndicator = TabIndicator.DEFAULT;
    public TabIndicatorAdaptor( FragmentManager fm, List<ThemeData> items) {
        super(fm);
        mItems = items;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }
    public int getPostionByTheme(String theme){
        for (int i = 0; i < mItems.size(); i++){
            if(mItems.get(i).id.equals(String.valueOf(theme))){
                return i;
            }
        }
        return -1;
    }

    @Override
    public Fragment getItem(int postion) {
        if (0 <= postion && postion < mItems.size()) {
            Bundle args = new Bundle();
            args.putSerializable("category", BasicCategory.ACTIVITY);
            args.putSerializable("indicator", mIndicator);
            args.putInt("theme", Integer.valueOf(mItems.get(postion).id));
            Fragment fg = new GoodsListViewPagingFragment();
            fg.setArguments(args);
            return fg;
        }
        return null;
    }
    public void setIndicator(TabIndicator indicator){
        mIndicator = indicator;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        return mItems.get(position % mItems.size()).name;
    }

    @Override
    public long getItemId(int postion) {
        return postion;
    }



}
