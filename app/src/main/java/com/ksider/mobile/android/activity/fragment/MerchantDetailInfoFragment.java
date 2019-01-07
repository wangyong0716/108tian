package com.ksider.mobile.android.activity.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.view.LoadImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yong on 8/1/15.
 */
public class MerchantDetailInfoFragment extends Fragment {
    protected View mRoot;
    private String brief;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_merchant_detail_info, container, false);
        brief = getArguments().getString("brief");
        try {
            JSONObject object = new JSONObject(brief);
            try {
                JSONArray imgs = object.getJSONArray("imgs");
                if (imgs != null && imgs.length() > 0) {
                    String img = imgs.getJSONObject(0).getString("url");
                    ((LoadImageView) mRoot.findViewById(R.id.pic)).setImageResource(img);
                }else {
                    mRoot.findViewById(R.id.pic).setVisibility(View.GONE);
                }
            } catch (JSONException js) {
                mRoot.findViewById(R.id.pic).setVisibility(View.GONE);
                js.printStackTrace();
            }

            try {
                String desc = object.getString("desc");
                ((TextView) mRoot.findViewById(R.id.desc)).setText(desc);
            } catch (JSONException js) {
                js.printStackTrace();
            }
        } catch (JSONException js) {
            js.printStackTrace();
        }
        return mRoot;
    }
}
