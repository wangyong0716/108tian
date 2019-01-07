package com.ksider.mobile.android.activity.fragment.buy;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.ksider.mobile.android.WebView.R;

public class GoodsNumCountView extends RelativeLayout {
    private Context mCtx;
    private ImageView mIncrease;
    private ImageView mDecrease;
    private EditText mInput;

    private int goodsNum = 1;
    private OnBuyNumChangedListener mListener;
    /**
     * the number of products in stock
     * -1 -> error
     * 0 -> none product left
     * 1 -> min
     * Integer.MAX_VALUE -> max
     */
    private static int stockNum = Integer.MAX_VALUE;
    /**
     * the number of products can be bought at least per
     * -1 -> error
     * 0 -> can't buy
     * 1 -> min && default
     * Integer.MAX_VALUE -> max
     */
    private static int userMin = 1;
    /**
     * the number of products can be bought at most per user
     * -1 -> error
     * 0 -> can't buy
     * 1 -> min && default
     * Integer.MAX_VALUE -> max
     */
    private static int userMax = Integer.MAX_VALUE;

    public interface OnBuyNumChangedListener {
        void onBuyNumChanged(int num);
    }

    public GoodsNumCountView(Context context) {
        super(context);
        mCtx = context;
        initView();
    }

    public GoodsNumCountView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;
        initView();
    }

    public void initView() {
        View v = LayoutInflater.from(mCtx.getApplicationContext()).inflate(R.layout.goods_num_count, this, true);
        mIncrease = (ImageView) v.findViewById(R.id.increase_goods_num);
        mDecrease = (ImageView) v.findViewById(R.id.decrease_goods_num);
        mInput = (EditText) v.findViewById(R.id.goods_num);

        setGoodsNum(1);
        setStockNum(Integer.MAX_VALUE);
        setUserMax(1);
        setUserMin(1);

        setListeners();
//        setButtonEnabled();
    }

    public void initValues(int stockNum, int userMax, int userMin) {
        setStockNum(stockNum);
        setUserMin(userMin);
        setUserMax(userMax);
        goodsNum = userMin;
        mInput.setText(String.valueOf(goodsNum));
        if (mListener != null) {
            mListener.onBuyNumChanged(goodsNum);
        }
    }

    private void setListeners() {
        mIncrease.setOnClickListener(mIncreaseListener);
        mDecrease.setOnClickListener(mDecreaseListener);
        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                int newNum = 0;
                long readTemp = 0;
                try {
                    readTemp = Long.valueOf(s.toString());
                } catch (Exception ex) {
                    setGoodsNum(userMin);
                    return;
                }
                if (stockNum < userMin) {
                    showError("该产品已售完");
                    return;
                }
                if (readTemp > Integer.MAX_VALUE) {
                    setGoodsNum(Integer.MAX_VALUE);
                    showError("每单最多只能购买" + Integer.MAX_VALUE + "件");
                    return;
                }
                newNum = (int) readTemp;
                if (newNum > stockNum || newNum > userMax) {
                    setGoodsNum(Math.min(stockNum, userMax));
                    showError("每单最多只能购买" + String.valueOf(Math.min(stockNum, userMax)) + "件");
                    return;
                }
                if (newNum < userMin) {
                    setGoodsNum(userMin);
                    showError("每个订单最少需购买" + String.valueOf(userMin) + "件");
                    return;
                }
                goodsNum = newNum;
                if (mListener != null) {
                    mListener.onBuyNumChanged(goodsNum);
                }

                Editable text = mInput.getText();

                if (text != null) {
                    mInput.setSelection(text.length());
                }
//                setButtonEnabled();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void setOnBuyNumChangedListener(OnBuyNumChangedListener l) {
        mListener = l;
    }

    public int getGoodsNum() {
        return goodsNum;
    }

    public void setGoodsNum(int goods) {
        goods = goods > 1 ? goods : 1;
        goods = goods < Math.min(userMax, stockNum) ? goods : Math.min(userMax, stockNum);
        this.goodsNum = goods;
        mInput.setText(String.valueOf(goodsNum));
        if (mListener != null) {
            mListener.onBuyNumChanged(goodsNum);
        }
    }

//    private void setButtonEnabled() {
//        setIncreaseButtonEnabled();
//        setDecreaseButtonEnabled();
//    }

//    private void setIncreaseButtonEnabled() {
//        mIncrease.setEnabled(increaseButtonEnabled());
//    }
//
//    private void setDecreaseButtonEnabled() {
//        mDecrease.setEnabled(decreaseButtonEnabled());
//    }

//    private boolean increaseButtonEnabled() {
//        boolean flag = true;
//        if (stockNum != -1 && goodsNum == stockNum) {
//            flag = false;
//        }
//        return flag;
//    }
//
//    private boolean decreaseButtonEnabled() {
//        boolean flag = true;
//        if (goodsNum <= userMin) {
//            flag = false;
//        }
//        return flag;
//    }

    private boolean canIncrease() {
        if (stockNum != -1 && goodsNum + 1 > stockNum) {
            showError("每单最多购买" + String.valueOf(stockNum) + "件");
            return false;
        }
        return true;
    }

    private boolean canDecrease() {
        if (goodsNum - 1 < userMin) {
            showError("此单限制最少购买" + String.valueOf(userMin) + "件");
            return false;
        }
        return true;
    }

    private boolean canChange(int deltaNum) {
        if (stockNum != -1 && goodsNum + deltaNum > stockNum) {
//            showError("每个订单最多只能购买" + String.valueOf(orderMax) + "件");
            return false;
        }
        return true;
    }

    public int getUserMax() {
        return userMax;
    }

    public void setUserMax(int userMax) {
        if (userMax == 0) {
            this.userMax = Integer.MAX_VALUE;
        } else {
            this.userMax = userMax > 0 ? userMax : Integer.MAX_VALUE;
        }
//        setButtonEnabled();
    }

    public int getUserMin() {
        return userMin;
    }

    public void setUserMin(int userMin) {
        this.userMin = userMin > 1 ? userMin : 1;
//        setButtonEnabled();
    }

    public int getStockNum() {
        return stockNum;
    }

    public void setStockNum(int stockNum) {
        this.stockNum = stockNum > 0 ? stockNum : 0;
//        setButtonEnabled();
    }


    private void showError(String msg) {
        Toast.makeText(mCtx, msg, Toast.LENGTH_LONG).show();
    }

    private OnClickListener mIncreaseListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (canIncrease()) {
                // 提交订单-数量+
                if (goodsNum == Integer.MAX_VALUE) {
                    showError("每单最多只能购买" + Integer.MAX_VALUE + "件");
                    return;
                }
                goodsNum++;
                mInput.setText(String.valueOf(goodsNum));
            }
        }
    };

    private OnClickListener mDecreaseListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (canDecrease()) {
                // 提交订单-数量-
                goodsNum--;
                mInput.setText(String.valueOf(goodsNum));
            }
        }
    };
}