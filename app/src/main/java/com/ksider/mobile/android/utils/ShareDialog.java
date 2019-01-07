package com.ksider.mobile.android.utils;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.*;
import com.ksider.mobile.android.WebView.R;

public class ShareDialog extends Dialog {
    public ShareDialog(Context context, int theme) {
        super(context, theme);
    }

    public ShareDialog(Context context) {
        super(context);
    }

    /**
     * Helper class for creating a Share dialog
     */
    public static class Builder {
        private Context context;
        private View.OnClickListener shareToQQ, shareToWeixin, shareToFriend, shareToWeibo;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setShareToQQ(View.OnClickListener shareToQQ) {
            this.shareToQQ = shareToQQ;
            return this;
        }

        public Builder setShareToWeixin(View.OnClickListener shareToWeixin) {
            this.shareToWeixin = shareToWeixin;
            return this;
        }

        public Builder setShareToFriend(View.OnClickListener shareToFriend) {
            this.shareToFriend = shareToFriend;
            return this;
        }

        public Builder setShareToWeibo(View.OnClickListener shareToWeibo) {
            this.shareToWeibo = shareToWeibo;
            return this;
        }

        /**
         * Create the custom dialog
         */
        public ShareDialog createDialog() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final ShareDialog dialog = new ShareDialog(context, R.style.refundDialogStyle);
            dialog.setCanceledOnTouchOutside(true);
            View layout = inflater.inflate(R.layout.share_layout, null);
            dialog.addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.findViewById(R.id.share_weixin).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (shareToWeixin != null) {
                        shareToWeixin.onClick(v);
                    }
                    dialog.dismiss();
                }
            });
            layout.findViewById(R.id.share_friend).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (shareToFriend != null) {
                        shareToFriend.onClick(v);
                    }
                    dialog.dismiss();
                }
            });
            layout.findViewById(R.id.share_weibo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (shareToWeibo != null) {
                        shareToWeibo.onClick(v);
                    }
                    dialog.dismiss();
                }
            });
            layout.findViewById(R.id.share_zone).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (shareToQQ != null) {
                        shareToQQ.onClick(v);
                    }
                    dialog.dismiss();
                }
            });
            WindowManager windowManager = ((Activity) context).getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.width = (int) (display.getWidth());
            dialog.getWindow().setAttributes(lp);
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.setContentView(layout);
            return dialog;
        }

        public void show() {
            createDialog().show();
        }
    }
}
