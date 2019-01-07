package com.ksider.mobile.android.WebView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.ksider.mobile.android.slide.SlidingLayout;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.UserInfo;
import com.umeng.message.PushAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yong on 8/29/15.
 */
public class FeedBackActivity extends BaseActivity {
    private FeedbackAgent agent;
    private UserInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        new SlidingLayout(this);
        customActionBar("意见反馈");
        TextView refund = (TextView) findViewById(R.id.more_choice);
        refund.setText("反馈历史记录");
        refund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent feedback = new Intent(FeedBackActivity.this, CheckFeedBackActivity.class);
                startActivity(feedback);
            }
        });

        agent = new FeedbackAgent(this);
        agent.sync();
        agent.openFeedbackPush();
        PushAgent.getInstance(this).enable();
        agent.setWelcomeInfo("Welcome to use umeng feedback app");
        info = agent.getUserInfo();
        if (info != null && info.getContact() != null) {
            ((TextView) findViewById(R.id.phone)).setText(info.getContact().get("phone"));
        }

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText phoneInput = (EditText) findViewById(R.id.phone);
                EditText contentInput = (EditText) findViewById(R.id.content);
                String phone = phoneInput.getEditableText().toString();
                String content = contentInput.getEditableText().toString();
                if (info == null) {
                    return;
                }

                if (content == null || content.equals("")) {
                    Toast.makeText(FeedBackActivity.this, "反馈内容不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (phone == null || phone.equals("") || phone.length() != 11) {
                    Toast.makeText(FeedBackActivity.this, "联系方式不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<String, String> contact = info.getContact();
                if (contact == null) {
                    contact = new HashMap<String, String>();
                }

                contact.put("phone", phone);
                info.setContact(contact);
                agent.setUserInfo(info);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean result = agent.updateUserInfo();
                    }
                }).start();

                Conversation conversation = agent.getDefaultConversation();
                conversation.addUserReply(content);
                contentInput.setText("");

                FeedBackActivity.this.finish();
            }
        });
    }

}
