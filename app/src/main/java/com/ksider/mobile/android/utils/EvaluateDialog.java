package com.ksider.mobile.android.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;

/**
 * Created by yong on 2015/5/29.
 */
public class EvaluateDialog extends Dialog {
    public EvaluateDialog(Context context, int theme) {
        super(context, theme);
    }

    public EvaluateDialog(Context context) {
        super(context);
    }

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
        private Context context;
        private String hint;
        private EditText text;
        private OnEditDoneListener onEditDoneListener;
        private OnDismissListener dismissListener;

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

        public interface OnEditDoneListener {
            public void editDone(String content);
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

        public Builder setDismissListener(OnDismissListener dismissListener) {
            this.dismissListener = dismissListener;
            return this;
        }

        //set listener of finishing edit
        public Builder setOnEditDoneListener(OnEditDoneListener onEditDoneListener) {
            this.onEditDoneListener = onEditDoneListener;
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
        public EvaluateDialog createDialog() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final EvaluateDialog dialog = new EvaluateDialog(context, R.style.refundDialogStyle);
            dialog.setCanceledOnTouchOutside(true);
            View layout = inflater.inflate(R.layout.evaluate_input_layout, null);
            text = (EditText) layout.findViewById(R.id.evaluate_content);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            if (hint != null) {
                text.setHint(hint);
            }

            text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        //hide soft-keyboard
                        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm.isActive()) {
                            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                        }

                        if (onEditDoneListener != null) {
                            onEditDoneListener.editDone(getInputContent());
                        }
                        dialog.dismiss();
                        return true;
                    }
                    return false;
                }
            });

            if (dismissListener != null) {
                dialog.setOnDismissListener(dismissListener);
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
