package com.ksider.mobile.android.activity.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.ksider.mobile.android.WebView.*;
import com.ksider.mobile.android.model.*;
import com.ksider.mobile.android.partion.ConsultView;
import com.ksider.mobile.android.partion.ShareView;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.view.LoadImageView;
import com.umeng.analytics.MobclickAgent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yong on 8/26/15.
 */
public class DetailProductFragmentNew extends Fragment {
    private View mRoot;
    private JSONObject response;

    protected BaseDataModel mParams;
    protected BasicCategory mCategory;
    protected String mEntity;
    protected JSONObject mProduct;
    protected boolean refundAble = false;
    private int productType = 0;
    protected ArrayList<ProductStockModel> stocks = new ArrayList<ProductStockModel>();
    private ProductStockModel selectedStock;

    private TrafficInfoModel mTrafficInfo;
    protected TravelTips mTips;

    private ConsultView consult;
    private int dataVersion;

    private JSONArray products;

    private String priceString = "";
    private String timeString = "";
    private boolean expired;
    private long validTime = 0;
    private boolean productExists;

    private String merchantId;

    @Override
    public void onResume() {
        super.onResume();
        if (consult != null) {
            consult.refreshConsult();
        }
        MobclickAgent.onPageStart(mEntity + "_detail"); //统计页面
        MobclickAgent.onResume(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().findViewById(R.id.detail_base_layout).setBackgroundColor(getResources().getColor(R.color.detail_divider_color));
        if (mRoot != null) {
            ViewGroup parent = (ViewGroup) mRoot.getParent();
            if (parent != null) {
                parent.removeView(mRoot);
                return mRoot;
            }
        }

        if (savedInstanceState == null) {
            Bundle args = getArguments();
            if (args != null) {
                try {
                    mEntity = args.getString("entity");
                    mCategory = (BasicCategory) args.getSerializable("category");
                    response = new JSONObject(args.getString("response"));
                    mCategory = mCategory == null ? BasicCategory.UNKOWN : mCategory;
                    dataVersion = args.getInt("dataVersion");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mParams = Utils.retrieveArgData(getActivity().getIntent());
        } else {
            try {
                mEntity = savedInstanceState.getString("entity");
                mCategory = (BasicCategory) savedInstanceState.getSerializable("category");
                response = new JSONObject(savedInstanceState.getString("response"));
                mCategory = mCategory == null ? BasicCategory.UNKOWN : mCategory;
                dataVersion = savedInstanceState.getInt("dataVersion");
                mParams = new BaseDataModel();
                mParams.id = savedInstanceState.getString("id");
                mParams.imgUrl = savedInstanceState.getString("imgUrl");
            } catch (JSONException js) {
                js.printStackTrace();
                return mRoot;
            }
        }

        mRoot = inflater.inflate(R.layout.product_detail_layout_new, container, false);

//        mRoot = inflater.inflate(R.layout.content_pickingpart_detail_product, container, false);
        render(response);
        MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_DETAIL);
        return mRoot;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("entity", mEntity);
        outState.putSerializable("category", mCategory);
        outState.putString("response", response.toString());
        outState.putInt("dataVersion", dataVersion);
        outState.putString("id", mParams.id);
        outState.putString("imgUrl", mParams.imgUrl);
    }

    /**
     * read data from json whether productExists or not
     */
    public void readData(JSONObject data) {
        DetailHeaderDataModel headerData = Parser.parse(data, getPairs());
        renderHeader(headerData);

        try {
            productExists = data.getBoolean("productExists");
        } catch (JSONException js) {
            productExists = false;
            js.printStackTrace();
        }

        try {
            expired = data.getBoolean("expired");
        } catch (JSONException js) {
            expired = false;
            js.printStackTrace();
        }
        if (mParams == null) {
            mParams = new BaseDataModel();
        }
        try {
            mParams.title = data.getString("name");
        } catch (JSONException e) {
            mParams.title = "";
            e.printStackTrace();
        }

        mTips = new TravelTips();
        try {
            JSONObject contact = data.getJSONObject("contact");
            mTips.contact_name = contact.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTrafficInfo = getTrafficInfo(data);

        try {
            JSONArray priceRange = data.getJSONArray("priceRange");
            if (priceRange.length() > 1) {
                priceString = StringUtils.getPriceRange(priceRange.getDouble(0), priceRange.getDouble(1));
            } else {
                priceString = "";
            }
        } catch (JSONException js) {
            priceString = "";
            js.printStackTrace();
        }

        if (productExists) {
            try {
                products = data.getJSONArray("product");
                getStockList(products);
                if (products != null && products.length() > 0) {
                    mProduct = products.getJSONObject(0);
                    readProduct(products.getJSONObject(0));
                    if (stocks.size() < 1) {
                        expired = true;
                    }
                } else {
                    productExists = false;
                }
                getTimeString();
            } catch (JSONException js) {
                js.printStackTrace();
            }
        } else {
            readRelativeInfo(data);
        }

        // set title of activity
        ((TextView) mRoot.findViewById(R.id.headline_title)).setText(mParams.title);
        //set expire icon
        if (productExists && expired) {
            mRoot.findViewById(R.id.expire_icon).setVisibility(View.VISIBLE);
        } else {
            mRoot.findViewById(R.id.expire_icon).setVisibility(View.GONE);
        }

    }

    /**
     * read product info from first product if productExists
     */
    public void readProduct(JSONObject product) {
        try {
            validTime = product.getLong("validTime");
        } catch (JSONException js) {
            validTime = 0;
            js.printStackTrace();
        }
        try {
            productType = product.getInt("productType");
        } catch (JSONException js) {
            js.printStackTrace();
        }
        try {
            if (product.getInt("refund") == 1) {
                refundAble = true;
            } else if (product.getInt("refund") == 2) {
                refundAble = false;
            } else {
                refundAble = false;
            }
        } catch (JSONException js) {
            js.printStackTrace();
        }
        try {
            mTips.contact_phone = product.getString("consultNumber");
        } catch (JSONException js) {
            mTips.contact_phone = "";
            js.printStackTrace();
        }
        try {
            merchantId = product.getString("merchantId");
        } catch (JSONException js) {
            merchantId = "";
            js.printStackTrace();
        }
    }

    public void readRelativeInfo(JSONObject data) {
        try {
            mTips.contact_phone = data.getJSONObject("contact").getString("phone");
        } catch (JSONException js) {
            mTips.contact_phone = "";
            js.printStackTrace();
        }
    }

    public void initPriceBar() {
        if (!productExists || priceString.equals("")) {
            getActivity().findViewById(R.id.price_toolbar).setVisibility(View.GONE);
        } else if (expired) {
            getActivity().findViewById(R.id.price_toolbar).setVisibility(View.VISIBLE);
            ((TextView) getActivity().findViewById(R.id.price)).setText(priceString);
            TextView participate = (TextView) getActivity().findViewById(R.id.participate);
            participate.setText(R.string.consult_participate);
            participate.setEnabled(false);
            participate.setAlpha(0.6f);
        } else {
            getActivity().findViewById(R.id.price_toolbar).setVisibility(View.VISIBLE);
            ((TextView) getActivity().findViewById(R.id.price)).setText(priceString);

            TextView participate = (TextView) getActivity().findViewById(R.id.participate);
            participate.setText(R.string.consult_participate);
            participate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (stocks.size() < 1) {
                        Toast.makeText(getActivity(), "购买时间已截止，请下次购买！", Toast.LENGTH_LONG);
                    } else if (productType == Status.PRODUCT_TYPE_ALWAYS && stocks.size() == 1) {
                        try {
                            ProductStockModel productStockModel = stocks.get(0);
                            Intent intent = new Intent(getActivity(), PurchaseAcitvity.class);
                            intent.putExtra("order", true);
                            JSONObject mProduct = getSelectedProduct(productStockModel.getProductId());
                            JSONObject product = new JSONObject();
                            product.put("productType", mProduct.getInt("productType"));
                            product.put("poiType", mProduct.getInt("poiType"));
                            product.put("refund", mProduct.getInt("refund"));
                            product.put("quantityPerUser", mProduct.getInt("quantityPerUser"));
                            product.put("productName", productStockModel.getProductName());
                            product.put("productId", productStockModel.getProductId());
                            product.put("startTime", productStockModel.getStartTime());
                            product.put("sellPrice", productStockModel.getSellPrice());
                            product.put("quantity", productStockModel.getQuantity());
                            intent.putExtra("product", product.toString());
                            startActivity(intent);
                        } catch (JSONException js) {
                            js.printStackTrace();
                        }
                    } else {
                        Intent intent = new Intent(getActivity(), PurchaseAcitvity.class);
                        intent.putExtra("product", products.toString());
                        intent.putParcelableArrayListExtra("stocks", stocks);
                        startActivity(intent);
                    }
                }
            });
        }
    }

    public void renderMarkView(View view) {
        TextView refundStatus = (TextView) view.findViewById(R.id.refund_status);
        Drawable drawable;
        if (refundAble) {

            drawable = getResources().getDrawable(R.drawable.refund_icon);

        } else {
            drawable = getResources().getDrawable(R.drawable.unrefund_icon);
        }
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        refundStatus.setCompoundDrawables(null, drawable, null, null);

        view.findViewById(R.id.consult_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mTips.contact_phone));
                getActivity().startActivity(phoneIntent);
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("productId", mParams.id == null ? "" : mParams.id);
                if (productExists) {
                    map.put("hasProduct", "true");
                } else {
                    map.put("hasProduct", "false");
                }
                switch (mCategory) {
                    case ATTRACTIONS:
                        MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_TAG_SCENE, map);
                        break;
                    case RESORT:
                    case FARMYARD:
                        MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_TAG_RESORT, map);
                        break;
                    case PICKINGPART:
                        MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_TAG_PICK, map);
                        break;
                    case ACTIVITY:
                        MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_TAG_EVENT, map);
                        break;
                    case GUIDE:
                        MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_TAG_RECOMMEND, map);
                        break;
                    default:
                        break;
                }
                MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_CONSULT);
            }
        });
    }

    public void renderSellerInfo(final View view) {
//        ((TextView) view.findViewById(R.id.title)).setText(R.string.merchant_info);
        ((TextView) view.findViewById(R.id.merchant)).setText(mTips.contact_name);
        if (!expired) {
            ((TextView) view.findViewById(R.id.date)).setText(timeString);
        }
        ((TextView) view.findViewById(R.id.distance)).setText(mTrafficInfo.distance);
        ((TextView) view.findViewById(R.id.location)).setText(mTrafficInfo.address);

        ImageRequest imageRequest = new ImageRequest(
                getGaodeMapUrl(),
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        ((ImageView) view.findViewById(R.id.location_map)).setImageBitmap(response);
                    }
                }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError paramVolleyError) {

            }
        });
        Network.getInstance().addToRequestQueue(imageRequest, "LoadImageView");
        view.findViewById(R.id.to_location_info).setOnClickListener(sellerInfoListener);
        view.findViewById(R.id.to_merchant_info).setOnClickListener(sellerInfoListener);
        view.findViewById(R.id.to_date_info).setOnClickListener(sellerInfoListener);
    }

    public String getGaodeMapUrl() {
        String url = "http://restapi.amap.com/v3/staticmap?";
        url = url + "markers=mid,0xFF0000,A:" + mTrafficInfo.lng + "," + mTrafficInfo.lat + "&";
        url = url + "size=800*200&zoom=17&key=a22c6eabfbafd37bfbadc7d8303a94c1";
        return url;
    }

    private View.OnClickListener sellerInfoListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!productExists) {
                return;
            }
            switch (view.getId()) {
                case R.id.to_location_info:
                    if (mTrafficInfo != null) {
                        Intent trafficIntent = new Intent(getActivity().getApplicationContext(), TrafficMapActivity.class);
                        trafficIntent.putExtra("data", mTrafficInfo);
                        getActivity().startActivity(trafficIntent);
                    }
                    break;
                case R.id.to_merchant_info:
                    if (productExists && merchantId != null && !merchantId.equals("")) {
                        Intent merchantIntent = new Intent(getActivity(), MoreMerchantInfoActivity.class);
                        merchantIntent.putExtra("brief", merchantId);
                        getActivity().startActivity(merchantIntent);
                    }
                    break;
                case R.id.to_date_info:
                    String productString = products.toString();
                    if (productExists && stocks.size() > 0 && productString != null && !productString.equals("") && !expired) {
                        Intent purchaseIntent = new Intent(getActivity(), PurchaseAcitvity.class);
                        purchaseIntent.putExtra("product", productString);
                        purchaseIntent.putParcelableArrayListExtra("stocks", stocks);
                        getActivity().startActivity(purchaseIntent);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void renderBuyTips(View view, JSONObject data) {
        ((TextView) view.findViewById(R.id.title)).setText("购买须知");
        ((TextView) view.findViewById(R.id.more)).setText("更多购买须知");

        try {
            final JSONArray array = data.getJSONArray("purchaseNote");
            String value = "";
            ArrayList<Integer> list = new ArrayList<Integer>();
            TextView content = (TextView) view.findViewById(R.id.part_content);
            for (int i = 0; i < array.length(); i++) {
                if (i != 0) {
                    value += "\n";
                }
                try {
                    JSONObject object = array.getJSONObject(i);
                    String type = object.getString("type");
                    if (type.equals("text")) {
                        value += object.getString("value");
                    } else if (type.equals("title1")) {
                        list.add(value.length());
                        value += getResources().getString(R.string.large_dot) + object.getString("value");
                    }

                } catch (JSONException js) {
                    js.printStackTrace();
                    continue;
                }
            }
            SpannableStringBuilder builder = new SpannableStringBuilder(value);
            for (int i = 0; i < list.size(); i++) {
                ForegroundColorSpan blueColor = new ForegroundColorSpan(getResources().getColor(R.color.personal_info_text_color_selected));
                builder.setSpan(blueColor, list.get(i), list.get(i) + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
            content.setText(builder);

            view.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), MoreDetailInfoActivity.class);
                    intent.putExtra("type", MoreDetailInfoActivity.DETAIL_PURCHASE_NOTE);
                    intent.putExtra("brief", array.toString());
                    getActivity().startActivity(intent);
                }
            });
        } catch (JSONException js) {
            view.setVisibility(View.GONE);
            js.printStackTrace();
        }

    }

    public void renderPackageDetail(View view, JSONObject data) {
        ((TextView) view.findViewById(R.id.title)).setText("套餐详情");
        ((TextView) view.findViewById(R.id.more)).setText("更多套餐详情");

        try {
            final JSONArray array = data.getJSONArray("feeDesc");
            String value = "";
            ArrayList<Integer> list = new ArrayList<Integer>();
            TextView content = (TextView) view.findViewById(R.id.part_content);
            for (int i = 0; i < array.length(); i++) {
                if (i != 0) {
                    value += "\n";
                }
                try {
                    JSONObject object = array.getJSONObject(i);
                    String type = object.getString("type");
                    if (type.equals("text")) {
                        value += object.getString("value");
                    } else if (type.equals("title1")) {
                        list.add(value.length());
                        value += getResources().getString(R.string.large_dot) + object.getString("value");
                    }

                } catch (JSONException js) {
                    js.printStackTrace();
                    continue;
                }
            }
            SpannableStringBuilder builder = new SpannableStringBuilder(value);
            for (int i = 0; i < list.size(); i++) {
                ForegroundColorSpan blueColor = new ForegroundColorSpan(getResources().getColor(R.color.personal_info_text_color_selected));
                builder.setSpan(blueColor, list.get(i), list.get(i) + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
            content.setText(builder);

            view.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), MoreDetailInfoActivity.class);
                    intent.putExtra("type", MoreDetailInfoActivity.DETAIL_FEE_DESC);
                    intent.putExtra("brief", array.toString());
                    getActivity().startActivity(intent);
                }
            });
        } catch (JSONException js) {
            view.setVisibility(View.GONE);
            js.printStackTrace();
        }
    }

    public void renderWebView(JSONObject data) {
//        ((TextView) mRoot.findViewById(R.id.layout_web_title)).setText("产品详情");
        try {
            WebView content = (WebView) (ViewGroup) mRoot.findViewById(R.id.layout_web_content);
//            content.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            content.getSettings().setJavaScriptEnabled(true);
            content.loadData(HtmlWraper.getHtmlDoc(data.getString("content")), "text/html; charset=UTF-8", null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void render(JSONObject data) {
        readData(data);

        if (getActivity().findViewById(R.id.price_toolbar) != null) {
            initPriceBar();
        }

        renderMarkView(mRoot.findViewById(R.id.part_mark_view));
        renderSellerInfo(mRoot.findViewById(R.id.part_seller_info));
        renderBuyTips(mRoot.findViewById(R.id.part_buy_tips), data);
        renderPackageDetail(mRoot.findViewById(R.id.part_package_detail), data);
        renderWebView(data);

        ShareView share = (ShareView) mRoot.findViewById(R.id.part_share_view);
        share.hideTitle(true);
        if (share != null) {
            share.setTitle(R.string.share);
            share.setValues(getDetailTitle(), mCategory, mParams.id, mParams.imgUrl);
        }

        consult = (ConsultView) mRoot.findViewById(R.id.part_consult_view);
        if (consult != null) {
            if (!expired && stocks.size() > 0) {
                consult.setValues(mParams.id, mCategory, productExists, products.toString());
            } else {
                consult.setValues(mParams.id, mCategory);
            }
        }
    }

    public JSONObject getSelectedProduct(long productId) {
        for (int i = 0; i < products.length(); i++) {
            try {
                if (productId == products.getJSONObject(i).getInt("productId")) {
                    return products.getJSONObject(i);
                }
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
        return null;
    }

    public void addView(ViewGroup container, JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject object = array.getJSONObject(i);
                String type = object.getString("type");
                if (type.equals("text")) {
                    TextView text = (TextView) getActivity().getLayoutInflater().inflate(R.layout.activity_detail_text, null);
                    text.setText(object.getString("value"));
                    container.addView(text);
                } else if (type.equals("title1")) {
                    TextView title1 = (TextView) getActivity().getLayoutInflater().inflate(R.layout.activity_detail_title1, null);
                    title1.setText(object.getString("value"));
                    container.addView(title1);
                }
            } catch (JSONException js) {
                js.printStackTrace();
                continue;
            }
        }
    }

    protected Map<String, String> getPairs() {
        Map<String, String> pairs = new HashMap<String, String>();
        pairs.put("imgUrl", "headImg");
        pairs.put("title", "name");
        pairs.put("collection", "fav");
        pairs.put("hasFavorator", "isFav");
        return pairs;
    }

    protected String caculateDistance(JSONArray coords) {
        try {
            if (coords != null && coords.length() > 0) {
                Double lng = coords.getDouble(0);
                Double lat = coords.getDouble(1);
                double distance = Maths.getSelfDistance(lat, lng);
                String dis = "";
                if (distance >= 100) {
                    dis += Math.round(distance);
                } else {
                    NumberFormat formatter = new DecimalFormat("#0.00");
                    dis += formatter.format(distance);
                }
                dis += "km";
                return dis;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected TrafficInfoModel getTrafficInfo(JSONObject data) {
        TrafficInfoModel info = new TrafficInfoModel();
        try {
            JSONObject address = data.getJSONObject("address");
            info.address = address.getString("detail");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            info.name = data.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONObject ti = data.getJSONObject("trafficInfo");
            info.carLines = ti.getString("carLines");
            info.busLines = ti.getString("busLines");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONArray lngLatitude = data.getJSONArray("lngLatitude");
            if (lngLatitude.length() == 2) {
                info.lng = lngLatitude.getDouble(0);
                info.lat = lngLatitude.getDouble(1);
            }
            info.distance = caculateDistance(data.getJSONArray("lngLatitude"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        info.phone = mTips.contact_phone;
        return info;
    }

    public void getStockList(JSONArray productArray) {
        stocks.clear();
        long currentMills = System.currentTimeMillis();

        for (int i = 0; i < productArray.length(); i++) {
            try {
                JSONObject productObject = productArray.getJSONObject(i);
                String productName = productObject.getString("productName");
                productType = productObject.getInt("productType");
                JSONArray stockList = productObject.getJSONArray("stockList");
                for (int j = 0; j < stockList.length(); j++) {
                    try {
                        JSONObject stockObject = stockList.getJSONObject(j);

                        long startTime = stockObject.getLong("startTime");
                        if (productType == Status.PRODUCT_TYPE_TIME && startTime < currentMills) {
                            continue;
                        }

                        ProductStockModel stock = new ProductStockModel();
                        stock.setStartTime(startTime);
                        stock.setSellPrice(stockObject.getDouble("sellPrice"));
                        stock.setQuantity(stockObject.getLong("quantity"));
                        stock.setMarketPrice(stockObject.getDouble("marketPrice"));
                        stock.setProductId(stockObject.getLong("productId"));
                        stock.setStockId(stockObject.getLong("stockId"));
                        stock.setProductName(productName);
                        stocks.add(stock);
                    } catch (JSONException js) {
                        js.printStackTrace();
                    }
                }
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
        Collections.sort(stocks, ProductStockModel.getComparator(BaseComparator.ASC_SORT));
    }

    public void getTimeString() {
        timeString = "";
        if (productType == Status.PRODUCT_TYPE_ALWAYS) {
            String time = DateUtils.getFormatDate(validTime);
            timeString = time.equals("") ? "" : getResources().getString(R.string.valid_time_to_date, DateUtils.getFormatDate(validTime));
            return;
        }
        int count = 0;
        long firstMills = DateUtils.getFirstMilSeconds(System.currentTimeMillis());
        for (int i = 0; i < stocks.size(); i++) {
            if (count >= 3) {
                timeString += " ...";
                break;
            }
            if (stocks.get(i).getStartTime() >= firstMills) {
                timeString += DateUtils.getFormatMonthDay(stocks.get(i).getStartTime()) + "，";
                firstMills = DateUtils.getFirstMilSeconds(stocks.get(i).getStartTime() + DateUtils.DAY_MILLIS);
                count++;
            }
        }
        if (timeString.endsWith("，")) {
            timeString = timeString.substring(0, timeString.length() - 1);
        }

    }

    protected String getDetailTitle() {
        if (mParams != null) {
            return mParams.title;
        }
        return "";
    }

    protected void renderHeader(DetailHeaderDataModel detail) {
        LoadImageView loadImage = (LoadImageView) getActivity().findViewById(R.id.header_image);
        loadImage.setImageResource(detail.imgUrl);
        if (detail.hasFavorator != null) {
            if (!detail.hasFavorator) {
                View view = (View) getActivity().findViewById(R.id.collect);
                view.setVisibility(View.VISIBLE);
                view = (View) getActivity().findViewById(R.id.collected);
                view.setVisibility(View.INVISIBLE);
            } else {
                View view = (View) getActivity().findViewById(R.id.collect);
                view.setVisibility(View.INVISIBLE);
                view = (View) getActivity().findViewById(R.id.collected);
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    public static class TravelTips implements Serializable {
        private static final long serialVersionUID = 244096542057318253L;
        public String tips;
        public String contact_phone;
        public String contact_name;
    }
}