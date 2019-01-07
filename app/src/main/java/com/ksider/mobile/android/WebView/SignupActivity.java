package com.ksider.mobile.android.WebView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.ksider.mobile.android.activity.fragment.signup.ChildCallback;
import com.ksider.mobile.android.activity.fragment.signup.SetPasswordFragment;
import com.ksider.mobile.android.activity.fragment.signup.VerifyCodeFragment;
import com.ksider.mobile.android.activity.fragment.signup.VerifyFragment;
import com.ksider.mobile.android.slide.SlidingLayout;

public class SignupActivity extends BaseActivity implements ChildCallback {
    Bundle mArgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sign_base);
        new SlidingLayout(this);
        if (savedInstanceState == null) {
            Fragment fragment = new VerifyCodeFragment();
            mArgs = getIntent().getExtras();
            if (mArgs == null) {
                mArgs = new Bundle();
                mArgs.putBoolean("reset", false);
            }
            if (mArgs.getBoolean("reset")) {
                customActionBar(R.string.reset_pwd);
            } else {
                customActionBar(R.string.signup);
            }
            fragment.setArguments(mArgs);
            getSupportFragmentManager().beginTransaction().add(R.id.content, fragment).commit();
        }
    }

    @Override
    public void onStageChange(VerifyFragment.VerifyStage stage, VerifyFragment.VerifyFactor factor) {

        switch (stage) {
            case SET_PASSWORD:
                step3SetPassword(factor);
                break;
            default:
                break;
        }
    }

    private void step3SetPassword(VerifyFragment.VerifyFactor factor) {
        try {
            Fragment fragment = new SetPasswordFragment();
            Bundle args = (Bundle) mArgs.clone();
            args.putString(SetPasswordFragment.ARG_SESSION, factor.sid);
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
