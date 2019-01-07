package com.ksider.mobile.android.activity.fragment;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.ListViewDataModel;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.scrollListView.BasePagingListView;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.MessageUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.view.LoadImageView;
import com.ksider.mobile.android.view.paging.PagingBaseAdapter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PersonalStorageAdaptor extends PagingBaseAdapter<ListViewDataModel> {
    protected Activity mContext;
    private BasePagingListView mListView;

    public PersonalStorageAdaptor(Activity context) {
        mContext = context;
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

    protected void setPrice(View rowView, String price, String type) {
        TextView textView = (TextView) rowView.findViewById(R.id.listview_price);
        if (price != null) {
            if (price.equals("-1")) {
                textView.setText("暂无");
            } else if (price.equals("0")) {
                if (type.equals("event") || type.equals("resort")) {
                    textView.setText("暂无");
                } else {
                    textView.setText("免费");
                }
            } else {
                textView.setText("￥" + price);
            }
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    protected void setTitle(View rowView, String title) {
        TextView textView = (TextView) rowView.findViewById(R.id.list_title);
        if (title != null) {
            textView.setText(title);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    protected void setLocation(View rowView, String location) {
        if (location != null) {
            TextView textView = (TextView) rowView.findViewById(R.id.listview_location);
            textView.setText(location);
        } else {
            View view = rowView.findViewById(R.id.listview_location);
            view.setVisibility(View.GONE);
        }
    }

    public void setMListView(BasePagingListView mListView) {
        this.mListView = mListView;
    }

    public void refreshData(int key, String type, String id) {
        if (mListView == null) {
            return;
        }
        items.remove(key);
        refreshList(type, id);
        int count = mListView.getChildCount();
        for (int i = 0; i < count; i++) {
            if (mListView.getChildAt(i) != null && mListView.getChildAt(i).findViewById(R.id.listview_img_layout) != null) {
                mListView.getChildAt(i).findViewById(R.id.listview_img_layout).setTranslationX(0);
                mListView.getChildAt(i).findViewById(R.id.trash).setClickable(false);
            }
        }
    }

    @Override
    public View getView(final int postion, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_view_storage_item, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.listtitle = (TextView) view.findViewById(R.id.list_title);
//			viewHolder.cardtitle = (TextView)view.findViewById(R.id.card_title);
            viewHolder.loadImageView = (LoadImageView) view.findViewById(R.id.listview_headImage);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
            view.findViewById(R.id.listview_img_layout).setTranslationX(0);
        }
        final ListViewDataModel data = getItem(postion);
//		if (data.type.equals("weekly")) {
//			viewHolder.cardtitle.setText("攻略");
//			viewHolder.cardtitle.setBackgroundResource(R.drawable.card_title_0);
//		}
//		if (data.type.equals("resort")) {
//			viewHolder.cardtitle.setText("度假村");
//			viewHolder.cardtitle.setBackgroundResource(R.drawable.card_title_1);
//		} else if (data.type.equals("farm")) {
//			viewHolder.cardtitle.setText("农家院");
//			viewHolder.cardtitle.setBackgroundResource(R.drawable.card_title_1);
//		} else if (data.type.equals("scene")) {
//			viewHolder.cardtitle.setText("景点");
//			viewHolder.cardtitle.setBackgroundResource(R.drawable.card_title_2);
//		} else if (data.type.equals("pick")) {
//			viewHolder.cardtitle.setText("采摘园");
//			viewHolder.cardtitle.setBackgroundResource(R.drawable.card_title_3);
//		} else if (data.type.equals("events")||("event").equals(data.type)) {
//			viewHolder.cardtitle.setText("活动");
//			viewHolder.cardtitle.setBackgroundResource(R.drawable.card_title_4);
//		}else if(data.type.equals("recommend")){
//			viewHolder.cardtitle.setText("精选");
//			viewHolder.cardtitle.setBackgroundResource(R.drawable.card_title_5);
//		}
        if (data.title != null) {
            viewHolder.listtitle.setText(data.title);
        } else {
            viewHolder.listtitle.setVisibility(View.GONE);
        }
        if (data.imageBitmap != null) {
            viewHolder.loadImageView.setImageBitmap(data.imageBitmap);
        } else if (data.imageDrawable != null) {
            viewHolder.loadImageView.setImageDrawable(data.imageDrawable);
        } else if (data.imgUrl != null && data.imgUrl.length() > 4) {
            viewHolder.loadImageView.setImageResource(data.imgUrl);
        }

        ImageView iv = (ImageView) view.findViewById(R.id.trash);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int x = (int) ((View) v.getParent().getParent()).findViewById(R.id.listview_img_layout).getX();
                if (x < 0) {
                    refreshData(postion, data.type, data.id);
                }
            }
        });
        iv.setClickable(false);
        return view;
    }

    public void refreshList(String type, String id) {
        notifyDataSetChanged();
        deleteStorage(type, id);
    }

    public void deleteStorage(String type, String id) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "delFav");
        params.put("POIType", type);
        params.put("POIId", id);

        JsonObjectRequest request = new JsonObjectRequest(APIUtils.getUserCenter(params), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getInt("status") == 0) {
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

    private static class ViewHolder {
        LoadImageView loadImageView;
        TextView listtitle;
//		TextView cardtitle;
    }
}
