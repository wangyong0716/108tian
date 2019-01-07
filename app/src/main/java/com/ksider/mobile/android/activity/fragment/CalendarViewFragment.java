package com.ksider.mobile.android.activity.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.ProductStockModel;
import com.ksider.mobile.android.utils.DateUtils;
import com.ksider.mobile.android.utils.Status;
import com.ksider.mobile.android.view.materialcalendarview.CalendarDay;
import com.ksider.mobile.android.view.materialcalendarview.MaterialCalendarView;
import com.ksider.mobile.android.view.materialcalendarview.OnDateChangedListener;
import com.ksider.mobile.android.view.materialcalendarview.decorators.EventDecorator;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by yong on 8/1/15.
 */
public class CalendarViewFragment extends Fragment {
    private View mRoot;
    private String productId;
    private long selectedDay;
    private int productType;
    private MaterialCalendarView calendarView;
    protected ArrayList<ProductStockModel> stocks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_calendar_view, container, false);
        try {
            JSONObject object = new JSONObject(getArguments().getString("brief"));
            productId = object.getString("productId");
            selectedDay = object.getLong("selectedDay");
            productType = object.getInt("productType");
        } catch (JSONException js) {
            js.printStackTrace();
        }
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

        if (selectedDay > 0) {
            calendarView.setSelectedDate(new Date(selectedDay));
        }

        calendarView.setShowOtherDates(true);
        calendarView.setOnDateChangedListener(new OnDateChangedListener() {
            @Override
            public void onDateChanged(MaterialCalendarView widget, final CalendarDay date) {
                ProductStockModel stock = getSelectStock(date.getDate().getTime());
                if (stock != null) {
                    Intent intent = new Intent();
                    intent.putExtra("startTime", stock.getStartTime());
                    intent.putExtra("sellPrice", stock.getSellPrice());
                    intent.putExtra("quantity", stock.getQuantity());
                    intent.putExtra("purchasePrice", stock.getPurchasePrice());
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                }
            }
        });

        calendarView.setHeaderTextAppearance(R.style.TextAppearance_MaterialCalendarWidget_Header);
        calendarView.setDateTextAppearance(R.style.TextAppearance_MaterialCalendarWidget_Date);
        calendarView.setWeekDayTextAppearance(R.style.TextAppearance_MaterialCalendarWidget_WeekDay);
        stocks = getActivity().getIntent().getParcelableArrayListExtra("stocks");
        fillCalendar();
        return mRoot;
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
        Date current = new Date();
        Long firstDate = null;
        for (int i = 0; i < stocks.size(); i++) {
            if (stocks.get(i).getStartTime() > current.getTime()) {
                if (firstDate == null) {
                    firstDate = stocks.get(i).getStartTime();
                }
                Date date = new Date(stocks.get(i).getStartTime());
                CalendarDay day = new CalendarDay(date);
                dates.add(day);
                moneyDates.put(day, stocks.get(i).getSellPrice());
            }
        }

        EventDecorator decorator = new EventDecorator(Color.RED, dates);
        calendarView.addDayviewSelectable(decorator);
        calendarView.addMoney(moneyDates);
        if (productType != Status.PRODUCT_TYPE_ALWAYS && firstDate != null) {
            Date startDate = new Date();
            startDate.setTime(firstDate);
            calendarView.setMinimumDate(startDate);

            Date maxDate = new Date();
            maxDate.setTime(stocks.get(stocks.size() - 1).getStartTime());
            calendarView.setMaximumDate(maxDate);
        } else {
            calendarView.setMinimumDate(new Date());
            calendarView.setMaximumDate(new Date());
        }
        calendarView.updateButtonState();
    }
}
