package com.ksider.mobile.android.partion;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.LoginActivity;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.BaseComparator;
import com.ksider.mobile.android.model.ConsultModel;
import com.ksider.mobile.android.personal.ConsultListActivity;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.view.CircularImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.*;

/**
 * Created by yong on 2015/6/2.
 */
public class ConsultView extends LinearLayout {
    private Context context;
    private String id;
    private BasicCategory mCategory;
    private int inputIntent = 1;
    private String parentId;
    protected boolean hasProduct = false;
    //    protected JSONObject mProduct;
    private String products;

    public ConsultView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ConsultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public ConsultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.partion_consult_view, this);
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.title)).setText(title);
    }

    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
    }

    public void setTitle() {
        if (hasProduct) {
            setTitle(R.string.consulting);
            setConsultText(R.string.consult_consult_title);
        } else {
            setTitle(R.string.comment);
            setConsultText(R.string.consult_comment_title);
        }
    }

    public void setConsultText(String consultText) {
        ((TextView) findViewById(R.id.consult)).setText(consultText);
    }

    public void setConsultText(int consultTextId) {
        setConsultText(getResources().getString(consultTextId));
    }

    public void setValues(String id, BasicCategory category) {
        this.id = id;
        this.mCategory = category;
        setTitle();
        findViewById(R.id.consult).setOnClickListener(listener);
        findViewById(R.id.more).setOnClickListener(listener);
//        refreshConsult();
    }

    public void setValues(String id, BasicCategory category, boolean hasProduct, String product) {
        this.products = product;
        this.hasProduct = hasProduct;
        this.setValues(id, category);
    }

    /**
     * open input text area to start a new comment/consult thread
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
     * open input text area to start a new comment/consult thread
     */
    public void openInput(String hint) {
        final CommentDialog.Builder builder = new CommentDialog.Builder(context);
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
                        Intent toLogin = new Intent(context, LoginActivity.class);
                        context.startActivity(toLogin);
                    } else if (inputIntent == ConsultModel.PROPOSE_QUESTION) {
                        Network.getInstance().addToRequestQueue(getRequest(ConsultModel.ADD_CONSULT, getUrl(ConsultModel.ADD_CONSULT, content)));
                    } else if (inputIntent == ConsultModel.REPLY_QUESTION) {
                        Network.getInstance().addToRequestQueue(getRequest(ConsultModel.ADD_REPLY, getUrl(ConsultModel.ADD_REPLY, content)));
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
     * open input text area with hint to start a new comment/consult thread
     *
     * @param hint show what the user intents to do
     */
    public void openInput(int title, String hint) {
        final CommentDialog.Builder builder = new CommentDialog.Builder(context);
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
                        Intent toLogin = new Intent(context, LoginActivity.class);
                        context.startActivity(toLogin);
                    } else if (inputIntent == ConsultModel.PROPOSE_QUESTION) {
                        Network.getInstance().addToRequestQueue(getRequest(ConsultModel.ADD_CONSULT, getUrl(ConsultModel.ADD_CONSULT, content)));
                    } else if (inputIntent == ConsultModel.REPLY_QUESTION) {
                        Network.getInstance().addToRequestQueue(getRequest(ConsultModel.ADD_REPLY, getUrl(ConsultModel.ADD_REPLY, content)));
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

    private OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.more:
                    Log.v("AAA", "hasProduct=" + hasProduct);
                    Intent consultIntent = new Intent(context, ConsultListActivity.class);
                    consultIntent.putExtra("poiId", id);
                    consultIntent.putExtra("poiType", getPoiType());
                    consultIntent.putExtra("hasProduct", hasProduct);
                    if (hasProduct) {
                        consultIntent.putExtra("products", products);
                    }
                    context.startActivity(consultIntent);
                    break;
                case R.id.consult:
                    openInput();
                    break;
                default:
                    break;
            }
        }
    };

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

    /**
     * get Consult results
     */
    public void processConsult(JSONArray array) {
        List<ConsultModel> consultList = new ArrayList<ConsultModel>();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject consultObject = array.getJSONObject(i);
//                int replyCount = consultObject.getInt("number");
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
        LinearLayout consultContainer = (LinearLayout) findViewById(R.id.consult_container);
        consultContainer.removeAllViews();
        for (int i = 0; i < consultList.size() && i < ConsultModel.DEFAULT_PRESHOW_CONSULT_NUM; i++) {
            addConsult(consultContainer, consultList.get(i));
        }
    }

    /**
     * add consult to view
     */
    public void addConsult(ViewGroup container, final ConsultModel consult) {
        if (consult == null) {
            return;
        }
        final View view = ((Activity) context).getLayoutInflater().inflate(R.layout.list_view_consult_item, null);
        CircularImageView avatar = (CircularImageView) view.findViewById(R.id.avatar);
        if (!consult.getAvatar().equals("")) {
            avatar.setImageResource(ImageUtils.formatImageUrl(
                    consult.getAvatar(), ImageUtils.THUMBNAIL));
        } else {
            avatar.setImageResource(R.drawable.default_avatar);
        }
        ((TextView) view.findViewById(R.id.user_name)).setText(consult.getUserName());
        ((TextView) view.findViewById(R.id.create_time)).setText(DateUtils.getDefaultDurationByNow(consult.getCreateTime()));
        ((TextView) view.findViewById(R.id.content)).setText(consult.getContent());
        TextView optCount = (TextView) view.findViewById(R.id.opt_count);
        optCount.setText(consult.getThumbsUp() + "");
        TextView replyCount = (TextView) view.findViewById(R.id.reply_count);
        replyCount.setText(consult.getReplies().size() + "");
        replyCount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout replyContainer = (LinearLayout) view.findViewById(R.id.reply_container);
                if (ConsultModel.DEFAULT_PRESHOW_REPLY_NUM >= consult.getShowNum()) {
                    if (consult.getShowNum() <= 0 && consult.getReplies().size() > 0) {
                        replyContainer.setVisibility(View.VISIBLE);
                    }
                    for (int i = replyContainer.getChildCount(); i < consult.getReplies().size(); i++) {
                        addReply(replyContainer, consult.getReplies().get(i), consult.getUserId());
                    }
                    consult.setShowNum(consult.getReplies().size());
                } else {
                    int min = Math.min(ConsultModel.DEFAULT_PRESHOW_REPLY_NUM, consult.getShowNum());
                    for (int i = replyContainer.getChildCount() - 1; i >= min; i--) {
                        replyContainer.removeViewAt(i);
                    }
                    if (min <= 0) {
                        replyContainer.setVisibility(View.GONE);
                    }
                    consult.setShowNum(min);
                }
            }
        });
        optCount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView opt_count = (TextView) view.findViewById(R.id.opt_count);
                if (!consult.isOpt()) {
                    consult.setThumbsUp(consult.getThumbsUp() + 1);
                    Network.getInstance().addToRequestQueue(getOptRequest(consult.getId()));
                    consult.setOpt(true);
                }
                opt_count.setText(consult.getThumbsUp() + "");
            }
        });
        LinearLayout question = (LinearLayout) view.findViewById(R.id.consult_question);
        question.setTag(consult);
        question.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ConsultModel model = (ConsultModel) v.getTag();
                openInput(model.getUserName(), model.getId(), model.getUserId());
            }
        });
        if (consult.getShowNum() < 1) {
            container.addView(view);
            return;
        }
        LinearLayout replyContainer = (LinearLayout) view.findViewById(R.id.reply_container);
        replyContainer.setVisibility(View.VISIBLE);
        for (int i = 0; i < consult.getShowNum(); i++) {
            addReply(replyContainer, consult.getReplies().get(i), consult.getUserId());
        }
        container.addView(view);
    }

    public void diffReplyColor(TextView textView, String parentName, String content) {
        String preFix = "@ " + parentName + "： ";
        SpannableStringBuilder builder = new SpannableStringBuilder(preFix + content);

        ForegroundColorSpan mainColor = new ForegroundColorSpan(getResources().getColor(R.color.black_0));
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan((int) getResources().getDimension(R.dimen.consult_input_reply_parent_name));
        builder.setSpan(mainColor, 0, preFix.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(absoluteSizeSpan, 0, preFix.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(builder);
    }

    /**
     * add reply to consult
     */
    public void addReply(ViewGroup container, ConsultModel consult, final String genId) {
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.list_view_consult_item_item, null);
        CircularImageView avatar = (CircularImageView) view.findViewById(R.id.avatar);
        if (!consult.getAvatar().equals("")) {
            avatar.setImageResource(ImageUtils.formatImageUrl(
                    consult.getAvatar(), ImageUtils.THUMBNAIL));
        } else {
            avatar.setImageResource(R.drawable.default_avatar);
        }
        ((TextView) view.findViewById(R.id.user_name)).setText(consult.getUserName());
        ((TextView) view.findViewById(R.id.create_time)).setText(DateUtils.getDefaultDurationByNow(consult.getCreateTime()));

        diffReplyColor((TextView) view.findViewById(R.id.content), consult.getParentName(), consult.getContent());
        view.setTag(consult);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ConsultModel model = (ConsultModel) v.getTag();
                openInput(model.getUserName(), model.getId(), genId);
            }
        });
        container.addView(view);
    }

    public String getPoiType() {
        switch (mCategory) {
            case ATTRACTIONS:
                return "scene";
            case FARMYARD:
                return "farm";
            case PICKINGPART:
                return "pick";
            case RESORT:
                return "resort";
            case ACTIVITY:
                return "event";
            case GUIDE:
                return "weekly";
            default:
                return "";
        }
    }

    public void refreshConsult() {
        Network.getInstance().addToRequestQueue(getRequest(ConsultModel.GET_THREAD_COUNT, getUrl(ConsultModel.GET_THREAD_COUNT)));
        Network.getInstance().addToRequestQueue(getRequest(ConsultModel.GET_CONSULT_LIST, getUrl(ConsultModel.GET_CONSULT_LIST)));
    }

    protected String getUrl(int step) {
        Map<String, Object> params = new HashMap<String, Object>();
        switch (step) {
            case ConsultModel.GET_THREAD_COUNT:
                params.put("action", "threadCount");
                params.put("poiId", id);
                break;
            case ConsultModel.GET_CONSULT_LIST:
                params.put("action", "getHotThread");
                params.put("poiId", id);
                break;
            default:
                break;
        }
        return APIUtils.getUrl(APIUtils.NEW_COMMENT, params);
    }

    protected String getUrl(int step, String content) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "add");
        params.put("content", content);
        switch (step) {
            case ConsultModel.ADD_CONSULT:
                params.put("poiType", getPoiType());
                params.put("poiId", id);
                break;
            case ConsultModel.ADD_REPLY:
                params.put("parent", parentId);
                break;
            default:
                break;
        }
        return APIUtils.getUrl(APIUtils.NEW_COMMENT, params);
    }


    protected JsonObjectRequest getRequest(final int step, String url) {
        if (url.equals("")) {
            return null;
        }
        Log.i("AAA","add consult->url="+url);
        return new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        switch (step) {
                            case ConsultModel.GET_THREAD_COUNT:
                                int threadCount = response.getInt("data");
                                if (threadCount > ConsultModel.DEFAULT_PRESHOW_CONSULT_NUM) {
                                    findViewById(R.id.more).setVisibility(View.VISIBLE);
                                    findViewById(R.id.consult_container).setVisibility(VISIBLE);
                                    findViewById(R.id.no_consult).setVisibility(GONE);
                                } else if (threadCount > 0) {
                                    findViewById(R.id.more).setVisibility(View.INVISIBLE);
                                    findViewById(R.id.consult_container).setVisibility(VISIBLE);
                                    findViewById(R.id.no_consult).setVisibility(GONE);
                                } else {
                                    findViewById(R.id.consult_container).setVisibility(GONE);
                                    findViewById(R.id.no_consult).setVisibility(VISIBLE);
                                }
                                break;
                            case ConsultModel.GET_CONSULT_LIST:
                                processConsult(response.getJSONArray("data"));
                                break;
                            case ConsultModel.ADD_CONSULT:
                            case ConsultModel.ADD_REPLY:
                                refreshConsult();
                                break;
                            default:
                                break;
                        }
                    } else {
                        if (step == ConsultModel.ADD_CONSULT || step == ConsultModel.ADD_REPLY) {
                            Toast.makeText(context, response.getString("msg"), Toast.LENGTH_LONG).show();
                        }
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
        params.put("uuid", new DeviceUuidFactory(context).getDeviceUuid());
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
}
