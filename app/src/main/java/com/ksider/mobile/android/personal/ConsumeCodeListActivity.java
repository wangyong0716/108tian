package com.ksider.mobile.android.personal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.ksider.mobile.android.WebView.BaseActivity;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.activity.fragment.OnScrollChanged;
import com.ksider.mobile.android.model.BaseComparator;
import com.ksider.mobile.android.model.ConsumeCodeModel;
import com.ksider.mobile.android.scrollListView.OverScrollPagingListView;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.Network;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by yong on 2015/5/22.
 */
public class ConsumeCodeListActivity extends BaseActivity {
    protected OverScrollPagingListView mListView;
    protected ConsumeCodeListAdaptor mAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_consume_code);
        new SlidingLayout(this);
        customActionBar("我的消费码");
//        customActionBar(R.layout.header_backtrack_info);
//
//        TextView title = (TextView) findViewById(R.id.list_title);
//        title.setText("我的消费码");
//        TextView sendTo = (TextView) findViewById(R.id.more_choice);
//        sendTo.setText("发送给好友");
//        sendTo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.v("AAA", "发送给好友");
//            }
//        });
//        sendTo.setVisibility(View.INVISIBLE);

        mListView = (OverScrollPagingListView) findViewById(R.id.content_list);
        mAdaptor = new ConsumeCodeListAdaptor(this);
        mListView.setAdapter(mAdaptor);

        mListView.setOnScrollChanged(new OnScrollChanged() {
            @Override
            public void onScrollChanged(int x, int y, int oldX, int oldY) {
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parents, View view, int position, long id) {
                if (position < mListView.getHeaderViewsCount() || position >= mListView.getHeaderViewsCount() + mAdaptor.getCount()) {
                    return;
                }
                ConsumeCodeModel consumeCodeModel = mAdaptor.getItem(position-mListView.getHeaderViewsCount());
                if (consumeCodeModel != null) {
                    Intent intent = new Intent(ConsumeCodeListActivity.this, OrderDetailActivity.class);
                    intent.putExtra("serialNumber", consumeCodeModel.getSerialNumber());
                    startActivity(intent);
                }
            }
        });
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

    @Override
    public void onResume() {
        super.onResume();
        initLoadingView();
        refresh();
    }

    protected String getRequestUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "getUnConsumedCodes");
        return APIUtils.getUrl(APIUtils.ORDER, params);
    }

    protected void refresh() {
        mAdaptor.removeAllItems();
        JsonRequest request = getRequest();
        request.setShouldCache(false);
        Network.getInstance().addToRequestQueue(request);
    }

    protected JsonObjectRequest getRequest() {
        Log.v("AAA", "codes->url=" + getRequestUrl());
        return new JsonObjectRequest(getRequestUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        setResponseView();
                        process(response.getJSONArray("data"));
                    } else {
                        setEmptyView();
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
    }

    protected void process(JSONArray codes) throws JSONException {
        mAdaptor.removeAllItems();
        if (codes.length() == 0) {
            setEmptyView();
            return;
        }
        List<ConsumeCodeModel> items = new ArrayList<ConsumeCodeModel>();
        for (int i = 0; i < codes.length(); i++) {
            JSONObject code = codes.getJSONObject(i);
            if (code.getInt("status") != 1) {
                break;
            }
            ConsumeCodeModel item = new ConsumeCodeModel();
            item.setCode(code.getString("code"));
            item.setStatus(code.getInt("status"));
            item.setSerialNumber(code.getLong("serialNumber"));
            item.setProductName(code.getString("productName"));
            try {
                item.setValidTime(code.getLong("consumeTime"));
            } catch (JSONException js) {
                js.printStackTrace();
            }
            items.add(item);
        }
        Collections.sort(items, ConsumeCodeModel.getComparator(BaseComparator.ASC_SORT));
        mListView.onFinishLoading(false, items);
    }
}



