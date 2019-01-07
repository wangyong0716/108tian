package com.ksider.mobile.android.activity.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.ChoicenessActivity;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.adaptor.ThemeIndicatorAdaptor;
import com.ksider.mobile.android.model.BaseDataModel;
import com.ksider.mobile.android.model.ListViewDataModel;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.view.paging.gridview.PagingGridView;
import com.ksider.mobile.android.view.viewpagerindicator.CirclePageIndicator;
import com.umeng.analytics.MobclickAgent;
import de.greenrobot.event.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChoicenessFragment extends BaseFragment {
    private int mPage = -1;
    private int mCityId = 1;
    private int mMaxPageSize = -1;
    private PagingGridView mGridView;
    private ChoicenessPagingAdaptor mAdaptor;
    private final String mTag = "Choiceness_list";
    private String THEME_TAG = "Choiceness_theme";
    private View mHeader;
    private View mRoot;
    private Map<String, BaseDataModel> mLeader = new HashMap<String, BaseDataModel>();

    @Override
    public void onResume() {
        super.onResume();
        if (mPage < 0) {
            mPage = 0;
            mCityId = Storage.sharedPref.getInt("cityId", 1);
            Network.getInstance().addToRequestQueue(getRequest(), mTag);
            Network.getInstance().addToRequestQueue(getThemesRequest(), THEME_TAG);
        }
        MobclickAgent.onPageStart("Choiceness_list");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("Choiceness_list");
    }

    private void clearData() {
        if (mAdaptor != null) {
            mPage = -1;
            mMaxPageSize = -1;
            Network.getInstance().cancelPendingRequests(mTag);
            mGridView.setHasMoreItems(false);
            mAdaptor.removeAllItems();
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StatHandle.postImpclkEvent(getActivity(), StatHandle.CHOINCELIST);
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(MessageEvent event) {
        if (event.getType() == MessageEvent.UPDATE_DEFAULTCITY) {
            clearData();
            mPage = 0;
            Network.getInstance().addToRequestQueue(getRequest(), mTag);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRoot != null) {
            ViewGroup parent = (ViewGroup) mRoot.getParent();
            if (parent != null) {
                parent.removeView(mRoot);
                return mRoot;
            }
        }
        mRoot = inflater.inflate(R.layout.choiceness_fragment, container, false);
        mGridView = (PagingGridView) mRoot.findViewById(R.id.choiceness_list);
        mGridView.setPagingableListener(new PagingGridView.Pagingable() {
            @Override
            public void onLoadMoreItems() {
                mPage++;
                if (mPage * Constants.PAGING_STEP < mMaxPageSize && mPage >= 0) {
                    Network.getInstance().addToRequestQueue(getRequest());
                } else {
                    mGridView.onFinishLoading(false, null);
                }
            }
        });

        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < mGridView.getNumColumns()) {
                    return;
                }
                ListViewDataModel item = mAdaptor.getItem(position - mGridView.getNumColumns());
                if (item != null) {
                    BaseDataModel args = new BaseDataModel();
                    args.id = item.id;
                    args.imgUrl = item.imgUrl;
                    args.title = item.title;
                    Intent intent = new Intent(getActivity(), ChoicenessActivity.class);
                    Utils.initDetailPageArg(intent, args);
                    StatHandle.increaseClick(StatHandle.CHOINCELIST);
                    startActivity(intent);
                }
            }
        });
        mHeader = inflater.inflate(R.layout.choiceness_header, null);
        mAdaptor = new ChoicenessPagingAdaptor(getActivity());

        TextView title = (TextView) mRoot.findViewById(R.id.list_title);
        mRoot.findViewById(R.id.header_banner).setBackgroundColor(getResources().getColor(R.color.toolbar_background_color));
        title.setText("发现");
        setLeaderClick("couple", mHeader.findViewById(R.id.couples));
        setLeaderClick("team", mHeader.findViewById(R.id.team));
        setLeaderClick("family", mHeader.findViewById(R.id.family));

        mGridView.addHeaderView(mHeader);
        mGridView.setAdapter(mAdaptor);
        Network.getInstance().addToRequestQueue(getThemesRequest(), THEME_TAG);
        EventBus.getDefault().registerSticky(this);
        return mRoot;
    }

    public void renderThemes(JSONArray array) {
        if (getActivity()==null||getActivity().isFinishing()) {
            return;
        }
        List<String> items = new ArrayList<String>();
        for (int j = 0; j < array.length(); j++) {
            String item = "";
            for (int i = 0; i < 6 && j < array.length(); i++, j++) {
                try {
                    JSONObject object = array.getJSONObject(j);
                    String id = object.getString("_id");
                    if (id.equals("0")) {
                        i--;
                        continue;
                    }
                    item += object.getString("name") + ",";
                    item += id;
                } catch (JSONException js) {
                    i--;
                    js.printStackTrace();
                    continue;
                }
                item += ";";
            }
            items.add(item);
        }

        ViewPager pager = (ViewPager) mHeader.findViewById(R.id.themes_pager);
        ThemeIndicatorAdaptor mTabAdaptor = new ThemeIndicatorAdaptor(getChildFragmentManager(), items);
        pager.setAdapter(mTabAdaptor);
        CirclePageIndicator indicator = (CirclePageIndicator) mHeader.findViewById(R.id.indicator);
        indicator.setViewPager(pager);
    }

    protected Map getRequestParams() {
        Map<String, Object> params = new HashMap<String, Object>();
        mCityId = Storage.sharedPref.getInt("cityId", 1);
        params.put("page", mPage);
        params.put("cityId", mCityId);
        params.put("step", Constants.PAGING_STEP);
        return params;
    }

    protected void setLeaderClick(final String key, View view) {
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseDataModel args = mLeader.get(key);
                if (args != null) {
                    Intent intent = new Intent(getActivity(), ChoicenessActivity.class);
                    Utils.initDetailPageArg(intent, args);
                    startActivity(intent);
                }
            }
        });
    }

    protected void extraleader(String key, JSONObject data) {
        BaseDataModel item = new BaseDataModel();
        try {
            item.id = data.getString("id");
            item.title = data.getString("name");
            item.imgUrl = data.getString("headImg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (item.id != null) {
            mLeader.put(key, item);
            Storage.putString(key, data.toString());
        }
    }

    protected void proccess(JSONObject data) {
        if (mPage == 0) {
            try {
                extraleader("family", data.getJSONObject("family"));
                extraleader("couple", data.getJSONObject("couple"));
                extraleader("team", data.getJSONObject("team"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            mMaxPageSize = data.getInt("number");
        } catch (JSONException e2) {
            e2.printStackTrace();
        }
        List<ListViewDataModel> items = new ArrayList<ListViewDataModel>();
        JSONArray lists;
        try {
            lists = data.getJSONArray("list");
            for (int i = 0; i < lists.length(); i++) {
                try {
                    ListViewDataModel item = new ListViewDataModel();
                    JSONObject o = (JSONObject) lists.get(i);
                    item.title = o.getString("name");
                    item.id = o.getString("id");
                    item.imgUrl = o.getString("headImg");
                    item.subTitle = o.getString("subtitle");
                    item.issued = StringUtils.formatDate(o.getLong("modified"), "MM-dd");
                    items.add(item);
                } catch (JSONException e) {
                    Log.v(Constants.LOG_TAG, "price JSONException!");
                    e.printStackTrace();
                }
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        if (mGridView != null) {
            mGridView.onFinishLoading(items.size() > 0, items);
        }
    }

    protected JsonObjectRequest getRequest() {
        Log.v("AAA", "getChoiceListUrl=" + APIUtils.getPOIList(APIUtils.CHOINCENESS, getRequestParams()));
        return new JsonObjectRequest(APIUtils.getPOIList(APIUtils.CHOINCENESS, getRequestParams()), null, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        proccess(response.getJSONObject("data"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mGridView.showFailed();
                Log.v(Constants.LOG_TAG, "response:" + error.toString());
            }
        });
    }

    protected JsonObjectRequest getThemesRequest() {
        Log.v("AAA", "ChoicenessFragment->themesUrl=" + APIUtils.getTheme("Scene"));
        return new JsonObjectRequest(APIUtils.getTheme("Scene"), null, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        renderThemes(response.getJSONArray("data"));
                        Log.v("AAA", "ChoicenessFragment->response=" + response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(Constants.LOG_TAG, "response:" + error.toString());
            }
        });
    }
}
