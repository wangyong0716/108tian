package com.ksider.mobile.android.WebView;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ksider.mobile.android.adaptor.BannerAlbumAdaptor;
import com.ksider.mobile.android.utils.Network;
import com.ksider.mobile.android.view.viewpagerindicator.BannerIndicator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yong on 7/24/15.
 */
public class PicsViewActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pictures_pager);

        initPager();
    }

    public void initPager() {
        ArrayList<String> imgs = getIntent().getStringArrayListExtra("pics");
        if (imgs == null) {
            return;
        }
        List<BannerAlbumAdaptor.BannerAlbumItem> album = new ArrayList<BannerAlbumAdaptor.BannerAlbumItem>();
        for (int i = 0; i < imgs.size(); i++) {
            BannerAlbumAdaptor.BannerAlbumItem item = new BannerAlbumAdaptor.BannerAlbumItem();
            item.image = imgs.get(i);
            album.add(item);
        }
        ViewPager pager = (ViewPager) findViewById(R.id.header_album);
        if (pager.getAdapter() == null) {
            BannerAlbumAdaptor adaptor = new BannerAlbumAdaptor(this, album);
            pager.setAdapter(adaptor);
            pager.setCurrentItem(album.size() * 100);
            BannerIndicator indicator = (BannerIndicator) findViewById(R.id.indicator);
            indicator.setViewPager(pager);
            pager.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
            pager.setOnTouchListener(new View.OnTouchListener() {
                float oldX = 0, newX = 0, sens = 5;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            oldX = event.getX();
                            break;
                        case MotionEvent.ACTION_UP:
                            newX = event.getX();
                            if (Math.abs(oldX - newX) < sens) {
                                v.performClick();
                                return true;
                            }
                            oldX = 0;
                            newX = 0;
                            break;
                    }
                    return false;
                }
            });
        } else {
            BannerAlbumAdaptor adaptor = (BannerAlbumAdaptor) pager.getAdapter();
            adaptor.removeAllItems();
            adaptor.addMoreItems(album);
        }
    }
}
