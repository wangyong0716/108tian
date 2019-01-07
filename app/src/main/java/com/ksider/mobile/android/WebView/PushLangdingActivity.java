package com.ksider.mobile.android.WebView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.ksider.mobile.android.model.BaseDataModel;
import com.ksider.mobile.android.personal.MessageListActivity;
import com.ksider.mobile.android.personal.OrderDetailActivity;
import com.ksider.mobile.android.utils.BasicCategory;
import com.ksider.mobile.android.utils.Utils;

/**
 * Created by wenkui on 6/8/15.
 */
public class PushLangdingActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = null;
        if (getIntent() != null) {
            intent = parseNotify();
        }
        if (intent == null) {
            intent = new Intent(this, HomeActivity.class);
        }
        startActivity(intent);
        finish();
    }

    protected Intent parseNotify() {
        String remoteNotification = getIntent().getStringExtra("RemoteNotification");
        Intent intent = null;
        if (remoteNotification == null) {
            return intent;
        }
        Uri uri = Uri.parse(remoteNotification);
        if (("http").equals(uri.getScheme()) || ("https").equals(uri.getScheme())) {
            if (uri.getHost() != null
                    && uri.getHost().contains("108tian.com")) {
                uri.getPath();
                String[] paths = uri.getPath().split("/");
                if ("choice".equals(paths[1])
                        || "theme".equals(paths[1])
                        || "weekly".equals(paths[1])
                        || "events".equals(paths[1])
                        || "event".equals(paths[1])) {
                    if (paths.length >= 3) {
                        String[] ids = paths[2].split("\\.");
                        if (ids.length >= 2) {
                            intent = getLandingActivity(paths[1]);
                            intent.putExtra(BaseDataModel.id_name, ids[0]);
                        }
                    }
                } else if ("dest".equals(paths[1]) && paths.length >= 4) {
                    String[] ids = paths[3].split("\\.");
                    if (ids.length >= 2) {
                        intent = getLandingActivity(paths[2]);
                        intent.putExtra(BaseDataModel.id_name, ids[0]);
                    }
                } else {
                    if (paths.length >= 3) {
                        intent = parseList(paths);
                    }
                }
                if (intent==null){
                    intent=new Intent(PushLangdingActivity.this,WebViewLandingActivity.class);
                    intent.putExtra("url", uri.toString());
                }
            }
        } else if (("notification").equals(uri.getHost())) {
            intent = keyResolver(uri);
        }
        return intent;
    }

    public Intent getLandingActivity(String type) {
        Intent intent = null;
        if (type.equals("resort")) {
            intent = Utils.getLandingActivity(PushLangdingActivity.this, BasicCategory.RESORT);
        } else if (type.equals("inn") || type.equals("farm")) {
            intent = Utils.getLandingActivity(PushLangdingActivity.this, BasicCategory.FARMYARD);
        } else if (type.equals("scenery") || type.equals("scene")) {
            intent = Utils.getLandingActivity(PushLangdingActivity.this, BasicCategory.ATTRACTIONS);
        } else if (type.equals("pick")) {
            intent = Utils.getLandingActivity(PushLangdingActivity.this, BasicCategory.PICKINGPART);
        } else if (type.equals("events") || type.equals("event")) {
            intent = Utils.getLandingActivity(PushLangdingActivity.this, BasicCategory.ACTIVITY);
        } else if (type.equals("weekly")) {
            intent = Utils.getLandingActivity(PushLangdingActivity.this, BasicCategory.GUIDE);
        } else if (type.equals("theme") || type.equals("choice")) {
            intent = new Intent(this, ChoicenessActivity.class);
        }
        return intent;
    }

    protected Intent parseList(String[] paths) {
        Intent intent = null;
        if ("theme".equals(paths[2])) {
            intent = new Intent(this, HomeActivity.class);
            intent.putExtra("index", "recommend");
        } else if ("weekly".equals(paths[2])) {
            intent = new Intent(this, HomeActivity.class);
            intent.putExtra("index", "weekly");
        } else if ("events".equals(paths[2])) {
            intent = new Intent(this, HomeActivity.class);
            intent.putExtra("index", "event");
        } else if ("dest".equals(paths[2]) && paths.length >= 4) {
            intent = new Intent(this, SelectorActivity.class);
            if ("scenery".equals(paths[3])) {
                intent.putExtra("category", BasicCategory.ATTRACTIONS);
            } else if ("inn".equals(paths[3])) {
                intent.putExtra("category", BasicCategory.FARMYARD);
            } else if ("resort".equals(paths[3])) {
                intent.putExtra("category", BasicCategory.RESORT);
            } else if ("pick".equals(paths[3])) {
                intent.putExtra("category", BasicCategory.PICKINGPART);
            }
        }
        return intent;
    }

    public Intent keyResolver(Uri uri) {
        String scheme = uri.getScheme();
        String[] paths = uri.getPath().split("/");
        Intent intent = null;
        if (paths.length < 1) {
            return intent;
        }
        if (("reply").equals(scheme)) {
            intent = new Intent(PushLangdingActivity.this, MessageListActivity.class);
        } else if (paths.length > 1) {
            long serialNumber = Long.parseLong(paths[1]);
            if (("order").equals(scheme)) {
                intent = new Intent(PushLangdingActivity.this, OrderDetailActivity.class);
                intent.putExtra("serialNumber", serialNumber);
            } else if (("refund").equals(scheme)) {
                intent = new Intent(PushLangdingActivity.this, OrderDetailActivity.class);
                intent.putExtra("serialNumber", serialNumber);
            } else if (("consume").equals(scheme)) {
                intent = new Intent(PushLangdingActivity.this, OrderDetailActivity.class);
                intent.putExtra("serialNumber", serialNumber);
            } else if (("expire").equals(scheme)) {
                intent = new Intent(PushLangdingActivity.this, OrderDetailActivity.class);
                intent.putExtra("serialNumber", serialNumber);
            }
        }
        return intent;
    }
}
