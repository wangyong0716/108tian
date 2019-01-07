package com.ksider.mobile.android.activity.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.*;
import com.ksider.mobile.android.adaptor.BannerAlbumAdaptor;
import com.ksider.mobile.android.adaptor.BannerScrollAlbumListener;
import com.ksider.mobile.android.model.ListViewDataModel;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.model.SubjectModel;
import com.ksider.mobile.android.scrollListView.OverScrollPullToRefreshListView;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.view.CircularImageView;
import com.ksider.mobile.android.view.LoadImageView;
import com.ksider.mobile.android.view.paging.PagingBaseAdapter;
import com.ksider.mobile.android.view.viewpagerindicator.BannerIndicator;
import com.umeng.analytics.MobclickAgent;
import de.greenrobot.event.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomePageFragment extends BaseFragment {
    protected int mPage = -1; // 页数
    protected int mCityId = 1;
    protected int mMaxPageSize = -1;
    protected HomePagingAdaptor mPagingAdaptor;
    protected static final int HOME_PAGE_STEP = 2;
    protected OverScrollPullToRefreshListView mPagingListView;
    protected final String HOME_TAG = "HomePage_Fragment";
    protected final String CHOICENESS_TAG = "Coiceness_Fragment";
    protected View mHeader;
    protected View mRoot;
    protected Boolean mLocationReady = false;

    private int cityId;
    private String cityName;

    private String homePageBanner = "home_page_banner";
    private String homePageLabelSurrounding = "home_page_label_surrounding";
    private String homePageLabelAccommodation = "home_page_label_accommodation";
    private String homePageLabelPicking = "home_page_label_picking";
    private String homePageLabelActivity = "home_page_label_activity";
    private String homePageSquare = "home_page_square_";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_guide, container, false);
        mPagingListView = (OverScrollPullToRefreshListView) mRoot.findViewById(R.id.content_list);
        mPagingListView.setOnRefreshListener(new OverScrollPullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshAll();
            }
        });
        mPagingAdaptor = new HomePagingAdaptor(getActivity());
        mPagingListView.setAdapter(mPagingAdaptor);
        mPagingListView.setPagingableListener(new OverScrollPullToRefreshListView.Pagingable() {
            @Override
            public void onLoadMoreItems() {
                mPage++;
                if (mPage * HOME_PAGE_STEP < mMaxPageSize) {
                    Network.getInstance().addToRequestQueue(getListRequest(), CHOICENESS_TAG);
                } else {
                    mPagingListView.onFinishLoading(false, null);
                }
            }
        });

        mPagingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> params, View view, int position, long id) {
                HomeItem item = mPagingAdaptor.getItem(position);
                if (item != null && item.type == 0) {
                    Intent intent = Utils.getLandingActivity(getActivity(), item.detail.type);
                    if (intent != null) {
                        Utils.initDetailPageArg(intent, item.detail);
                        startActivity(intent);
                    }
                }
            }
        });
        mHeader = inflater.inflate(R.layout.guide_header, null);
        mPagingListView.addHeaderView(mHeader);
        TextView title = (TextView) mRoot.findViewById(R.id.list_title);
        mRoot.findViewById(R.id.topBar).setBackgroundColor(getResources().getColor(R.color.toolbar_background_color));
        title.setText("首页");
        TextView citySelect = (TextView) mRoot.findViewById(R.id.cityName);
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                switch (view.getId()) {
                    case R.id.cityName:
                        intent = new Intent(getActivity(), CitySelectedActivity.class);
                        break;
                    case R.id.search:
                        intent = new Intent(getActivity(), SearchActivity.class);
                        break;
                    default:
                        break;
                }
                if (intent != null) {
                    startActivity(intent);
                }
            }
        };

        citySelect.setOnClickListener(listener);
        citySelect.setText(Storage.getSharedPref().getString("cityName", ""));
        mRoot.findViewById(R.id.search).setOnClickListener(listener);
        return mRoot;
    }

    private OnClickListener choseListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            BasicCategory category = null;
            switch (v.getId()) {
                case R.id.view_surrounding:
                    category = BasicCategory.ATTRACTIONS;
                    MobclickAgent.onEvent(getActivity(), homePageLabelSurrounding);
                    break;
                case R.id.view_accommodation:
                    category = BasicCategory.RESORT;
                    MobclickAgent.onEvent(getActivity(), homePageLabelAccommodation);
//                    category = BasicCategory.FARMYARD;
                    break;
                case R.id.view_activity:
                    ((ViewPager) getActivity().findViewById(R.id.homePage)).setCurrentItem(2);
                    MobclickAgent.onEvent(getActivity(), homePageLabelActivity);
                    return;
                case R.id.view_picking:
                    category = BasicCategory.PICKINGPART;
                    MobclickAgent.onEvent(getActivity(), homePageLabelPicking);
                    break;
                default:
                    break;
            }
            if (category != null) {
                Map value = new HashMap();
                value.put("content", category.toString());
                value.put("channel", DeviceUuid.getChannel());
                MobclickAgent.onEventValue(getActivity(), "navclick", value, 1);
                Intent intent = new Intent(getActivity(), SelectorActivity.class);
                intent.putExtra("category", category);
                startActivity(intent);
            }
        }
    };

    public void onEventMainThread(MessageEvent event) {
        if (event.getType() == MessageEvent.UPDATE_POSITION) {
            if (!mLocationReady) {
                mLocationReady = true;
                String position = Storage.sharedPref.getString("position", null);
                if (position != null) {
                    String[] pos = position.split(",");
                    refresh();
                    if (pos.length == 2) {
                        Network.getInstance().addToRequestQueue(getRequest(APIUtils.getHome(Double.parseDouble(pos[0]), Double.parseDouble(pos[1]))), HOME_TAG);
                    }
                }
            }
        } else if (event.getType() == MessageEvent.UPDATE_DEFAULTCITY) {
            int cityId = Storage.sharedPref.getInt("cityId", 1);
            if (mCityId != cityId) {
                String cityName = Storage.sharedPref.getString("cityName", "");
                setTextView(getView(), R.id.cityName, cityName);
                refreshAll();
            }
        } else if (event.getType() == MessageEvent.NOTIFY_GET_CITY) {
            CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    renderCitySelected(Storage.getSharedPref().getString("cityName", ""));
                    refreshAll();
                    EventBus.getDefault().postSticky(new MessageEvent(MessageEvent.UPDATE_DEFAULTCITY));
                }
            }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Storage.putInt("cityId", cityId);
                    Storage.putString("cityName", cityName);
                    EventBus.getDefault().postSticky(new MessageEvent(MessageEvent.UPDATE_DEFAULTCITY));
                }
            }).setTitle("是否切换到" + cityName + "？").show();
        }
    }

    public void renderAdlet(JSONArray adlet) {
        if (adlet.length() != 4) {
            mHeader.findViewById(R.id.subject_layout).setVisibility(View.GONE);
            return;
        }
        mHeader.findViewById(R.id.subject_layout).setVisibility(View.VISIBLE);
        for (int i = 0; i < adlet.length(); i++) {
            try {
                JSONObject let = adlet.getJSONObject(i);
                SubjectModel subject = new SubjectModel();
                subject.setId(let.getString("_id"));
                subject.setType(let.getString("type"));
                subject.setTo(let.getString("to"));
                subject.setName(let.getString("name"));
                subject.setSubTitle(let.getString("subtitle"));
                subject.setImg(let.getString("img"));
                try {
                    subject.setStartTime(let.getLong("startTime"));
                    subject.setModified(let.getLong("modified"));
                    subject.setEndTime(let.getLong("endTime"));
                } catch (JSONException js) {
                    js.printStackTrace();
                }
                fillSubject(i + 1, subject);
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
    }

    public void fillSubject(final int id, final SubjectModel subjectModel) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        View layout = mHeader.findViewById(getResources().getIdentifier("subject_layout_" + id, "id", getActivity().getPackageName()));
        if (layout == null) {
            return;
        }
        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(getActivity(), homePageSquare + id);
                Intent intent = Utils.getLandingActivity(getActivity(), subjectModel.getType());
                if (intent != null) {
                    Utils.appendArgs(intent, subjectModel);
                    startActivity(intent);
                }
            }
        });

        TextView title = (TextView) mHeader.findViewById(getResources().getIdentifier("subject_title_" + id, "id", getActivity().getPackageName()));
        title.setText(subjectModel.getName());
        TextView subTitle = (TextView) mHeader.findViewById(getResources().getIdentifier("subject_subtitle_" + id, "id", getActivity().getPackageName()));
        subTitle.setText(subjectModel.getSubTitle());
        CircularImageView image = (CircularImageView) mHeader.findViewById(getResources().getIdentifier("subject_img_" + id, "id", getActivity().getPackageName()));
        image.setImageResource(subjectModel.getImg());
    }

    protected JsonObjectRequest getRequest(String url) {
        Log.v("AAA", "homePage->url=" + url);
        return new JsonObjectRequest(url, null, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        JSONObject data = response.getJSONObject("data");
                        try {
                            renderAdlet(data.getJSONArray("adlet"));
                        } catch (JSONException js) {
                            js.printStackTrace();
                        }
                        proccessCity(data);
                        renderBanner(data);
                        renderThemes();
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

    public void proccessCity(JSONObject data) {
        try {
            Storage.putString("homePage", data.toString());
            cityId = data.getInt("cityId");
            cityName = data.getString("cityName");
            int defaultCityId = Storage.getSharedPref().getInt("cityId", -1);
            if (defaultCityId == -1) {
                Storage.putInt("cityId", cityId);
                Storage.putString("cityName", cityName);
                renderCitySelected(Storage.getSharedPref().getString("cityName", ""));
                EventBus.getDefault().postSticky(new MessageEvent(MessageEvent.UPDATE_DEFAULTCITY));
            } else if (defaultCityId != cityId) {
                MessageUtils.postSticky(MessageUtils.NOTIFY_GET_CITY, new MessageEvent(MessageEvent.NOTIFY_GET_CITY));
            }
//            Boolean notified = cityId != Storage.getSharedPref().getInt("cityId", -1);


            JSONObject openCity = data.getJSONObject("openCity");
            Storage.putString("openCity", openCity.toString());

//            if (notified) {
//                EventBus.getDefault().postSticky(new MessageEvent(MessageEvent.UPDATE_DEFAULTCITY));
//            }
        } catch (JSONException e) {
            renderCitySelected(Storage.getSharedPref().getString("cityName", ""));
            e.printStackTrace();
        }
    }

    public void renderBanner(JSONObject data) {
        try {
            JSONArray indexBanner = data.getJSONArray("indexBanner");
            List<BannerAlbumAdaptor.BannerAlbumItem> album = new ArrayList<BannerAlbumAdaptor.BannerAlbumItem>();

            for (int index = 0; index < indexBanner.length(); index++) {
                try {
                    BannerAlbumAdaptor.BannerAlbumItem item = new BannerAlbumAdaptor.BannerAlbumItem();
                    JSONObject banner = indexBanner.getJSONObject(index);
                    item.dest = banner.getString("to");
                    item.image = banner.getString("img");
                    item.name = banner.getString("name");
                    item.type = banner.getString("type");
                    album.add(item);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            final ViewPager pager = (ViewPager) mHeader.findViewById(R.id.header_album);
            if (pager.getAdapter() == null) {
                BannerAlbumAdaptor adaptor = new BannerAlbumAdaptor(getActivity(), album);
                pager.setAdapter(adaptor);
                pager.setCurrentItem(album.size() * 100);
                BannerIndicator indicator = (BannerIndicator) mHeader.findViewById(R.id.indicator);
                indicator.setViewPager(pager);
                BannerScrollAlbumListener albumListener = new BannerScrollAlbumListener(pager, true);
                pager.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MobclickAgent.onEvent(getActivity(), homePageBanner);
                        int position = pager.getCurrentItem();
                        BannerAlbumAdaptor adaptor = (BannerAlbumAdaptor) pager.getAdapter();
                        BannerAlbumAdaptor.BannerAlbumItem item = adaptor.getItem(position);
                        Intent intent = Utils.getLandingActivity(getActivity(), item.type);
                        if (intent != null) {
                            Utils.appendArgs(intent, item);
                            startActivity(intent);
                        }
                    }
                });
                pager.setOnTouchListener(new View.OnTouchListener() {
                    float oldX = 0, newX = 0, sens = 5;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                oldX = event.getX();
                                break;
                            case MotionEvent.ACTION_UP:
                                newX = event.getX();
                                if (Math.abs(oldX - newX) < sens) {
                                    v.performClick();
                                    return true;
                                }
                                oldX = 0;
                                newX = 0;
                                break;
                        }
                        return false;
                    }
                });
                indicator.setOnPageChangeListener(albumListener);
            } else {
                BannerAlbumAdaptor adaptor = (BannerAlbumAdaptor) pager.getAdapter();
                adaptor.removeAllItems();
                adaptor.addMoreItems(album);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void renderThemes() {
        mHeader.findViewById(R.id.view_surrounding).setOnClickListener(choseListener);
        mHeader.findViewById(R.id.view_accommodation).setOnClickListener(choseListener);
        mHeader.findViewById(R.id.view_picking).setOnClickListener(choseListener);
        mHeader.findViewById(R.id.view_activity).setOnClickListener(choseListener);
    }

    protected void renderCitySelected(String cityName) {
        setTextView(mRoot, R.id.cityName, cityName);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCityId = Storage.sharedPref.getInt("cityId", 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPage == -1) {
            mPage = 0;
            refreshAll();
        }
        MobclickAgent.onPageStart("guide_list");
        EventBus.getDefault().registerSticky(this);
        MessageUtils.register(MessageUtils.NOTIFY_GET_CITY, this);
        MobclickAgent.onResume(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("guide_list");
        EventBus.getDefault().unregister(this);
        MessageUtils.unregister(MessageUtils.NOTIFY_GET_CITY, this);
        MobclickAgent.onPause(getActivity());
    }

    private void clearData() {
        if (mPagingAdaptor != null) {
            mPage = -1;
            mMaxPageSize = -1;
            Network.getInstance().cancelPendingRequests(CHOICENESS_TAG);
            mPagingListView.setHasMoreItems(false);
            mPagingAdaptor.removeAllItems();
        }
    }

    protected void refresh() {
        clearData();
        mPage = 0;
        Network.getInstance().addToRequestQueue(getListRequest(), CHOICENESS_TAG);
    }

    protected void refreshAll() {
        Network.getInstance().cancelPendingRequests(HOME_TAG);
        int cityId = Storage.sharedPref.getInt("cityId", 1);
        Network.getInstance().addToRequestQueue(getRequest(APIUtils.getHome(cityId)), HOME_TAG);
        refresh();
    }

    public Map<String, Object> getRequestParams() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("step", HOME_PAGE_STEP);
        params.put("page", mPage);
        mCityId = Storage.sharedPref.getInt("cityId", 1);
        params.put("cityId", mCityId);
        return params;
    }

    @Override
    public void onDestroy() {
        StatHandle.postImpclkEvent(getActivity(), StatHandle.GUIDE_LIST);
        super.onDestroy();
    }

    protected JsonObjectRequest getListRequest() {
        Log.v("AAA", "Homepage->listurl=" + APIUtils.getUrl(APIUtils.CHOINCENESSDETAILIST, getRequestParams()));
        return new JsonObjectRequest(APIUtils.getUrl(APIUtils.CHOINCENESSDETAILIST, getRequestParams()), null, new Listener<JSONObject>() {
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
                mPagingListView.onRefreshComplete();
                mPagingListView.showFailed();
                Log.v(Constants.LOG_TAG, "response:" + error.toString());
            }
        });
    }

    protected void proccess(JSONObject data) {
        mPagingListView.onRefreshComplete();
        try {
            mMaxPageSize = data.getInt("number");
            List<HomeItem> homeItems = new ArrayList<HomeItem>();
            JSONArray lists = data.getJSONArray("list");
            for (int i = 0; i < lists.length(); i++) {
                try {
                    HomeItem item = new HomeItem();
                    JSONObject o = (JSONObject) lists.get(i);
                    item.type = 1; //title
                    ListViewDataModel detail = new ListViewDataModel();
                    detail.title = o.getString("name");
                    detail.subTitle = o.getString("subtitle");
                    item.detail = detail;
                    homeItems.add(item);
                    JSONArray items = o.getJSONArray("items");
                    for (int index = 0; index < items.length(); index++) {
                        try {
                            JSONObject itemDetail = items.getJSONObject(index);
                            item = new HomeItem();
                            detail = new ListViewDataModel();
                            item.type = 0; //detail
                            item.detail = detail;
                            detail.title = itemDetail.getString("name");
                            detail.imgUrl = itemDetail.getString("img");
                            JSONObject recommend = itemDetail.getJSONObject("recommend");
                            detail.id = recommend.getString("id");
                            detail.type = recommend.getString("type");
                            try {
                                detail.collection = recommend.getString("fav");
                            } catch (JSONException js) {
                                js.printStackTrace();
                                detail.collection = "";
                            }
                            try {
                                detail.isFav = recommend.getBoolean("isFav");
                            } catch (JSONException js) {
                                js.printStackTrace();
                                detail.isFav = false;
                            }

                            try {
                                JSONArray list = recommend.getJSONArray("priceRange");
                                if (list.length() > 1) {
                                    detail.price = StringUtils.getPriceRange(list.getDouble(0), list.getDouble(1));
                                } else {
                                    detail.price = "";
                                }
                            } catch (JSONException js) {
                                detail.price = "";
                                js.printStackTrace();
                            }

//                            try {
////                                detail.price = recommend.getString("price");
//                                detail.price = StringUtils.getPrice(recommend.getString("price"));
////                                detail.price = StringUtils.getPrice(recommend.getDouble("price"));
//                            } catch (JSONException js) {
//                                js.printStackTrace();
//                                detail.price = "";
//                            }
//                            Double dist = getDistance(recommend);
//                            if (dist >= 0) {
//                                detail.location = dist + "km";
//                            } else {
//                                detail.location = "";
//                            }
                            try {
                                detail.location = StringUtils.getDistance(recommend.getJSONArray("lnglat").getDouble(1), recommend.getJSONArray("lnglat").getDouble(0)) + "km";
                            } catch (Exception js) {
                                detail.location = "";
                                js.printStackTrace();
                            }
                            try {
                                detail.startDate = DateUtils.getRecentDate(recommend.getLong("startTime"));
                            } catch (JSONException js) {
                                js.printStackTrace();
                                detail.startDate = "";
                            }
                            homeItems.add(item);
                        } catch (JSONException js) {
                            js.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    Log.v(Constants.LOG_TAG, " JSONException " + e.toString());
                    e.printStackTrace();
                }
            }
            mPagingListView.onFinishLoading(homeItems.size() > 0, homeItems);
        } catch (JSONException e) {
            Log.v(Constants.LOG_TAG, " JSONException " + e.toString());
            e.printStackTrace();
        }
    }

    protected double getDistance(JSONObject item) {
        Double dist = -1.0;
        try {
            JSONArray lngLatitude = item.getJSONArray("lnglat");
            Double lng = lngLatitude.getDouble(0);
            Double lat = lngLatitude.getDouble(1);
            dist = Maths.getSelfDistance(lat, lng);

            // fix 显示
            if (dist == 0.0) {
                dist = Math.random() * 5 / 10 + 0.1;
                dist = Math.round(dist * 100) * 1.0 / 100;
            }
        } catch (JSONException e) {
            dist = -1.0;
        } catch (Exception e) {
            Log.v(Constants.LOG_TAG, e.toString());
        }
        return dist;
    }

    public class HomeItem {
        public int type;
        public ListViewDataModel detail;
    }

    public class HomePagingAdaptor extends PagingBaseAdapter<HomeItem> {
        protected Activity mContext;

        public HomePagingAdaptor(Activity context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public HomeItem getItem(int postion) {
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
            return getItem(position) != null && getItem(position).type == 0;
        }

        @Override
        public long getItemId(int postion) {
            return postion;
        }

        @Override
        public View getView(int postion, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            final View view;
            final ListViewDataModel data = getItem(postion).detail;
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            }
            if (convertView == null || (holder != null && holder.type != getItem(postion).type)) {
                if (getItem(postion).type == 0) {
                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    holder = new ViewHolder();
                    view = inflater.inflate(R.layout.list_view_rounded_corner_item_new, viewGroup, false);
                    holder.title = (TextView) view.findViewById(R.id.list_title);
                    holder.date = (TextView) view.findViewById(R.id.listview_date);
                    holder.distance = (TextView) view.findViewById(R.id.listview_distance);
                    holder.price = (TextView) view.findViewById(R.id.listview_price);
                    holder.image = (LoadImageView) view.findViewById(R.id.listview_headImage);
                    holder.collection = (TextView) view.findViewById(R.id.listview_collection);
                    holder.dateDistance = (LinearLayout) view.findViewById(R.id.date_distance);
                    holder.distanceIcon = (ImageView) view.findViewById(R.id.distance_icon);
                    holder.type = 0;
                } else {
                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    holder = new ViewHolder();
                    view = inflater.inflate(R.layout.header_rec_title_new, viewGroup, false);
                    holder.title = (TextView) view.findViewById(R.id.title);
                    holder.label = (TextView) view.findViewById(R.id.subtitle);
                    holder.type = 1;
                }
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }
            if (getItem(postion).type == 0) {
                if (data.title != null) {
                    holder.title.setText(data.title);
                }
                if (data.imgUrl != null && data.imgUrl.length() > 4) {
                    holder.image.setImageResource(data.imgUrl);
                }
                if ((data.startDate == null || data.startDate.equals("")) && (data.location == null || data.location.equals(""))) {
                    holder.dateDistance.setVisibility(View.GONE);
                } else if ((data.startDate == null || data.startDate.equals("")) && !(data.location == null || data.location.equals(""))) {
                    holder.dateDistance.setVisibility(View.VISIBLE);
                    holder.distanceIcon.setVisibility(View.VISIBLE);
                    holder.date.setVisibility(View.GONE);
                    holder.distance.setVisibility(View.VISIBLE);
                    holder.distance.setText(data.location);
                } else if (!(data.startDate == null || data.startDate.equals("")) && (data.location == null || data.location.equals(""))) {
                    holder.dateDistance.setVisibility(View.VISIBLE);
                    holder.distanceIcon.setVisibility(View.GONE);
                    holder.date.setVisibility(View.VISIBLE);
                    holder.date.setText(data.startDate);
                    holder.distance.setVisibility(View.GONE);
                } else {
                    holder.distanceIcon.setVisibility(View.GONE);
                    holder.dateDistance.setVisibility(View.VISIBLE);
                    holder.date.setVisibility(View.VISIBLE);
                    holder.distance.setVisibility(View.VISIBLE);
                    holder.date.setText(data.startDate);
                    holder.distance.setText(data.location);
                }
                if (data.collection.equals("")) {
                    holder.collection.setVisibility(View.GONE);
                } else {
                    holder.collection.setVisibility(View.VISIBLE);
                    holder.collection.setText(data.collection + "人");
                    if (data.isFav) {
                        Drawable drawable = getResources().getDrawable(R.drawable.list_collected_icon);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        holder.collection.setCompoundDrawables(drawable, null, null, null);
                    } else {
                        Drawable drawable = getResources().getDrawable(R.drawable.list_collection_icon);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        holder.collection.setCompoundDrawables(drawable, null, null, null);
                    }
                    holder.collection.setOnClickListener(new OnClickListener() {
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
                holder.price.setText(data.price);
            } else {
                holder.title.setText(data.title);
                holder.label.setText(data.subTitle);
            }
            StatHandle.increaseImpression(StatHandle.GUIDE_LIST);
            return view;
        }
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
        params.put("POIType", data.type);
        params.put("POIId", data.id);
        JsonObjectRequest request = new JsonObjectRequest(APIUtils.getUserCenter(params), null, new Listener<JSONObject>() {
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
        }, new ErrorListener() {
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
            drawable = getResources().getDrawable(R.drawable.list_collection_icon);
        } else {
            data.collection = calculate(data.collection, 1);
            data.isFav = true;
            drawable = getResources().getDrawable(R.drawable.list_collected_icon);
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

//    protected void postFavorite(FavoriteActions action, final  data, final View collect) {
//        Map<String, Object> params = new HashMap<String, Object>();
//        switch (action) {
//            case FAVORATOR:
//                params.put("action", "setFav");
//                MessageUtils.eventBus.post(new MessageEvent(MessageEvent.NOTIFY_COLLECTED_CHANGE));
//                break;
//            case CANCEL_FAVORATOR:
//                MessageUtils.eventBus.post(new MessageEvent(MessageEvent.NOTIFY_COLLECTED_CHANGE));
//                params.put("action", "delFav");
//                break;
//            case BEEN:
//                params.put("action", "setBeen");
//                break;
//            case CANCEL_BEEN:
//                params.put("action", "delBeen");
//                break;
//            default:
//                return;
//        }
//        params.put("POIType", data.type);
//        params.put("POIId", data.id);
//        JsonObjectRequest request = new JsonObjectRequest(APIUtils.getUserCenter(params), null, new Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                try {
//                    if (response.getInt("status") == 0) {
//                        switchCollectView(data, collect);
//                        MessageUtils.eventBus.post(new MessageEvent(MessageEvent.NOTIFY_COLLECTED_CHANGE));
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.v(Constants.LOG_TAG, error.toString());
//            }
//        });
//        Network.getInstance().addToRequestQueue(request);
//    }

    /**
     * ViewHolder
     */
    protected static class ViewHolder {
        public int type = -1;
        public TextView title;
        public TextView label;
        public TextView collection;
        public TextView date;
        public TextView distance;
        public TextView price;
        public ImageView distanceIcon;
        public LinearLayout dateDistance;
        public LoadImageView image;
    }
}
