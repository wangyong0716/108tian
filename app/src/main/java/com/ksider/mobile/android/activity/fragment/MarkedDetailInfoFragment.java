package com.ksider.mobile.android.activity.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by yong on 8/1/15.
 */
public class MarkedDetailInfoFragment extends Fragment {
    protected View mRoot;
    private JSONArray array;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_fee_detail_info, container, false);
        try {
            array = new JSONArray(getArguments().getString("brief"));
        } catch (JSONException js) {
            js.printStackTrace();
        }
        String value = "";
        ArrayList<Integer> list = new ArrayList<Integer>();
        TextView content = (TextView) mRoot.findViewById(R.id.desc);
        for (int i = 0; i < array.length(); i++) {
            if (i != 0) {
                value += "\n";
            }
            try {
                JSONObject object = array.getJSONObject(i);
                String type = object.getString("type");
                if (type.equals("text")) {
                    value += object.getString("value");
//                    TextView text = (TextView) getActivity().getLayoutInflater().inflate(R.layout.activity_detail_text, null);
//                    text.setText(object.getString("value"));
//                    container.addView(text);
                } else if (type.equals("title1")) {
                    list.add(value.length());
                    value += getResources().getString(R.string.large_dot) + object.getString("value");
//                    TextView title1 = (TextView) getActivity().getLayoutInflater().inflate(R.layout.activity_detail_title1, null);
//                    title1.setText(object.getString("value"));
//                    container.addView(title1);
                }

            } catch (JSONException js) {
                js.printStackTrace();
                continue;
            }
        }
        SpannableStringBuilder builder = new SpannableStringBuilder(value);
        for (int i = 0; i < list.size(); i++) {
//            Drawable d = getResources().getDrawable(R.drawable.list_dot_icon);
//            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
//            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
//            builder.setSpan(span, list.get(i), list.get(i) + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            AbsoluteSizeSpan largeSizeSpan = new AbsoluteSizeSpan((int) getResources().getDimension(R.dimen.text_large_size));
            ForegroundColorSpan blueColor = new ForegroundColorSpan(getResources().getColor(R.color.personal_info_text_color_selected));
            builder.setSpan(blueColor, list.get(i), list.get(i) + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//            builder.setSpan(largeSizeSpan, list.get(i), list.get(i) + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        content.setText(builder);
        return mRoot;
    }
}
