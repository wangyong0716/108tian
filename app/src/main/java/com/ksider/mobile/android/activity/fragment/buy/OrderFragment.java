package com.ksider.mobile.android.activity.fragment.buy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.PurchaseAcitvity;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.activity.fragment.FragmentCallback;
import com.ksider.mobile.android.activity.fragment.signup.ModifyPhoneDialogFragment;
import com.ksider.mobile.android.activity.fragment.signup.OrderVerifyCodeFragment;
import com.ksider.mobile.android.model.BaseComparator;
import com.ksider.mobile.android.model.CouponModel;
import com.ksider.mobile.android.model.OrderModel;
import com.ksider.mobile.android.model.ProductStockModel;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.utils.net.toolbox.FormJsonObjectRequest;
import com.umeng.analytics.MobclickAgent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wenkui on 4/28/15.
 */
public class OrderFragment extends Fragment {
    protected long mStartTime = -1;
    protected long quantity = -1;
    protected long orderQuantity = -1;
    private double sellPrice = -1;
    protected JSONObject mProduct;
    protected View mRoot;
    private double couponWorth = 0;
    private double shouldPay = 0;
    private long couponId;
    private long productId = 0;
    private int poiType = 0;
    private int refund = 0;
    private int productType = 0;
    private int quantityPerUser = 0;
    private OrderVerifyCodeFragment fragment;

    private ProductStockModel selectedStock = new ProductStockModel();

    protected ArrayList<ProductStockModel> stocks = new ArrayList<ProductStockModel>();

    private ArrayList<CouponModel> coupons = new ArrayList<CouponModel>();
    private ArrayList<Integer> filteredIndex = new ArrayList<Integer>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView more = (TextView) getActivity().findViewById(R.id.more_choice);
        if (more != null) {
            more.setVisibility(View.VISIBLE);
            more.setText("客服");
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConsultDialog.Builder builder = new ConsultDialog.Builder(getActivity());
                    builder.setTitle(getResources().getString(R.string.contact_service)).setPhone(getResources().getString(R.string.consult_phone_tips, Constants.CONSULT_NUMBER))
                            .setNegativeButton(getResources().getString(R.string.edit_profile_cancel), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).setPositiveButton(getResources().getString(R.string.phone_call), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Constants.CONSULT_NUMBER));
                            startActivity(phoneIntent);
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("hasProduct", "true");
                            map.put("productId", productId + "");
                            switch (poiType) {
                                case 1:
                                    MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_TAG_SCENE, map);
                                    break;
                                case 2:
                                case 3:
                                    MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_TAG_RESORT, map);
                                    break;
                                case 4:
                                    MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_TAG_PICK, map);
                                    break;
                                case 5:
                                    MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_TAG_EVENT, map);
                                    break;
                                default:
                                    MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_TAG_RECOMMEND, map);
                                    break;
                            }
                        }
                    }).show();
                }
            });
        }

        if (mRoot != null) {
            ViewGroup parent = (ViewGroup) mRoot.getParent();
            if (parent != null) {
                parent.removeView(mRoot);
                return mRoot;
            }
        }
        mRoot = inflater.inflate(R.layout.fragment_buy_order_coupon, container, false);
        ((TextView) mRoot.findViewById(R.id.submit)).setText("提交订单");

        String product = null;
        if (savedInstanceState == null) {
            Bundle args = getArguments();
            product = args.getString("product");
            try {
                mProduct = new JSONObject(product);
                productType = mProduct.getInt("productType");
                productId = mProduct.getLong("productId");
                poiType = mProduct.getInt("poiType");
                refund = mProduct.getInt("refund");
                try {
                    quantityPerUser = mProduct.getInt("quantityPerUser");
                } catch (JSONException js) {
                    js.printStackTrace();
                }
                selectedStock.setStartTime(mProduct.getLong("startTime"));
                selectedStock.setQuantity(mProduct.getInt("quantity"));
                selectedStock.setSellPrice(mProduct.getDouble("sellPrice"));
            } catch (JSONException js) {
                js.printStackTrace();
            }
        } else {
            product = savedInstanceState.getString("product", "");
            mStartTime = savedInstanceState.getLong("startTime", -1);
            quantity = savedInstanceState.getLong("quantity", -1);
            orderQuantity = savedInstanceState.getLong("orderQuantity", -1);
            sellPrice = savedInstanceState.getDouble("sellPrice", -1);
            couponWorth = savedInstanceState.getDouble("couponWorth");
            shouldPay = savedInstanceState.getDouble("shouldPay");
            couponId = savedInstanceState.getLong("couponId");
            productId = savedInstanceState.getLong("productId");
            poiType = savedInstanceState.getInt("poiType");
            refund = savedInstanceState.getInt("refund");
            quantityPerUser = savedInstanceState.getInt("quantityPerUser");
            try {
                mProduct = new JSONObject(product);
                productType = mProduct.getInt("productType");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (selectedStock == null) {
                selectedStock = new ProductStockModel();
            }
            selectedStock.setStartTime(mStartTime);
            selectedStock.setQuantity(quantity);
            selectedStock.setSellPrice(sellPrice);
        }
        if (productType == Status.PRODUCT_TYPE_ALWAYS) {
            mRoot.findViewById(R.id.date_clickarea).setVisibility(View.GONE);
        }
        initOrder();
        MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_PURCHASE_ORDER);
        return mRoot;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        new SlidingLayoutFragment(getActivity(),this);
    }

    protected void initOrder() {
        if (mProduct == null || mRoot == null) {
            return;
        }
        try {
            TextView textView = (TextView) mRoot.findViewById(R.id.order_title);
            textView.setText(mProduct.getString("productName"));
            textView = (TextView) mRoot.findViewById(R.id.order_name);
            textView.setText(mProduct.getString("productName"));

            if (productType != Status.PRODUCT_TYPE_ALWAYS && selectedStock.getStartTime() > 0) {
                ((TextView) mRoot.findViewById(R.id.use_date)).setText(DateUtils.getFormatDateWithWeek(selectedStock.getStartTime()));
            } else {
                mRoot.findViewById(R.id.date_clickarea).setVisibility(View.GONE);
            }

            textView = (TextView) mRoot.findViewById(R.id.phone);
            textView.setText(UserInfo.getPhone());
            if (selectedStock != null && selectedStock.getSellPrice() > 0) {
                textView = (TextView) mRoot.findViewById(R.id.unit_price);
                textView.setText(StringUtils.getPrice(selectedStock.getSellPrice()) + "元");
            }

            final GoodsNumCountView count = (GoodsNumCountView) mRoot.findViewById(R.id.goods_num_count);
            count.setOnBuyNumChangedListener(new GoodsNumCountView.OnBuyNumChangedListener() {
                @Override
                public void onBuyNumChanged(int num) {
                    orderQuantity = num;
                    if (selectedStock.getSellPrice() > 0) {
                        shouldPay = selectedStock.getSellPrice() * num;

                        double endPay = shouldPay - couponWorth;
                        endPay = endPay >= 0.01 ? endPay : 0.01;
                        ((TextView) mRoot.findViewById(R.id.price)).setText(StringUtils.getPrice(endPay) + "元");
                        addCouponList(shouldPay);
                    } else {
                        ((TextView) mRoot.findViewById(R.id.price)).setText("0元");
                    }
                }
            });
            long quantityTemp = (selectedStock == null || selectedStock.getQuantity() < 0) ? 0 : selectedStock.getQuantity();
            if (selectedStock == null || selectedStock.getQuantity() < 0) {
                count.initValues(Integer.MAX_VALUE, Integer.MAX_VALUE, 1);
            } else {
                count.initValues((int) quantityTemp, quantityPerUser, 1);
            }

            if (orderQuantity > 0 && orderQuantity <= selectedStock.getQuantity() && orderQuantity <= quantityPerUser) {
                count.setGoodsNum((int) orderQuantity);
            }

            if (refund == 1) {
                ((TextView) mRoot.findViewById(R.id.refundAble)).setText(R.string.support_refund);
            } else {
                ((TextView) mRoot.findViewById(R.id.refundAble)).setText(R.string.no_support_refund);
            }
            View view = mRoot.findViewById(R.id.bindPhoneClick);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDialog();
                }
            });

            mRoot.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!UserInfo.isLogin()) {
                        if (getChildFragmentManager().findFragmentByTag("login") != null) {
                            ((OrderVerifyCodeFragment) getChildFragmentManager().findFragmentByTag("login")).verifyCode();
                        }
                        return;
                    } else {
                        if (selectedStock.getQuantity() <= 0) {
                            Toast.makeText(getActivity(), "已无库存！", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (UserInfo.getPhone() == null || UserInfo.getPhone().length() != 11) {
                            Toast.makeText(getActivity(), "请绑定手机号！", Toast.LENGTH_LONG).show();
                            return;
                        }

                        mRoot.findViewById(R.id.submit).setEnabled(false);
                        try {
                            OrderModel mOrderModel = new OrderModel();
                            mOrderModel.setProductName(mProduct.getString("productName"));
                            mOrderModel.setSellPrice(selectedStock.getSellPrice());
                            mOrderModel.setConsumeTime(selectedStock.getStartTime());
                            mOrderModel.setCouponDiscount(couponWorth);
                            mOrderModel.setCouponId(couponId);
                            Log.v(Constants.LOG_TAG, "product_consume_date:" + mOrderModel.getConsumeTime());
                            mOrderModel.setQuantity(count.getGoodsNum());
                            mOrderModel.setProductId(mProduct.getLong("productId"));
                            //add an order!
                            Network.getInstance().addToRequestQueue(addOrderRequest(mOrderModel));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showLogin();
    }

    public void getCoupons(JSONArray array) {
        if (array.length() == 0) {
            setNoCouponView();
            return;
        }
        coupons.clear();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject o = array.getJSONObject(i);
                CouponModel coupon = new CouponModel();
                try {
                    coupon.setCouponId(o.getLong("couponId"));
                } catch (JSONException js) {
                    js.printStackTrace();
                }
                try {
                    coupon.setCouponName(o.getString("couponName"));
                    coupon.setWorth(o.getDouble("worth"));
                    coupon.setValidTime(o.getLong("validTime"));
                    coupon.setStatus(o.getInt("status"));
                } catch (JSONException js) {
                    js.printStackTrace();
                }
                try {
                    coupon.setFeeConstraint(o.getDouble("feeConstraint"));
                } catch (JSONException js) {
                    coupon.setFeeConstraint(0);
                    js.printStackTrace();
                }
                coupons.add(coupon);
                Collections.sort(coupons, CouponModel.getComparator(BaseComparator.MULTI_SORT));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        filterCoupon();
        addCouponList(shouldPay);
    }

    public void filterCoupon() {
        filteredIndex.clear();
        if (coupons.size() > 0) {
            filteredIndex.add(0);
        } else {
            return;
        }
        for (int i = 1, j = 0; i < coupons.size() && j < coupons.size(); i++) {
            if (equals(coupons.get(i), coupons.get(j))) {
                continue;
            } else {
                j = i;
                filteredIndex.add(j);
            }
        }
    }

    public boolean equals(CouponModel coupon1, CouponModel coupon2) {
        if (coupon1.getFeeConstraint() == coupon2.getFeeConstraint() && coupon1.getWorth() == coupon2.getWorth()) {
            return true;
        }
        return false;
    }

    public void addCouponList(double totalFee) {
        if (filteredIndex.size() < 1) {
            return;
        }
        LinearLayout couponListLayout = (LinearLayout) mRoot.findViewById(R.id.coupon_list_layout);
        couponListLayout.removeAllViews();
        int choice = 0;
        for (int i = 0; i < filteredIndex.size(); i++) {
            if (coupons.get(filteredIndex.get(i)).getFeeConstraint() > totalFee) {
                addItem(couponListLayout, coupons.get(filteredIndex.get(i)));
            } else {
                if (choice == 0) {
                    addItem(couponListLayout, coupons.get(filteredIndex.get(i)), true, choice);
                } else {
                    addItem(couponListLayout, coupons.get(filteredIndex.get(i)), false, choice);
                }
                choice++;
            }
        }
//        int i = 0;
//        //visit coupon cant be used
//        while (i < coupons.size() && coupons.get(i).getFeeConstraint() > totalFee) {
//            i++;
//        }
//        //replace old view with new coupon
//        while (i < coupons.size() && choice < couponListLayout.getChildCount()) {
//            fillItem(couponListLayout.getChildAt(choice), coupons.get(i), choice == 0);
//            i++;
//            choice++;
//        }
//        //add item to show more coupon
//        while (i < coupons.size()) {
//            addItem(couponListLayout, coupons.get(i), choice == 0);
//            i++;
//            choice++;
//        }
//        //remove useless item
//        for (i = couponListLayout.getChildCount() - 1; i >= choice; i--) {
//            couponListLayout.removeViewAt(i);
//        }

        if (choice == 0) {
            setNoCouponView();
        }
    }

    public void setNoCouponView() {
        LinearLayout couponListLayout = (LinearLayout) mRoot.findViewById(R.id.coupon_list_layout);
        couponListLayout.removeAllViews();
        View view = getActivity().getLayoutInflater().inflate(R.layout.list_view_empty_coupon_item, null);
        couponListLayout.addView(view);
    }

    public void addItem(LinearLayout couponListLayout, CouponModel coupon) {
        CheckBox box = (CheckBox) getActivity().getLayoutInflater().inflate(R.layout.choose_coupon_item, null);
        box.setEnabled(false);
        String couponDes = getResources().getString(R.string.coupon_des, StringUtils.getPrice(coupon.getFeeConstraint())) + " ";
        couponDes += getResources().getString(R.string.coupon_profit, StringUtils.getPrice(coupon.getWorth()));
        box.setText(couponDes);
        box.setTextColor(getResources().getColor(R.color.gray_1));
        box.setCompoundDrawables(null, null, null, null);
        couponListLayout.addView(box);
    }

    public void addItem(LinearLayout couponListLayout, CouponModel coupon, boolean selected, int index) {
        CheckBox box = (CheckBox) getActivity().getLayoutInflater().inflate(R.layout.choose_coupon_item, null);
        box.setTag(coupon);
        box.setOnCheckedChangeListener(couponCheckedChangeListener);
        String couponDes = getResources().getString(R.string.coupon_des, StringUtils.getPrice(coupon.getFeeConstraint())) + " ";
        int length = couponDes.length();
        couponDes += getResources().getString(R.string.coupon_profit, StringUtils.getPrice(coupon.getWorth()));
        SpannableStringBuilder builder = new SpannableStringBuilder(couponDes);
        ForegroundColorSpan colorSpan;
        if (selected) {
            box.setChecked(true);
            colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.orange));
        } else {
            box.setChecked(false);
            colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.content_color));
        }
        builder.setSpan(colorSpan, length, couponDes.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        box.setText(builder);
        couponListLayout.addView(box, index);
    }

    private CompoundButton.OnCheckedChangeListener couponCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            LinearLayout couponListLayout = (LinearLayout) mRoot.findViewById(R.id.coupon_list_layout);
            for (int i = 0; i < couponListLayout.getChildCount(); i++) {
                CheckBox box = (CheckBox) couponListLayout.getChildAt(i);
                box.setChecked(false);
            }
            CouponModel coupon = (CouponModel) buttonView.getTag();
            if (coupon != null) {
                buttonView.setChecked(isChecked);
                updateCouponSelected(coupon, isChecked);
            }
        }
    };

    protected void showLogin() {
        if (!UserInfo.isLogin()) {
            fragment = new OrderVerifyCodeFragment();
            Bundle args = new Bundle();
            args.putString("action", "login");
            fragment.setArguments(args);
            getChildFragmentManager().beginTransaction().add(R.id.register, fragment, "login").commit();
            mRoot.findViewById(R.id.modify_phone).setVisibility(View.GONE);
            fragment.setOnHide(new OrderVerifyCodeFragment.OnDialogHide() {
                @Override
                public void onHide() {
                    hideLogin();
                    String phone = UserInfo.getPhone();
                    if (phone != null && getView() != null) {
                        TextView textView = (TextView) getView().findViewById(R.id.phone);
                        textView.setText(phone);
                    }
                }
            });
        }
    }

    protected void hideLogin() {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        Fragment login = getChildFragmentManager().findFragmentByTag("login");
        if (login != null) {
            ft.remove(login).commit();
        }
        mRoot.findViewById(R.id.modify_phone).setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRoot.findViewById(R.id.purchase_bottom_bar) != null) {
            mRoot.findViewById(R.id.purchase_bottom_bar).setBackgroundColor(getResources().getColor(R.color.divider_line_color));
        }
        View view = getActivity().findViewById(R.id.date_clickarea);
        if (view != null) {
            view.setEnabled(true);
        }

        coupons.clear();
        Network.getInstance().addToRequestQueue(getCouponListRequest());
    }

    protected void showDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        ModifyPhoneDialogFragment newFragment = new ModifyPhoneDialogFragment();
        Bundle args = new Bundle();
        args.putString("action", "bind");
        newFragment.setArguments(args);
        newFragment.show(ft, "dialog");
        newFragment.setOnHide(new ModifyPhoneDialogFragment.OnDialogHide() {
            @Override
            public void onHide() {
                String phone = UserInfo.getPhone();
                if (phone != null && getView() != null) {
                    TextView textView = (TextView) getView().findViewById(R.id.phone);
                    textView.setText(phone);
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("product", mProduct.toString());
        outState.putLong("startTime", selectedStock != null ? selectedStock.getStartTime() : -1);
        outState.putLong("quantity", selectedStock != null ? selectedStock.getQuantity() : -1);
        outState.putLong("orderQuantity", orderQuantity);
        outState.putDouble("sellPrice", selectedStock != null ? selectedStock.getSellPrice() : -1);
        outState.putDouble("couponWorth", couponWorth);
        outState.putLong("couponId", couponId);
        outState.putDouble("shouldPay", shouldPay);
        outState.putLong("productId", productId);
        outState.putDouble("poiType", poiType);
        outState.putLong("productId", productId);
        outState.putInt("refund", refund);
        outState.putInt("quantityPerUser", quantityPerUser);
    }

    public void updateCouponSelected(CouponModel coupon, boolean isChecked) {
        if (isChecked) {
            couponWorth = coupon.getWorth();
            couponId = coupon.getCouponId();
        } else {
            couponWorth = 0;
            couponId = 0;
        }
        double endPay = shouldPay - couponWorth;
        endPay = endPay >= 0.01 ? endPay : 0.01;
        ((TextView) mRoot.findViewById(R.id.price)).setText(getResources().getString(R.string.coupon_worth, StringUtils.getPrice(endPay)));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected FormJsonObjectRequest addOrderRequest(OrderModel mOrderModel) {
        FormJsonObjectRequest request = new FormJsonObjectRequest(APIUtils.getUrl(APIUtils.ORDER),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject reponse) {
                        try {
                            mRoot.findViewById(R.id.submit).setEnabled(true);
                            if (reponse.getInt("status") == 0) {
                                if (getActivity() instanceof FragmentCallback) {
                                    JSONObject params = reponse.getJSONObject("data");
                                    JSONObject order = params.getJSONObject("order");
                                    order.put("refund", refund);
                                    order.put("sellPrice", selectedStock.getSellPrice());
                                    FragmentCallback callback = (FragmentCallback) getActivity();
                                    try {
                                        order.put(PurchaseAcitvity.STAGE, PurchaseAcitvity.ORDER);
                                        callback.next(order);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                Toast.makeText(getActivity(), reponse.getString("msg"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "服务器异常！", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(Constants.LOG_TAG, volleyError.toString());
                Toast.makeText(getActivity(), "访问网络失败！", Toast.LENGTH_LONG).show();
            }
        });
        request.addFormParams("action", "add");

        request.addFormParams("productId", String.valueOf(mOrderModel.getProductId()));
        String sid = Storage.sharedPref.getString(Storage.SESSION_ID, null);
        if (sid != null) {
            request.addFormParams("sid", sid);
        }
        request.addFormParams("startTime", String.valueOf(mOrderModel.getConsumeTime()));
        request.addFormParams("quantity", String.valueOf(mOrderModel.getQuantity()));
        request.setShouldCache(false);
        if (mOrderModel.getCouponId() > 0) {
            request.addFormParams("couponId", mOrderModel.getCouponId() + "");
        }
        return request;
    }

    protected String getCouponListRequestUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "listNew");
        params.put("type", "valid");
        params.put("productId", productId);
        params.put("poiType", poiType);
        params.put("quantity", quantity);
        return APIUtils.getUrl(APIUtils.COUPON, params);
    }

    protected JsonObjectRequest getCouponListRequest() {
        Log.v("AAA", "orderFragment-getCouponList->" + getCouponListRequestUrl());
        return new JsonObjectRequest(getCouponListRequestUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        getCoupons(response.getJSONObject("data").getJSONArray("list"));
                    } else {
                        setNoCouponView();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setNoCouponView();
            }
        });
    }
}
