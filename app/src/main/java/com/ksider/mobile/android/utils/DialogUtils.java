package com.ksider.mobile.android.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * 弹窗工具类
 *
 * @author mashengchao 2012-3-19 下午12:05:14
 */
public class DialogUtils {
    /**
     * 弹出框，带button
     * <p/>
     * 如果确定事件未定义，点击按钮默认为消失弹出框
     * <p/>
     * 如果第二个按钮未定义，不显示第二个按钮;
     *
     * @param context
     * @param title
     * @param msg
     * @param icon
     * @param btn1
     * @param btn2
     * @param clickListener
     * @param cancelClickListener
     * @author mashengchao 2012-3-19 下午2:02:56
     */
    public static void showDialogWithButton(Activity context, String title,
                                            CharSequence msg, int icon, String btn1, String btn2,
                                            DialogInterface.OnClickListener clickListener,
                                            DialogInterface.OnClickListener cancelClickListener) {
        final AlertDialog tDialog = new AlertDialog.Builder(context).create();

        if (icon > 0) {
            tDialog.setIcon(icon);
        }

        tDialog.setCancelable(false);
        tDialog.setTitle(title);
        tDialog.setMessage(msg);

        if (!TextUtils.isEmpty(btn1)) {
            tDialog.setButton(
                    btn1,
                    clickListener == null ? new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tDialog.dismiss();
                        }
                    }
                            : clickListener
            );

        }
        if (!TextUtils.isEmpty(btn2)) {
            tDialog.setButton2(btn2, cancelClickListener);
        }

        if (!context.isFinishing()) {
            tDialog.show();
        }
    }

    public static void showDialogWithButton(Activity context, int title,
                                            int msg, int icon, int btn1, int btn2,
                                            DialogInterface.OnClickListener clickListener,
                                            DialogInterface.OnClickListener cancelClickListener) {
        String titleStr = context.getString(title);
        String message = context.getString(msg);
        String button1 = context.getString(btn1);
        String button2 = context.getString(btn2);

        showDialogWithButton(context, titleStr, message, icon, button1,
                button2, clickListener, cancelClickListener);
    }

    /**
     * 显示带有一个按钮的框，可自定义点击事件
     */
    public static void showDialogWithButton(Activity context, String strTitle,
                                            String strText, int icon, String submit,
                                            DialogInterface.OnClickListener clickListener) {
        showDialogWithButton(context, strTitle, strText, icon, submit, null,
                clickListener, null);
    }

    /**
     * 只显示一个按钮的框,点击后弹框消失
     *
     * @param context
     * @param strTitle
     * @param strText
     * @param icon
     * @param submit
     * @author mashengchao 2012-3-19 下午2:09:27
     */
    public static void showDialogWithButton(Activity context, String strTitle,
                                            String strText, int icon, String submit) {
        showDialogWithButton(context, strTitle, strText, icon, submit, null,
                null, null);
    }

    /**
     * 显示一个按钮的弹出框，可自定义点击事件，默认显示"确定"
     *
     * @param context
     * @param strTitle
     * @param strText
     * @param icon
     * @param clickListener
     * @author mashengchao 2012-3-19 下午2:55:06
     */
    public static void showDialogWithButton(Activity context, String strTitle,
                                            String strText, int icon,
                                            DialogInterface.OnClickListener clickListener) {
        showDialogWithButton(context, strTitle, strText, icon, "确定",
                clickListener);
    }

    /**
     * 显示一个按钮的框，默认显示“确定”，点击后弹框消失
     *
     * @param context
     * @param strTitle
     * @param strText
     * @param icon
     * @author mashengchao 2012-3-19 下午2:15:30
     */
    public static void showDialogWithButton(Activity context, String strTitle,
                                            String strText, int icon) {
        showDialogWithButton(context, strTitle, strText, icon, "确定");
    }

    /**
     * 显示一个不带按钮的弹出框
     *
     * @param context
     * @param strTitle
     * @param strText
     * @param icon
     * @param cancelable 点击外围是否消失
     * @author mashengchao 2012-3-19 下午2:16:07
     */
    public static void showDialog(Activity context, String strTitle,
                                  String strText, int icon, boolean cancelable) {
        AlertDialog tDialog = new AlertDialog.Builder(context).create();
        tDialog.setIcon(icon);
        tDialog.setTitle(strTitle);
        tDialog.setMessage(strText);
        tDialog.setCanceledOnTouchOutside(cancelable);

        if (!context.isFinishing()) {
            tDialog.show();
        }
    }

    /**
     * 显示进度提示框
     *
     * @param context
     * @param title
     * @param message
     * @param indeterminate
     * @param cancelable
     * @param cancelListener
     * @return
     * @author mashengchao 2012-3-19 下午2:25:08
     */
    public static ProgressDialog showProgress(Context context,
                                              CharSequence title, CharSequence message, boolean indeterminate,
                                              boolean cancelable, OnCancelListener cancelListener) {
        ProgressDialog dialog = null;

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            dialog = new ProgressDialog(activity);
            dialog.setTitle(title);
            dialog.setMessage(message);
            dialog.setIndeterminate(indeterminate);
            dialog.setCancelable(cancelable);

            if (cancelListener != null) {
                dialog.setOnCancelListener(cancelListener);
            }

            if (!activity.isFinishing()) {
                dialog.show();
            }
        }

        return dialog;
    }

    /**
     * 自定义的进度框
     * <p/>
     * 一个菊花，一行字
     *
     * @param activity
     * @param title
     * @param showProgress 是否显示进度
     * @return
     * @author mashengchao 2012-5-18 下午7:46:11
     */
//    public static Dialog showCustomerProgress(Activity activity, String title,
//                                              boolean showProgress) {
//        final Dialog dialog = new Dialog(activity, R.style.giftcardDialog);
//        dialog.setContentView(R.layout.progress_custome);
//        TextView textViewContent = (TextView) dialog.findViewById(R.id.content);
//        textViewContent.setText(title);
//        ProgressBar progress = (ProgressBar) dialog.findViewById(R.id.bar);
//        progress.setVisibility(showProgress ? View.VISIBLE : View.GONE);
//        dialog.setCanceledOnTouchOutside(false);
//
//        if (!activity.isFinishing()) {
//            dialog.show();
//        }
//
//        return dialog;
//    }

    /**
     * 显示进度提示框，默认无取消事件
     *
     * @param context
     * @param title
     * @param message
     * @param indeterminate
     * @param cancelable
     * @return
     * @author mashengchao 2012-3-19 下午2:25:26
     */
    public static ProgressDialog showProgress(Context context,
                                              CharSequence title, CharSequence message, boolean indeterminate,
                                              boolean cancelable) {
        return showProgress(context, title, message, indeterminate, cancelable,
                null);
    }

    /**
     * 显示一个toast信息
     */
    public static void showToast(Context context, Object content,
                                 boolean longtime) {
        if (context == null) {
            return;
        }
        String msg = String.valueOf(content);
        String message = "";

        if (content instanceof Integer) {
            int rid = Integer.valueOf(msg);
            message = context.getString(rid);
        } else {
            message = msg;
        }

        Toast toast = Toast.makeText(context, message,
                longtime ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        // toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showToast(Context context, Object msgid) {
        showToast(context, msgid, false);
    }
}
