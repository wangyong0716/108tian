package com.ksider.mobile.android.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;

/**
 * Created by yong on 2015/5/29.
 */
public class CommentDialog extends Dialog {
    public CommentDialog(Context context, int theme) {
        super(context, theme);
    }

    public CommentDialog(Context context) {
        super(context);
    }

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
        private Context context;
        private String title;
        private String hint;
        private String positiveButtonText;
        private String negativeButtonText;
        private EditText text;

        private OnClickListener positiveButtonClickListener, negativeButtonClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Set the Dialog message from String
         *
         * @param hint
         * @return
         */
        public Builder setHint(String hint) {
            this.hint = hint;
            return this;
        }

        /**
         * Set the Dialog message from resource
         *
         * @param hint
         * @return
         */
        public Builder setHint(int hint) {
            this.hint = (String) context.getText(hint);
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
         * set title for dialog with string id
         *
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        /**
         * set title for dialog with string
         *
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * get the message input area
         *
         * @return
         */
        public EditText getEditText() {
            return text;
        }

        /**
         * get the message
         *
         * @return
         */
        public String getInputContent() {
            if (text == null) {
                return "";
            }
            return getEditText().getText().toString().trim();
        }

        /**
         * Create the custom dialog
         */
        public CommentDialog createDialog() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final CommentDialog dialog = new CommentDialog(context, R.style.refundDialogStyle);
            dialog.setCanceledOnTouchOutside(true);
            View layout = inflater.inflate(R.layout.comment_input_layout, null);
            text = (EditText) layout.findViewById(R.id.consult_content);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            if (positiveButtonText != null) {
                if (positiveButtonClickListener != null) {
                    ((LinearLayout) layout.findViewById(R.id.confirm)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                            positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.confirm).setVisibility(View.GONE);
            }
//
            if (negativeButtonText != null) {
                if (negativeButtonClickListener != null) {
                    ((LinearLayout) layout.findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                            negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.cancel).setVisibility(View.GONE);
            }

            if (title!=null){
                ((TextView)layout.findViewById(R.id.consult_input_title)).setText(title);
            }

            if (hint != null) {
                ((EditText) layout.findViewById(R.id.consult_content)).setHint(hint);
            }
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
            Dialog dialog = createDialog();
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            dialog.show();
        }
    }
}
