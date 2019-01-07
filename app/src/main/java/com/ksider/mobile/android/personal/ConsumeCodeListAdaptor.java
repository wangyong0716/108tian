package com.ksider.mobile.android.personal;

import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.ConsumeCodeModel;
import com.ksider.mobile.android.utils.StringUtils;
import com.ksider.mobile.android.view.paging.PagingBaseAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yong on 2015/5/22.
 */
public class ConsumeCodeListAdaptor extends PagingBaseAdapter<ConsumeCodeModel> {
    protected Activity mContext;

    public ConsumeCodeListAdaptor(Activity context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ConsumeCodeModel getItem(int postion) {
        if (0 <= postion && postion < items.size()) {
            return items.get(postion);
        }
        return null;
    }

    @Override
    public long getItemId(int postion) {
        return postion;
    }


    @Override
    public View getView(final int postion, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_view_consume_item, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.productName = (TextView) view.findViewById(R.id.productName);
            viewHolder.validTime = (TextView) view.findViewById(R.id.validTime);
            viewHolder.code = (TextView) view.findViewById(R.id.code);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        final ConsumeCodeModel data = getItem(postion);
        Resources res = mContext.getResources();
        viewHolder.productName.setText(data.getProductName());
        if (getTimeByLongTime(data.getValidTime()).equals("")) {
            viewHolder.validTime.setText(res.getString(R.string.valid_time, res.getString(R.string.code_consume_always)));
        } else {
            viewHolder.validTime.setText(res.getString(R.string.valid_time, getTimeByLongTime(data.getValidTime())));
        }
        viewHolder.code.setText(res.getString(R.string.consume_code_value, StringUtils.consumeCodeFormat(data.getCode())));
        return view;
    }

    public String getTimeByLongTime(long time) {
        if (time <= 0l) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date(time));
    }

    private static class ViewHolder {
        TextView productName;
        TextView validTime;
        TextView code;
    }
}
