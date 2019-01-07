package com.ksider.mobile.android.WebView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.ksider.mobile.android.activity.fragment.LoginFragment;
import com.ksider.mobile.android.slide.SlidingLayout;

import java.util.List;

public class LoginActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base_override_toolbar);
        new SlidingLayout(this);
        customActionBar();
        Bundle args = new Bundle();
        args.putBoolean("third", true);
        Fragment fragment = Fragment.instantiate(this, LoginFragment.class.getName(), args);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_content, fragment, "login").commit();
        customActionBar();
    }

    protected void customActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_background_color));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        TextView titleTextView = (TextView) findViewById(R.id.list_title);
        if (titleTextView != null) {
            titleTextView.setText(getResources().getString(R.string.login));
        }
        findViewById(R.id.list_backbutton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity.this.onBackPressed();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
