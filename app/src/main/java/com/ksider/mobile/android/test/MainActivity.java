package com.ksider.mobile.android.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.slide.SlidingLayout;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new SlidingLayout(this) ;
    }



    public void gotoSecond(View view){
        Intent intent = new Intent(this,SecondActivity.class) ;
        startActivity(intent);
    }
}
