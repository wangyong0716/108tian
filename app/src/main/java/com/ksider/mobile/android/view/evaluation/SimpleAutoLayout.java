package com.ksider.mobile.android.view.evaluation;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.EvaluationModel;

import java.util.ArrayList;

/**
 * Created by yong on 11/5/15.
 */
public class SimpleAutoLayout extends RelativeLayout {
    //=margin_left=margin_right
    private static int SIDE_MARGIN = 0;

    //margin_left of item
    private static int dividerWidth = 30;
    //margin_top of item
    private static int dividerHeight = 10;

    private int count = 0;

    private TextView[] tvs;
    //define the max textView to show, should not be larger than the size of tvs
    private static int maxNum = 5;

    //height of childView
    private static int height = 0;
    //total height of childView and dividerHeight
    private static int columnHeight;

    /**
     * @param context
     */
    public SimpleAutoLayout(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public SimpleAutoLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.AutoLayout, defStyle, 0);

        dividerWidth = attributes.getDimensionPixelOffset(R.styleable.AutoLayout_divider_width, 30);
        dividerHeight = attributes.getDimensionPixelOffset(R.styleable.AutoLayout_divider_height, 10);
        SIDE_MARGIN = attributes.getDimensionPixelOffset(R.styleable.AutoLayout_horizontal_margin, 0);

        height = attributes.getDimensionPixelOffset(R.styleable.AutoLayout_item_height, 93);
        columnHeight = height + dividerHeight;
        init();
    }

    /**
     * @param context
     * @param attrs
     */
    public SimpleAutoLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void init() {
        TextView tv1 = (TextView) ((Activity) getContext()).getLayoutInflater().inflate(R.layout.evaluation_textview, null);
        TextView tv2 = (TextView) ((Activity) getContext()).getLayoutInflater().inflate(R.layout.evaluation_textview, null);
        TextView tv3 = (TextView) ((Activity) getContext()).getLayoutInflater().inflate(R.layout.evaluation_textview, null);
        TextView tv4 = (TextView) ((Activity) getContext()).getLayoutInflater().inflate(R.layout.evaluation_textview, null);
        TextView tv5 = (TextView) ((Activity) getContext()).getLayoutInflater().inflate(R.layout.evaluation_textview, null);
        super.addView(tv1);
        super.addView(tv2);
        super.addView(tv3);
        super.addView(tv4);
        super.addView(tv5);
        tvs = new TextView[]{tv1, tv2, tv3, tv4, tv5};
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int actualWidth = r - l;
        int left = SIDE_MARGIN;
        int right;
        int top = 0;
        int bottom = height;

        for (int i = 0; i < count; i++) {
            int width = tvs[i].getMeasuredWidth();
            right = left + width;
            if (right > actualWidth && i != 0) {
                left = SIDE_MARGIN;
                right = left + width;
                top += columnHeight;
                bottom += columnHeight;
            }
            tvs[i].layout(left, top, right, bottom);
            left = right + dividerWidth;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int x = 0;
        int y = height;
        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int actualWidth = specWidth - SIDE_MARGIN * 2;

        for (int index = 0; index < count; index++) {
            tvs[index].measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            int width = tvs[index].getMeasuredWidth();
            x += width;
            if (x > actualWidth && index != 0) {
                x = width;
                y += columnHeight;
            }
            x += dividerWidth;
        }
        setMeasuredDimension(actualWidth, y);
    }

    public void addContent(ArrayList<EvaluationModel> arrays) {
        int length = 0;
        if (arrays != null) {
            length = arrays.size() < maxNum ? arrays.size() : maxNum;
        }
        int i = 0;
        while (i < length) {
            tvs[i].setVisibility(VISIBLE);
            tvs[i].setText(arrays.get(i).getContent());
            i++;
        }
        count = i;
        while (i < tvs.length) {
            tvs[i++].setVisibility(GONE);
        }
    }
}