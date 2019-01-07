package com.ksider.mobile.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.ksider.mobile.android.utils.Utils;
import com.umeng.message.PushAgent;

/**
 * Created by yong on 7/21/15.
 */
public class LocalService extends Service {
    private static final String TAG = "LocalService";
    private IBinder binder = new LocalService.LocalBinder();
    private PushAgent mPushAgent;

    public void onCreate() {
        super.onCreate();
        mPushAgent = PushAgent.getInstance(this);
        mPushAgent.enable();
        mPushAgent.onAppStart();
        Utils.init(mPushAgent);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Notification notification = new Notification(android.R.drawable.ic_notification_overlay,"AAAAA",3000);
//        PendingIntent pendingintent = PendingIntent.getActivity(this, 0,
//                new Intent(this, HomeActivity.class), 0);
//        notification.setLatestEventInfo(this, "uploadservice", "请保持程序在后台运行",
//                pendingintent);
//        startForeground(0x111, notification);
        mPushAgent.enable();
        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        LocalService getService() {
            return LocalService.this;
        }
    }
}
