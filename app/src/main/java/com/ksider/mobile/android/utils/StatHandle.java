package com.ksider.mobile.android.utils;

import android.content.Context;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wenkui on 2/7/15.
 */
public class StatHandle {
    public static String CHOINCELIST="choince_list";        //精选列表统计
    public static String CHOINCEDETAIL="choince_detail";    //精选详情统计
    public static String EVENT_LIST="event_list";           //活动列表统计
    public static String GUIDE_LIST="weekly_list";          //功率列表统计
    public static String ATTRACTION_LIST="attr_list";   //景点列表统计
    public static String FARM_LIST="farm_list";         //农家院列表统计
    public static String RESORT_LIST="resort_list";           //景点列表统计
    public static String PICK_LIST="pick_list";         //景点列表统计
    public static String PRODUCT_LIST="product_list";   //商家详情列表统计

    protected static Map<String,Object> mItemsStat = new HashMap<String, Object>();
    public static void increaseImpression(String id){
        Map item = (Map) mItemsStat.get(id);
        if(item == null) {
            item = new HashMap<String, Integer>();
            item.put("imp", 0);
            item.put("clk", 0);
            mItemsStat.put(id, item);
        }
        Integer count = (Integer) item.get("imp");
        item.put("imp",++count);
    }
    public static void increaseClick(String id){
        Map item = (Map) mItemsStat.get(id);
        if(item == null) {
            item = new HashMap<String, Integer>();
            item.put("imp", 0);
            item.put("clk", 0);
            mItemsStat.put(id, item);
        }
        Integer count = (Integer) item.get("clk");
        item.put("clk",++count);
    }
    public static  void postImpclkEvent(Context ct, String id){
        Map item = (Map) mItemsStat.get(id);
        if(item != null) {
            mItemsStat.remove(id);
            Map params = new HashMap();
            params.put("channel", DeviceUuid.getChannel());
            if((Integer)item.get("imp") != 0) {
                MobclickAgent.onEventValue(ct, id+"_imp", params, (Integer)item.get("imp"));
            }
            if((Integer)item.get("clk") != 0) {
                MobclickAgent.onEventValue(ct, id+"_clk", params, (Integer)item.get("clk"));
            }
        }
    }
}
