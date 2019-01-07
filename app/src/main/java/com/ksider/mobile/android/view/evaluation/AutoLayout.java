package com.ksider.mobile.android.view.evaluation;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import com.ksider.mobile.android.WebView.R;

import java.util.ArrayList;

/**
 * Created by yong on 11/5/15.
 */
public class AutoLayout extends ViewGroup {
    //=margin_left=margin_right
    private int SIDE_MARGIN = 0;

    //max rows to be shown when open items
    private int ROW_MAX = 6;
    //max rows to be shown when close items
    private int ROW_MIN = 3;
    //rows to be shown
    private int maxRow = Integer.MAX_VALUE;
    //margin_left of item
    private int dividerWidth = 30;
    //margin_top of item
    private int dividerHeight = 10;

    private boolean hasMoreButton = false;
    private boolean hasAddButton = false;
    //index of moreButton
    private int moreId = -1;
    //index of addButton
    private int addId = -1;
    //moreButton
    private View moreView;
    // add button
    private View addView;
    //if has moreButton
    private boolean moreEnable = false;
    //if has addButton
    private boolean addEnable = false;
    //listener of button click
    private OnItemClickListener onItemClickListener, moreClickListener, addClickListener;
    //store index of selected item
    private ArrayList<Integer> selectedArray = new ArrayList<Integer>();

    //to indicate whether all item have been measured
    private boolean reDrawn = false;

    /**
     * @param context
     */
    public AutoLayout(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public AutoLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // load the styled attributes and set their properties
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.AutoLayout, defStyle, 0);

        ROW_MIN = attributes.getInteger(R.styleable.AutoLayout_row_min, 3);
        ROW_MAX = attributes.getInteger(R.styleable.AutoLayout_row_max, 6);
        maxRow = attributes.getInteger(R.styleable.AutoLayout_max_row, 3);

        dividerWidth = attributes.getDimensionPixelOffset(R.styleable.AutoLayout_divider_width, 30);
        dividerHeight = attributes.getDimensionPixelOffset(R.styleable.AutoLayout_divider_height, 10);
        SIDE_MARGIN = attributes.getDimensionPixelOffset(R.styleable.AutoLayout_horizontal_margin, 0);
    }

    /**
     * @param context
     * @param attrs
     */
    public AutoLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int actualWidth = r - l;
        int x = SIDE_MARGIN;
        int y = 0;
        int rows = 1;

        int buttonWidth = 0;
        int moreWidth = 0, moreHeight = 0;
        int addWidth = 0, addHeight = 0;
        if (hasMoreButton && moreView != null) {
            moreWidth = moreView.getMeasuredWidth();
            moreHeight = moreView.getMeasuredHeight();
            buttonWidth += moreWidth;
        }
        if (hasAddButton && addView != null) {
            addWidth = addView.getMeasuredWidth();
            addHeight = addView.getMeasuredHeight();
            buttonWidth += addWidth;
        }
        buttonWidth += dividerWidth;
        for (int i = 0; i < childCount; i++) {
            if (rows < maxRow || !reDrawn) {
                View view = getChildAt(i);
                int width = view.getMeasuredWidth();
                int height = view.getMeasuredHeight();
                x += width + dividerWidth;
                if (x - dividerWidth > actualWidth && i != 0) {
                    x = width + SIDE_MARGIN + dividerWidth;
                    rows++;
                }
                y = rows * (height + dividerHeight) - dividerHeight;
                if (rows == maxRow && hasAddButton && hasMoreButton && x + buttonWidth > actualWidth) {
                    view.setVisibility(GONE);
                    x = SIDE_MARGIN;
                    moreView.layout(x, y - height, x + moreWidth, y);
                    x += (moreWidth + dividerWidth);
                    addView.layout(x, y - height, x + addWidth, y);
                    for (; i < childCount; i++) {
                        if (i != addId && i != moreId) {
                            getChildAt(i).setVisibility(GONE);
                        }
                    }
                    break;
                } else {
                    view.setVisibility(VISIBLE);
                    view.layout(x - width - dividerWidth, y - height, x - dividerWidth, y);
                }
            } else {
                View view = getChildAt(i);
                int width = view.getMeasuredWidth();
                int height = view.getMeasuredHeight();
                x += width + dividerWidth;
                if (hasMoreButton && hasAddButton) {
                    if (x + buttonWidth > actualWidth) {
                        view.setVisibility(GONE);
                        x -= (width + dividerWidth);
                        moreView.layout(x, y - height, x + moreWidth, y);
                        x += (moreWidth + dividerWidth);
                        addView.layout(x, y - height, x + addWidth, y);
                        for (; i < childCount; i++) {
                            if (i != addId && i != moreId) {
                                getChildAt(i).setVisibility(GONE);
                            }
                        }
                        break;
                    } else {
                        view.setVisibility(VISIBLE);
                        view.layout(x - width - dividerWidth, y - height, x - dividerWidth, y);
                    }
                } else {
                    if (x - dividerWidth <= actualWidth) {
                        view.setVisibility(VISIBLE);
                        view.layout(x - width - dividerWidth, y - height, x - dividerWidth, y);
                    }
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int x = 0;
        int y = 0;
        int rows = 1;
        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int actualWidth = specWidth - SIDE_MARGIN * 2;
        int childCount = getChildCount();
        for (int index = 0; index < childCount; index++) {
            View child = getChildAt(index);
            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            x += width + dividerWidth;
            if (x - dividerWidth > actualWidth && index != 0) {
                x = width + SIDE_MARGIN + dividerWidth;
                rows++;
            }
            if (rows > maxRow && reDrawn) {
                break;
            }
            y = rows * (height + dividerHeight) - dividerHeight;
        }
        setMeasuredDimension(actualWidth, y);
    }

    /**
     * set listener
     * has to be executed finally
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (i == addId && addClickListener != null) {
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addClickListener.onClick(addId);
                    }
                });
            } else if (i == moreId && moreClickListener != null) {
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setMoreEnable(!moreEnable);
                        moreClickListener.onClick(moreId);
                    }
                });
            } else if (onItemClickListener != null) {
                final int index = i;
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setCheckedId(index);
                        AutoLayout.this.onItemClickListener.onClick(index);
                    }
                });
            }
        }

        this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!reDrawn) {
                    reDrawn = true;
                    setMaxRow(maxRow);
                }
            }
        });
    }

    /**
     * set the value of whether all the view need shown have been measured which
     * means views haven't been measure won't be shown forever if true
     *
     * @param reDrawn
     */
    public void setReDrawn(boolean reDrawn) {
        this.reDrawn = reDrawn;
    }

    /**
     * change check-state of item
     *
     * @param index
     */
    public void setCheckedId(int index) {
        if (index > getChildCount()) {
            return;
        }
        if (selectedArray.contains(index)) {
            View view = getChildAt(index);
            view.setBackgroundResource(R.drawable.evaluate_unselect_background);
            selectedArray.remove((Object) index);
        } else {
            View view = getChildAt(index);
            view.setBackgroundResource(R.drawable.evaluate_selected_background);
            selectedArray.add(index);
        }
    }

    /**
     * whether to check a item decided by boolean value checked
     *
     * @param index
     * @param checked
     */
    public void setCheckedId(int index, boolean checked) {
        if (index > getChildCount()) {
            return;
        }
        if (checked) {
            if (selectedArray.contains(index)) {
                return;
            } else {
                View view = getChildAt(index);
                view.setBackgroundResource(R.drawable.evaluate_selected_background);
                selectedArray.add(index);
            }
        } else {
            if (selectedArray.contains(index)) {
                View view = getChildAt(index);
                view.setBackgroundResource(R.drawable.evaluate_unselect_background);
                selectedArray.remove((Object) index);
            } else {
                return;
            }
        }
    }

    /**
     * open/close item hidden
     *
     * @param moreEnable
     */
    public void setMoreEnable(boolean moreEnable) {
        this.moreEnable = moreEnable;
        if (this.moreEnable) {
            if (moreView != null) {
                moreView.setBackgroundResource(R.drawable.more_evaluation_selected);
            }
            setMaxRow(ROW_MAX);
        } else {
            if (moreView != null) {
                moreView.setBackgroundResource(R.drawable.more_evaluation_unselect);
            }
            setMaxRow(ROW_MIN);
        }
    }

    /**
     * change add button state when open/close the dialog to input
     *
     * @param addEnable
     */
    public void setAddEnable(boolean addEnable) {
        this.addEnable = addEnable;
        if (addView != null) {
            if (this.addEnable) {
                addView.setBackgroundResource(R.drawable.add_evaluation_selected);
            } else {
                addView.setBackgroundResource(R.drawable.add_evaluation_unselect);
            }
        }
    }

    /**
     * set moreButton
     *
     * @param moreButton
     * @param moreListener
     */
    public void setMoreButton(View moreButton, OnItemClickListener moreListener) {
        if (moreButton != null) {
            hasMoreButton = true;
            this.moreView = moreButton;
            this.moreClickListener = moreListener;
            this.moreId = getChildCount();
            super.addView(moreView);
        }
    }

    /**
     * add a child view with a value determines the view selected or not
     *
     * @param view
     * @param checked
     */
    public void addView(View view, boolean checked) {
        super.addView(view);
        final int index = getChildCount() - 1;
        if (onItemClickListener != null) {
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setCheckedId(index);
                    onItemClickListener.onClick(index);
                }
            });
            setCheckedId(index, checked);
        }
    }

    /**
     * return the index array of selected items
     *
     * @return
     */
    public ArrayList<Integer> getSelectedArray() {
        return selectedArray;
    }

    /**
     * set addButton
     *
     * @param addButton
     * @param addListener
     */
    public void setAddButton(View addButton, OnItemClickListener addListener) {
        if (addButton != null) {
            hasAddButton = true;
            this.addView = addButton;
            this.addClickListener = addListener;
            this.addId = getChildCount();
            super.addView(addView);
        }
    }

    /**
     * set the value of rows to be shown
     *
     * @param max
     */
    public void setMaxRow(int max) {
        this.maxRow = max;
        requestLayout();
    }

    public interface OnItemClickListener {
        public void onClick(int index);
    }
}