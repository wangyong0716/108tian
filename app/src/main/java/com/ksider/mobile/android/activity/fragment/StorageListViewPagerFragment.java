package com.ksider.mobile.android.activity.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.BaseDataModel;
import com.ksider.mobile.android.model.ListViewDataModel;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.scrollListView.OverScrollPullToRefreshListView;
import com.ksider.mobile.android.swipe.SwipeDismissListViewTouchListener;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.MessageUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.Utils;
import com.ksider.mobile.android.view.paging.EmptyView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageListViewPagerFragment extends Fragment {
    private String choice = "";

    protected int mPage = 0;
    protected View mRoot;
    protected OverScrollPullToRefreshListView mListView;
    protected PersonalStorageAdaptor mAdaptor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            choice = getArguments().getString("choice");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        if (mRoot != null) {
            ViewGroup parent = (ViewGroup) mRoot.getParent();
            if (parent != null) {
                parent.removeView(mRoot);
                mListView.onRefreshComplete();
                return mRoot;
            }
        }

        try {
            mRoot = inflater.inflate(R.layout.fragment_storage_paging_listview, container, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        mListView = (OverScrollPullToRefreshListView) mRoot.findViewById(R.id.content_list);
        mAdaptor = new PersonalStorageAdaptor(getActivity());
        mListView.setAdapter(mAdaptor);
        mAdaptor.setMListView(mListView);

        mListView.setOnRefreshListener(new OverScrollPullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
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
                BaseDataModel base = mAdaptor.getItem(position);
                if (base != null) {
                    Intent intent = Utils.getLandingActivity(getActivity(), base.type);
                    if (intent != null) {
                        Utils.initDetailPageArg(intent, base);
                        startActivity(intent);
                    }
                }
            }
        });

        SwipeDismissListViewTouchListener mTouchListener = new SwipeDismissListViewTouchListener(
                mListView,
                new SwipeDismissListViewTouchListener.DismissCallbacks() {
                    @Override
                    public void currentlyAnimatingSwipeOrScroll(boolean isAnimating) {

                    }

                    @Override
                    public void onDismiss(View viewToRemove, int position) {
                    }

                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }
                });

        mListView.setOnTouchListener(mTouchListener);
        mListView.setOnScrollListener(mTouchListener.makeScrollListener());

        refresh();
        return mRoot;
    }

    public void refreshList(String type, String id) {
        mAdaptor.notifyDataSetChanged();
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

    protected String getRequestUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "getTypeFav");
        params.put("favType", choice);
        return APIUtils.getUserCenter(params);
    }

    protected void refresh() {
        initEmptyView();
        mAdaptor.removeAllItems();
        Network.getInstance().addToRequestQueue(getRequest());
    }

    protected void proccess(JSONArray array) {
        if (array.length() == 0) {
            setEmptyView();
            return;
        }
        mListView.onRefreshComplete();
        List<ListViewDataModel> items = new ArrayList<ListViewDataModel>();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                ListViewDataModel item = new ListViewDataModel();
                item.type = o.getString("type");
                item.id = o.getString("id");
                item.title = o.getString("name");
                try {
                    item.imgUrl = o.getString("headImg");
                } catch (JSONException e) {
                }
                items.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mListView.onFinishLoading(false, items);
    }

    protected JsonObjectRequest getRequest() {
        Log.i("AAA", "getStorageList->url=" + getRequestUrl());
        return new JsonObjectRequest(getRequestUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        proccess(response.getJSONArray("data"));
                    } else {
                        setEmptyView();
                    }
                } catch (JSONException e) {
                    setEmptyView();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setEmptyView();
                Log.v(Constants.LOG_TAG, "response:" + error.toString());
            }
        });
    }

    public void initEmptyView() {
//        mListView.deleteFooterView();
        mRoot.findViewById(R.id.empty_view).setVisibility(View.GONE);
    }

    public void setEmptyView() {
        mListView.setHasMoreItems(false);
        mListView.onRefreshComplete();
        EmptyView emptyView = (EmptyView) mRoot.findViewById(R.id.empty_view);
        emptyView.setVisibility(View.VISIBLE);
    }
}
