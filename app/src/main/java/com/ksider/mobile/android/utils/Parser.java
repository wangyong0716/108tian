package com.ksider.mobile.android.utils;

import com.ksider.mobile.android.model.DetailHeaderDataModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

public class Parser {
    public static DetailHeaderDataModel parse(JSONObject data, Map<String, String> pairs) {
        DetailHeaderDataModel detail = new DetailHeaderDataModel();
        for (Iterator iterator = pairs.keySet().iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            if (key.equals("id")) {
                detail.id = getvalue(data, pairs.get(key));
            } else if (key.equals("title")) {
                detail.title = getvalue(data, pairs.get(key));
            } else if (key.equals("imgUrl")) {
                detail.imgUrl = getvalue(data, pairs.get(key));
            } else if (key.equals("description")) {
                detail.description = getvalue(data, pairs.get(key));
            } else if (key.equals("label")) {
                detail.label = getvalue(data, pairs.get(key));
            } else if (key.equals("collection")) {
                detail.collection = getvalue(data, pairs.get(key));
            } else if (key.equals("hasFavorator")) {
                try {
                    detail.hasFavorator = data.getBoolean(pairs.get(key));
                } catch (Exception e) {
//	                e.printStackTrace();
                }
            }
        }
        return detail;
    }

    public static String getvalue(JSONObject data, String key) {
        String value = null;
        if (data != null && key != null) {
            try {
                value = data.getString(key);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
//			e.printStackTrace();
            }
        }
        return value;
    }
}
