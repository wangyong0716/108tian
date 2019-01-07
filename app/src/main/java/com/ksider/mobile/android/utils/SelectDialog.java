package com.ksider.mobile.android.utils;

import android.app.Dialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;

/**
 * Created by yong on 2015/5/29.
 */
public class SelectDialog extends Dialog {
    public SelectDialog(Context context, int theme) {
        super(context, theme);
    }

    public SelectDialog(Context context) {
        super(context);
    }

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
        private Context context;
        private String item1Text;
        private String item2Text;
        private String item3Text;
        private String item4Text;
        private String item5Text;
        private View.OnClickListener item1ClickListener;
        private View.OnClickListener item2ClickListener;
        private View.OnClickListener item3ClickListener;
        private View.OnClickListener item4ClickListener;
        private View.OnClickListener item5ClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * add item1
         *
         * @param item1Text          content of item1
         * @param item1ClickListener clickListener of item1
         * @return
         */
        public Builder setItem1(String item1Text, View.OnClickListener item1ClickListener) {
            this.item1Text = item1Text;
            this.item1ClickListener = item1ClickListener;
            return this;
        }

        /**
         * add item1
         *
         * @param item1TextId        resource id of content of item1
         * @param item1ClickListener clickListener of item1
         * @return
         */
        public Builder setItem1(int item1TextId, View.OnClickListener item1ClickListener) {
            return setItem1((String) context.getText(item1TextId), item1ClickListener);
        }

        /**
         * add item2
         *
         * @param item2Text          content of item2
         * @param item2ClickListener clickListener of item2
         * @return
         */
        public Builder setItem2(String item2Text, View.OnClickListener item2ClickListener) {
            this.item2Text = item2Text;
            this.item2ClickListener = item2ClickListener;
            return this;
        }

        /**
         * add item2
         *
         * @param item2TextId        resource id of content of item2
         * @param item2ClickListener clickListener of item2
         * @return
         */
        public Builder setItem2(int item2TextId, View.OnClickListener item2ClickListener) {
            return setItem2((String) context.getText(item2TextId), item2ClickListener);
        }

        /**
         * add item3
         *
         * @param item3Text          content of item3
         * @param item3ClickListener clickListener of item3
         * @return
         */
        public Builder setItem3(String item3Text, View.OnClickListener item3ClickListener) {
            this.item3Text = item3Text;
            this.item3ClickListener = item3ClickListener;
            return this;
        }

        /**
         * add item3
         *
         * @param item3TextId        resource id of content of item3
         * @param item3ClickListener clickListener of item3
         * @return
         */
        public Builder setItem3(int item3TextId, View.OnClickListener item3ClickListener) {
            return setItem3((String) context.getText(item3TextId), item3ClickListener);
        }

        /**
         * add item4
         *
         * @param item4Text          content of item4
         * @param item4ClickListener clickListener of item4
         * @return
         */
        public Builder setItem4(String item4Text, View.OnClickListener item4ClickListener) {
            this.item4Text = item4Text;
            this.item4ClickListener = item4ClickListener;
            return this;
        }

        /**
         * add item4
         *
         * @param item4TextId        resource id of content of item4
         * @param item4ClickListener clickListener of item4
         * @return
         */
        public Builder setItem4(int item4TextId, View.OnClickListener item4ClickListener) {
            return setItem4((String) context.getText(item4TextId), item4ClickListener);
        }

        /**
         * add item5
         *
         * @param item5Text          content of item5
         * @param item5ClickListener clickListener of item5
         * @return
         */
        public Builder setItem5(String item5Text, View.OnClickListener item5ClickListener) {
            this.item5Text = item5Text;
            this.item5ClickListener = item5ClickListener;
            return this;
        }

        /**
         * add item5
         *
         * @param item5TextId        resource id of content of item5
         * @param item5ClickListener clickListener of item5
         * @return
         */
        public Builder setItem5(int item5TextId, View.OnClickListener item5ClickListener) {
            return setItem5((String) context.getText(item5TextId), item5ClickListener);
        }

        /**
         * Create the custom dialog
         */
        public SelectDialog createDialog() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final SelectDialog dialog = new SelectDialog(context, R.style.selectDialogStyle);
            dialog.setCanceledOnTouchOutside(true);
            View layout = inflater.inflate(R.layout.select_dialog, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            //set item1
            if (item1Text != null) {
                ((TextView) layout.findViewById(R.id.item1)).setText(item1Text);
                if (item1ClickListener != null) {
                    layout.findViewById(R.id.item1).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                            item1ClickListener.onClick(v);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.item1).setVisibility(View.GONE);
            }
            //set item2
            if (item2Text != null) {
                ((TextView) layout.findViewById(R.id.item2)).setText(item2Text);
                if (item2ClickListener != null) {
                    layout.findViewById(R.id.item2).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                            item2ClickListener.onClick(v);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.item2).setVisibility(View.GONE);
            }
            //set item3
            if (item3Text != null) {
                ((TextView) layout.findViewById(R.id.item3)).setText(item3Text);
                if (item3ClickListener != null) {
                    layout.findViewById(R.id.item3).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                            item3ClickListener.onClick(v);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.item3).setVisibility(View.GONE);
            }
            //set item4
            if (item4Text != null) {
                ((TextView) layout.findViewById(R.id.item4)).setText(item4Text);
                if (item4ClickListener != null) {
                    layout.findViewById(R.id.item4).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                            item4ClickListener.onClick(v);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.item4).setVisibility(View.GONE);
            }
            //set item5
            if (item5Text != null) {
                ((TextView) layout.findViewById(R.id.item5)).setText(item5Text);
                if (item5ClickListener != null) {
                    layout.findViewById(R.id.item5).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                            item5ClickListener.onClick(v);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.item5).setVisibility(View.GONE);
            }
            dialog.setContentView(layout);
            return dialog;
        }

        public void show() {
            Dialog dialog = createDialog();
            Window dialogWindow = dialog.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.x = 0;
            lp.y = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics()
            );
            dialogWindow.setAttributes(lp);
            dialogWindow.setGravity(Gravity.RIGHT | Gravity.TOP);
            dialog.show();
        }
    }
}
