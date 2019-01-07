package com.ksider.mobile.android.personal;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.*;
import com.ksider.mobile.android.activity.fragment.OnScrollChanged;
import com.ksider.mobile.android.model.BaseComparator;
import com.ksider.mobile.android.model.ConsultModel;
import com.ksider.mobile.android.model.ProductStockModel;
import com.ksider.mobile.android.scrollListView.OverScrollPagingListView;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.ksider.mobile.android.utils.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.*;

/**
 * Created by yong on 2015/6/23.
 */
public class ConsultListActivity extends BaseActivity {
    protected OverScrollPagingListView mListView;
    protected ConsultListAdaptor mAdaptor;
    protected int mPage = -1;
    private String poiId = "";
    private String poiType = "";
    private boolean hasMore = true;
    private int inputIntent = 1;
    private String parentId;
    //    private String product;
    private boolean hasProduct;
    private static final int OPEN_INPUT_CONSULT = 300;

    private int productType = 0;
    protected ArrayList<ProductStockModel> stocks = new ArrayList<ProductStockModel>();
    private JSONArray products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consult_detail);
        new SlidingLayout(this);
        hasProduct = getIntent().getBooleanExtra("hasProduct", false);
        poiId = getIntent().getStringExtra("poiId");
        poiType = getIntent().getStringExtra("poiType");
        if (hasProduct) {
            customActionBar("咨询详情");
//            product = getIntent().getStringExtra("product");
            try {
                products = new JSONArray(getIntent().getStringExtra("products"));
                getStockList(products);
            } catch (JSONException js) {
                js.printStackTrace();
            }
        } else {
            customActionBar("评论详情");
            findViewById(R.id.participate).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.write)).setText(getResources().getString(R.string.consult_write_comment));
        }

        findViewById(R.id.write).setOnClickListener(consultListener);
        findViewById(R.id.participate).setOnClickListener(consultListener);
        findViewById(R.id.cancel).setOnClickListener(consultListener);
        findViewById(R.id.confirm).setOnClickListener(consultListener);

        mListView = (OverScrollPagingListView) findViewById(R.id.content_list);
        mAdaptor = new ConsultListAdaptor(this);
        mListView.setAdapter(mAdaptor);
        mListView.setPagingableListener(new OverScrollPagingListView.Pagingable() {
            @Override
            public void onLoadMoreItems() {
                mPage++;
                if (hasMore) {
                    Network.getInstance().addToRequestQueue(getConsultList());
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
        refresh();
    }

    /**
     * show the loading view before getting response
     */
    public void initLoadingView() {
        findViewById(R.id.consult_toolbar).setVisibility(View.INVISIBLE);
        mListView.deleteFooterView();
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
        findViewById(R.id.consult_toolbar).setVisibility(View.VISIBLE);
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
        ((TextView) findViewById(R.id.no_consult_info)).setText(R.string.net_acc_failed);
    }

    public void getStockList(JSONArray productArray) {
        stocks.clear();
        long firstMills = DateUtils.getFirstMilSeconds(System.currentTimeMillis());

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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPage = -1;
    }

    protected void refresh() {
        initLoadingView();
        mPage = 0;
        mAdaptor.removeAllItems();
        Network.getInstance().addToRequestQueue(getConsultList());
    }

    protected String getConsultListUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "list");
        params.put("page", mPage);
        params.put("step", Constants.PAGING_STEP);
        params.put("poiId", poiId);
        return APIUtils.getUrl(APIUtils.NEW_COMMENT, params);
    }

    protected JsonObjectRequest getConsultList() {
        String url = getConsultListUrl();
        if (url.equals("")) {
            return null;
        }
        Log.i("AAA", "getConsultList->url=" + url);
        return new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        setResponseView();
                        processConsult(response.getJSONArray("data"));
                    } else {
                        setEmptyView();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.v("AAA", "getConsultList failed!");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setErrorView();
            }
        });
    }

    public ArrayList<ConsultModel> getConsultList(JSONArray consultArray) {
        ArrayList<ConsultModel> consults = new ArrayList<ConsultModel>();
        for (int i = 0; i < consultArray.length(); i++) {

            ConsultModel consult = new ConsultModel();
            try {
                JSONObject object = consultArray.getJSONObject(i);
                consult.setId(object.getString("_id"));
                consult.setContent(object.getString("content"));
                consult.setCreateTime(object.getLong("createTime"));
                consult.setParent(object.getString("parent"));
                consult.setPoiId(object.getString("poiId"));
                consult.setPoiType(object.getString("poiType"));
                consult.setThreadId(object.getString("threadId"));
                consult.setUserId(object.getString("userId"));
                consult.setUserName(object.getString("userName"));
                try {
                    consult.setRole(object.getString("role"));
                } catch (JSONException js) {
                    js.printStackTrace();
                    consult.setRole("");
                }
                try {
                    consult.setAvatar(object.getString("avatar"));
                } catch (JSONException js) {
                    consult.setAvatar("");
                    js.printStackTrace();
                }
                try {
                    consult.setThumbsUp(object.getInt("thumbsUp"));
                } catch (JSONException js) {
                    consult.setThumbsUp(0);
                    js.printStackTrace();
                }
            } catch (JSONException js) {
                js.printStackTrace();
            }
            consults.add(consult);
        }
        return consults;
    }

    public void getParentName(List<ConsultModel> consults) {
        for (ConsultModel consult : consults) {
            for (ConsultModel mode : consults) {
                if (consult.getParent().equals(mode.getId())) {
                    consult.setParentName(mode.getUserName());
                    break;
                }
            }
        }
    }

    public ConsultModel changeToConsultObject(List<ConsultModel> consults) {
        if (consults.size() < 1) {
            return null;
        }
        ConsultModel consult;
        if (consults.size() == 1) {
            consult = consults.get(0);
            consult.setShowNum(0);
            return consult;
        }
        consult = new ConsultModel();
        int size = consults.size();
        for (int i = 0; i < size; i++) {
            if (consults.get(i).getParent().equals("")) {
                consult.copyValues(consults.get(i));
            } else {
                consult.addReply(consults.get(i));
            }
        }
        consult.setShowNum(size - 1 < ConsultModel.DEFAULT_PRESHOW_REPLY_NUM ? size - 1 : ConsultModel.DEFAULT_PRESHOW_REPLY_NUM);
        consult.setOpt(false);
        return consult;
    }

    /**
     * get Consult results
     */
    public void processConsult(JSONArray array) {
        if (array.length() == 0) {
            setEmptyView();
            return;
        }
        List<ConsultModel> consultList = new ArrayList<ConsultModel>();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject consultObject = array.getJSONObject(i);
                JSONArray consultArray = consultObject.getJSONArray("comments");
                List<ConsultModel> consults = getConsultList(consultArray);
                Collections.sort(consults, ConsultModel.getComparator(BaseComparator.ASC_SORT));
                getParentName(consults);
                ConsultModel consult = changeToConsultObject(consults);
                consultList.add(consult);
            } catch (JSONException js) {
                js.printStackTrace();
            }
        }
//        Collections.sort(consultList, new ConsultComparator(ConsultComparator.DESC_SORT));
        if (consultList.size() < Constants.PAGING_STEP) {
            hasMore = false;
        } else {
            hasMore = true;
        }
        mListView.onFinishLoading(hasMore, consultList);
    }

    private View.OnClickListener consultListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.write:
                    openInput();
                    break;
                case R.id.participate:
                    if (stocks.size() < 1) {
                        Toast.makeText(ConsultListActivity.this, "购买时间已截止，请下次购买！", Toast.LENGTH_LONG).show();
                    } else if (productType == Status.PRODUCT_TYPE_ALWAYS && stocks.size() == 1) {
                        try {
                            ProductStockModel productStockModel = stocks.get(0);
                            Intent intent = new Intent(ConsultListActivity.this, PurchaseAcitvity.class);
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
                        Intent intent = new Intent(ConsultListActivity.this, PurchaseAcitvity.class);
                        intent.putExtra("product", products.toString());
                        intent.putParcelableArrayListExtra("stocks", stocks);
                        startActivity(intent);
                    }
                    break;
                default:
                    break;
            }
        }
    };

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

    /**
     * open input text area to start a new thread
     */
    public void openInput() {
        if (hasProduct) {
            openInput(R.string.consult_consult_title, getResources().getString(R.string.consult_consult_hint));
        } else {
            openInput(R.string.consult_comment_title, getResources().getString(R.string.consult_comment_hint));
        }
        inputIntent = ConsultModel.PROPOSE_QUESTION;
    }

    /**
     * open input text area to start a new thread
     */
    public void openInput(String hint) {
        final CommentDialog.Builder builder = new CommentDialog.Builder(ConsultListActivity.this);
        builder.setPositiveButton("", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String content = builder.getInputContent();
                try {
                    content = URLEncoder.encode(content, "utf-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (content != null && !content.equals("")) {
                    if (!UserInfo.isLogin()) {
                        Intent toLogin = new Intent(ConsultListActivity.this, LoginActivity.class);
                        startActivity(toLogin);
                    } else if (inputIntent == ConsultModel.PROPOSE_QUESTION) {
                        Network.getInstance().addToRequestQueue(getRequest(ConsultModel.ADD_CONSULT, content));
                    } else if (inputIntent == ConsultModel.REPLY_QUESTION) {
                        Network.getInstance().addToRequestQueue(getRequest(ConsultModel.ADD_REPLY, content));
                    }
                    inputIntent = ConsultModel.PROPOSE_QUESTION;
                }
            }
        }).setNegativeButton("", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).setHint(hint).show();
    }

    /**
     * open input text area with hint to start a new thread
     *
     * @param hint show what the user intents to do
     */
    public void openInput(int title, String hint) {
        final CommentDialog.Builder builder = new CommentDialog.Builder(ConsultListActivity.this);
        builder.setTitle(title).setPositiveButton("", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String content = builder.getInputContent();
                try {
                    content = URLEncoder.encode(content, "utf-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (content != null && !content.equals("")) {
                    if (!UserInfo.isLogin()) {
                        Intent toLogin = new Intent(ConsultListActivity.this, LoginActivity.class);
                        startActivity(toLogin);
                    } else if (inputIntent == ConsultModel.PROPOSE_QUESTION) {
                        Network.getInstance().addToRequestQueue(getRequest(ConsultModel.ADD_CONSULT, content));
                    } else if (inputIntent == ConsultModel.REPLY_QUESTION) {
                        Network.getInstance().addToRequestQueue(getRequest(ConsultModel.ADD_REPLY, content));
                    }
                    inputIntent = ConsultModel.PROPOSE_QUESTION;
                }
            }
        }).setNegativeButton("", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).setHint(hint).show();
    }

    /**
     * open reply input area in comment situation
     *
     * @param parentName name of whom the user intents to reply
     * @param parentId   id of comment the user intents to reply
     */
    public void openInput(String parentName, String parentId) {
        if (hasProduct) {
            openInput(R.string.consult_consult_title, getResources().getString(R.string.consult_reply_hint, parentName));
        } else {
            openInput(R.string.consult_comment_title, getResources().getString(R.string.consult_reply_hint, parentName));
        }

        this.parentId = parentId;
        inputIntent = ConsultModel.REPLY_QUESTION;
    }

    /**
     * open replay input area when the thread is started by the user in consult situation
     *
     * @param parentName name of whom the user intents to reply
     * @param parentId   id of consult the user intents to reply
     * @param genId      id of whom start the thread
     */
    public void openInput(String parentName, String parentId, String genId) {
        if (!UserInfo.getUserId().equals(genId) && hasProduct) {
            return;
        }
        openInput(parentName, parentId);
    }

    protected String getUrl(int step, String content) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "add");
        params.put("content", content);
        switch (step) {
            case ConsultModel.ADD_CONSULT:
                params.put("poiType", poiType);
                params.put("poiId", poiId);
                break;
            case ConsultModel.ADD_REPLY:
                params.put("parent", parentId);
                break;
            default:
                break;
        }
        return APIUtils.getUrl(APIUtils.NEW_COMMENT, params);
    }

    protected JsonObjectRequest getRequest(int step, String content) {
        String url = getUrl(step, content);
        if (url.equals("")) {
            return null;
        }
        Log.v("AAA", "add consult->url=" + url);
        return new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        refresh();
                    } else {
                        Toast.makeText(ConsultListActivity.this, response.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("AAA", "request error!");
            }
        });
    }

    protected String getOptUrl(String commentId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "thumbsUp");
        params.put("commentId", commentId);
        params.put("uuid", new DeviceUuidFactory(this).getDeviceUuid());
        return APIUtils.getUrl(APIUtils.NEW_COMMENT, params);
    }

    protected JsonObjectRequest getOptRequest(String commentId) {
        String url = getOptUrl(commentId);
        if (url.equals("")) {
            return null;
        }
        return new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("AAA", "request error!");
            }
        });
    }

    public void addOpt(String commentId) {
        Network.getInstance().addToRequestQueue(getOptRequest(commentId));
    }
}
