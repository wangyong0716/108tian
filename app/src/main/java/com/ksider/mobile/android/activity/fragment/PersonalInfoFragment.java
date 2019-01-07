package com.ksider.mobile.android.activity.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.WebView.EditProfileActivity;
import com.ksider.mobile.android.WebView.LoginActivity;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.WebView.SettingActivity;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.personal.*;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.view.CircularImageView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yong on 2015/5/16.
 */
public class PersonalInfoFragment extends BaseFragment {
    private static final int STORAGE = 0;
    private static final int CONSUME = 1;
    private static final int GET_UNREAD_REPLY_COUNT = 2;
    protected View mRoot;

    private MyHandler myHandler;
    private final int BEGIN = 60000;
    private final int REPEAT = 60000;
    private int unReadNum = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MessageUtils.eventBus.register(this);

        myHandler = new MyHandler(getActivity());
        TimerTask timerTask = new TimerTask() {
            public void run() {
                Message message = new Message();
                message.what = 1;
                myHandler.sendMessage(message);
            }
        };
        Timer timer = new Timer();
//        timer.schedule(timerTask, BEGIN, REPEAT);
        timer.schedule(timerTask, BEGIN);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MessageUtils.eventBus.unregister(this);
    }

    public void onEventMainThread(MessageEvent event) {
        switch (event.getType()) {
            case MessageEvent.NOTIFY_COLLECTED_CHANGE:
                refresh();
                break;
            case MessageEvent.NOTIFY_PERSONAL_INFO_CHANGE:
                refreshPersonalInfo();
                break;
        }
    }

    private View.OnClickListener personalListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            if (v.getId() == R.id.personal_setting) {
                intent.setClass(getActivity(), SettingActivity.class);
                getActivity().startActivity(intent);
                return;
            }
            if (!UserInfo.isLogin()) {
                intent.setClass(getActivity(), LoginActivity.class);
                getActivity().startActivity(intent);
                return;
            }
            switch (v.getId()) {
                case R.id.personal_message:
                    intent.setClass(getActivity(), MessageListActivity.class);
                    intent.putExtra("unread", unReadNum >= 0 ? unReadNum : 0);
                    getActivity().startActivity(intent);
                    break;
                case R.id.personal_storage:
                    intent.setClass(getActivity(), StorageActivity.class);
                    getActivity().startActivity(intent);
                    break;
                case R.id.view_personal_consume_code:
                    intent.setClass(getActivity(), ConsumeCodeListActivity.class);
                    getActivity().startActivity(intent);
                    break;
                case R.id.view_personal_unpay:
                    intent.setClass(getActivity(), OrderActivity.class);
                    intent.putExtra("index", 0);
                    getActivity().startActivity(intent);
                    break;
                case R.id.view_personal_consumed:
                    intent.setClass(getActivity(), OrderActivity.class);
                    intent.putExtra("index", 3);
                    getActivity().startActivity(intent);
                    break;
                case R.id.view_personal_unconsume:
                    intent.setClass(getActivity(), OrderActivity.class);
                    intent.putExtra("index", 1);
                    getActivity().startActivity(intent);
                    break;
                case R.id.personal_order:
                    intent.setClass(getActivity(), OrderActivity.class);
                    intent.putExtra("index", 0);
                    getActivity().startActivity(intent);
                    break;
                case R.id.personal_coupon:
                    intent.setClass(getActivity(), CouponActivity.class);
                    getActivity().startActivity(intent);
                    break;
                case R.id.personal_score:
                    intent.setClass(getActivity(), ScoreListActivity.class);
                    getActivity().startActivity(intent);
                    break;
//                case R.id.personal_setting:
//                    intent.setClass(getActivity(), SettingActivity.class);
//                    getActivity().startActivity(intent);
//                    break;
                case R.id.avatar:
                    intent.setClass(getActivity(), EditProfileActivity.class);
                    getActivity().startActivity(intent);
                    break;
//                case R.id.personal_login_button:
//                    intent.setClass(getActivity(), LoginActivity.class);
//                    getActivity().startActivity(intent);
//                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.personal_info, container, false);
        mRoot.findViewById(R.id.personal_message).setOnClickListener(personalListener);
        mRoot.findViewById(R.id.personal_storage).setOnClickListener(personalListener);
        mRoot.findViewById(R.id.view_personal_consume_code).setOnClickListener(personalListener);
        mRoot.findViewById(R.id.view_personal_unpay).setOnClickListener(personalListener);
        mRoot.findViewById(R.id.view_personal_consumed).setOnClickListener(personalListener);
        mRoot.findViewById(R.id.view_personal_unconsume).setOnClickListener(personalListener);
        mRoot.findViewById(R.id.personal_order).setOnClickListener(personalListener);
        mRoot.findViewById(R.id.personal_coupon).setOnClickListener(personalListener);
        mRoot.findViewById(R.id.personal_score).setOnClickListener(personalListener);
        mRoot.findViewById(R.id.personal_setting).setOnClickListener(personalListener);
        mRoot.findViewById(R.id.avatar).setOnClickListener(personalListener);

        refreshPersonalInfo();
        refresh();
        return mRoot;
    }

    protected void refreshPersonalInfo() {
        String userInfo = Storage.getSharedPref().getString(Storage.USER_INFO, "");
        if (userInfo == null || userInfo.equals("")) {
            ((TextView) mRoot.findViewById(R.id.username)).setText(R.string.default_name);
            ((CircularImageView) mRoot.findViewById(R.id.avatar)).setImageResource(R.drawable.default_avatar_1);
            mRoot.findViewById(R.id.avatar).setOnClickListener(personalListener);
            mRoot.findViewById(R.id.sex_icon).setVisibility(View.GONE);
            return;
        }
        try {
            JSONObject data = new JSONObject(userInfo);
            renderAvatar(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void renderAvatar(JSONObject data) {
        try {
            ((TextView) mRoot.findViewById(R.id.username)).setText(data.getString("name"));
        } catch (JSONException e) {
            ((TextView) mRoot.findViewById(R.id.username)).setText("");
            e.printStackTrace();
        }
        try {
            ((CircularImageView) mRoot.findViewById(R.id.avatar)).setImageResource(ImageUtils.formatImageUrl(data.getString("figureurl") + "?t=" + System.currentTimeMillis(), ImageUtils.THUMBNAIL));
        } catch (JSONException js) {
            ((CircularImageView) mRoot.findViewById(R.id.avatar)).setImageResource(R.drawable.default_avatar_1);
            js.printStackTrace();
        }
        try {
            String sex = data.getString("gender");
            if (sex.equals("男")) {
                mRoot.findViewById(R.id.sex_icon).setVisibility(View.VISIBLE);
                ((ImageView) mRoot.findViewById(R.id.sex_icon)).setImageResource(R.drawable.boy_icon);
            } else if (sex.equals("女")) {
                mRoot.findViewById(R.id.sex_icon).setVisibility(View.VISIBLE);
                ((ImageView) mRoot.findViewById(R.id.sex_icon)).setImageResource(R.drawable.girl_icon);
            } else {
                mRoot.findViewById(R.id.sex_icon).setVisibility(View.GONE);
            }
        } catch (JSONException js) {
            mRoot.findViewById(R.id.sex_icon).setVisibility(View.GONE);
            js.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void refresh() {
//        Network.getInstance().addToRequestQueue(getRequest(STORAGE, getUrl(STORAGE)));
//        Network.getInstance().addToRequestQueue(getRequest(CONSUME, getUrl(CONSUME)));
        Network.getInstance().addToRequestQueue(getRequest(GET_UNREAD_REPLY_COUNT, getUnreadReplyCountUrl()));
    }

    protected String getUrl(int choice) {
        String action = "";
        if (choice == STORAGE) {
            action = "getFavCount";
        } else if (choice == CONSUME) {
            action = "getCodesCount";
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", action);
        return APIUtils.getUserCenter(params);
    }

    protected JsonObjectRequest getRequest(final int choice, String url) {
        Log.i("AAA", "PersonalInfoFragment->url=" + url);
        return new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        if (choice == STORAGE) {
//                            ((TextView) mRoot.findViewById(R.id.storage_num)).setText(response.getInt("data") + "");
                        } else if (choice == CONSUME) {
//                            ((TextView) mRoot.findViewById(R.id.consume_num)).setText(response.getInt("data") + "");
                        } else if (choice == GET_UNREAD_REPLY_COUNT) {
                            unReadNum = response.getInt("data");
                            if (unReadNum <= 0) {
                                mRoot.findViewById(R.id.new_reply_tag).setVisibility(View.INVISIBLE);
                            } else {
                                mRoot.findViewById(R.id.new_reply_tag).setVisibility(View.VISIBLE);
//                                ((TextView) mRoot.findViewById(R.id.message_count)).setText(unReadNum + "");
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }

    public String getUnreadReplyCountUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", "getUnreadReplayCount");
        return APIUtils.getUrl(APIUtils.USER_CENTER, params);
    }

    class MyHandler extends Handler {
        private Context context;

        public MyHandler(Context context) {
            this.context = context;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Network.getInstance().addToRequestQueue(getRequest(GET_UNREAD_REPLY_COUNT, getUnreadReplyCountUrl()));
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }
}


