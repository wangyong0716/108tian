package com.ksider.mobile.android.activity.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class GoodsListViewPagingAdaptor extends
        PagingBaseAdapter<ListViewDataModel> {
    protected Activity mContext;
    protected BasicCategory mBasicCategory;

    public GoodsListViewPagingAdaptor(Activity context, BasicCategory category) {
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

//    protected void setPrice(TextView textView, TextView price_symbol, String price) {
//        if (price_symbol != null) {
//            price_symbol.setVisibility(View.GONE);
//        }
//        if (price != null) {
//            if (price.equals("")||price.equals("-1")) {
//                textView.setVisibility(View.GONE);
//            } else if (price.equals("0")) {
//                textView.setVisibility(View.VISIBLE);
//                textView.setText("免费");
//            } else {
//                textView.setVisibility(View.VISIBLE);
//                textView.setText(mContext.getResources().getString(R.string.toolbar_price, price));
//                if (price_symbol != null) {
//                    price_symbol.setVisibility(View.VISIBLE);
//                }
//            }
//        } else {
//            textView.setVisibility(View.GONE);
//        }
//    }

    protected void setLocation(TextView textView, String location) {
        if (location != null) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(location);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        final View view;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            holder = new ViewHolder();
            switch (mBasicCategory) {
                case GUIDE:
                    view = inflater.inflate(R.layout.list_view_lite_item,
                            viewGroup, false);
                    holder.title = (TextView) view
                            .findViewById(R.id.list_title);
                    break;
                case ACTIVITY:
                    view = inflater.inflate(
                            R.layout.list_view_activity_item, viewGroup, false);
                    holder.title = (TextView) view
                            .findViewById(R.id.list_title);
                    holder.price = (TextView) view
                            .findViewById(R.id.listview_price);
                    holder.location = (TextView) view
                            .findViewById(R.id.listview_location);
                    holder.startDate = (TextView) view
                            .findViewById(R.id.listview_StartDate);
                    holder.collection = (TextView) view.findViewById(R.id.listview_collection);
                    break;
                default:
                    view = inflater.inflate(R.layout.list_view_item,
                            viewGroup, false);
                    holder.title = (TextView) view
                            .findViewById(R.id.list_title);
                    holder.price = (TextView) view
                            .findViewById(R.id.listview_price);
                    holder.location = (TextView) view
                            .findViewById(R.id.listview_location);
                    holder.collection = (TextView) view
                            .findViewById(R.id.listview_collection);
                    break;
            }
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
        switch (mBasicCategory) {
            case GUIDE:
                break;
            case ACTIVITY:
//                setPrice(holder.price, null, data.price);
                holder.price.setText(data.price);
                setLocation(holder.location, data.location);
                if (data.startDate != null && holder.startDate != null) {
                    if (data.startDate.equals("")) {
                        holder.startDate.setVisibility(View.GONE);
                    } else {
                        holder.startDate.setText(data.startDate);
                    }
                }
                if (data.collection.equals("")) {
                    holder.collection.setVisibility(View.GONE);
                } else {
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
                }
                break;
            default:
//                setPrice(holder.price, holder.price_symbol, data.price);
                holder.price.setText(data.price);
                setLocation(holder.location, data.location);
                if (data.collection != null) {
                    holder.collection.setText(data.collection);
                } else {
                    holder.collection.setText("0");
                }
                break;
        }

        if (data.imageBitmap != null) {
            holder.image.setImageBitmap(data.imageBitmap);
        } else if (data.imageDrawable != null) {
            holder.image.setImageDrawable(data.imageDrawable);
        } else if (data.imgUrl != null && data.imgUrl.length() > 4) {
            holder.image.setImageResource(data.imgUrl);
        }
        StatHandle.increaseImpression(StatHandle.EVENT_LIST);
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
        params.put("POIType", "events");
        params.put("POIId", data.id);
        Log.v("AAA", "favUrl=" + APIUtils.getUserCenter(params));
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

    /**
     * ViewHolder
     */
    static class ViewHolder {
        public TextView title;
        public TextView price;
        public TextView price_symbol;
        public TextView collection;
        public TextView location;
        public TextView startDate;
        public LoadImageView image;
    }

}
