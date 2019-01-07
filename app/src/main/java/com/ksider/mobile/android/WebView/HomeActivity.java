/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ksider.mobile.android.WebView;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.activity.fragment.*;
import com.ksider.mobile.android.model.MessageEvent;
import com.ksider.mobile.android.service.LocalService;
import com.ksider.mobile.android.utils.*;
import com.ksider.mobile.android.view.PagerSlidingTabStrip;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengRegistrar;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends FragmentActivity {
    private PagerSlidingTabStrip mTabs;
    private ViewPager mPager;
    private TabPageAdaptor mAdapter;
    private PushAgent mPushAgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MobclickAgent.updateOnlineConfig(this);
//        Log.v("AAA","userId="+ UserInfo.getUserId());
//        mPushAgent = PushAgent.getInstance(this);
//        mPushAgent.enable();
//        mPushAgent.onAppStart();
//        Utils.init(mPushAgent);
        Network.getInstance().addToRequestQueue(getPromotionRequest());
        Intent intent = new Intent(getApplicationContext(), LocalService.class);
        startService(intent);
        UmengUpdateAgent.update(this);
        MobclickAgent.openActivityDurationTrack(false);
        if (Utils.hasSmartBar()) {
            setTheme(R.style.CustomTheme);
        }

//		String device_token = UmengRegistrar.getRegistrationId(this);
//        Log.v("AAA","token="+device_token);
//        Log.v("AAA","device="+getDeviceInfo(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        if (Utils.hasSmartBar()) {
            initSmartBar();
        } else {
            initNormal();
        }
    }


//    public static String getDeviceInfo(Context context) {
//        try{
//            org.json.JSONObject json = new org.json.JSONObject();
//            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
//                    .getSystemService(Context.TELEPHONY_SERVICE);
//
//            String device_id = tm.getDeviceId();
//
//            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//
//            String mac = wifi.getConnectionInfo().getMacAddress();
//            json.put("mac", mac);
//
//            if( TextUtils.isEmpty(device_id) ){
//                device_id = mac;
//            }
//
//            if( TextUtils.isEmpty(device_id) ){
//                device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
//            }
//
//            json.put("device_id", device_id);
//
//            return json.toString();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }

    protected void initNormal() {
        MessageUtils.eventBus.register(this);
        mTabs = (PagerSlidingTabStrip) findViewById(R.id.homeTabs);
        mPager = (ViewPager) findViewById(R.id.homePage);
        mAdapter = new TabPageAdaptor(getSupportFragmentManager(), getApplicationContext());
        mPager.setAdapter(mAdapter);
        mTabs.setViewPager(mPager);
        String index = getIntent().getStringExtra("index");
        int pageId = 0;
        if (index != null) {
            if (index.equals("weekly")) {
                pageId = 0;
            } else if (index.equals("event")) {
                pageId = 2;
            } else if (index.equals("recommend")) {
                pageId = 1;
            } else if (index.equals("personal")) {
                pageId = 3;
            }
        }
        mPager.setCurrentItem(pageId);
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse response) {
                if (UpdateStatus.Yes == updateStatus) {
                    UmengUpdateAgent.showUpdateDialog(HomeActivity.this, response);
                }
            }
        });
    }

    protected void initSmartBar() {
        final ActionBar bar = getActionBar();
        getWindow().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);

        bar.addTab(bar.newTab().setCustomView(R.layout.tab_find_indicator)
                .setTabListener(new HomeTabListener<HomePageFragment>(this, "home", HomePageFragment.class)), 0, true);
        bar.addTab(bar.newTab().setCustomView(R.layout.tab_choiceness_indicator)
                .setTabListener(new HomeTabListener<ChoicenessFragment>(this, "choiceness", ChoicenessFragment.class)), 1, false);
        bar.addTab(bar.newTab().setCustomView(R.layout.tab_activity_indicator)
                .setTabListener(new HomeTabListener<ListFragment>(this, "activity", ListFragment.class)), 2, false);

        bar.addTab(bar.newTab().setCustomView(R.layout.tab_me_indicator).setTabListener(new HomeTabListener<MeFramment>(this, "MeFramment", MeFramment.class)), 3, false);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // 设置若顶栏没有显示内空，则隐藏
        getActionBar().setDisplayOptions(0);
        SmartBarUtils.setBackIcon(bar, getResources().getDrawable(R.drawable.meizu_backicon));
        SmartBarUtils.setActionBarViewCollapsable(getActionBar(), true);
        // 设置ActionBar Tab显示在底栏
        SmartBarUtils.setActionBarTabsShowAtBottom(bar, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MessageUtils.eventBus.unregister(this);
    }

    public void onEventMainThread(MessageEvent event) {
        if (event.getType() == MessageEvent.NOTIFY_ChOINESS_SELECTED) {
            mPager.setCurrentItem(2);
        }
    }

    /**
     * Overrides the back key handler
     *
     * @param keyCode - the pressed key identifier
     * @param event   - the key event type
     * @return - true if we handled the key pressed
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode && event.getAction() == KeyEvent.ACTION_DOWN) {
            CustomDialog.Builder builder = new CustomDialog.Builder(HomeActivity.this);
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    HomeActivity.this.finish();
                }
            }).setTitle("确定退出？").show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        Log.v("AAA", "dis");
        for (Fragment fragment : fragments) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static class HomeTabListener<T extends Fragment> implements ActionBar.TabListener {
        private final FragmentActivity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private final Bundle mArgs;
        private Fragment mFragment;

        public HomeTabListener(FragmentActivity activity, String tag, Class<T> clz) {
            this(activity, tag, clz, null);
        }

        public HomeTabListener(FragmentActivity activity, String tag, Class<T> clz, Bundle args) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mArgs = args;

            mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }

        @Override
        public void onTabReselected(Tab arg0, android.app.FragmentTransaction arg1) {
        }

        @Override
        public void onTabSelected(Tab arg0, android.app.FragmentTransaction arg1) {
            mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
            FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
            }
            ft.replace(android.R.id.content, mFragment, mTag);
            ft.commitAllowingStateLoss();
        }

        @Override
        public void onTabUnselected(Tab arg0, android.app.FragmentTransaction arg1) {
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.detach(mFragment);
//                ft.remove(mFragment);
            }
        }
    }

    protected String getPromotionUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("platform", "android");
        return APIUtils.getUrl(APIUtils.PROMOTION, params);
    }

    protected JsonObjectRequest getPromotionRequest() {
        Log.v("AAA", "HomeActivity->getPromotion=" + getPromotionUrl());
        return new JsonObjectRequest(getPromotionUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        readPromotion(response.getJSONObject("data"));
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

    public void readPromotion(JSONObject promotion) {
        String id = "";
        String imgUrl = "";
        int repeatCount = 0;
        String to = "";
        try {
            id = promotion.getString("_id");
        } catch (JSONException js) {
            js.printStackTrace();
        }
        try {
            imgUrl = promotion.getString("img");
        } catch (JSONException js) {
            js.printStackTrace();
        }
        try {
            to = promotion.getString("to");
        } catch (JSONException js) {
            to = "";
            js.printStackTrace();
        }
        try {
            repeatCount = promotion.getInt("repeatCount");
        } catch (JSONException js) {
            js.printStackTrace();
        }
        final String toUrl = to;

        int shownTimes = Storage.getSharedPref().getInt(id, 0);
        if (shownTimes >= repeatCount && repeatCount > 0 || this.isFinishing()) {
            return;
        }
        Storage.putInt(id, ++shownTimes);

        CouponDialog.Builder builder = new CouponDialog.Builder(this);
        builder.setImg(imgUrl, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toUrl != null && !("").equals(toUrl)) {
                    Intent intent = new Intent(HomeActivity.this, WebViewLandingActivity.class);
                    intent.putExtra("url", toUrl);
                    intent.putExtra("share", true);
                    startActivity(intent);
                }
            }
        }).setButton("关闭", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }
}
