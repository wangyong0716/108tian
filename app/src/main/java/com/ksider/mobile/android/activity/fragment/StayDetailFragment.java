package com.ksider.mobile.android.activity.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.Constants;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yong on 8/26/15.
 */
public class StayDetailFragment extends Fragment {
    private View mRoot;
    private JSONObject response;

    protected BaseDataModel mParams;
    protected BasicCategory mCategory;
    protected String mEntity;
    protected boolean hasProduct = false;
    protected JSONObject mProduct;
    protected boolean refundAble = false;
    protected String phone = "";
    protected double price = -1;
    private int productType = 0;
    protected ArrayList<ProductStockModel> stocks = new ArrayList<ProductStockModel>();
    private ProductStockModel selectedStock;

    private ConsultView consult;

    private JSONObject mFee;
    protected CostDetail mDetail;
    protected TravelTips mTips;
    protected TrafficInfoModel mTrafficInfo;

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
                    response = new JSONObject(args.getString("response"));
                    mEntity = args.getString("entity");
                    mCategory = (BasicCategory) args.getSerializable("category");
                    mCategory = mCategory == null ? BasicCategory.UNKOWN : mCategory;
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
                mParams = new BaseDataModel();
                mParams.id = savedInstanceState.getString("id");
                mParams.imgUrl = savedInstanceState.getString("imgUrl");
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }

        mRoot = inflater.inflate(R.layout.content_stay_detail, container, false);
        render(response);
        return mRoot;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("entity", mEntity);
        outState.putSerializable("category", mCategory);
        outState.putString("response", response.toString());
        outState.putString("id", mParams.id);
        outState.putString("imgUrl", mParams.imgUrl);
    }

    public void render(JSONObject data) {
        AtlasModel brief = getAtlasModel("brief", data);
        AtlasModel accommodation = getAtlasModel("accommodation", data);
        AtlasModel catering = getAtlasModel("catering", data);
        AtlasModel entertainment = getAtlasModel("entertainment", data);

        DetailHeaderDataModel headerData = Parser.parse(data, getPairs());
        if (headerData.imgUrl == null || headerData.imgUrl.equals("")) {
            if (brief.getImgsCount() > 0) {
                headerData.imgUrl = brief.getImgs().get(0);
            } else if (accommodation.getImgsCount() > 0) {
                headerData.imgUrl = accommodation.getImgs().get(0);
            } else if (catering.getImgsCount() > 0) {
                headerData.imgUrl = catering.getImgs().get(0);
            } else if (entertainment.getImgsCount() > 0) {
                headerData.imgUrl = entertainment.getImgs().get(0);
            }
        }
        renderHeader(headerData);

        try {
            JSONArray product = data.getJSONArray("product");
            if (product != null && product.length() > 0) {
                mProduct = product.getJSONObject(0);
//                hasProduct = true;
                productType = mProduct.getInt("productType");
                getDefaultStock(mProduct);
                try {
                    if (mProduct.getInt("refund") == 1) {
                        refundAble = true;
                    } else if (mProduct.getInt("refund") == 2) {
                        refundAble = false;
                    } else {
                        refundAble = false;
                    }
                } catch (JSONException js) {
                    js.printStackTrace();
                }
                try {
                    phone = mProduct.getString("consultNumber");
                } catch (JSONException js) {
                    phone = "";
                    js.printStackTrace();
                }
                if (selectedStock != null) {
                    price = selectedStock.getSellPrice();
                } else {
                    try {
                        price = mProduct.getDouble("sellPrice");
                    } catch (JSONException js) {
                        js.printStackTrace();
                        price = -1;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (phone == null || phone.equals("")) {
            try {
                phone = data.getJSONObject("contact").getString("phone");
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
        if (price < 0) {
            try {
                price = data.getDouble("price");
            } catch (JSONException js) {
                price = -1;
                js.printStackTrace();
            }
        }
        try {
            mParams.title = data.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (getActivity().findViewById(R.id.price_toolbar) != null) {
            if (price < 0) {
                getActivity().findViewById(R.id.price_toolbar).setVisibility(View.GONE);
            } else {
                getActivity().findViewById(R.id.price_toolbar).setVisibility(View.VISIBLE);
                if (price < 0.01) {
                    ((TextView) getActivity().findViewById(R.id.price)).setText("免费");
                } else {
                    ((TextView) getActivity().findViewById(R.id.price)).setText(getResources().getString(R.string.toolbar_price, price));
                }
                TextView participate = (TextView) getActivity().findViewById(R.id.participate);
//                if (hasProduct) {
//                    participate.setText(R.string.consult_participate);
//                } else {
                participate.setText(R.string.consulting);
//                }
                participate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        if (hasProduct) {
//                            Intent intent = new Intent(getActivity(), PurchaseAcitvity.class);
//                            if (intent != null && mProduct.length() > 0) {
//                                intent.putExtra("product", mProduct.toString());
//                                startActivity(intent);
//                            }
//                        } else {
                        Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                        startActivity(phoneIntent);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("hasProduct", "false");
                        map.put("productId", mParams.id == null ? "" : mParams.id);
                        MobclickAgent.onEvent(getActivity(), Constants.UMENG_STATISTICS_TAG_RESORT, map);
//                        }
                    }
                });
            }
        }
//////////////////////////////////////////////////////////////////////////////////////////////
        mTrafficInfo = getTrafficInfo(data);

        //init new activity info
        // get title of activity
        try {
            String title = data.getString("name");
            ((TextView) mRoot.findViewById(R.id.headline_title)).setText(title);
        } catch (JSONException js) {
            js.printStackTrace();
        }

        //init merchant info
        MerchantInfoView merchant = (MerchantInfoView) mRoot.findViewById(R.id.part_merchant_view);
        merchant.setTitle(R.string.merchant_info);
        merchant.setMoreText(R.string.more_merchant_detail);
        merchant.setCategory(mCategory);
        merchant.setHasProduct(hasProduct);
        merchant.setRefundAble(refundAble);
        merchant.setTrafficInfo(mTrafficInfo);
        merchant.setProductId(mParams.id);
        try {
            merchant.setDistance(caculateDistance(data.getJSONArray("lngLatitude")));
        } catch (JSONException js) {
            js.printStackTrace();
        }
//        if (hasProduct) {
//            try {
//                merchant.setMoreMerchantInfo(data.getJSONArray("product").getJSONObject(0).getString("merchantId"));
//            } catch (JSONException js) {
//                js.printStackTrace();
//            }
//        }
        try {
            SimpleDateFormat date = new SimpleDateFormat("MM月dd日 HH:mm");
            merchant.setDate(date.format(data.getLong("startTime")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            merchant.setLocation(data.getJSONObject("address").getString("detail"));
        } catch (JSONException js) {
            js.printStackTrace();
        }
        merchant.setValues();

        FacilitiesView fv = (FacilitiesView) mRoot.findViewById(R.id.part_facilities_view);
        fv.setTitle("便利设施");
        fv.setHasProduct(hasProduct);

        AtlasView briefAtlas = (AtlasView) mRoot.findViewById(R.id.part_brief_view);
        briefAtlas.setTitle("住宿及周边");
        if (brief != null && brief.getImgsCount() > 0) {
            briefAtlas.addAtlas(brief);
            if (accommodation != null && accommodation.getImgsCount() > 0) {
                briefAtlas.addAtlas(accommodation);
            }
        } else {
            if (accommodation != null && accommodation.getImgsCount() > 0) {
                briefAtlas.addAtlas(accommodation);
            } else {
                briefAtlas.setVisibility(View.GONE);
            }
        }

        AtlasView cateringAtlas = (AtlasView) mRoot.findViewById(R.id.part_catering_view);
        cateringAtlas.setTitle("好吃的");
        if (catering != null && catering.getImgsCount() > 0) {
            cateringAtlas.addAtlas(catering);
        } else {
            cateringAtlas.setVisibility(View.GONE);
        }

        AtlasView entertainmentAtlas = (AtlasView) mRoot.findViewById(R.id.part_entertainment_view);
        entertainmentAtlas.setTitle("好玩的");
        if (entertainment != null && entertainment.getImgsCount() > 0) {
            entertainmentAtlas.addAtlas(entertainment);
        } else {
            entertainmentAtlas.setVisibility(View.GONE);
        }

        RecommendView rv = (RecommendView) mRoot.findViewById(R.id.part_recommend_view);
        rv.setTitle("周边推荐");
        rv.setValues(mParams.title, mTrafficInfo);
        /////////////////////////////////////
        //init share
        ShareView share = (ShareView) mRoot.findViewById(R.id.part_share_view);
        if (share != null) {
            share.setTitle(R.string.share);
            share.setValues(getDetailTitle(), mCategory, mParams.id, mParams.imgUrl);
        }
        //init consult
        consult = (ConsultView) mRoot.findViewById(R.id.part_consult_view);
        if (consult != null) {
            consult.setValues(mParams.id, mCategory);
        }
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
            return model;
        }
        return model;
    }

    public void getDefaultStock(JSONObject product) {
        stocks.clear();
        try {
            JSONArray stockList = product.getJSONArray("stockList");
            for (int i = 0; i < stockList.length(); i++) {
                try {
                    JSONObject stockObject = stockList.getJSONObject(i);
                    ProductStockModel stock = new ProductStockModel();
                    stock.setStartTime(stockObject.getLong("startTime"));
                    stock.setSellPrice(stockObject.getDouble("sellPrice"));
                    stock.setQuantity(stockObject.getLong("quantity"));
                    stock.setPurchasePrice(stockObject.getDouble("purchasePrice"));
                    stocks.add(stock);
                } catch (JSONException js) {
                    js.printStackTrace();
                }
            }
            Collections.sort(stocks, ProductStockModel.getComparator(BaseComparator.ASC_SORT));
        } catch (JSONException js) {
            js.printStackTrace();
        }

        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < stocks.size(); i++) {
            if (stocks.get(i).getStartTime() >= currentTime || productType == Status.PRODUCT_TYPE_ALWAYS) {
                if (selectedStock == null) {
                    selectedStock = new ProductStockModel();
                }
                selectedStock.setStartTime(stocks.get(i).getStartTime());
                selectedStock.setQuantity(stocks.get(i).getQuantity());
                selectedStock.setSellPrice(stocks.get(i).getSellPrice());
                return;
            }
        }
    }

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
//        if (detail.collection == null) {
//            detail.collection = "0";
//        }
//        setTextView(R.id.collection_count, detail.collection);
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

    protected Map<String, String> getPairs() {
        Map<String, String> pairs = new HashMap<String, String>();
        pairs.put("imgUrl", "headImg");
        pairs.put("title", "name");
        pairs.put("collection", "fav");
        pairs.put("hasFavorator", "isFav");
        return pairs;
    }

    protected TrafficInfoModel getTrafficInfo(JSONObject data) {
        TrafficInfoModel info = new TrafficInfoModel();
        try {
            info.address = data.getString("dest");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            info.name = data.getString("name");
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
        info.phone = phone;
        return info;
    }

    public static class CostDetail implements Serializable {
        private static final long serialVersionUID = -5847574871133506209L;
        public String detail;
        public String mode;
    }

    public static class TravelTips implements Serializable {
        private static final long serialVersionUID = 244096542057318253L;
        public String tips;
        public String contact_phone;
        public String contact_name;
    }
}