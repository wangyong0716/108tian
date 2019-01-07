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
import android.widget.RatingBar;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.BaseActivity;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.EvaluationModel;
import com.ksider.mobile.android.model.MerchantEvaluationModel;
import com.ksider.mobile.android.scrollListView.OverScrollPagingListView;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.DateUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.StringUtils;
import com.ksider.mobile.android.view.CircularImageView;
import com.ksider.mobile.android.view.evaluation.AutoLayout;
import com.ksider.mobile.android.view.evaluation.SimpleAutoLayout;
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
public class MerchantScoreActivity extends BaseActivity {
    private final int REQUEST_SCORE = 1;
    private final int REQUEST_TAGS = 2;
    private final int REQUEST_LIST = 3;
    private AutoLayout evaluationLayout;
    private ArrayList<EvaluationModel> originEvaluations = new ArrayList<EvaluationModel>();

    private OverScrollPagingListView mListView;
    private MerchantEvaluationAdapter scoreAdapter;
    private View mHeader;

    private int mPage = 0;
    private boolean hasMore = true;

    private String merchantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.merchant_score);
        new SlidingLayout(this);
        customActionBar();
        TextView title = (TextView) findViewById(R.id.list_title);
        title.setText("用户点评");

        merchantId = getIntent().getExtras().getString("merchantId");

        mHeader = getLayoutInflater().inflate(R.layout.merchant_score_header, null);
        mListView = (OverScrollPagingListView) findViewById(R.id.content_list);
        mListView.addHeaderView(mHeader);
        scoreAdapter = new MerchantEvaluationAdapter(this);
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
        evaluationLayout = (AutoLayout) findViewById(R.id.evaluation_layout);
        evaluationLayout.setReDrawn(true);

        initLoadingView();
        Network.getInstance().addToRequestQueue(getRequest(REQUEST_SCORE));
        Network.getInstance().addToRequestQueue(getRequest(REQUEST_TAGS));
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

    public void addEvaluation(AutoLayout layout, ArrayList<EvaluationModel> evaluations) {
        layout.removeAllViews();
        for (int i = 0; i < evaluations.size(); i++) {
            TextView evaluation = (TextView) getLayoutInflater().inflate(R.layout.evaluation_textview, null);
            evaluation.setText(evaluations.get(i).getContent());
            layout.addView(evaluation);
        }
    }

    public String getUrl(int requestType) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", merchantId);
        if (requestType == REQUEST_SCORE) {
            params.put("action", "getMerchantScore");
            return APIUtils.getUrl(APIUtils.EVALUATE, params);
        } else if (requestType == REQUEST_TAGS) {
            params.put("action", "getEvaluateTags");
            return APIUtils.getUrl(APIUtils.EVALUATE, params);
        } else if (requestType == REQUEST_LIST) {
            params.put("action", "getEvaluateDetailList");
            params.put("page", mPage);
            params.put("step", Constants.PAGING_STEP);
            return APIUtils.getUrl(APIUtils.EVALUATE, params);
        }
        return null;
    }

    public JsonObjectRequest getRequest(final int requestType) {
        String url = getUrl(requestType);
        Log.v("AAA", "MerchantScoreActivity->url=" + url);
        return new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject object) {
                try {
                    if (object.getInt("status") == 0) {
                        setResponseView();
                        if (requestType == REQUEST_SCORE) {
                            processScore(object.getJSONObject("data"));
                        } else if (requestType == REQUEST_TAGS) {
                            processTags(object.getJSONArray("data"));
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

    public void processList(JSONArray array) {
        ArrayList<MerchantEvaluationModel> items = new ArrayList<MerchantEvaluationModel>();
        for (int i = 0; i < array.length(); i++) {
            try {
                MerchantEvaluationModel item = new MerchantEvaluationModel();
                JSONObject object = array.getJSONObject(i);
                try {
                    item.setAvatar(object.getString("avatar"));
                    item.setCreateTime(object.getLong("createTime"));
                    item.setScore(object.getDouble("score"));
                    item.setUserName(object.getString("userName"));
                } catch (JSONException js) {
                    js.printStackTrace();
                }
                try {
                    JSONArray contentList = object.getJSONArray("contentList");
                    for (int j = 0; j < contentList.length(); j++) {
                        try {
                            EvaluationModel model = new EvaluationModel();
                            model.setContent(contentList.getString(j));
                            item.addEvaluation(model);
                        } catch (JSONException js) {
                            js.printStackTrace();
                        }
                    }
                } catch (JSONException js) {
                    js.printStackTrace();
                }
                items.add(item);
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
        hasMore = items.size() > 0;
        mListView.onFinishLoading(hasMore, items);
    }

    public void processScore(JSONObject object) {
        try {
            ((TextView) findViewById(R.id.score)).setText(StringUtils.getScore(object.getDouble("score")));
        } catch (JSONException js) {
            js.printStackTrace();
        }
        try {
            ((TextView) findViewById(R.id.count)).setText(object.getString("totalCount"));
        } catch (JSONException js) {
            js.printStackTrace();
        }
    }

    public void processTags(JSONArray array) {
        originEvaluations.clear();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject object = array.getJSONObject(i);
                EvaluationModel model = new EvaluationModel();
                model.setContent(getResources().getString(R.string.evaluate_tag, object.getString("content"), object.getString("hitCount")));
                originEvaluations.add(model);
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
        addEvaluation(evaluationLayout, originEvaluations);
    }

    class MerchantEvaluationAdapter extends PagingBaseAdapter<MerchantEvaluationModel> {
        protected Activity mContext;

        public MerchantEvaluationAdapter(Activity context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public MerchantEvaluationModel getItem(int postion) {
            if (0 <= postion && postion < items.size()) {
                return items.get(postion);
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
        public View getView(int postion, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            final View view;
            final MerchantEvaluationModel model = getItem(postion);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                holder = new ViewHolder();
                view = inflater.inflate(R.layout.list_view_evaluation_item, viewGroup, false);
                holder.avatar = (CircularImageView) view.findViewById(R.id.avatar);
                holder.userName = (TextView) view.findViewById(R.id.user_name);
                holder.score = (RatingBar) view.findViewById(R.id.score);
                holder.createTime = (TextView) view.findViewById(R.id.create_time);
                holder.autoLayout = (SimpleAutoLayout) view.findViewById(R.id.evaluation_layout);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }
            holder.avatar.setImageResource(model.getAvatar());
            holder.userName.setText(model.getUserName());
            holder.score.setRating((float) (model.getScore()));
            holder.createTime.setText(DateUtils.getFormatDate(model.getCreateTime()));
            holder.autoLayout.addContent(model.getEvaluations());
            return view;
        }
    }

    public class ViewHolder {
        CircularImageView avatar;
        TextView userName;
        RatingBar score;
        TextView createTime;
        SimpleAutoLayout autoLayout;
    }
}