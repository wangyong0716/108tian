package com.ksider.mobile.android.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ksider.mobile.android.WebView.R;

import java.util.Calendar;

/**
 * Created by yong on 10/20/15.
 */
public class DateDialog extends Dialog {
    public DateDialog(Context context, int theme) {
        super(context, theme);
    }

    public DateDialog(Context context) {
        super(context);
    }

    /**
     * The callback used to indicate the user is done filling in the date.
     */
    public interface OnDateSetListener {
        void onDateSet(DatePicker startDatePicker, int year, int monthOfYear, int dayOfMonth);
    }

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
        private Context context;
        private String title;
        private String positiveButtonText;
        private String negativeButtonText;

        private View.OnClickListener positiveButtonClickListener, negativeButtonClickListener;

        private OnDateSetListener callBack;
        private DatePicker mDatePicker;
        private int year;
        private int monthOfYear;
        private int dayOfMonth;
        private long minTime;
        private long maxTime;

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

        public Builder setCallBack(OnDateSetListener callBack) {
            this.callBack = callBack;
            return this;
        }

        public Builder setTime(long time) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(time);
            this.year = cal.get(Calendar.YEAR);
            this.monthOfYear = cal.get(Calendar.MONTH);
            this.dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            return this;
        }

        public Builder setDate(int year, int monthOfYear, int dayOfMonth) {
            this.year = year;
            this.monthOfYear = monthOfYear;
            this.dayOfMonth = dayOfMonth;
            return this;
        }

        public Builder setMinTime(long minTime) {
            this.minTime = minTime;
            return this;
        }

        public Builder setMinDate(int year, int monthOfYear, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth);
            setMinTime(calendar.getTimeInMillis());
            return this;
        }

        public Builder setMaxTime(long maxTime) {
            this.maxTime = maxTime;
            return this;
        }

        public Builder setMaxDate(int year, int monthOfYear, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth);
            setMaxTime(calendar.getTimeInMillis());
            return this;
        }

        /**
         * Set the positive button resource and it's listener
         *
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText, View.OnClickListener listener) {
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
        public Builder setPositiveButton(String positiveButtonText, View.OnClickListener listener) {
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
        public Builder setNegativeButton(int negativeButtonText, View.OnClickListener listener) {
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
        public Builder setNegativeButton(String negativeButtonText, View.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }

        /**
         * Create the custom dialog
         */
        private DateDialog createDialog() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final DateDialog dialog = new DateDialog(context, R.style.couponDialogStyle);
            dialog.setCanceledOnTouchOutside(true);
            View layout = inflater.inflate(R.layout.date_picker_dialog, null);
            dialog.addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mDatePicker = (DatePicker) layout.findViewById(R.id.datePicker);
            //set title
            if (title != null) {
                ((TextView) layout.findViewById(R.id.dialog_title)).setText(title);
            } else {
                layout.findViewById(R.id.title).setVisibility(View.GONE);
            }
            //init time
            if (minTime > 0) {
                mDatePicker.setMinDate(minTime);
            }
            if (maxTime >= minTime) {
                mDatePicker.setMaxDate(maxTime);
            }

            mDatePicker.init(year, monthOfYear, dayOfMonth, new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    if (callBack != null) {
                        callBack.onDateSet(view, year, monthOfYear, dayOfMonth);
                    }
                }
            });
            if (callBack != null) {
                callBack.onDateSet(mDatePicker, year, monthOfYear, dayOfMonth);
            }

            //set positive button
            if (positiveButtonText != null) {
                ((TextView) layout.findViewById(R.id.positive_text)).setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    ((RelativeLayout) layout.findViewById(R.id.positive_button)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            positiveButtonClickListener.onClick(v);
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
                        @Override
                        public void onClick(View v) {
                            negativeButtonClickListener.onClick(v);
                            dialog.dismiss();
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.negative_button).setVisibility(View.GONE);
            }

            dialog.setContentView(layout);
            return dialog;
        }


        public void show() {
            createDialog().show();
        }
    }
}
