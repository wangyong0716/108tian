package com.ksider.mobile.android.WebView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import com.ksider.mobile.android.activity.fragment.FragmentCallback;
import com.ksider.mobile.android.activity.fragment.buy.ApplyRefundFragment;
import com.ksider.mobile.android.activity.fragment.buy.RefundConfirmFragment;
import com.ksider.mobile.android.slide.SlidingLayout;
import org.json.JSONObject;

/**
 * Created by yong on 2015/6/2.
 */
public class RefundActivity extends BaseActivity implements FragmentCallback {
    private long serialNumber = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base_with_toolbar);
        new SlidingLayout(this);
        customActionBar(R.string.apply_refund);
        if (getIntent() != null) {
            serialNumber = getIntent().getLongExtra("serialNumber", -1);
            if (serialNumber != -1) {
                try {
                    Fragment fragment = new ApplyRefundFragment();
                    Bundle args = new Bundle();
                    args.putLong("serialNumber", serialNumber);
                    fragment.setArguments(args);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_content, fragment).commitAllowingStateLoss();
                } catch (Exception e) {
                    e.printStackTrace();
                    finish();
                }
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    @Override
    public void next(JSONObject response) {
        Fragment fragment = new RefundConfirmFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_content, fragment).commitAllowingStateLoss();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return false;
    }
}
