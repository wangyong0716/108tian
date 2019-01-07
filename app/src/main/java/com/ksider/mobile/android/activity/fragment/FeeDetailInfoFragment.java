package com.ksider.mobile.android.activity.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.utils.AlignTextView;

/**
 * Created by yong on 8/1/15.
 */
public class FeeDetailInfoFragment extends Fragment {
    protected View mRoot;
    private String brief;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_fee_detail_info, container, false);
        brief = getArguments().getString("brief");
//        ((AlignTextView) mRoot.findViewById(R.id.desc)).setContent(brief);
        ((TextView) mRoot.findViewById(R.id.desc)).setText(brief);
        return mRoot;
    }
}
