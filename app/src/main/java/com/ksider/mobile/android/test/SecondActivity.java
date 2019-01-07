package com.ksider.mobile.android.test;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.slide.SlidingLayout;


public class SecondActivity extends ActionBarActivity {;

    SlidingLayout mSlidingLayout ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        mSlidingLayout = new SlidingLayout(this) ;
        /*设置自己定义的动画效果*/
        mSlidingLayout.setOnAnimListener(new SlidingLayout.OnAnimListener() {
            final int MAX_DEGREE = 180 ;
            @Override
            public void onAnimationSet(View view, float offSet, int offSetPix) {
                view.setPivotX(view.getWidth()/2.0F);
                view.setPivotY(view.getHeight() / 2.0F);
                View.TRANSLATION_X.set(view, (float) -offSetPix);
                View.ROTATION_Y.set(view,MAX_DEGREE*offSet);
                view.setAlpha((1-offSet));
            }
        });

    }

}
