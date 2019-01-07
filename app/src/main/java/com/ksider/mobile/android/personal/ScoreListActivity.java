package com.ksider.mobile.android.personal;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.BaseActivity;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.ScoreModel;
import com.ksider.mobile.android.scrollListView.OverScrollPagingListView;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.DateUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.view.paging.PagingBaseAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yong on 11/4/15.
 */
public class ScoreListActivity extends BaseActivity {
    private final int REQUEST_POINT = 1;
    private final int REQUEST_LIST = 2;
    private OverScrollPagingListView mListView;
    private ScoreAdapter scoreAdapter;
    private View mHeader;

    private int mPage = 0;
    private boolean hasMore = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_score);
        new SlidingLayout(this);
        customActionBar();
        mHeader = getLayoutInflater().inflate(R.layout.score_header, null);
        mListView = (OverScrollPagingListView) findViewById(R.id.content_list);
        mListView.addHeaderView(mHeader);
        scoreAdapter = new ScoreAdapter(this);
        mListView.setAdapter(scoreAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        mListView.setPagingableListener(new OverScrollPagingListView.Pagingable() {
            @Override
            public void onLoadMoreItems() {
                mPage++;
                if (hasMore) {
                    Network.getInstance().addToRequestQueue(getRequest(REQUEST_LIST));
                } else {
                    mListView.onFinishLoading(false, null);
                }
            }
        });

        TextView title = (TextView) findViewById(R.id.list_title);
        title.setText("我的积分");
//        TextView help = (TextView) findViewById(R.id.more_choice);
//        help.setText(getResources().getString(R.string.help));
//        help.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("AAA", "view help in webView");
//
//            }
//        });
        initLoadingView();
        Network.getInstance().addToRequestQueue(getRequest(REQUEST_POINT));
        Network.getInstance().addToRequestQueue(getRequest(REQUEST_LIST));
    }

    /**
     * show the loading view before getting response
     */
    public void initLoadingView() {
        mListView.deleteFooterView();
        findViewById(R.id.empty_list_item).setVisibility(View.GONE);
//        findViewById(R.id.ptr_id_image).setVisibility(View.GONE);
//        findViewById(R.id.video_item_image).setVisibility(View.VISIBLE);
//        TextView tv = (TextView) findViewById(R.id.video_item_label);
//        tv.setVisibility(View.VISIBLE);
//        tv.setText(R.string.loading);
    }

    /**
     * hide the loading view after getting response
     */
    public void setResponseView() {
        mListView.setVisibility(View.VISIBLE);
        findViewById(R.id.empty_list_item).setVisibility(View.GONE);
        LinearLayout baseLinearLayout = (LinearLayout) findViewById(R.id.baseLinearLayout);
        if (baseLinearLayout != null) {
            baseLinearLayout.setVisibility(View.GONE);
        }
    }

    /**
     * show the empty view if the data from the response is empty
     */
    public void setEmptyView() {
        mListView.onFinishLoading(false, null);
        setResponseView();
        mListView.setVisibility(View.GONE);
        findViewById(R.id.empty_list_item).setVisibility(View.VISIBLE);
    }

    /**
     * show error message if the connection fails
     */
    public void setErrorView() {
        setEmptyView();
        ((TextView) findViewById(R.id.no_consume_code_info)).setText(R.string.net_acc_failed);
    }


    public void processPoint(JSONObject object) {
        try {
            ((TextView) findViewById(R.id.score)).setText(object.getInt("memberPoint") + "");
            ((TextView) findViewById(R.id.used_score)).setText(object.getInt("memberPointUsed") + "");
        } catch (JSONException js) {
            js.printStackTrace();
        }
    }

    public void processList(JSONArray array) {
        ArrayList<ScoreModel> scores = new ArrayList<ScoreModel>();
        for (int i = 0; i < array.length(); i++) {
            try {
                ScoreModel model = new ScoreModel();
                JSONObject object = array.getJSONObject(i);
                try {
                    model.setChange(object.getInt("change"));
                    model.setCreateTime(object.getLong("createTime"));
                    model.setReason(object.getString("reason"));
                    model.setReasonDetail(object.getString("reasonDetail"));
                    scores.add(model);
                } catch (JSONException js) {
                    js.printStackTrace();
                }
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
        hasMore = scores.size() > 0;
        mListView.onFinishLoading(hasMore, scores);
    }

    public String getUrl(int requestType) {
        Map<String, Object> params = new HashMap<String, Object>();
        if (requestType == REQUEST_LIST) {
            params.put("action", "getMemberPointRecordList");
            params.put("step", Constants.PAGING_STEP);
            params.put("page", mPage);
            return APIUtils.getUrl(APIUtils.USER_CENTER, params);
        } else if (requestType == REQUEST_POINT) {
            params.put("action", "getUserInfo");
            return APIUtils.getUrl(APIUtils.REGISTER, params);
        }
        return null;
    }

    public JsonObjectRequest getRequest(final int requestType) {
        String url = getUrl(requestType);
        Log.v("AAA", "ScoreListActivity->url=" + url);
        return new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject object) {
                try {
                    if (object.getInt("status") == 0) {
                        setResponseView();
                        if (requestType == REQUEST_POINT) {
                            processPoint(object.getJSONObject("data"));
                        } else if (requestType == REQUEST_LIST) {
                            processList(object.getJSONArray("data"));
                        }
                    }
                } catch (JSONException js) {
                    js.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                setErrorView();
            }
        });
    }

    public class ScoreAdapter extends PagingBaseAdapter<ScoreModel> {
        protected Activity mContext;

        public ScoreAdapter(Activity context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public ScoreModel getItem(int position) {
            if (0 <= position && position < items.size()) {
                return items.get(position);
            }
            return null;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position) != null;
        }

        @Override
        public long getItemId(int postion) {
            return postion;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            final View view;
            final ScoreModel score = getItem(position);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                holder = new ViewHolder();
                view = inflater.inflate(R.layout.score_list_item, viewGroup, false);
                holder.typeDes = (TextView) view.findViewById(R.id.type_des);
                holder.recordTime = (TextView) view.findViewById(R.id.record_time);
                holder.scoreDes = (TextView) view.findViewById(R.id.score_des);
                holder.scoreRecord = (TextView) view.findViewById(R.id.score_record);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }
            holder.typeDes.setText(score.getReason());
            holder.recordTime.setText(DateUtils.getFormatDate(score.getCreateTime()));
            holder.scoreDes.setText(score.getReasonDetail());
            setChange(holder.scoreRecord, score.getChange());
            return view;
        }

        public void setChange(TextView view, int change) {
            if (change >= 0) {
                view.setText("+" + change);
                view.setTextColor(getResources().getColor(R.color.main_color));
            } else {
                view.setText(change);
                view.setTextColor(getResources().getColor(R.color.gray_1));
            }
        }
    }

    public class ViewHolder {
        TextView typeDes;
        TextView recordTime;
        TextView scoreDes;
        TextView scoreRecord;
    }
}