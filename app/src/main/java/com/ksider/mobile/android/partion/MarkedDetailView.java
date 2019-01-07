package com.ksider.mobile.android.partion;


import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.MoreDetailInfoActivity;
import com.ksider.mobile.android.WebView.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by yong on 2015/6/2.
 */
public class MarkedDetailView extends LinearLayout {
    private Context context;
    private JSONArray array;
    private int type;

    public MarkedDetailView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MarkedDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public MarkedDetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.partion_fee_detail_view, this);
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }

    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setContent(JSONArray array) {
        this.array = array;
//        ((AlignTextView) findViewById(R.id.part_content)).setContent(content);
        String value = "";
        ArrayList<Integer> list = new ArrayList<Integer>();
        TextView content = (TextView) findViewById(R.id.part_content);
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
//            Drawable d = getResources().getDrawable(R.drawable.dot_blue);
//            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
//            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
//            builder.setSpan(span, list.get(i), list.get(i) + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            AbsoluteSizeSpan largeSizeSpan = new AbsoluteSizeSpan((int) getResources().getDimension(R.dimen.text_large_size));
            ForegroundColorSpan blueColor = new ForegroundColorSpan(getResources().getColor(R.color.personal_info_text_color_selected));
            builder.setSpan(blueColor, list.get(i), list.get(i) + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//            builder.setSpan(largeSizeSpan, list.get(i), list.get(i) + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        content.setText(builder);
    }

    public void setContent(int contentId) {
        setTitle(getResources().getString(contentId));
    }

    public void setMoreText(String moreText) {
        ((TextView) findViewById(R.id.more)).setText(moreText);
    }

    public void setMoreText(int moreTextId) {
        setMoreText(getResources().getString(moreTextId));
    }

    public void setMoreClickEvent() {
        findViewById(R.id.more).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MoreDetailInfoActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("brief", array.toString());
                context.startActivity(intent);
            }
        });
    }
}
