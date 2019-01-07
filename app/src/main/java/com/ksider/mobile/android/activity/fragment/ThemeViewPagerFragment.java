package com.ksider.mobile.android.activity.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.WebView.ThemeListActivity;
import com.ksider.mobile.android.model.ThemeData;
import com.ksider.mobile.android.view.CircularImageView;

import java.util.ArrayList;

public class ThemeViewPagerFragment extends Fragment {
    private View mRoot;
    private String themes;
    private ArrayList<ThemeData> list = new ArrayList<ThemeData>();
    private int[] themeLayout = {R.id.theme_layout_0, R.id.theme_layout_1, R.id.theme_layout_2, R.id.theme_layout_3, R.id.theme_layout_4, R.id.theme_layout_5};
    private int[] textIds = {R.id.theme_name_0, R.id.theme_name_1, R.id.theme_name_2, R.id.theme_name_3, R.id.theme_name_4, R.id.theme_name_5};
    private int[] imgsIds = {R.id.theme_img_0, R.id.theme_img_1, R.id.theme_img_2, R.id.theme_img_3, R.id.theme_img_4, R.id.theme_img_5};
    private int[] picsIds = {R.drawable.theme_icon_2, R.drawable.theme_icon_2, R.drawable.theme_icon_2, R.drawable.theme_icon_3, R.drawable.theme_icon_4,
            R.drawable.theme_icon_5, R.drawable.theme_icon_6, R.drawable.theme_icon_7, R.drawable.theme_icon_8, R.drawable.theme_icon_2,
            R.drawable.theme_icon_10, R.drawable.theme_icon_11, R.drawable.theme_icon_12, R.drawable.theme_icon_13, R.drawable.theme_icon_14,
            R.drawable.theme_icon_15, R.drawable.theme_icon_16, R.drawable.theme_icon_17, R.drawable.theme_icon_2, R.drawable.theme_icon_19,
            R.drawable.theme_icon_2, R.drawable.theme_icon_2, R.drawable.theme_icon_2, R.drawable.theme_icon_23, R.drawable.theme_icon_24,
            R.drawable.theme_icon_25, R.drawable.theme_icon_2, R.drawable.theme_icon_27, R.drawable.theme_icon_2, R.drawable.theme_icon_29,
            R.drawable.theme_icon_30};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            themes = getArguments().getString("themes");
            if (themes != null && !themes.equals("")) {
                String[] all = themes.split(";");
                for (int i = 0; i < all.length; i++) {
                    if (all[i] != null && !all[i].equals("")) {
                        String[] item = all[i].split(",");
                        if (item.length >= 2) {
                            ThemeData data = new ThemeData();
                            data.name = item[0];
                            data.id = item[1];
                            list.add(data);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        if (mRoot != null) {
            ViewGroup parent = (ViewGroup) mRoot.getParent();
            if (parent != null) {
                parent.removeView(mRoot);
                return mRoot;
            }
        }

        try {
            mRoot = inflater.inflate(R.layout.fragment_theme_pager, container, false);
            for (int i = 0; i < list.size() && i < 6; i++) {
                final ThemeData themeData = list.get(i);
                mRoot.findViewById(themeLayout[i]).setTag(list.get(i).id);
                mRoot.findViewById(themeLayout[i]).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ThemeListActivity.class);
                        intent.putExtra("name", themeData.name);
                        intent.putExtra("id", themeData.id);
                        startActivity(intent);
                    }
                });
                ((TextView) mRoot.findViewById(textIds[i])).setText(list.get(i).name);
                int index = Integer.valueOf(list.get(i).id);
                if (index < 0 || index >= picsIds.length) {
                    index = 0;
                }
                ((CircularImageView) mRoot.findViewById(imgsIds[i])).setImageResource(picsIds[index]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return mRoot;
    }
}
