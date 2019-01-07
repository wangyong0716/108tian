package com.ksider.mobile.android.adaptor;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.view.LoadImageView;
import com.ksider.mobile.android.view.viewpagerindicator.IconPagerAdapter;

import java.util.List;

/**
 * Created by wenkui on 3/19/15.
 */
public class BannerAlbumAdaptor extends PagerAdapter implements IconPagerAdapter {
    protected List<BannerAlbumItem> items;
    protected Context mContext;

    public BannerAlbumAdaptor(Context context, List<BannerAlbumItem> data) {
        mContext = context;
        items = data;
    }
    public void addMoreItems(List<BannerAlbumItem> newItems) {
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void removeAllItems() {
        this.items.clear();
        notifyDataSetChanged();
    }
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
    @Override
    public int getCount() {
//        return items.size();
        return Integer.MAX_VALUE;
    }

    public int getSize(){
        return items.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == ((ViewGroup) o);
    }

    public BannerAlbumItem getItem(int position) {
        if (items.size()<=0){
            return null;
        }
        return items.get(position%items.size());
    }

    /**
     * 载入图片进去，用当前的position 除以 图片数组长度取余数是关键
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ViewGroup group = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.banner_item, null, false);
        LoadImageView image = (LoadImageView) group.findViewById(R.id.image);
        if (items.size()<=0){
            return null;
        }
        BannerAlbumItem item = items.get(position % items.size());
        if(item != null) {
            image.setImageResource(item.image);
            if (item.name != null) {
                TextView text = (TextView) group.findViewById(R.id.header_title);
                text.setText(item.name);
            }
        }
        ((ViewPager) container).addView(group, 0);
        return group;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((View) object);
    }

    @Override
    public int getIconResId(int index) {
        return 0;
    }
    public static  class BannerAlbumItem{
        public String image;
        public String name;
        public String type;
        public String dest;
    }
}
