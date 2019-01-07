package com.ksider.mobile.android.activity.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.MoreDetailInfoActivity;
import com.ksider.mobile.android.WebView.PurchaseAcitvity;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.*;
import com.ksider.mobile.android.partion.*;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.view.LoadImageView;
import com.umeng.analytics.MobclickAgent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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
public class DetailProductFragment extends Fragment {
    private View mRoot;
    private JSONObject response;

    protected BaseDataModel mParams;
    protected BasicCategory mCategory;
    protected String mEntity;
    protected JSONObject mProduct;
    protected boolean refundAble = false;
    //    protected double price = -1;
    private int productType = 0;
    protected ArrayList<ProductStockModel> stocks = new ArrayList<ProductStockModel>();
    private ProductStockModel selectedStock;

    private TrafficInfoModel mTrafficInfo;
    protected TravelTips mTips;

    private ConsultView consult;
    private int dataVersion;

    private JSONArray products;
//    //min and max price of all stocks, include expired product
//    private double minAllPrice = Double.MAX_VALUE;
//    private double maxAllPrice = -1;
//    //min and max price of all stocks, include unexpired product only
//    private double minPrice = Double.MAX_VALUE;
//    private double maxPrice = -1;

//    private String price = "";
    private String priceString = "";
    private String timeString = "";
    private boolean expired;
    private long validTime = 0;
    private boolean productExists;

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

        mRoot = inflater.inflate(R.layout.content_pickingpart_detail_product, container, false);
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
                priceString = StringUtils.getPriceRange(priceRange.getDouble(0),priceRange.getDouble(1));
            } else {
                priceString="";
            }
        } catch (JSONException js) {
            priceString="";
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
//        getPrice();
        if (!productExists || priceString.equals("")) {
            getActivity().findViewById(R.id.price_toolbar).setVisibility(View.GONE);
        } else if (expired) {
            getActivity().findViewById(R.id.price_toolbar).setVisibility(View.VISIBLE);
            ((TextView) getActivity().findViewById(R.id.price)).setText(priceString);
            TextView participate = (TextView) getActivity().findViewById(R.id.participate);
            participate.setText(R.string.consult_participate);
            participate.setEnabled(false);
            participate.setAlpha(0.6f);

//            TextView participate = (TextView) getActivity().findViewById(R.id.participate);
//            participate.setText(R.string.consulting);
//            participate.setBackgroundColor(getResources().getColor(R.color.gray_1));
//            participate.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mTips.contact_phone));
//                    startActivity(phoneIntent);
//                    HashMap<String, String> map = new HashMap<String, String>();
//                    map.put("hasProduct", productExists ? "true" : "false");
//                    map.put("productId", mParams.id == null ? "" : mParams.id);
//                    switch (mCategory) {
//                        case ATTRACTIONS:
//                            MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_TAG_SCENE, map);
//                            break;
//                        case RESORT:
//                        case FARMYARD:
//                            MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_TAG_RESORT, map);
//                            break;
//                        case PICKINGPART:
//                            MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_TAG_PICK, map);
//                            break;
//                        case ACTIVITY:
//                            MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_TAG_EVENT, map);
//                            break;
//                        case GUIDE:
//                            MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_TAG_RECOMMEND, map);
//                            break;
//                        default:
//                            break;
//                    }
//                }
//            });
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

    public void render(JSONObject data) {
        readData(data);

        if (getActivity().findViewById(R.id.price_toolbar) != null) {
            initPriceBar();
        }

        //init markview
        MarkView markView = (MarkView) mRoot.findViewById(R.id.part_mark_view);
        markView.setHasProduct(productExists);
        markView.setRefundAble(refundAble);
        markView.setValues(mParams.id, mCategory, mTips.contact_phone);
        //init merchant info
        SellerInfoView merchant = (SellerInfoView) mRoot.findViewById(R.id.part_merchant_view);
        merchant.setTitle(R.string.merchant_info);
        merchant.setMerchant(mTips.contact_name);
        merchant.setTrafficInfo(mTrafficInfo);
        merchant.setDate(expired, timeString);
        merchant.setStocks(stocks);
        merchant.setProductExists(productExists);
        if (productExists) {
            merchant.setProductString(products.toString());
            try {
                merchant.setMoreMerchantInfo(data.getJSONArray("product").getJSONObject(0).getString("merchantId"));
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
        try {
            merchant.setDistance(caculateDistance(data.getJSONArray("lngLatitude")));
        } catch (JSONException js) {
            js.printStackTrace();
        }
        try {
            merchant.setLocation(data.getJSONObject("address").getString("detail"));
        } catch (JSONException js) {
            js.printStackTrace();
        }
        merchant.setValues();

        MarkedDetailView buyTips = (MarkedDetailView) mRoot.findViewById(R.id.buy_tips);
        buyTips.setTitle("购买须知");
        try {
            buyTips.setContent(data.getJSONArray("purchaseNote"));
        } catch (JSONException js) {
            buyTips.setVisibility(View.GONE);
            js.printStackTrace();
        }
        buyTips.setType(MoreDetailInfoActivity.DETAIL_PURCHASE_NOTE);
        buyTips.setMoreText("更多购买须知");
        buyTips.setMoreClickEvent();

        MarkedDetailView packageDetail = (MarkedDetailView) mRoot.findViewById(R.id.package_detail);
        packageDetail.setTitle("套餐详情");
        try {
            packageDetail.setContent(data.getJSONArray("feeDesc"));
        } catch (JSONException js) {
            packageDetail.setVisibility(View.GONE);
            js.printStackTrace();
        }
        packageDetail.setType(MoreDetailInfoActivity.DETAIL_FEE_DESC);
        packageDetail.setMoreText("更多套餐详情");
        packageDetail.setMoreClickEvent();
        //get webView
        ((TextView) mRoot.findViewById(R.id.layout_web_title)).setText("产品详情");
        try {
            WebView content = (WebView) (ViewGroup) mRoot.findViewById(R.id.layout_web_content);
            content.getSettings().setJavaScriptEnabled(true);
            content.loadData(HtmlWraper.getHtmlDoc(data.getString("content")), "text/html; charset=UTF-8", null);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ShareView share = (ShareView) mRoot.findViewById(R.id.part_share_view);
        if (share != null) {
            share.setTitle(R.string.share);
            share.setValues(getDetailTitle(), mCategory, mParams.id, mParams.imgUrl);
        }
        //init consult
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

    protected AtlasModel getAtlasModel(String key, JSONObject data) {
        AtlasModel model = new AtlasModel();
        try {
            JSONObject item = data.getJSONObject(key);
            model.setDesc(item.getString("desc"));

            JSONArray imgs = item.getJSONArray("imgs");
            for (int i = 0; i < imgs.length(); i++) {
                JSONObject img = imgs.getJSONObject(i);
                model.addImg(img.getString("url"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return model;
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
//        long firstMills = DateUtils.getFirstMilSeconds(System.currentTimeMillis());
        long currentMills = System.currentTimeMillis();

        for (int i = 0; i < productArray.length(); i++) {
            try {
                JSONObject productObject = productArray.getJSONObject(i);
                String productName = productObject.getString("productName");
                productType = productObject.getInt("productType");
                JSONArray stockList = productObject.getJSONArray("stockList");
                for (int j = 0; j < stockList.length(); j++) {
//                    //check
//                    try {
//                        JSONObject temp = stockList.getJSONObject(j);
//                        Log.v("AAA", "origin->date=" + DateUtils.getFormatMonthDay(temp.getLong("startTime")) + "|sellPrice=" + StringUtils.getPrice(temp.getDouble("sellPrice")));
//                    } catch (JSONException js) {
//                        js.printStackTrace();
//                    }

                    try {
                        JSONObject stockObject = stockList.getJSONObject(j);
//                        double stockPrice = stockObject.getDouble("sellPrice");
//                        minAllPrice = Math.min(minPrice, stockPrice);
//                        maxAllPrice = Math.max(maxPrice, stockPrice);

                        long startTime = stockObject.getLong("startTime");
                        if (productType == Status.PRODUCT_TYPE_TIME && startTime < currentMills) {
                            continue;
                        }

//                        minPrice = Math.min(minPrice, stockPrice);
//                        maxPrice = Math.max(maxPrice, stockPrice);

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
//        //check
//        for (int i = 0; i < stocks.size(); i++) {
//            Log.v("AAA", "sorted->date=" + DateUtils.getFormatMonthDay(stocks.get(i).getStartTime()) + "|sellPrice=" + StringUtils.getPrice(stocks.get(i).getSellPrice()));
//        }
    }

//    public void getPrice() {
//        if (maxPrice > minPrice) {
//            if (expired) {
//                price = getResources().getString(R.string.toolbar_price, StringUtils.getPrice(minAllPrice) + "-" + StringUtils.getPrice(maxAllPrice));
//            } else {
//                price = getResources().getString(R.string.toolbar_price, StringUtils.getPrice(minPrice) + "-" + StringUtils.getPrice(maxPrice));
//            }
//        } else if (maxPrice == minPrice) {
//            if (maxPrice < 0.01) {
//                price = "免费";
//            } else {
//                price = getResources().getString(R.string.toolbar_price, StringUtils.getPrice(minPrice));
//            }
//        } else {
//            price = "";
//        }
//    }

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
//    public void getDefaultStock(JSONObject product) {
//        stocks.clear();
//        try {
//            JSONArray stockList = product.getJSONArray("stockList");
//            for (int i = 0; i < stockList.length(); i++) {
//                try {
//                    JSONObject stockObject = stockList.getJSONObject(i);
//                    ProductStockModel stock = new ProductStockModel();
//                    stock.setStartTime(stockObject.getLong("startTime"));
//                    stock.setSellPrice(stockObject.getDouble("sellPrice"));
//                    stock.setQuantity(stockObject.getLong("quantity"));
//                    stock.setPurchasePrice(stockObject.getDouble("purchasePrice"));
//                    stocks.add(stock);
//                } catch (JSONException js) {
//                    js.printStackTrace();
//                }
//            }
//            Collections.sort(stocks, ProductStockModel.getComparator(BaseComparator.ASC_SORT));
//        } catch (JSONException js) {
//            js.printStackTrace();
//        }
//
//        long currentTime = System.currentTimeMillis();
//        for (int i = 0; i < stocks.size(); i++) {
//            if (stocks.get(i).getStartTime() >= currentTime || productType == Status.PRODUCT_TYPE_ALWAYS) {
//                if (selectedStock == null) {
//                    selectedStock = new ProductStockModel();
//                }
//                selectedStock.setStartTime(stocks.get(i).getStartTime());
//                selectedStock.setQuantity(stocks.get(i).getQuantity());
//                selectedStock.setSellPrice(stocks.get(i).getSellPrice());
//                return;
//            }
//        }
//    }

    protected String getDetailTitle() {
        if (mParams != null) {
            return mParams.title;
        }
        return "";
    }

    public Bitmap getHeaderImage() {
        LoadImageView image = (LoadImageView) getActivity().findViewById(R.id.header_image);
        Bitmap bit = null;
        if (image != null) {
            image.setDrawingCacheEnabled(true);
            image.buildDrawingCache(true);
            bit = image.getDrawingCache(true);
            if (bit != null) {
                if (bit.getWidth() > 0 && bit.getHeight() > 0) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bit.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                    byte[] b = baos.toByteArray();
                    bit = BitmapFactory.decodeByteArray(b, 0, b.length);
                    if (b.length >= 32768) {
                        float scale = (float) (32768.0 / (b.length + 1));
                        Matrix matrix = new Matrix();
                        matrix.postScale(scale, scale);
                        bit = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
                    }
                } else {
                    bit = null;
                }
            }
        }
        return bit;
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