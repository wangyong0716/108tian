package com.ksider.mobile.android.activity.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.LoginActivity;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.ListViewDataModel;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.view.LoadImageView;
import com.ksider.mobile.android.view.paging.PagingBaseAdapter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ListViewPagingAdaptor extends PagingBaseAdapter<ListViewDataModel> {
    protected Activity mContext;
    protected BasicCategory mBasicCategory;

    public ListViewPagingAdaptor(Activity context, BasicCategory category) {
        mContext = context;
        mBasicCategory = category;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ListViewDataModel getItem(int postion) {
        if (0 <= postion && postion < items.size()) {
            return items.get(postion);
        }
        return null;
    }

    @Override
    public long getItemId(int postion) {
        return postion;
    }

//    protected void setPrice(ViewHolder holder, String price) {
//        if (price != null && !price.equals("")) {
//            if (price.equals("-1")) {
//                holder.price.setVisibility(View.INVISIBLE);
//
//            } else if (price.equals("0")) {
//                holder.price.setVisibility(View.VISIBLE);
//                holder.price.setText("免费");
//            } else {
//                holder.price.setVisibility(View.VISIBLE);
//                holder.price.setText(mContext.getResources().getString(R.string.toolbar_price, price));
//            }
//        } else {
//            holder.price.setVisibility(View.INVISIBLE);
//        }
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        final View view;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.list_view_item, viewGroup, false);
            holder.title = (TextView) view.findViewById(R.id.list_title);
            holder.price = (TextView) view.findViewById(R.id.listview_price);
            holder.location = (TextView) view.findViewById(R.id.listview_location);
            holder.distance = (TextView) view.findViewById(R.id.listview_distance);
            holder.collection = (TextView) view.findViewById(R.id.listview_collection);
            holder.locationDistance = (LinearLayout) view.findViewById(R.id.location_distance);
            holder.image = (LoadImageView) view.findViewById(R.id.listview_headImage);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        final ListViewDataModel data = getItem(position);
        if (data == null)
            return view;
        holder.title.setText(data.title);
//        setPrice(holder, data.price);
        holder.price.setText(data.price);
        if ((data.location == null || data.location.equals("")) && (data.distance == null || data.distance.equals(""))) {
            holder.locationDistance.setVisibility(View.GONE);
        } else if ((data.distance == null || data.distance.equals("")) && !(data.location == null || data.location.equals(""))) {
            holder.locationDistance.setVisibility(View.VISIBLE);
            holder.distance.setVisibility(View.GONE);
            holder.location.setVisibility(View.VISIBLE);
            holder.location.setText(data.location);
        } else if (!(data.distance == null || data.distance.equals("")) && (data.location == null || data.location.equals(""))) {
            holder.locationDistance.setVisibility(View.VISIBLE);
            holder.location.setVisibility(View.GONE);
            holder.distance.setVisibility(View.VISIBLE);
            holder.distance.setText(data.distance);
        } else {
            holder.locationDistance.setVisibility(View.VISIBLE);
            holder.location.setVisibility(View.VISIBLE);
            holder.distance.setVisibility(View.VISIBLE);
            holder.location.setText(data.location);
            holder.distance.setText(data.distance);
        }
        if (data.collection != null && !data.collection.equals("")) {
            holder.collection.setVisibility(View.VISIBLE);
            holder.collection.setText(data.collection + "人");
            if (data.isFav) {
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.list_collected_icon);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                holder.collection.setCompoundDrawables(drawable, null, null, null);
            } else {
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.list_collection_icon);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                holder.collection.setCompoundDrawables(drawable, null, null, null);
            }
            holder.collection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!UserInfo.isLogin()) {
                        Intent intent = new Intent(mContext, LoginActivity.class);
                        mContext.startActivity(intent);
                        return;
                    }
                    postFavorite(data, view);
                }
            });
        } else {
            holder.collection.setVisibility(View.GONE);
        }

        if (data.imageBitmap != null) {
            holder.image.setImageBitmap(data.imageBitmap);
        } else if (data.imageDrawable != null) {
            holder.image.setImageDrawable(data.imageDrawable);
        } else if (data.imgUrl != null && data.imgUrl.length() > 4) {
            holder.image.setImageResource(data.imgUrl);
        }
        statistics();
        return view;
    }

    protected void postFavorite(final ListViewDataModel data, final View collect) {
        Map<String, Object> params = new HashMap<String, Object>();
        if (data.isFav) {
            MessageUtils.eventBus.post(new MessageEvent(MessageEvent.NOTIFY_COLLECTED_CHANGE));
            params.put("action", "delFav");
        } else {
            params.put("action", "setFav");
            MessageUtils.eventBus.post(new MessageEvent(MessageEvent.NOTIFY_COLLECTED_CHANGE));
        }
        switch (mBasicCategory) {
            case ATTRACTIONS:
                params.put("POIType", "scene");
                break;
            case FARMYARD:
                params.put("POIType", "farm");
                break;
            case PICKINGPART:
                params.put("POIType", "pick");
                break;
            case RESORT:
                params.put("POIType", "resort");
                break;
            case ACTIVITY:
                params.put("POIType", "events");
                break;
            case GUIDE:
                params.put("POIType", "weekly");
                break;
            default:
                break;
        }
        params.put("POIId", data.id);
        JsonObjectRequest request = new JsonObjectRequest(APIUtils.getUserCenter(params), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getInt("status") == 0) {
                        switchCollectView(data, collect);
                        MessageUtils.eventBus.post(new MessageEvent(MessageEvent.NOTIFY_COLLECTED_CHANGE));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(Constants.LOG_TAG, error.toString());
            }
        });
        Network.getInstance().addToRequestQueue(request);
    }

    protected void switchCollectView(ListViewDataModel data, View item) {
        TextView collect = (TextView) item.findViewById(R.id.listview_collection);
        Drawable drawable = null;
        if (data.isFav) {
            data.collection = calculate(data.collection, -1);
            data.isFav = false;
            drawable = mContext.getResources().getDrawable(R.drawable.list_collection_icon);
        } else {
            data.collection = calculate(data.collection, 1);
            data.isFav = true;
            drawable = mContext.getResources().getDrawable(R.drawable.list_collected_icon);
        }
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        collect.setCompoundDrawables(drawable, null, null, null);
        collect.setText(data.collection + "人");
    }

    /**
     * calculate on a number which is saved as a string
     *
     * @param number
     * @param value
     * @return
     */
    public String calculate(String number, int value) {
        int num = Integer.parseInt(number);
        num += value;
        num = num > 0 ? num : 0;
        return num + "";
    }

    protected void statistics() {
        switch (mBasicCategory) {
            case ATTRACTIONS:
                StatHandle.increaseImpression(StatHandle.ATTRACTION_LIST);
                break;
            case ACTIVITY:
                StatHandle.increaseImpression(StatHandle.EVENT_LIST);
                break;
            case RESORT:
                StatHandle.increaseImpression(StatHandle.RESORT_LIST);
                break;
            case FARMYARD:
                StatHandle.increaseImpression(StatHandle.FARM_LIST);
                break;
            case GUIDE:
                StatHandle.increaseImpression(StatHandle.GUIDE_LIST);
                break;
            case PICKINGPART:
                StatHandle.increaseImpression(StatHandle.PICK_LIST);
                break;
            default:
                break;
        }
    }

    /**
     * ViewHolder
     */
    static class ViewHolder {
        public TextView title;
        public TextView price;
        public TextView location;
        public TextView distance;
        public TextView collection;
        public LinearLayout locationDistance;
        public LoadImageView image;
    }

}
