package com.ksider.mobile.android.WebView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.ksider.mobile.android.model.BaseDataModel;
import com.ksider.mobile.android.personal.ConsumeCodeListActivity;
import com.ksider.mobile.android.personal.CouponActivity;
import com.ksider.mobile.android.personal.OrderActivity;
import com.ksider.mobile.android.utils.BasicCategory;
import com.ksider.mobile.android.utils.Utils;

/**
 * Created by wenkui on 3/12/15.
 */
public class SchemaSupportActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getIntent().getData();
        if (uri.getScheme().equals("ksider")) {
            processUriAction(uri);
        } else if (uri.getScheme().equals("https")) {
            processHttpsAction(uri);
        } else {
            this.finish();
        }
    }

    protected void processUriAction(Uri uri) {
        String path = uri.getPath();
        String[] paths = path.split("/");
        Log.v("AAA", "uri=" + uri + "|host=" + uri.getHost() + "|path=" + uri.getPath() + "|length=" + paths.length);
        Intent intent = null;
        if (uri.getHost().equals("u") && paths.length > 1) {
            intent = toUserCenter(paths);
        } else if (paths != null && paths.length > 1) {
            if (paths[1].equals("list")) {
                intent = getListIntent(uri.getHost(), paths);
            } else if (paths[1].equals("detail") && paths.length >= 3) {
                intent = getDetailIntent(uri.getHost(), paths[2]);
            }
            else {
                Log.v("AAA", "unexcepted uri=" + uri + "|host=" + uri.getHost() + "|path=" + uri.getPath() + "|length=" + paths.length);
            }
        }
        if (intent == null) {
            intent = new Intent(this, HomeActivity.class);
        }
        startActivity(intent);
        this.finish();
    }

    protected void parseList(Intent intent, String[] params) {
        if (params.length >= 3) {
            int cityId = Integer.valueOf(params[2]);
            if (cityId > 0) {
                intent.putExtra("regionId", cityId);
            }
        }
        if (params.length >= 4) {
            int theme = Integer.valueOf(params[3]);
            if (theme > 0) {
                intent.putExtra("theme", theme);
            }
        }
        if (params.length >= 5) {
            int groupId = Integer.valueOf(params[4]);
            if (groupId > 0) {
                intent.putExtra("groupId", groupId);
            }
        }
    }

    protected Intent getListIntent(String host, String[] params) {
        if (host == null) {
            return null;
        }
        Intent intent = null;
        BasicCategory category = BasicCategory.UNKOWN;
        if (host.equals("scene")) {
            category = BasicCategory.ATTRACTIONS;
            intent = new Intent(this, SelectorActivity.class);
            intent.putExtra("category", category);
        } else if (host.equals("farm")) {
            category = BasicCategory.FARMYARD;
            intent = new Intent(this, SelectorActivity.class);
            intent.putExtra("category", category);
        } else if (host.equals("resort")) {
            category = BasicCategory.RESORT;
            intent = new Intent(this, SelectorActivity.class);
            intent.putExtra("category", category);
        } else if (host.equals("pick")) {
            category = BasicCategory.PICKINGPART;
            intent = new Intent(this, SelectorActivity.class);
            intent.putExtra("category", category);
        } else if (host.equals("weekly")) {
            intent = new Intent(this, HomeActivity.class);
            intent.putExtra("index", "weekly");
        } else if (host.equals("event")) {
            intent = new Intent(this, HomeActivity.class);
            intent.putExtra("index", "event");
        } else if (host.equals("recommend")) {
            intent = new Intent(this, HomeActivity.class);
            intent.putExtra("index", "recommend");
        } else if (host.equals("choice")) {
            intent = new Intent(this, HomeActivity.class);
            intent.putExtra("index", "recommend");
        }
        if (intent == null) {
            intent = new Intent(this, HomeActivity.class);
            intent.putExtra("index", category);
        }
        parseList(intent, params);
        return intent;
    }

    protected Intent getDetailIntent(String host, String id) {
        Intent intent = Utils.getLandingActivity(SchemaSupportActivity.this, host);
        BaseDataModel base = new BaseDataModel();
        base.id = id;
        Utils.initDetailPageArg(intent, base);
        return intent;
    }

    protected Intent toUserCenter(String[] params) {
        Intent intent = new Intent();
        if (params.length < 2 || "".equals(params[1])) {
            return null;
        }
        if ("login".equals(params[1])) {
            intent.setClass(this, LoginActivity.class);
        } else if ("index".equals(params[1])) {
            intent = new Intent(this, HomeActivity.class);
            intent.putExtra("index", "personal");
        } else if ("order".equals(params[1])) {
            intent.setClass(this, OrderActivity.class);
            intent.putExtra("index", 0);
        } else if ("codes".equals(params[1])) {
            intent.setClass(this, ConsumeCodeListActivity.class);
        } else if ("coupon".equals(params[1])) {
            intent.setClass(this, CouponActivity.class);
        } else {
            return null;
        }
        return intent;
    }

    public void processHttpsAction(Uri uri) {
        String path = uri.getPath();
        String[] paths = path.split("/");
        Log.v("AAA", "uri=" + uri + "|host=" + uri.getHost() + "|path=" + uri.getPath() + "|length=" + paths.length);
        Intent intent = null;
        if (paths.length == 3) {
            intent = getDetailIntent(paths[1], paths[2].substring(0, paths[2].indexOf(".")));
        }
        if (intent == null) {
            intent = new Intent(this, HomeActivity.class);
        }
        startActivity(intent);
        this.finish();
    }
}
