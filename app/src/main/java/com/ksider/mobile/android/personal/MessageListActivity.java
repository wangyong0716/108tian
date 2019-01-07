package com.ksider.mobile.android.personal;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.BaseActivity;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.activity.fragment.OnScrollChanged;
import com.ksider.mobile.android.model.ReplyModel;
import com.ksider.mobile.android.scrollListView.OverScrollPagingListView;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.Network;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yong on 2015/6/23.
 */
public class MessageListActivity extends BaseActivity {
    protected OverScrollPagingListView mListView;
    protected MessageListAdaptor mAdaptor;
    protected int mPage = -1;
    private boolean hasMore = true;
    private int unReadNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_messages);
        new SlidingLayout(this);
        customActionBar("我的消息");

        mListView = (OverScrollPagingListView) findViewById(R.id.content_list);
        mAdaptor = new MessageListAdaptor(this, mListView);
        mListView.setAdapter(mAdaptor);
        mListView.setPagingableListener(new OverScrollPagingListView.Pagingable() {
            @Override
            public void onLoadMoreItems() {
                mPage++;
                if (hasMore) {
                    Network.getInstance().addToRequestQueue(getRequest());
                } else {
                    mListView.onFinishLoading(false, null);
                }
            }
        });

        mListView.setOnScrollChanged(new OnScrollChanged() {
            @Override
            public void onScrollChanged(int x, int y, int oldX, int oldY) {
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parents, View view, int position, long id) {

            }
        });
        initLoadingView();
        unReadNum = getIntent().getIntExtra("unread", -1);
        if (unReadNum == -1) {
            Network.getInstance().addToRequestQueue(getUnreadReplyCountRequest());
        } else {
            refresh();
        }
    }

    /**
     * show the loading view before getting response
     */
    public void initLoadingView() {
        findViewById(R.id.empty_list_item).setVisibility(View.INVISIBLE);
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
        ((TextView) findViewById(R.id.no_message_info)).setText(R.string.net_acc_failed);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPage = -1;
    }

    protected String getRequestUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "getReplayComment");
        params.put("step", Constants.PAGING_STEP);
        params.put("page", mPage);
        return APIUtils.getUrl(APIUtils.USER_CENTER, params);
    }

    protected void refresh() {
        mPage = 0;
        mAdaptor.removeAllItems();
        Network.getInstance().addToRequestQueue(getRequest());
    }

    public ReplyModel mergeReply(List<ReplyModel> replyList) {
        if (replyList.size() >= 2) {
            if (replyList.get(0).getParent().equals(replyList.get(1).getId())) {
                replyList.get(0).setOrigin(replyList.get(1));
                replyList.get(0).setParentName(replyList.get(1).getUserName());
                return replyList.get(0);
            } else if (replyList.get(1).getParent().equals(replyList.get(0).getId())) {
                replyList.get(1).setOrigin(replyList.get(0));
                replyList.get(1).setParentName(replyList.get(0).getUserName());
                return replyList.get(1);
            }
        }
        return null;
    }

    protected void process(JSONArray array) {
        if (array.length() == 0) {
            hasMore = false;
            setEmptyView();
            return;
        }
        if (array.length() < Constants.PAGING_STEP) {
            hasMore = false;
        }
        try {

            List<ReplyModel> items = new ArrayList<ReplyModel>();
            for (int i = 0; i < array.length(); i++) {
                JSONArray commentList = array.getJSONObject(i).getJSONArray("comments");
                List<ReplyModel> replyList = new ArrayList<ReplyModel>();
                for (int j = 0; j < commentList.length(); j++) {
                    JSONObject comment = commentList.getJSONObject(j);
                    ReplyModel reply = new ReplyModel();
                    reply.setId(comment.getString("_id"));
                    reply.setCreateTime(comment.getLong("createTime"));
                    reply.setPoiId(comment.getString("poiId"));
//                    reply.setThumbsUp(comment.getInt("thumbsUp"));
                    reply.setParent(comment.getString("parent"));
                    reply.setAvatar(comment.getString("avatar"));
//                    reply.setDeleted(comment.getBoolean("deleted"));
                    reply.setContent(comment.getString("content"));
                    reply.setPoiType(comment.getString("poiType"));
                    reply.setUserId(comment.getString("userId"));
                    reply.setUserName(comment.getString("userName"));
                    reply.setRole(comment.getString("role"));

                    try {
                        reply.setOriginUserId(comment.getString("originUserId"));
                    } catch (JSONException js) {
                        js.printStackTrace();
                    }
                    replyList.add(reply);
                }
                ReplyModel replyModel = mergeReply(replyList);
                if (unReadNum > 0) {
                    replyModel.setRead(false);
                    unReadNum--;
                } else {
                    replyModel.setRead(true);
                }
                items.add(replyModel);
            }
            mListView.onFinishLoading(items.size() == Constants.PAGING_STEP, items);
        } catch (JSONException js) {
            js.printStackTrace();
            setErrorView();
        }
    }

    protected JsonObjectRequest getRequest() {
        Log.v("AAA", "messageUrl=" + getRequestUrl());
        JsonObjectRequest request = new JsonObjectRequest(getRequestUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        setResponseView();
                        process(response.getJSONArray("data"));
                    } else {
                        setEmptyView();
                        Log.v("AAA", "get message error!");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setErrorView();
            }
        });
        return request;
    }

    protected JsonObjectRequest getUnreadReplyCountRequest() {
        return new JsonObjectRequest(getUnreadReplyCountUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        unReadNum = response.getInt("data");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                refresh();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }

    public String getUnreadReplyCountUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "getUnreadReplayCount");
        return APIUtils.getUrl(APIUtils.USER_CENTER, params);
    }
}
