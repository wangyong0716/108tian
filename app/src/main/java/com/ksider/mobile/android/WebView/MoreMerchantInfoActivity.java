package com.ksider.mobile.android.WebView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.activity.fragment.OnScrollChanged;
import com.ksider.mobile.android.adaptor.MerchantProductListAdaptor;
import com.ksider.mobile.android.model.BaseDataModel;
import com.ksider.mobile.android.model.ListViewDataModel;
import com.ksider.mobile.android.personal.MerchantScoreActivity;
import com.ksider.mobile.android.scrollListView.OverScrollPagingListView;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.view.CircularImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yong on 8/1/15.
 */
public class MoreMerchantInfoActivity extends ActionBarActivity {
    private int GET_MERCHANT_DETAIL = 1;
    private int GET_MERCHANT_PRODUCT = 2;

    protected OverScrollPagingListView listView;
    protected MerchantProductListAdaptor mAdaptor;

    private View header;
    private String merchantId;
    public int mPage = 0;
    private boolean hasMore = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        overridePendingTransition(R.anim.activity_slide_up, R.anim.activity_slide_down);
        setContentView(R.layout.activity_more_merchant_info);
        new SlidingLayout(this);
        merchantId = getIntent().getExtras().getString("brief");
        if (merchantId == null || merchantId.equals("")) {
            Toast.makeText(this, "商户ID有误！", Toast.LENGTH_LONG).show();
            finish();
        }
        listView = (OverScrollPagingListView) findViewById(R.id.content_list);
        customActionBar("");
        header = getLayoutInflater().inflate(R.layout.merchant_info_header, null);
        listView.addHeaderView(header);
        mAdaptor = new MerchantProductListAdaptor(this);
        listView.setAdapter(mAdaptor);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parents, View view, int position, long id) {
                if (position < listView.getHeaderViewsCount() || position >= listView.getHeaderViewsCount() + mAdaptor.getCount()) {
                    return;
                }
                ListViewDataModel model = mAdaptor.getItem(position - listView.getHeaderViewsCount());
                if (model != null) {
                    Intent intent = Utils.getLandingActivity(MoreMerchantInfoActivity.this, model.type);
                    if (intent != null) {
                        intent.putExtra(BaseDataModel.id_name, model.id);
                        startActivity(intent);
                    }
                }
            }
        });

        listView.setPagingableListener(new OverScrollPagingListView.Pagingable() {
            @Override
            public void onLoadMoreItems() {
                if (hasMore) {
                    mPage++;
                    Network.getInstance().addToRequestQueue(getMerchantInfo(GET_MERCHANT_PRODUCT));
                } else {
                    listView.onFinishLoading(false, null);
                }
            }
        });

        findViewById(R.id.more_evaluation_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (merchantId != null && merchantId.length() > 0) {
                    Intent intent = new Intent(MoreMerchantInfoActivity.this, MerchantScoreActivity.class);
                    intent.putExtra("merchantId", merchantId);
                    startActivity(intent);
                }
            }
        });
        Network.getInstance().addToRequestQueue(getMerchantInfo(GET_MERCHANT_DETAIL));
        Network.getInstance().addToRequestQueue(getMerchantInfo(GET_MERCHANT_PRODUCT));
        Network.getInstance().addToRequestQueue(getMerchantScoreRequest());
        initLoadingView();
    }

    protected String getMerchantInfoUrl(int type) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", merchantId);
        if (type == GET_MERCHANT_DETAIL) {
            params.put("action", "detail");
            return APIUtils.getUrl(APIUtils.MERCHANT, params);
        } else if (type == GET_MERCHANT_PRODUCT) {
            params.put("action", "getMerchantProduct");
            params.put("step", Constants.PAGING_STEP);
            params.put("page", mPage);
            return APIUtils.getUrl(APIUtils.PRODUCT, params);
        }
        return null;
    }

    protected JsonObjectRequest getMerchantInfo(final int type) {
        Log.v("AAA", "merchantUrl=" + getMerchantInfoUrl(type));
        return new JsonObjectRequest(getMerchantInfoUrl(type), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        if (type == GET_MERCHANT_DETAIL) {
                            handleDetail(response.getJSONObject("data"));
                        } else if (type == GET_MERCHANT_PRODUCT) {
                            setResponseView();
                            handleProduct(response.getJSONArray("data"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.v("AAA", "get merchant info failed!");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setErrorView();
            }
        });
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

    public String getMechantScoreUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", merchantId);
        params.put("action", "getMerchantScore");
        return APIUtils.getUrl(APIUtils.EVALUATE, params);
    }

    public JsonObjectRequest getMerchantScoreRequest() {
        Log.v("AAA", "getMerchantScore->url=" + getMechantScoreUrl());
        return new JsonObjectRequest(getMechantScoreUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject object) {
                try {
                    processScore(object.getJSONObject("data"));
                } catch (JSONException js) {
                    js.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
    }

    public void handleDetail(JSONObject detail) {
        try {
            getBlurBitmap(detail.getString("avatar"));
        } catch (JSONException js) {
            js.printStackTrace();
        }
        try {
            ((TextView) header.findViewById(R.id.merchant_name)).setText(detail.getString("merchantName"));
            ((TextView) header.findViewById(R.id.merchant_desc)).setText(detail.getString("merchantDesc"));
        } catch (JSONException js) {
            js.printStackTrace();
        }
    }

    public void getBlurBitmap(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            if (url.startsWith("u_")) {
                url = url.substring(2);
            }
            url = ImageUtils.formatImageUrl(url, ImageUtils.MOBILE);
        }
        ImageRequest imageRequest = new ImageRequest(
                url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        ((CircularImageView) header.findViewById(R.id.avatar)).setImageBitmap(response);
                        ((ImageView) header.findViewById(R.id.avatar_background)).setImageBitmap(Blur.fastblur(MoreMerchantInfoActivity.this, response, 40));
                    }
                }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError paramVolleyError) {

            }
        });
        Network.getInstance().addToRequestQueue(imageRequest, "LoadImageView");
    }

    public void handleProduct(JSONArray products) {
        if (products.length() <= 0) {
            hasMore = false;
            listView.onFinishLoading(false, null);
            return;
        }
        if (products.length() < Constants.PAGING_STEP) {
            hasMore = false;
        }
        List<ListViewDataModel> items = new ArrayList<ListViewDataModel>();
        try {
            for (int i = 0; i < products.length(); i++) {
                JSONObject product = products.getJSONObject(i);
                ListViewDataModel item = new ListViewDataModel();
                item.id = product.getString("id");
                try {
                    item.imgUrl = product.getString("headImg");
                } catch (JSONException js) {
                    item.imgUrl = "";
                    js.printStackTrace();
                }
                item.title = product.getString("name");
                item.type = product.getString("type");

                try {
                    item.location = StringUtils.getDistance(product.getJSONArray("lngLatitude").getDouble(1), product.getJSONArray("lngLatitude").getDouble(0)) + "km";
                } catch (Exception e) {
                    item.location = "";
                    e.printStackTrace();
                }
                try {
                    item.price = StringUtils.getPrice(product.getString("price"));
                } catch (JSONException e) {
                    item.price = "";
                    Log.v(Constants.LOG_TAG, "price JSONException!");
                    e.printStackTrace();
                }
                try {
                    item.collection = product.getString("fav");
                } catch (JSONException e) {
                    item.collection = "";
                    Log.v(Constants.LOG_TAG, e.toString());
                    e.printStackTrace();
                }
                try {
                    item.startDate = DateUtils.getRecentDate(product.getLong("startTime"));
                } catch (JSONException js) {
                    js.printStackTrace();
                    item.startDate = "";
                }

                items.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        listView.onFinishLoading(items.size() > 0, items);
    }

//    protected double getDistance(JSONArray lngLatitude) {
//        Double dist = -1.0;
//        try {
//            Double lng = lngLatitude.getDouble(0);
//            Double lat = lngLatitude.getDouble(1);
//            dist = Maths.getSelfDistance(lat, lng);
//            // fix 显示
//            if (dist == 0.0) {
//                dist = Math.random() * 5 / 10 + 0.1;
//                dist = Math.round(dist * 100) * 1.0 / 100;
//            }
//        } catch (JSONException e) {
//            dist = -1.0;
//        } catch (Exception e) {
//            Log.v(Constants.LOG_TAG, e.toString());
//        }
//        return dist;
//    }

    /**
     * show the loading view before getting response
     */
    public void initLoadingView() {
        listView.setVisibility(View.GONE);
        findViewById(R.id.empty_list_item).setVisibility(View.INVISIBLE);
        findViewById(R.id.ptr_id_image).setVisibility(View.GONE);
        findViewById(R.id.video_item_image).setVisibility(View.VISIBLE);
        TextView tv = (TextView) findViewById(R.id.video_item_label);
        tv.setVisibility(View.VISIBLE);
        tv.setText(R.string.loading);
    }

    /**
     * hide the loading view after getting response
     */
    public void setResponseView() {
        listView.setVisibility(View.VISIBLE);
        LinearLayout baseLinearLayout = (LinearLayout) findViewById(R.id.baseLinearLayout);
        if (baseLinearLayout != null) {
            baseLinearLayout.setVisibility(View.GONE);
        }
    }

    /**
     * show the empty view if the data from the response is empty
     */
    public void setEmptyView() {
        listView.onFinishLoading(false, null);
        setResponseView();
        listView.setVisibility(View.GONE);
        findViewById(R.id.empty_list_item).setVisibility(View.VISIBLE);
    }

    /**
     * show error message if the connection fails
     */
    public void setErrorView() {
        setEmptyView();
        findViewById(R.id.empty_list_item).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.no_storage_info1)).setText(R.string.net_acc_failed);
        findViewById(R.id.no_storage_info2).setVisibility(View.GONE);
    }

//    @Override
//    public void finish() {
//        super.finish();
//        overridePendingTransition(R.anim.activity_slide_up, R.anim.activity_slide_down);
//    }

    protected void back() {
        onBackPressed();
    }

    protected void customActionBar(String title) {
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolBar);
//        toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_background_color));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        TextView titleTextView = (TextView) findViewById(R.id.list_title);
        if (titleTextView != null) {
            titleTextView.setText(title);
        }

        findViewById(R.id.list_backbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });

        toolbar.getBackground().setAlpha(0);
        final ImageView background = (ImageView) findViewById(R.id.background);
        final ImageView backButton = (ImageView) findViewById(R.id.list_backbutton);
        final ImageView line = (ImageView) findViewById(R.id.line);

        background.setImageResource(R.color.toolbar_background_color);
        backButton.setImageResource(R.drawable.backbutton_white_icon);
        line.setImageResource(R.color.divider_line_color);
        background.getDrawable().setAlpha(0);
        backButton.getDrawable().setAlpha(255);
        line.getDrawable().setAlpha(0);

        final float imgHeight = (int) getResources().getDimension(R.dimen.header_image_height);
        final float height = (int) getResources().getDimension(R.dimen.header_banner_height);
        final float ratio0 = 255 / getResources().getDimension(R.dimen.header_image_height);
        final float ratio1 = 255 / height;
        final float ratio2 = 255 / (getResources().getDimension(R.dimen.header_image_height) - getResources().getDimension(R.dimen.header_banner_height));

        listView.setOnScrollChanged(new OnScrollChanged() {
            @Override
            public void onScrollChanged(int left, int top, int oldLeft, int oldTop) {
                Log.i("GGG", "top=" + top + "|height=" + height + "|imgHeight=" + imgHeight + "headerHeight=" + header.getMeasuredHeight());
                top -= header.getMeasuredHeight();
                if (top >= 0) {
                    if (top < height) {
                        backButton.setImageResource(R.drawable.backbutton_white_icon);
                        int alpha = (int) ((height - top) * ratio1);
                        backButton.getDrawable().setAlpha(alpha);
                    } else if (top <= imgHeight) {
                        backButton.setImageResource(R.drawable.backbutton_icon);
                        int alpha = (int) ((top - height) * ratio2);
                        alpha = alpha < 255 ? alpha : 255;
                        backButton.getDrawable().setAlpha(alpha);
                    } else {
                        backButton.getDrawable().setAlpha(255);
                    }
                    if (top < imgHeight) {
                        int alpha = (int) (top * ratio0);
                        alpha = alpha < 240 ? alpha : 240;
                        background.getDrawable().setAlpha(alpha);
                        line.getDrawable().setAlpha(alpha);
                    } else {
                        background.getDrawable().setAlpha(240);
                        line.getDrawable().setAlpha(240);
                    }
                } else {
                    backButton.getDrawable().setAlpha(255);
                    background.getDrawable().setAlpha(0);
                    line.getDrawable().setAlpha(0);
                }
            }
        });
    }
}
