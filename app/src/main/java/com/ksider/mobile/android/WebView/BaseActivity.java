package com.ksider.mobile.android.WebView;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.ksider.mobile.android.view.pulltozoomview.PullToZoomScrollView;

public abstract class BaseActivity extends ActionBarActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected void proccessBack() {
        onBackPressed();
    }

    /**
     * 必须在调用setcontentview之后用
     * <p/>
     * *
     */
    protected void initHeadBanner() {
        View backbutton = findViewById(R.id.list_backbutton);
        backbutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                proccessBack();
            }
        });
    }

    protected void customActionBar(int titleId) {
        String title = getResources().getString(titleId);
        customActionBar(title);
    }

    protected void customActionBar() {
        customActionBar("");
    }

    /**
     * 必须在调用setcontentview之后用
     * <p/>
     * *
     */
    @SuppressWarnings("deprecation")
    protected void customActionBar(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_background_color));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        TextView titleTextView = (TextView) findViewById(R.id.list_title);
        if (titleTextView != null) {
            titleTextView.setText(title);
        }

        final ImageView backButton = (ImageView) findViewById(R.id.list_backbutton);
        backButton.setImageResource(R.drawable.backbutton_icon);
        backButton.getDrawable().setAlpha(255);

        findViewById(R.id.list_backbutton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                proccessBack();
            }
        });

        final PullToZoomScrollView scroll = (PullToZoomScrollView) findViewById(R.id.scrollView);
        if (scroll != null) {
            toolbar.getBackground().setAlpha(0);
            final ImageView background = (ImageView) findViewById(R.id.background);

            final ImageView collection = (ImageView) findViewById(R.id.collect);
            final ImageView share = (ImageView) findViewById(R.id.share);
            final ImageView line = (ImageView) findViewById(R.id.line);

            background.setImageResource(R.color.toolbar_background_color);
            backButton.setImageResource(R.drawable.backbutton_white_icon);
            collection.setImageResource(R.drawable.collect_white_icon);
            share.setImageResource(R.drawable.share_white_icon);
            line.setImageResource(R.color.divider_line_color);
            background.getDrawable().setAlpha(0);
            backButton.getDrawable().setAlpha(255);
            collection.getDrawable().setAlpha(255);
            share.getDrawable().setAlpha(255);
            line.getDrawable().setAlpha(0);

            final float imgHeight = (int) getResources().getDimension(R.dimen.header_image_height);
            final float height = (int) getResources().getDimension(R.dimen.header_banner_height);
            final float ratio0 = 255 / imgHeight;
            final float ratio1 = 255 / height;
            final float ratio2 = 255 / (imgHeight - height);

            scroll.setOnScrollListener(new PullToZoomScrollView.OnScrollViewChangedListener() {
                @Override
                public void onScrollChanged(int left, int top, int oldLeft, int oldTop) {
                    if (top >= 0) {
                        if (top < height) {
                            backButton.setImageResource(R.drawable.backbutton_white_icon);
                            collection.setImageResource(R.drawable.collect_white_icon);
                            share.setImageResource(R.drawable.share_white_icon);
                            int alpha = (int) ((height - top) * ratio1);
                            backButton.getDrawable().setAlpha(alpha);
                            collection.getDrawable().setAlpha(alpha);
                            share.getDrawable().setAlpha(alpha);
                        } else if (top <= imgHeight) {
                            backButton.setImageResource(R.drawable.backbutton_icon);
                            collection.setImageResource(R.drawable.collect_icon);
                            share.setImageResource(R.drawable.share_icon);
                            int alpha = (int) ((top - height) * ratio2);
                            alpha = alpha < 255 ? alpha : 255;
                            backButton.getDrawable().setAlpha(alpha);
                            collection.getDrawable().setAlpha(alpha);
                            share.getDrawable().setAlpha(alpha);
                        } else {
                            backButton.getDrawable().setAlpha(255);
                            collection.getDrawable().setAlpha(255);
                            share.getDrawable().setAlpha(255);
                        }
                        if (top < imgHeight) {
                            int alpha = (int) (top * ratio0);
                            alpha = alpha < 240 ? alpha : 240;
                            background.getDrawable().setAlpha(alpha);
                            line.getDrawable().setAlpha(alpha);
                        } else {
                            background.getDrawable().setAlpha(240);
                            line.getDrawable().setAlpha(240);
                        }
                    } else {
                        backButton.getDrawable().setAlpha(255);
                        collection.getDrawable().setAlpha(255);
                        share.getDrawable().setAlpha(255);
                        background.getDrawable().setAlpha(0);
                        line.getDrawable().setAlpha(0);
                    }
                }

            });
        }
    }

    public void setBanner(int ratio) {
        ImageView backButton = (ImageView) findViewById(R.id.list_backbutton);
        ImageView collection = (ImageView) findViewById(R.id.collect);
        ImageView share = (ImageView) findViewById(R.id.share);

        backButton.setImageResource(R.drawable.backbutton_white_icon);
        collection.setImageResource(R.drawable.collect_white_icon);
        share.setImageResource(R.drawable.share_white_icon);
        backButton.setAlpha(1.0f);
        collection.setAlpha(1.0f);
        share.setAlpha(1.0f);
    }

    protected void setTitle(String title) {
        TextView textView = (TextView) findViewById(R.id.list_title);
        if (textView != null) {
            textView.setText(title);
        }
    }

    protected void setTextView(int viewId, String value) {
        if (value != null) {
            TextView text = (TextView) findViewById(viewId);
            if (text != null) {
                text.setText(value);
            }
        }
    }

    protected void setTextViewBold(int viewId) {
        TextView text = (TextView) findViewById(viewId);
        if (text != null) {
            text.getPaint().setFakeBoldText(true);
        }
    }
}
