package com.ksider.mobile.android.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;

/**
 * Created by yong on 2015/5/29.
 */
public class CustomDialog extends Dialog {
    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }

    public CustomDialog(Context context) {
        super(context);
    }

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
        private Context context;
        private String title;
        private String item1;
        private View.OnClickListener item1ClickListener;
        private String item2;
        private View.OnClickListener item2ClickListener;
        private String hint;
        private String positiveButtonText;
        private String negativeButtonText;
        private EditText input;
        private View contentView;

        private OnClickListener positiveButtonClickListener, negativeButtonClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Set the Dialog title from String
         *
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set the Dialog title from resource
         *
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        /**
         * Set the Dialog item1 from resource
         *
         * @param item1
         * @return
         */
        public Builder setItem1(int item1) {
            this.item1 = (String) context.getText(item1);
            return this;
        }

        /**
         * Set the Dialog item1 from String
         *
         * @param item1
         * @return
         */
        public Builder setItem1(String item1) {
            this.item1 = item1;
            return this;
        }

        /**
         * set content and clickListener for item1
         *
         * @param item1
         * @param item1ClickListener
         * @return
         */
        public Builder setItem1(String item1, View.OnClickListener item1ClickListener) {
            this.item1 = item1;
            this.item1ClickListener = item1ClickListener;
            return this;
        }

        /**
         * set content and clickListener for item1
         *
         * @param item1
         * @param item1ClickListener
         * @return
         */
        public Builder setItem1(int item1, View.OnClickListener item1ClickListener) {
            this.item1 = (String) context.getText(item1);
            this.item1ClickListener = item1ClickListener;
            return this;
        }

        /**
         * Set the Dialog item2 from resource
         *
         * @param item2
         * @return
         */
        public Builder setItem2(int item2) {
            this.item2 = (String) context.getText(item2);
            return this;
        }

        /**
         * Set the Dialog item2 from String
         *
         * @param item2
         * @return
         */
        public Builder setItem2(String item2) {
            this.item2 = item2;
            return this;
        }

        /**
         * set content and clickListener for item2
         *
         * @param item2
         * @param item2ClickListener
         * @return
         */
        public Builder setItem2(int item2, View.OnClickListener item2ClickListener) {
            this.item2 = (String) context.getText(item2);
            this.item2ClickListener = item2ClickListener;
            return this;
        }

        /**
         * set content and clickListener for item2
         *
         * @param item2
         * @param item2ClickListener
         * @return
         */
        public Builder setItem2(String item2, View.OnClickListener item2ClickListener) {
            this.item2 = item2;
            this.item2ClickListener = item2ClickListener;
            return this;
        }

        /**
         * Set the EditText hint from resource
         *
         * @param hint
         * @return
         */
        public Builder setHint(int hint) {
            this.hint = (String) context.getText(hint);
            return this;
        }

        /**
         * Set the EditText hint from String
         *
         * @param hint
         * @return
         */
        public Builder setHint(String hint) {
            this.hint = hint;
            return this;
        }

        /**
         * Set a custom content view for the Dialog.
         * If a message is set, the contentView is not
         * added to the Dialog...
         *
         * @param v
         * @return
         */
        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }

        /**
         * Set the positive button resource and it's listener
         *
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText, OnClickListener listener) {
            this.positiveButtonText = (String) context.getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }

        /**
         * Set the positive button text and it's listener
         *
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(String positiveButtonText, OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        /**
         * Set the negative button resource and it's listener
         *
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(int negativeButtonText, OnClickListener listener) {
            this.negativeButtonText = (String) context.getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }

        /**
         * Set the negative button text and it's listener
         *
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(String negativeButtonText, OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }

        /**
         * get input content
         *
         * @return
         */
        public String getInput() {
            if (input == null) {
                return "";
            }
            return input.getText().toString().trim();
        }

        /**
         * Create the custom dialog
         */
        public CustomDialog createDialog() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final CustomDialog dialog = new CustomDialog(context, R.style.refundDialogStyle);
            dialog.setCanceledOnTouchOutside(true);
            View layout = inflater.inflate(R.layout.custom_dialog, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            //set title
            if (title != null) {
                ((TextView) layout.findViewById(R.id.title)).setText(title);
            } else {
                layout.findViewById(R.id.title).setVisibility(View.GONE);
            }
            //set item1
            if (item1 != null) {
                ((TextView) layout.findViewById(R.id.item1)).setText(item1);
                if (item1ClickListener != null) {
                    layout.findViewById(R.id.item1).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            item1ClickListener.onClick(v);
                            dialog.dismiss();
                        }
                    });
                }
            } else {
                ((TextView) layout.findViewById(R.id.item1)).setVisibility(View.GONE);
            }
            //set item2
            if (item2 != null) {
                ((TextView) layout.findViewById(R.id.item2)).setText(item2);
                if (item2ClickListener != null) {
                    layout.findViewById(R.id.item2).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            item2ClickListener.onClick(v);
                            dialog.dismiss();
                        }
                    });
                }
            } else {
                ((TextView) layout.findViewById(R.id.item2)).setVisibility(View.GONE);
            }

            //set hint for edittext
            if (hint != null) {
                input = (EditText) layout.findViewById(R.id.input);
                input.setHint(hint);
            } else {
                ((EditText) layout.findViewById(R.id.input)).setVisibility(View.GONE);
            }
            //set content
            if (contentView != null) {
                ((LinearLayout) layout.findViewById(R.id.content)).removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.content)).addView(contentView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            } else {
                ((LinearLayout) layout.findViewById(R.id.content)).setVisibility(View.GONE);
            }

            //set positive button
            if (positiveButtonText != null) {
                ((TextView) layout.findViewById(R.id.positive_text)).setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    ((RelativeLayout) layout.findViewById(R.id.positive_button)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                            dialog.dismiss();
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.positive_button).setVisibility(View.GONE);
            }

            //set negative button
            if (negativeButtonText != null) {
                ((TextView) layout.findViewById(R.id.negative_text)).setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    ((RelativeLayout) layout.findViewById(R.id.negative_button)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                            dialog.dismiss();
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.negative_button).setVisibility(View.GONE);
            }
            if (positiveButtonText == null && negativeButtonText == null) {
                layout.findViewById(R.id.buttons).setVisibility(View.GONE);
            }else if (positiveButtonText == null && negativeButtonText != null){
                layout.findViewById(R.id.negative_button).setBackgroundResource(R.drawable.refund_neutral_button_background);
            }
            dialog.setContentView(layout);
            return dialog;
        }
        public void show() {
            createDialog().show();
        }
    }
}
