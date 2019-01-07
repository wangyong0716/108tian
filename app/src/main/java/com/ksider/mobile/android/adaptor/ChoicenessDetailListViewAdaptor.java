package com.ksider.mobile.android.adaptor;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.BaseDataModel;
import com.ksider.mobile.android.model.DetailHeaderDataModel;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.view.LoadImageView;
import com.ksider.mobile.android.view.paging.PagingBaseAdapter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChoicenessDetailListViewAdaptor extends PagingBaseAdapter<DetailHeaderDataModel> {
    protected Activity mContext;

    public ChoicenessDetailListViewAdaptor(Activity activity) {
        mContext = activity;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public DetailHeaderDataModel getItem(int position) {
        if (0 <= position && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        ViewHolder view;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.choiceness_detail_item, viewGroup, false);
            view = new ViewHolder();
            view.image = (LoadImageView) convertView.findViewById(R.id.listview_headImage);
            view.title = (TextView) convertView.findViewById(R.id.list_title);
            view.descript = (TextView) convertView.findViewById(R.id.descript);
            view.expireImage = (ImageView) convertView.findViewById(R.id.expire_icon);
            view.price = (TextView) convertView.findViewById(R.id.price);
//            view.collect = (View) convertView.findViewById(R.id.choiceness_collect);
//            view.collected = (View) convertView.findViewById(R.id.choiceness_collected);
//            view.collectionCount = (TextView) convertView.findViewById(R.id.choiceness_collection_count);
//            view.collectarea = (ViewGroup) convertView.findViewById(R.id.choiceness_collect_area);
            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }
        final DetailHeaderDataModel data = getItem(position);
        if (data == null) {
            return convertView;
        }

        if (data.imgUrl != null) {
            view.image.setImageResource(data.imgUrl);
        }
        if (data.title != null) {
            view.title.setText(data.title);
        }
        if (data.description != null) {
            view.descript.setText(data.description);
        }
        if (data.expire) {
            view.expireImage.setVisibility(View.VISIBLE);
        } else {
            view.expireImage.setVisibility(View.INVISIBLE);
        }
        view.price.setText(data.price);
//        if (data.hasFavorator != null) {
//            if (data.hasFavorator) {
//                view.collect.setVisibility(View.INVISIBLE);
//                view.collected.setVisibility(View.VISIBLE);
//            } else {
//                view.collect.setVisibility(View.VISIBLE);
//                view.collected.setVisibility(View.INVISIBLE);
//            }
//        }
//        if (data.collection != null) {
//            view.collectionCount.setText(data.collection);
//        }
//        final View rawView = convertView;
//        OnClickListener listener = new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!UserInfo.isLogin()) {
//                    Intent intent = new Intent(mContext, LoginActivity.class);
//                    mContext.startActivity(intent);
//                    return;
//                }
//                View collect = v.findViewById(R.id.choiceness_collect);
//                if (collect != null && collect.getVisibility() == View.VISIBLE) {
//                    data.hasFavorator = true;
//                    data.collection = calculate(data.collection, 1);
//                    postFavorite(FavoriteActions.FAVORATOR, data, rawView, false);
//                } else {
//                    data.hasFavorator = false;
//                    data.collection = calculate(data.collection, -1);
//                    postFavorite(FavoriteActions.CANCEL_FAVORATOR, data, rawView, true);
//                }
//            }
//        };
//        view.collectarea.setOnClickListener(listener);
        StatHandle.increaseImpression(StatHandle.CHOINCEDETAIL);
        return convertView;
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
        return num + "";
    }

    protected void postFavorite(FavoriteActions action, BaseDataModel data, final View itemView, final Boolean collected) {
        Map<String, Object> params = new HashMap<String, Object>();
        switch (action) {
            case FAVORATOR:
                params.put("action", "setFav");
                MessageUtils.eventBus.post(new MessageEvent(MessageEvent.NOTIFY_COLLECTED_CHANGE));
                break;
            case CANCEL_FAVORATOR:
                MessageUtils.eventBus.post(new MessageEvent(MessageEvent.NOTIFY_COLLECTED_CHANGE));
                params.put("action", "delFav");
                break;
            case BEEN:
                params.put("action", "setBeen");
                break;
            case CANCEL_BEEN:
                params.put("action", "delBeen");
                break;
            default:
                return;
        }
        params.put("POIType", data.type);
        params.put("POIId", data.id);
        JsonObjectRequest request = new JsonObjectRequest(APIUtils.getUserCenter(params), null, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getInt("status") == 0) {
//                        switchCollectView(collected, itemView);
                        MessageUtils.eventBus.post(new MessageEvent(MessageEvent.NOTIFY_COLLECTED_CHANGE));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(Constants.LOG_TAG, error.toString());
            }
        });
        Network.getInstance().addToRequestQueue(request);
    }

//    protected void switchCollectView(Boolean collected, View item) {
//        TextView collection_count = (TextView) item.findViewById(R.id.choiceness_collection_count);
//        String fav = collection_count.getText().toString();
//        Integer count = Integer.valueOf(fav);
//        if (collected) {
//            count -= 1;
//            View view = (View) item.findViewById(R.id.choiceness_collect);
//            view.setVisibility(View.VISIBLE);
//            view = (View) item.findViewById(R.id.choiceness_collected);
//            view.setVisibility(View.INVISIBLE);
//        } else {
//            count += 1;
//            View view = (View) item.findViewById(R.id.choiceness_collect);
//            view.setVisibility(View.INVISIBLE);
//            view = (View) item.findViewById(R.id.choiceness_collected);
//            view.setVisibility(View.VISIBLE);
//        }
//        count = count > 0 ? count : 0;
//        collection_count.setText(count.toString());
//    }

    /**
     * ViewHolder
     */
    static class ViewHolder {
        public TextView title;
        public TextView descript;
        public LoadImageView image;
        public ImageView expireImage;
        public TextView price;
    }

}
