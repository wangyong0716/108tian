package com.ksider.mobile.android.activity.fragment.buy;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.MoreDetailInfoActivity;
import com.ksider.mobile.android.WebView.PurchaseAcitvity;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.activity.fragment.FragmentCallback;
import com.ksider.mobile.android.model.BaseComparator;
import com.ksider.mobile.android.model.ProductStockModel;
import com.ksider.mobile.android.utils.DateUtils;
import com.ksider.mobile.android.utils.Status;
import com.ksider.mobile.android.utils.StringUtils;
import com.ksider.mobile.android.view.materialcalendarview.CalendarDay;
import com.ksider.mobile.android.view.materialcalendarview.MaterialCalendarView;
import com.ksider.mobile.android.view.materialcalendarview.OnDateChangedListener;
import com.ksider.mobile.android.view.materialcalendarview.decorators.EventDecorator;
import com.umeng.analytics.MobclickAgent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by yong on 8/1/15.
 */
public class OrderSelectFragment extends Fragment {
    private View mRoot;
    private int productType = 0;
    private MaterialCalendarView calendarView;
    private LinearLayout productContainer;
    /**
     * store all stocks order by startTime in ASC
     */
    private ArrayList<ProductStockModel> stocks = new ArrayList<ProductStockModel>();
    /**
     * product list of the selected day
     */
    private ArrayList<Integer> selectedLocations = new ArrayList<Integer>();
    /**
     * the default product list to show in Calendar
     */
    private ArrayList<Integer> showLocations = new ArrayList<Integer>();
    private JSONArray products;
    private long selectedDay = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView more = (TextView) getActivity().findViewById(R.id.more_choice);
        if (more != null) {
            more.setVisibility(View.INVISIBLE);
        }

        if (mRoot != null) {
            ViewGroup parent = (ViewGroup) mRoot.getParent();
            if (parent != null) {
                parent.removeView(mRoot);
                return mRoot;
            }
        }
        mRoot = inflater.inflate(R.layout.fragment_calendar_view, container, false);
        productContainer = (LinearLayout) mRoot.findViewById(R.id.product_container);

        stocks = getArguments().getParcelableArrayList("stocks");
        try {
            products = new JSONArray(getArguments().getString("product"));
        } catch (JSONException js) {
            js.printStackTrace();
        }
        getStockList(products);

        if (productType == Status.PRODUCT_TYPE_TIME) {
            initCalendarView();
            fillCalendar();
        } else {
            mRoot.findViewById(R.id.calendarView).setVisibility(View.GONE);
        }
        refreshProductList();
        MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_PURCHASE_SELECT);
        return mRoot;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        new SlidingLayoutFragment(getActivity(),this);
    }

    public void initCalendarView() {
        calendarView = (MaterialCalendarView) mRoot.findViewById(R.id.calendarView);
        //3 display of calendar view
        calendarView.setTileSizeDp(49);

        /*//set the calendar view adapt to the screen fully
        DisplayMetrics metric = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels; // 屏幕宽度（像素）
        int height = metric.heightPixels; // 屏幕高度（像素）
        int labelWidth=width/7;
        Log.v("AAA","width="+width+"|height="+height+"|labekWidth="+labelWidth);
        calendarView.setTileSize(labelWidth);*/

        //set the calendar view adapt to the display layout
//        calendarView.setMatchParent(6);

        //default selected day
//        if (selectedLocations.size() > 0) {
//            calendarView.setSelectedDate(new Date(stocks.get(selectedLocations.get(0)).getStartTime()));
//        }

        calendarView.setShowOtherDates(true);
        calendarView.setOnDateChangedListener(new OnDateChangedListener() {
            @Override
            public void onDateChanged(MaterialCalendarView widget, final CalendarDay date) {
                getSelectedLocations(date.getDate().getTime());
                refreshProductList();
            }
        });

        calendarView.setHeaderTextAppearance(R.style.TextAppearance_MaterialCalendarWidget_Header);
        calendarView.setDateTextAppearance(R.style.TextAppearance_MaterialCalendarWidget_Date);
        calendarView.setWeekDayTextAppearance(R.style.TextAppearance_MaterialCalendarWidget_WeekDay);
    }

    public ProductStockModel getSelectStock(long mSelectDate) {
        if (productType == Status.PRODUCT_TYPE_ALWAYS && stocks.size() > 0) {
            return stocks.get(0);
        }
        String selectDate = DateUtils.getFormatDate(mSelectDate);
        for (int i = 0; i < stocks.size(); i++) {
            String date = DateUtils.getFormatDate(stocks.get(i).getStartTime());
            if (selectDate.equals(date)) {
                return stocks.get(i);
            }
        }
        return null;
    }

    public void fillCalendar() {
        if (stocks == null) {
            return;
        }
        ArrayList<CalendarDay> dates = new ArrayList<CalendarDay>();
        HashMap<CalendarDay, Double> moneyDates = new HashMap<CalendarDay, Double>();
//        Date current = new Date();
//        Long firstDate = null;
        long firstMills = DateUtils.getFirstMilSeconds(System.currentTimeMillis());
        long lastMills = firstMills;
        long selectedTime = 0;
        double minMoney = Double.MAX_VALUE;
        for (int i = 0; i < stocks.size(); i++) {
            if (stocks.get(i).getStartTime() >= firstMills) {
                if (stocks.get(i).getStartTime() >= lastMills) {
                    //save
                    if (selectedTime > 0) {
                        Date date = new Date(selectedTime);
                        CalendarDay day = new CalendarDay(date);
                        dates.add(day);
                        moneyDates.put(day, minMoney);
                    }
                    //reset
                    selectedTime = stocks.get(i).getStartTime();
                    minMoney = stocks.get(i).getSellPrice();
                    lastMills = DateUtils.getFirstMilSeconds(selectedTime) + DateUtils.DAY_MILLIS;
                    if (i == stocks.size() - 1) {
                        Date date = new Date(stocks.get(i).getStartTime());
                        CalendarDay day = new CalendarDay(date);
                        dates.add(day);
                        moneyDates.put(day, stocks.get(i).getSellPrice());
                    }
                } else {
                    minMoney = Math.min(minMoney, stocks.get(i).getSellPrice());
                    if (i == stocks.size() - 1 && selectedTime > 0) {
                        Date date = new Date(selectedTime);
                        CalendarDay day = new CalendarDay(date);
                        dates.add(day);
                        moneyDates.put(day, minMoney);
                    }
                }
            }
        }

        EventDecorator decorator = new EventDecorator(Color.RED, dates);
        calendarView.addDayviewSelectable(decorator);
        calendarView.addMoney(moneyDates);
        if (productType != Status.PRODUCT_TYPE_ALWAYS && dates.size() > 0) {
            calendarView.setMinimumDate(dates.get(0).getDate());
            calendarView.setMaximumDate(dates.get(dates.size() - 1).getDate());
        } else {
            calendarView.setMinimumDate(new Date());
            calendarView.setMaximumDate(new Date());
        }
        calendarView.updateButtonState();
    }

    public void getStockList(JSONArray productArray) {
        if (productArray == null || productArray.length() <= 0) {
            return;
        }
        if (stocks == null) {
            stocks = new ArrayList<ProductStockModel>();
        }
        if (stocks.size() <= 0) {
            long currentMills = System.currentTimeMillis();
            for (int i = 0; i < productArray.length(); i++) {
                try {
                    JSONObject productObject = productArray.getJSONObject(i);
                    String productName = productObject.getString("productName");
                    productType = productObject.getInt("productType");
                    JSONArray stockList = productObject.getJSONArray("stockList");
                    for (int j = 0; j < stockList.length(); j++) {
                        try {
                            JSONObject stockObject = stockList.getJSONObject(j);
                            long startTime = stockObject.getLong("startTime");
                            if (productType == Status.PRODUCT_TYPE_TIME && startTime < currentMills) {
                                continue;
                            }
                            ProductStockModel stock = new ProductStockModel();
                            stock.setStartTime(startTime);
                            stock.setSellPrice(stockObject.getDouble("sellPrice"));
                            stock.setQuantity(stockObject.getLong("quantity"));
                            stock.setMarketPrice(stockObject.getDouble("marketPrice"));
                            stock.setProductId(stockObject.getLong("productId"));
                            stock.setStockId(stockObject.getLong("stockId"));
                            stock.setProductName(productName);
                            stocks.add(stock);
                        } catch (JSONException js) {
                            js.printStackTrace();
                        }
                    }
                } catch (JSONException js) {
                    js.printStackTrace();
                }
            }
            Collections.sort(stocks, ProductStockModel.getComparator(BaseComparator.ASC_SORT));
        } else {
            try {
                JSONObject productObject = productArray.getJSONObject(0);
                productType = productObject.getInt("productType");
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }

        if (productType == Status.PRODUCT_TYPE_ALWAYS) {
            selectedLocations.clear();
            for (int i = 0; i < stocks.size(); i++) {
                selectedLocations.add(i);
            }
        } else {
            /*//get products in first day at first
            long currentMills = System.currentTimeMillis();
            for (int i = 0; i < stocks.size(); i++) {
                if (stocks.get(i).getStartTime() > currentMills) {
                    getSelectedLocations(stocks.get(i).getStartTime());
                    break;
                }
            }*/
            //get all products to show at first
            getDefaultSelectedLocations();
        }
    }

    /**
     * get all product to show at first
     */
    public void getDefaultSelectedLocations() {
        selectedLocations.clear();
        String productIds = "";
        for (int i = 0; i < stocks.size(); i++) {
            if (!productIds.contains(stocks.get(i).getProductId() + "")) {
                selectedLocations.add(i);
                productIds += (stocks.get(i).getProductId() + "|");
            }
        }
    }

    public void getSelectedLocations(long time) {
        selectedLocations.clear();
        long firstMills = DateUtils.getFirstMilSeconds(time);
        long lastMills = firstMills + DateUtils.DAY_MILLIS;
        for (int i = 0; i < stocks.size(); i++) {
            if (stocks.get(i).getStartTime() < firstMills) {
                continue;
            } else if (stocks.get(i).getStartTime() < lastMills) {
                selectedLocations.add(i);
            } else {
                break;
            }
        }
    }

    public void refreshProductList() {
        productContainer.removeAllViews();
        if (stocks.size() < 1 || selectedLocations.size() < 1) {
            return;
        }
        ((TextView) mRoot.findViewById(R.id.product_count)).setText(getResources().getString(R.string.choose_products, selectedLocations.size()));
        for (int i = 0; i < selectedLocations.size(); i++) {
            int index = selectedLocations.get(i);
            if (index <= stocks.size() - 1) {
                final ProductStockModel productStockModel = stocks.get(index);
                View view = getActivity().getLayoutInflater().inflate(R.layout.list_view_product_item, null);
                ((TextView) view.findViewById(R.id.product_title)).setText(productStockModel.getProductName());
                ((TextView) view.findViewById(R.id.price_now)).setText(StringUtils.getPrice(productStockModel.getSellPrice()));
                TextView priceOrigin = (TextView) view.findViewById(R.id.price_origin);
                priceOrigin.setText(getResources().getString(R.string.toolbar_price, StringUtils.getPrice(productStockModel.getMarketPrice())));
                priceOrigin.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                view.findViewById(R.id.purchase).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (productType == Status.PRODUCT_TYPE_TIME && null == calendarView.getSelectedDate()) {
                            Toast.makeText(getActivity(), "请选择日期", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (getActivity() instanceof FragmentCallback) {
                            try {
                                JSONObject mProduct = getSelectedProduct(productStockModel.getProductId());
                                JSONObject product = new JSONObject();
                                product.put("productType", mProduct.getInt("productType"));
                                product.put("poiType", mProduct.getInt("poiType"));
                                product.put("refund", mProduct.getInt("refund"));
                                product.put("quantityPerUser", mProduct.getInt("quantityPerUser"));
                                product.put("productName", productStockModel.getProductName());
                                product.put("productId", productStockModel.getProductId());
                                product.put("startTime", productStockModel.getStartTime());
                                product.put("sellPrice", productStockModel.getSellPrice());
                                product.put("quantity", productStockModel.getQuantity());
                                FragmentCallback callback = (FragmentCallback) getActivity();
                                try {
                                    product.put(PurchaseAcitvity.STAGE, PurchaseAcitvity.SELECT);
                                    callback.next(product);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } catch (JSONException js) {
                                js.printStackTrace();
                            }
                        }
                    }
                });

                TextView viewDetail = (TextView) view.findViewById(R.id.view_detail);
                viewDetail.setTag(productStockModel);
                viewDetail.setOnClickListener(viewDetailListener);
                productContainer.addView(view);
            }
        }
    }

    public JSONObject getSelectedProduct(long productId) {
        for (int i = 0; i < products.length(); i++) {
            try {
                if (productId == products.getJSONObject(i).getInt("productId")) {
                    return products.getJSONObject(i);
                }
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
        return null;
    }

    private View.OnClickListener viewDetailListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ProductStockModel productStock = (ProductStockModel) v.getTag();
            if (productStock != null) {
                JSONObject mProduct = getSelectedProduct(productStock.getProductId());
                String brief;
                try {
                    brief = mProduct.getString("feeDesc");
                } catch (JSONException js) {
                    brief = "";
                    js.printStackTrace();
                }
                if (brief == null || brief.equals("")) {
                    return;
                }
                Intent intent = new Intent(getActivity(), MoreDetailInfoActivity.class);
                intent.putExtra("type", MoreDetailInfoActivity.DETAIL_FEE_DESC);
                intent.putExtra("brief", brief);
                startActivity(intent);
            }
        }
    };
}
