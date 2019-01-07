package com.ksider.mobile.android.WebView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;
import com.ksider.mobile.android.activity.fragment.CalendarViewFragment;
import com.ksider.mobile.android.activity.fragment.FeeDetailInfoFragment;
import com.ksider.mobile.android.activity.fragment.MarkedDetailInfoFragment;
import com.ksider.mobile.android.activity.fragment.MerchantDetailInfoFragment;

/**
 * Created by yong on 8/1/15.
 */
public class MoreDetailInfoActivity extends FragmentActivity {
    public static final int DETAIL_MERCHANT_INTO = 0;
    public static final int DETAIL_FEE_INFO = 1;
    public static final int DATE_SELECTOR = 2;
    public static final int DETAIL_FEE_DESC = 3;
    public static final int DETAIL_PURCHASE_NOTE = 4;

    private int detailType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_detail_info);
        overridePendingTransition(R.anim.activity_slide_up, R.anim.activity_slide_down);
        detailType = getIntent().getIntExtra("type", -1);
        String brief = getIntent().getStringExtra("brief");
        if (detailType == -1 || brief == null) {
            finish();
        }
        Fragment fragment = null;

        switch (detailType) {
            case DETAIL_MERCHANT_INTO:
                ((TextView) findViewById(R.id.detail_title)).setText("商家详情");
                fragment = new MerchantDetailInfoFragment();
                break;
            case DETAIL_FEE_INFO:
                ((TextView) findViewById(R.id.detail_title)).setText(getIntent().getStringExtra("title"));
                fragment = new FeeDetailInfoFragment();
                break;
            case DATE_SELECTOR:
                ((TextView) findViewById(R.id.detail_title)).setText("出行日期");
                fragment = new CalendarViewFragment();
                break;
            case DETAIL_FEE_DESC:
                ((TextView) findViewById(R.id.detail_title)).setText("套餐详情");
                fragment = new MarkedDetailInfoFragment();
                break;
            case DETAIL_PURCHASE_NOTE:
                ((TextView) findViewById(R.id.detail_title)).setText("购买须知");
                fragment = new MarkedDetailInfoFragment();
            default:
                break;
        }

        if (fragment != null) {
            Bundle args = new Bundle();
            args.putString("brief", brief);
            fragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_content, fragment).commitAllowingStateLoss();
        } else {
            finish();
        }

        findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_slide_up, R.anim.activity_slide_down);
    }
}
