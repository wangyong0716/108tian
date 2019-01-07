package com.ksider.mobile.android.adaptor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import com.ksider.mobile.android.WebView.ThemeListActivity;
import com.ksider.mobile.android.activity.fragment.ListViewPagingFragment;
import com.ksider.mobile.android.model.ThemeData;

import java.util.List;

/**
 * Created by wenkui on 3/24/15.
 */
public class ListThemeTabIndicatorAdaptor extends TabIndicatorAdaptor {
    public ListThemeTabIndicatorAdaptor(FragmentManager fm, List<ThemeData> items) {
        super(fm, items);
    }

    @Override
    public Fragment getItem(int postion) {
        if (0 <= postion && postion < mItems.size()) {
            Bundle args = new Bundle();
            args.putSerializable("category", ThemeListActivity.categories[postion]);
            args.putInt("theme", Integer.valueOf(mItems.get(postion).id));
            Fragment fg = new ListViewPagingFragment();
            fg.setArguments(args);
            return fg;
        }
        return null;
    }
}
