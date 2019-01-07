package com.ksider.mobile.android.partion;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.Constants;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.utils.BasicCategory;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

/**
 * Created by yong on 2015/6/2.
 */
public class MarkView extends LinearLayout {
    private Context context;
    private boolean refundAble;
    private String phone;
    private String productId;
    private boolean hasProduct;
    private BasicCategory category;

    public MarkView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MarkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public MarkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.partion_mark_view, this);
    }

    public void setValues(String productId, BasicCategory category, String phone) {
        this.productId = productId;
        this.category = category;
        this.phone = phone;
        findViewById(R.id.consult_icon).setOnClickListener(listener);
    }

    private OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
            context.startActivity(phoneIntent);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("productId", productId == null ? "" : productId);
            if (hasProduct) {
                map.put("hasProduct", "true");
            } else {
                map.put("hasProduct", "false");
            }
            switch (category) {
                case ATTRACTIONS:
                    MobclickAgent.onEvent(context, Constants.UMENG_STATISTICS_TAG_SCENE, map);
                    break;
                case RESORT:
                case FARMYARD:
                    MobclickAgent.onEvent(context, Constants.UMENG_STATISTICS_TAG_RESORT, map);
                    break;
                case PICKINGPART:
                    MobclickAgent.onEvent(context, Constants.UMENG_STATISTICS_TAG_PICK, map);
                    break;
                case ACTIVITY:
                    MobclickAgent.onEvent(context, Constants.UMENG_STATISTICS_TAG_EVENT, map);
                    break;
                case GUIDE:
                    MobclickAgent.onEvent(context, Constants.UMENG_STATISTICS_TAG_RECOMMEND, map);
                    break;
                default:
                    break;
            }
            MobclickAgent.onEvent(context, Constants.UMENG_STATISTICS_CONSULT);
        }
    };

    public void setRefundAble(boolean refundAble) {
        this.refundAble = refundAble;
        TextView refundStatus = (TextView) findViewById(R.id.refund_status);
        Drawable drawable;
        if (refundAble) {

            drawable = getResources().getDrawable(R.drawable.refund_icon);

        } else {
            drawable = getResources().getDrawable(R.drawable.unrefund_icon);
        }
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        refundStatus.setCompoundDrawables(null, drawable, null, null);
    }

    public void setHasProduct(boolean hasProduct) {
        this.hasProduct = hasProduct;
    }
}
