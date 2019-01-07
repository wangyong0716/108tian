package com.ksider.mobile.android.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;
import com.ksider.mobile.android.view.LoadImageView;

/**
 * Created by yong on 2015/5/29.
 */
public class CouponDialog extends Dialog {
    public CouponDialog(Context context, int theme) {
        super(context, theme);
    }

    public CouponDialog(Context context) {
        super(context);
    }

    public static class Builder {
        private Context context;
        private String imgUrl;
        private String buttonText;
        private View.OnClickListener imgClickListener;
        private View.OnClickListener buttonClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
            return this;
        }

        public Builder setButtonText(String buttonText) {
            this.buttonText = buttonText;
            return this;
        }

        public Builder setImgClickListener(View.OnClickListener imgClickListener) {
            this.imgClickListener = imgClickListener;
            return this;
        }

        public Builder setButtonClickListener(View.OnClickListener buttonClickListener) {
            this.buttonClickListener = buttonClickListener;
            return this;
        }

        public Builder setImg(String imgUrl, View.OnClickListener imgClickListener) {
            this.imgUrl = imgUrl;
            this.imgClickListener = imgClickListener;
            return this;
        }

        public Builder setButton(String buttonText, View.OnClickListener buttonClickListener) {
            this.buttonText = buttonText;
            this.buttonClickListener = buttonClickListener;
            return this;
        }

        /**
         * Create the custom dialog
         */
        public CouponDialog createDialog() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final CouponDialog dialog = new CouponDialog(context, R.style.couponDialogStyle);
            dialog.setCanceledOnTouchOutside(false);
            View layout = inflater.inflate(R.layout.coupon_dialog, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            int screenWidth = ((Activity) context).getWindowManager().getDefaultDisplay().getWidth();       // 屏幕宽（像素，如：480px）
            LoadImageView img = (LoadImageView) layout.findViewById(R.id.img);
            int width = screenWidth - 60;
            int height = (int) (width * 1.2345f);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
            img.setLayoutParams(layoutParams);

            if (imgUrl != null) {
                ((LoadImageView) layout.findViewById(R.id.img)).setImageResource(imgUrl);
            } else {
//                ((LoadImageView) layout.findViewById(R.id.img)).setImageDrawable(context.getResources().getDrawable(R.drawable.lead_01));
            }
            if (imgClickListener != null) {
                layout.findViewById(R.id.img).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        imgClickListener.onClick(v);
                        dialog.dismiss();
                    }
                });
            }
            if (buttonText != null) {
                ((TextView) layout.findViewById(R.id.close)).setText(buttonText);
            }
            if (buttonClickListener != null) {
                layout.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        buttonClickListener.onClick(v);
                        dialog.dismiss();
                    }
                });
            }
            dialog.setContentView(layout);
            return dialog;
        }

        public void show() {
            createDialog().show();
        }
    }
}
