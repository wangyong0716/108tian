package com.ksider.mobile.android.WebView;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.SyncListener;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.Reply;
import com.umeng.fb.model.UserInfo;
import com.umeng.message.PushAgent;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by yong on 2015/6/18.
 */
public class CheckFeedBackActivity extends BaseActivity {
    private FeedbackAgent agent;
    private UserInfo info;
    private final int INPUT_MESSAGE = 0;
    private final int INPUT_PHONE = 1;
    private final int INPUT_QQ = 2;
    private final int INPUT_EMAIL = 3;
    private final int INPUT_PLAIN = 4;
    private int currentState = INPUT_MESSAGE;
    private ConversationListAdapter mConversationListAdapter;
    private ListView listView;

    public CheckFeedBackActivity() {
    }

    @TargetApi(11)
    @SuppressLint({"NewApi"})
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (Build.VERSION.SDK_INT >= 11 && this.getActionBar() != null) {
            this.getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //////
        setContentView(R.layout.check_feedback_layout);
        new SlidingLayout(this);
        customActionBar("联系信息");
        agent = new FeedbackAgent(this);
        agent.sync();
        agent.openFeedbackPush();
        PushAgent.getInstance(this).enable();
        agent.setWelcomeInfo("Welcome to use umeng feedback app");
        info = agent.getUserInfo();
        getContactInfo();
        findViewById(R.id.send_message_button).setOnClickListener(listener);
        findViewById(R.id.phone_display).setOnClickListener(listener);
        findViewById(R.id.qq_display).setOnClickListener(listener);
        findViewById(R.id.email_display).setOnClickListener(listener);
        findViewById(R.id.plain_display).setOnClickListener(listener);
        ((ListView) findViewById(R.id.reply_list)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                changeToMessage();
            }
        });
        ((ListView) findViewById(R.id.reply_list)).setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                changeToMessage();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        mConversationListAdapter = new ConversationListAdapter(CheckFeedBackActivity.this);
        listView = (ListView) findViewById(R.id.reply_list);
        listView.setAdapter(mConversationListAdapter);
        //////////
//        setContentView(R.layout.umeng_fb_activity_conversation);
//        if (bundle == null) {
//            String conversationId = this.getIntent().getStringExtra("conversation_id");
//            if (conversationId == null) {
//                conversationId = (new FeedbackAgent(this)).getDefaultConversation().getId();
//            }
//
//            FeedbackFragment fragment = new FeedbackFragment();
//            Bundle arg = new Bundle();
//            arg.putString("conversation_id", conversationId);
//            fragment.setArguments(arg);
//            this.getSupportFragmentManager().beginTransaction().add(R.id.umeng_fb_container, fragment).commit();
//        }
    }

    class ConversationListAdapter extends ArrayAdapter {
        private List<Conversation> conversationList;
        LayoutInflater mInflater;
        private Context context;
        private FeedbackAgent agent;
        private List<Reply> replyList;
        private Conversation conversation;

        public ConversationListAdapter(Context context) {
            super(context, R.layout.umeng_fb_reply_item_text);
            this.context = context;
            conversationList = new ArrayList<Conversation>();
            agent = new FeedbackAgent(context);
            for (String id : agent.getAllConversationIds()) {
                conversationList.add(agent.getConversationById(id));
            }
            Collections.sort(conversationList);
            if (conversationList.size() > 0) {
                conversation = conversationList.get(0);
                replyList = conversation.getReplyList();
            }
//            if (replyList.size()==0){
//                String welconme = "亲，欢迎您对我们的产品和服务提供宝贵的意见，我们会尽快对您作出回复，并根据您的意见，提供更优质的服务！";
//                conversation.addUserReply(welconme);
//                sync();
//            }
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return replyList == null ? 0 : replyList.size();
        }

        @Override
        public Object getItem(int position) {
            return replyList == null ? null : replyList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        public void sync() {

            conversation.sync(new SyncListener() {

                @Override
                public void onSendUserReply(List<Reply> replyList) {
                    //replyList->has gotten from Web
                    if (replyList.size()>0) {
//                        Log.v("AAA", "send->list=" + replyList.get(0));
                    }
                }

                @Override
                public void onReceiveDevReply(List<Reply> replyList) {
                    //replyList->has sent to web from user
                    if (replyList.size()>0) {
//                        Log.v("AAA", "receive->list=" + replyList.get(0));
                    }
                    notifyDataSetChanged();
                }
            });
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.feedback_list_user_item, null);
                holder = new ViewHolder();
                holder.content = (TextView) convertView
                    .findViewById(R.id.reply_content);
                holder.date = (TextView) convertView
                    .findViewById(R.id.reply_date);
                holder.userName=(TextView)convertView.findViewById(R.id.username);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            long time = replyList.get(position).created_at;
            String sTime = new SimpleDateFormat("yy/MM/dd HH:mm:ss").format(new Date(time));
            holder.content.setText(replyList.get(position).content);
            holder.date.setText(sTime);
            if (replyList.get(position).type.equals("user_reply")) {
                holder.userName.setText("我");
            }else if(replyList.get(position).type.equals("dev_reply")){
                holder.userName.setText("客服");
            }else if(replyList.get(position).type.equals("new_feedback")){
                holder.userName.setText("我");
            }
            return convertView;
        }

        class ViewHolder {
            TextView content;
            TextView date;
            TextView userName;
        }

        public void addConversation(Conversation conversation) {
            conversationList.add(conversation);
            notifyDataSetChanged();
        }

        public String getConversationId(int position) {
            return conversationList.get(position).getId();
        }

    }

    public void getContactInfo() {
        if (info == null) {
            agent = new FeedbackAgent(this);
            info = agent.getUserInfo();
        }
        if (info != null && info.getContact() != null) {
            ((TextView) findViewById(R.id.phone)).setText(info.getContact().get("phone"));
            ((TextView) findViewById(R.id.qq)).setText(info.getContact().get("qq"));
            ((TextView) findViewById(R.id.email)).setText(info.getContact().get("email"));
            ((TextView) findViewById(R.id.plain)).setText(info.getContact().get("plain"));
        }
    }

    public void changeToMessage() {
        findViewById(R.id.reminder).setVisibility(View.GONE);
        ((EditText) findViewById(R.id.edit_message)).setHint(getResources().getString(R.string.feedback_hint));
        ((Button) findViewById(R.id.send_message_button)).setText(getResources().getString(R.string.feedback_send));
        ((EditText) findViewById(R.id.edit_message)).setText("");
        ((EditText) findViewById(R.id.edit_message)).setInputType(InputType.TYPE_CLASS_TEXT);
        currentState = INPUT_MESSAGE;
    }

    public void handleButtonClick() {
        if (info == null) {
            info = new UserInfo();
        }
        Map<String, String> contact = info.getContact();
        if (contact == null) {
            contact = new HashMap<String, String>();
        }
        String message = ((EditText) findViewById(R.id.edit_message)).getEditableText().toString();
        switch (currentState) {
            case INPUT_PHONE:
                contact.put("phone", message);
                ((TextView) findViewById(R.id.phone)).setText(message);
                break;
            case INPUT_QQ:
                contact.put("qq", message);
                ((TextView) findViewById(R.id.qq)).setText(message);
                break;
            case INPUT_PLAIN:
                contact.put("plain", message);
                ((TextView) findViewById(R.id.plain)).setText(message);
                break;
            case INPUT_EMAIL:
                contact.put("email", message);
                ((TextView) findViewById(R.id.email)).setText(message);
                break;
            default:
                break;
        }
        changeToMessage();
        info.setContact(contact);
        agent.setUserInfo(info);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean result = agent.updateUserInfo();
            }
        }).start();
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView reminder = (TextView) findViewById(R.id.reminder);
            switch (v.getId()) {
                case R.id.send_message_button:
                    if (currentState == INPUT_MESSAGE) {
                        String content = ((EditText) findViewById(R.id.edit_message)).getEditableText().toString();
                        if (content == null || content.equals("")) {
                            return;
                        }
                        Conversation conversation = agent.getDefaultConversation();
                        conversation.addUserReply(content);
                        ((EditText) findViewById(R.id.edit_message)).setText("");
                        mConversationListAdapter.sync();
                        listView.setSelection(mConversationListAdapter.getCount());
                    } else {
                        handleButtonClick();
                    }
                    break;
                case R.id.phone_display:
                    if (info != null && info.getContact() != null) {
                        ((TextView) findViewById(R.id.edit_message)).setText(info.getContact().get("phone"));
                    }
                    reminder.setVisibility(View.VISIBLE);
                    reminder.setText(getResources().getString(R.string.feedback_mobile));
                    ((Button) findViewById(R.id.send_message_button)).setText(getResources().getString(R.string.feedback_save));
                    ((EditText) findViewById(R.id.edit_message)).setHint("");
                    ((EditText) findViewById(R.id.edit_message)).setInputType(InputType.TYPE_CLASS_PHONE);
                    currentState = INPUT_PHONE;
                    break;
                case R.id.qq_display:
                    if (info != null && info.getContact() != null) {
                        ((TextView) findViewById(R.id.edit_message)).setText(info.getContact().get("qq"));
                    }
                    reminder.setVisibility(View.VISIBLE);
                    reminder.setText(getResources().getString(R.string.feedback_qq));
                    ((Button) findViewById(R.id.send_message_button)).setText(getResources().getString(R.string.feedback_save));
                    ((EditText) findViewById(R.id.edit_message)).setHint("");
                    ((EditText) findViewById(R.id.edit_message)).setInputType(InputType.TYPE_CLASS_NUMBER);
                    currentState = INPUT_QQ;
                    break;
                case R.id.email_display:
                    if (info != null && info.getContact() != null) {
                        ((TextView) findViewById(R.id.edit_message)).setText(info.getContact().get("email"));
                    }
                    reminder.setVisibility(View.VISIBLE);
                    reminder.setText(getResources().getString(R.string.feedback_email));
                    ((Button) findViewById(R.id.send_message_button)).setText(getResources().getString(R.string.feedback_save));
                    ((EditText) findViewById(R.id.edit_message)).setHint("");
                    ((EditText) findViewById(R.id.edit_message)).setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    currentState = INPUT_EMAIL;
                    break;
                case R.id.plain_display:
                    if (info != null && info.getContact() != null) {
                        ((TextView) findViewById(R.id.edit_message)).setText(info.getContact().get("plain"));
                    }
                    reminder.setVisibility(View.VISIBLE);
                    reminder.setText(getResources().getString(R.string.feedback_plain));
                    ((Button) findViewById(R.id.send_message_button)).setText(getResources().getString(R.string.feedback_save));
                    ((EditText) findViewById(R.id.edit_message)).setHint("");
                    ((EditText) findViewById(R.id.edit_message)).setInputType(InputType.TYPE_CLASS_TEXT);
                    currentState = INPUT_PLAIN;
                    break;
                default:
                    changeToMessage();
                    break;
            }
        }
    };
}
