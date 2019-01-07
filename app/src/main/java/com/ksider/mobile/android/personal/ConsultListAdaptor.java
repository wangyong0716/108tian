package com.ksider.mobile.android.personal;

import android.app.Activity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.model.ConsultModel;
import com.ksider.mobile.android.utils.DateUtils;
import com.ksider.mobile.android.utils.ImageUtils;
import com.ksider.mobile.android.view.CircularImageView;
import com.ksider.mobile.android.view.paging.PagingBaseAdapter;

/**
 * Created by yong on 2015/6/23.
 */
public class ConsultListAdaptor extends PagingBaseAdapter<ConsultModel> {
    protected Activity mContext;

    public ConsultListAdaptor(Activity context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ConsultModel getItem(int position) {
        if (0 <= position && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        View view = convertView;
        final ConsultModel consult = getItem(position);
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_view_consult_item, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.avatar = (CircularImageView) view.findViewById(R.id.avatar);
            viewHolder.userName = (TextView) view.findViewById(R.id.user_name);
            viewHolder.createTime = (TextView) view.findViewById(R.id.create_time);
            viewHolder.content = (TextView) view.findViewById(R.id.content);
            viewHolder.optCount = (TextView) view.findViewById(R.id.opt_count);
            viewHolder.replyCount = (TextView) view.findViewById(R.id.reply_count);
            viewHolder.replyContainer = (LinearLayout) view.findViewById(R.id.reply_container);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        if (!consult.getAvatar().equals("")) {
            viewHolder.avatar.setImageResource(ImageUtils.formatImageUrl(
                    consult.getAvatar(), ImageUtils.THUMBNAIL));
        } else {
            viewHolder.avatar.setImageResource(R.drawable.default_avatar);
        }
        viewHolder.userName.setText(consult.getUserName());
        viewHolder.createTime.setText(DateUtils.getDefaultDurationByNow(consult.getCreateTime()));
        viewHolder.content.setText(consult.getContent());
        viewHolder.optCount.setText(consult.getThumbsUp() + "");
        viewHolder.replyCount.setText(consult.getReplies().size() + "");
        viewHolder.replyCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open or close the other replies
                if (ConsultModel.DEFAULT_PRESHOW_REPLY_NUM >= consult.getShowNum()) {
                    if (consult.getShowNum() <= 0 && consult.getReplies().size() > 0) {
                        viewHolder.replyContainer.setVisibility(View.VISIBLE);
                    }
                    for (int i = viewHolder.replyContainer.getChildCount(); i < consult.getReplies().size(); i++) {
                        addReply(viewHolder.replyContainer, consult.getReplies().get(i), consult.getUserId());
                    }
                    consult.setShowNum(consult.getReplies().size());
                } else {
                    int min = Math.min(ConsultModel.DEFAULT_PRESHOW_REPLY_NUM, consult.getShowNum());
                    for (int i = viewHolder.replyContainer.getChildCount() - 1; i >= min; i--) {
                        viewHolder.replyContainer.removeViewAt(i);
                    }
                    if (min <= 0) {
                        viewHolder.replyContainer.setVisibility(View.GONE);
                    }
                    consult.setShowNum(min);
                }
            }
        });
        viewHolder.optCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // +1 or -1 to optCount
                if (!consult.isOpt()) {
                    consult.setThumbsUp(consult.getThumbsUp() + 1);
                    ((ConsultListActivity) mContext).addOpt(consult.getId());
                    consult.setOpt(true);
                }
                viewHolder.optCount.setText(consult.getThumbsUp() + "");
            }
        });
        LinearLayout question = (LinearLayout) view.findViewById(R.id.consult_question);
        question.setTag(consult);
        question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConsultModel model = (ConsultModel) v.getTag();
                ((ConsultListActivity) mContext).openInput(model.getUserName(), model.getId(), model.getUserId());
            }
        });
        if (consult.getShowNum() <= 0) {
            viewHolder.replyContainer.removeAllViews();
            viewHolder.replyContainer.setVisibility(View.GONE);
            return view;
        }
        viewHolder.replyContainer.setVisibility(View.VISIBLE);
        //fill data into the item that can be reused
        int min = Math.min(consult.getShowNum(), viewHolder.replyContainer.getChildCount());
        for (int i = 0; i < min; i++) {
            fillReply(viewHolder.replyContainer.getChildAt(i), consult.getReplies().get(i), consult.getUserId());
        }
        //delete view that won't be used
        for (int i = viewHolder.replyContainer.getChildCount() - 1; i >= consult.getShowNum(); i--) {
            viewHolder.replyContainer.removeViewAt(i);
        }
        //add lack view
        for (int i = viewHolder.replyContainer.getChildCount(); i < consult.getShowNum(); i++) {
            addReply(viewHolder.replyContainer, consult.getReplies().get(i), consult.getUserId());
        }
        return view;
    }

    public void diffReplyColor(TextView textView, String parentName, String content) {
        String preFix = "@ " + parentName + "： ";
        SpannableStringBuilder builder = new SpannableStringBuilder(preFix + content);

        ForegroundColorSpan mainColor = new ForegroundColorSpan(mContext.getResources().getColor(R.color.black_0));
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan((int) mContext.getResources().getDimension(R.dimen.consult_input_reply_parent_name));
        builder.setSpan(mainColor, 0, preFix.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(absoluteSizeSpan, 0, preFix.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(builder);
    }

    /**
     * add reply to consult
     */
    public void addReply(ViewGroup container, ConsultModel consult, String genId) {
        View view = mContext.getLayoutInflater().inflate(R.layout.list_view_consult_item_item, null);
        fillReply(view, consult, genId);
        container.addView(view);
    }

    /**
     * fill reply to the exit view of consult
     */
    public void fillReply(View view, ConsultModel consult, final String genId) {
        CircularImageView avatar = (CircularImageView) view.findViewById(R.id.avatar);
        if (!consult.getAvatar().equals("")) {
            avatar.setImageResource(ImageUtils.formatImageUrl(
                    consult.getAvatar(), ImageUtils.THUMBNAIL));
        } else {
            avatar.setImageResource(R.drawable.default_avatar);
        }
        ((TextView) view.findViewById(R.id.user_name)).setText(consult.getUserName());
        ((TextView) view.findViewById(R.id.create_time)).setText(DateUtils.getDefaultDurationByNow(consult.getCreateTime()));
        diffReplyColor((TextView) view.findViewById(R.id.content), consult.getParentName(), consult.getContent());
        view.setTag(consult);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConsultModel model = (ConsultModel) v.getTag();
                ((ConsultListActivity) mContext).openInput(model.getUserName(), model.getId(), genId);
            }
        });
    }

    private static class ViewHolder {
        CircularImageView avatar;
        TextView userName;
        TextView createTime;
        TextView content;
        LinearLayout replyContainer;
        TextView optCount;
        TextView replyCount;
    }
}

