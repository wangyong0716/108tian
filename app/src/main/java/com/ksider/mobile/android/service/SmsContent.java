package com.ksider.mobile.android.service;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yong on 10/8/15.
 */
public class SmsContent extends ContentObserver {

    public static final String SMS_URI_INBOX = "content://sms/inbox";
    private Activity activity = null;
    private EditText verifyText = null;

    public SmsContent(Activity activity, Handler handler, EditText verifyText) {
        super(handler);
        this.activity = activity;
        this.verifyText = verifyText;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Cursor cursor = null;
        // 读取收件箱中指定号码的短信
//        cursor = activity.managedQuery(Uri.parse(SMS_URI_INBOX), new String[] { "_id", "address", "body", "read" }, "address=? and read=?",
//                new String[] { "5554", "0" }, "date desc");
        cursor = activity.managedQuery(Uri.parse(SMS_URI_INBOX), new String[]{"_id", "address", "body", "read"}, "type=1",
                new String[]{}, "date desc");
        if (cursor != null) {// 如果短信为未读模式
            cursor.moveToFirst();
            if (cursor.moveToFirst()) {
                String smsBody = cursor.getString(cursor.getColumnIndex("body"));
//                Log.v("AAA", "server->body=" + smsBody);
                if (smsBody.contains("108天周边游") || smsBody.contains("一零八天周边游")) {
                    String regEx = "\\d{4}";
                    Pattern p = Pattern.compile(regEx);
                    Matcher m = p.matcher(smsBody);
                    if (m.find()) {
                        try {
                            Log.v("AAA", "server->code=" + m.group(0));
                            verifyText.setText(m.group(0));
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
