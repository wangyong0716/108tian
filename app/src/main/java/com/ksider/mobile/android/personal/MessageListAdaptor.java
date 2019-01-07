package com.ksider.mobile.android.personal;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.BaseDataModel;
import com.ksider.mobile.android.model.ReplyModel;
import com.ksider.mobile.android.utils.DateUtils;
import com.ksider.mobile.android.utils.Utils;
import com.ksider.mobile.android.view.CircularImageView;
import com.ksider.mobile.android.view.paging.PagingBaseAdapter;

/**
 * Created by yong on 2015/6/23.
 */
public class MessageListAdaptor extends PagingBaseAdapter<ReplyModel> {
    protected Activity mContext;
    private ListView mListView;

    public MessageListAdaptor(Activity context, ListView mListView) {
        mContext = context;
        this.mListView = mListView;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ReplyModel getItem(int postion) {
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
            view = LayoutInflater.from(mContext).inflate(R.layout.list_view_message_item, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.providerAvatar = (CircularImageView) view.findViewById(R.id.provider_avatar);
            viewHolder.serviceProvider = (TextView) view.findViewById(R.id.service_provider);
            viewHolder.time = (TextView) view.findViewById(R.id.time);
            viewHolder.askTime = (TextView) view.findViewById(R.id.ask_time);
            viewHolder.answer = (TextView) view.findViewById(R.id.answer);
//            viewHolder.product = (TextView) view.findViewById(R.id.product);
            viewHolder.question = (TextView) view.findViewById(R.id.question);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        final ReplyModel data = getItem(postion);
        if (data.getUserName() != null) {
            viewHolder.serviceProvider.setText(data.getUserName());
        }
        viewHolder.time.setText(DateUtils.getDefaultDurationByNow(data.getCreateTime()));
        viewHolder.askTime.setText(DateUtils.getDefaultDurationByNow(data.getOrigin().getCreateTime()));
        viewHolder.answer.setText(data.getContent());
//        viewHolder.product.setText(data.getPoiId());
        viewHolder.question.setText(data.getOrigin().getContent());
        if (data.getAvatar() != null && data.getAvatar().length() > 4) {
            viewHolder.providerAvatar.setImageResource(data.getAvatar());
        }
        if (data.isRead()) {
            view.findViewById(R.id.unread_icon).setVisibility(View.INVISIBLE);
        } else {
            view.findViewById(R.id.unread_icon).setVisibility(View.VISIBLE);
            data.setRead(true);
        }

        view.findViewById(R.id.to_product).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Utils.getLandingActivity(mContext, data.getPoiType());
                if (intent != null) {
                    intent.putExtra(BaseDataModel.id_name, data.getPoiId());
                    intent.putExtra(BaseDataModel.type_name, data.getPoiType());
                    mContext.startActivity(intent);
                }
            }
        });

        return view;
    }

    private static class ViewHolder {
        CircularImageView providerAvatar;
        TextView serviceProvider;
        TextView time;
        TextView askTime;
        TextView answer;
        TextView product;
        TextView question;
    }
}

