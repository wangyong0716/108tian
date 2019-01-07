package com.ksider.mobile.android.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;

/**
 * Created by yong on 2015/5/29.
 */
public class UniversalDialog extends Dialog {
    public UniversalDialog(Context context, int theme) {
        super(context, theme);
    }

    public UniversalDialog(Context context) {
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
        private String item3;
        private View.OnClickListener item3ClickListener;
        private String hint;
        private String confirmButtonText;
        private EditText input;

        private OnClickListener confirmButtonClickListener;

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
         * Set the Dialog item3 from resource
         *
         * @param item3
         * @return
         */
        public Builder setItem3(int item3) {
            this.item3 = (String) context.getText(item3);
            return this;
        }

        /**
         * Set the Dialog item3 from String
         *
         * @param item3
         * @return
         */
        public Builder setItem3(String item3) {
            this.item3 = item3;
            return this;
        }

        /**
         * set content and clickListener for item3
         *
         * @param item3
         * @param item3ClickListener
         * @return
         */
        public Builder setItem3(int item3, View.OnClickListener item3ClickListener) {
            this.item3 = (String) context.getText(item3);
            this.item3ClickListener = item3ClickListener;
            return this;
        }

        /**
         * set content and clickListener for item3
         *
         * @param item3
         * @param item3ClickListener
         * @return
         */
        public Builder setItem3(String item3, View.OnClickListener item3ClickListener) {
            this.item3 = item3;
            this.item3ClickListener = item3ClickListener;
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
         * Set the confirm button resource and it's listener
         *
         * @param confirmButtonText
         * @param listener
         * @return
         */
        public Builder setConfirmButton(int confirmButtonText, OnClickListener listener) {
            this.confirmButtonText = (String) context.getText(confirmButtonText);
            this.confirmButtonClickListener = listener;
            return this;
        }

        /**
         * Set the confirm button text and it's listener
         *
         * @param confirmButtonText
         * @param listener
         * @return
         */
        public Builder setConfirmButton(String confirmButtonText, OnClickListener listener) {
            this.confirmButtonText = confirmButtonText;
            this.confirmButtonClickListener = listener;
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
        public UniversalDialog createDialog() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final UniversalDialog dialog = new UniversalDialog(context, R.style.refundDialogStyle);
            dialog.setCanceledOnTouchOutside(true);
            View layout = inflater.inflate(R.layout.universal_dialog, null);
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
            //set item3
            if (item3 != null) {
                ((TextView) layout.findViewById(R.id.item3)).setText(item3);
                if (item3ClickListener != null) {
                    layout.findViewById(R.id.item3).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            item3ClickListener.onClick(v);
                            dialog.dismiss();
                        }
                    });
                }
            } else {
                ((TextView) layout.findViewById(R.id.item3)).setVisibility(View.GONE);
            }

            //set hint for edittext
            if (hint != null) {
                input = (EditText) layout.findViewById(R.id.input);
                input.setHint(hint);
            } else {
                ((EditText) layout.findViewById(R.id.input)).setVisibility(View.GONE);
            }

            //set confirm button
            if (confirmButtonText != null) {
                ((TextView) layout.findViewById(R.id.confirm_button)).setText(confirmButtonText);
                if (confirmButtonClickListener != null) {
                    layout.findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            confirmButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                            dialog.dismiss();
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.confirm_button).setVisibility(View.GONE);
            }
            dialog.setContentView(layout);
            return dialog;
        }

        public void show() {
            createDialog().show();
        }
    }
}
