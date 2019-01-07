package com.ksider.mobile.android.WebView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.model.AdModel;
import com.ksider.mobile.android.model.BaseDataModel;
import com.ksider.mobile.android.service.SystemBarTintManager;
import com.ksider.mobile.android.utils.APIUtils;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.utils.Storage;
import com.ksider.mobile.android.utils.Utils;
import com.ksider.mobile.android.view.LoadImageView;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.multiwindow.SMultiWindow;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends Activity {
    private final int LOAD_AD_DELAY = 0;
    private final int LOAD_AD_PEROID = 100;
    private final int NO_AD_DELAY = 1000;
    private final int SHOW_AD_ANIMATION_DURATION = 1000;
    private ImageView[] mTips;

    private AdModel ad = new AdModel();
    private TimerTask timerTask;
    private Timer timer = new Timer();
    private Handler mHandler = new MyHandler(WelcomeActivity.this);

    private SMultiWindow mMultiWindow = null;

    public WelcomeActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Boolean firstLoad = Storage.sharedPref.getBoolean("firstLoad", true);
        if (firstLoad) {
            Storage.putBoolean("firstLoad", false);
            setContentView(R.layout.activity_welcome_lead);
            ViewPager pager = (ViewPager) findViewById(R.id.header_album);
            initIndicator();
            mTips[0].setImageResource(R.drawable.dot_gray);
            pager.setAdapter(pagerAdapter);
            pager.setOnPageChangeListener(listener);
        } else {
            setContentView(R.layout.activity_welcome);
            timerTask = new TimerTask() {
                public void run() {
                    Message message = new Message();
                    message.what = 1;
                    mHandler.sendMessage(message);
                }
            };
//            timer.schedule(timerTask, LOAD_AD_DELAY, LOAD_AD_PEROID);
            timer.schedule(timerTask, LOAD_AD_DELAY);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    timerTask.cancel();
                    if (ad.isFilled()) {
                        loadAd();
                    } else {
                        Intent mainIntent = new Intent(WelcomeActivity.this, HomeActivity.class);
                        WelcomeActivity.this.startActivity(mainIntent);
                        WelcomeActivity.this.finish();
                    }
                }
            }, NO_AD_DELAY);
        }

        mMultiWindow = new SMultiWindow();
        try {
            mMultiWindow.initialize(this);
        } catch (SsdkUnsupportedException se) {
            se.printStackTrace();
        }
        setFullScreen();
//        setTranslucentStatus(Color.WHITE);
    }

    /**
     * set full screen, remove status bar and navigation bar
     */
    public void setFullScreen() {
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window = WelcomeActivity.this.getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
    }

    public void loadAd() {
        LoadImageView adImage = (LoadImageView) findViewById(R.id.ad_image);
//        adImage.setImageResource(ad.getImg());
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(WelcomeActivity.this, HomeActivity.class);
                WelcomeActivity.this.startActivity(mainIntent);
                WelcomeActivity.this.finish();
            }
        };
        final Handler handler = new Handler();
        handler.postDelayed(runnable, ad.getHoldTime() * 1000);
        adImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ad.getTo() != null && !ad.getTo().equals("")) {
                    handler.removeCallbacks(runnable);
                    Intent intent = Utils.getLandingActivity(WelcomeActivity.this, ad.getType());
                    if (intent == null) {
                        return;
                    }
                    intent.putExtra("go_home", true);
                    if (ad.getType().equals("outapp")) {
                        intent.putExtra("url", ad.getTo());
                        intent.putExtra("share", false);
                    } else {
                        intent.putExtra(BaseDataModel.id_name, ad.getTo());
                        intent.putExtra(BaseDataModel.type_name, ad.getType());
                    }
                    startActivity(intent);
                    WelcomeActivity.this.finish();
                }
            }
        });
        AlphaAnimation animation = new AlphaAnimation(0f, 1.0f);
        animation.setDuration(SHOW_AD_ANIMATION_DURATION);
        findViewById(R.id.welcome_image).setVisibility(View.INVISIBLE);
        adImage.setVisibility(View.VISIBLE);
        adImage.startAnimation(animation);
    }

    public String getUrl() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("platform", "android");
        params.put("deviceType", Build.BRAND);
        params.put("reslotion", getResolution());
        return APIUtils.getUrl("Ad", params);
    }

    public String getResolution() {
        Display display = getWindowManager().getDefaultDisplay(); //Activity#getWindowManager()
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        return height + "x" + width;
    }

    public void process(JSONObject data) {
        if (data == null || data.length() < 1) {
            //if data is null, set default image to show
            //((ImageView)findViewById(R.id.welcome_image)).setImageURI();
            return;
        }
        timerTask.cancel();
        ad.setFilled(true);
        try {
            String img = data.getString("img");
            if (!img.startsWith("http://")) {
                ad.setImg(Constants.IMAGE_BASE_URL + img);
            } else {
                ad.setImg(img);
            }
        } catch (JSONException js) {
            js.printStackTrace();
        }
        try {
            ad.setTo(data.getString("to"));
        } catch (JSONException js) {
            ad.setTo("");
            js.printStackTrace();
        }
        try {
            ad.setHoldTime(data.getInt("holdTime"));
        } catch (JSONException js) {
            ad.setHoldTime(3);
            js.printStackTrace();
        }
        try {
            ad.setType(data.getString("type"));
        } catch (JSONException js) {
            js.printStackTrace();
        }
        try {
            ad.setName(data.getString("name"));
            ad.setModified(data.getLong("modified"));
        } catch (JSONException js) {
            js.printStackTrace();
        }
        LoadImageView adImage = (LoadImageView) findViewById(R.id.ad_image);
        adImage.setImageResource(ad.getImg());
    }

    protected JsonObjectRequest getRequest() {
        Log.v("AAA", "welcome->url=" + getUrl());
        return new JsonObjectRequest(getUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null && response.getInt("status") == 0) {
                        process(response.getJSONObject("data"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(Constants.LOG_TAG, "response:" + error.toString());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void initIndicator() {
        LinearLayout tablist = (LinearLayout) findViewById(R.id.tablist);
        mTips = new ImageView[3];
        for (int i = 0; i < 3; i++) {
            ImageView imageView = new ImageView(this);
            mTips[i] = imageView;
            mTips[i].setBackgroundResource(R.drawable.dot_white);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, dm);
            layoutParams.leftMargin = margin;
            layoutParams.rightMargin = margin;
            tablist.addView(imageView, layoutParams);
        }
    }

    private PagerAdapter pagerAdapter = new PagerAdapter() {
        protected int image[] = {R.drawable.lead_01, R.drawable.lead_02, R.drawable.lead_03};

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return image.length;
        }

        /**
         * 载入图片进去，用当前的position 除以 图片数组长度取余数是关键
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            position = position % image.length;
            ViewGroup group = (ViewGroup) LayoutInflater.from(WelcomeActivity.this).inflate(R.layout.lead_format, null, false);
            ImageView loader = (ImageView) group.findViewById(R.id.image);
            loader.setImageResource(image[position]);
            if (position == image.length - 1) {
                Button button = (Button) group.findViewById(R.id.start_button);
                button.setVisibility(View.VISIBLE);
                button.setClickable(true);
                button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
                        WelcomeActivity.this.startActivity(intent);
                        WelcomeActivity.this.finish();
                    }
                });
                RelativeLayout buttonLayout = (RelativeLayout) group.findViewById(R.id.button_layout);
                buttonLayout.setClickable(true);
                buttonLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
                        WelcomeActivity.this.startActivity(intent);
                        WelcomeActivity.this.finish();
                    }
                });
                View tablist = findViewById(R.id.tablist);
                tablist.setVisibility(View.GONE);
            } else {
                View tablist = findViewById(R.id.tablist);
                tablist.setVisibility(View.VISIBLE);
            }
            ((ViewPager) container).addView(group, 0);
            return group;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }
    };

    private OnPageChangeListener listener = new OnPageChangeListener() {
        protected int LastPosition = 0;

        @Override
        public void onPageSelected(int position) {
            position = position % mTips.length;
            mTips[LastPosition].setImageResource(R.drawable.dot_white);
            mTips[position].setImageResource(R.drawable.dot_gray);
            LastPosition = position;
        }

        @Override
        public void onPageScrolled(int position, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    @Override
    public void onBackPressed() {
        return;
    }

    /**
     * set status bar color
     */
    private void setTranslucentStatus(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            winParams.flags |= bits;
            win.setAttributes(winParams);
        }
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
//        tintManager.setStatusBarTintEnabled(false);
//        tintManager.setNavigationBarTintEnabled(false);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setTintColor(color);
//        tintManager.setNavigationBarTintEnabled(true);
//        tintManager.setTintColor(Color.WHITE);
    }
}

class MyHandler extends Handler {
    private Context context;

    public MyHandler(Context context) {
        this.context = context;
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                Network.getInstance().addToRequestQueue(((WelcomeActivity) context).getRequest());
                break;
            default:
                break;
        }
        super.handleMessage(msg);
    }
}
