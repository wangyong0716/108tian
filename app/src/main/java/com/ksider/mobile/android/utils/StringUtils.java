package com.ksider.mobile.android.utils;

import android.annotation.SuppressLint;
import android.net.ParseException;
import android.os.Handler;
import android.text.Html;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressLint("SimpleDateFormat")
public class StringUtils {
    public static String appendbulletLineHeader(String data) {
        if (data == null) {
            return data;
        }
        String[] lines = data.split("\n");
        String output = "";
        for (int i = 0; i < lines.length - 1; i++) {
            output += "•" + lines[i] + "\n";
        }
        output += "•" + lines[lines.length - 1];
        return output;
    }

    /**
     * 按照指定长度截断字符串，不达到指定长度就原字符串返回，超过部分截断补上三个点
     *
     * @param str   源字符串
     * @param start 开始位置
     * @param end   结束位置
     * @return
     */
    public static String truncate(String str, int start, int end) {
        if (str != null && str.length() > end && start < end) {
            str = str.substring(start, end) + "...";
        }
        return str;
    }

    public static boolean checkEmail(String email) {
        boolean tag = true;
        String pattern1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(pattern1);
        Matcher mat = pattern.matcher(email);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    public static boolean checkPhone(String phone) {
        boolean tag = true;
        String pattern1 = "((\\d{4}|\\d{3})?-?(\\d{7,8})(-\\d{1,2})?(\\d{1,2})?)|((\\d{4}|\\d{3})-(\\d{4}|\\d{3  })-(\\d{4}|\\d{3}))";
        Pattern pattern = Pattern.compile(pattern1);
        Matcher mat = pattern.matcher(phone);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    public static boolean checkMobilePhone(String phone) {
        boolean tag = true;
        if (phone.length() != 11) {
            return false;
        }

        String pattern1 = "(\\d{11})";
        Pattern pattern = Pattern.compile(pattern1);
        Matcher mat = pattern.matcher(phone);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    public static String formatDate(Date date) throws ParseException {
        return formatDate(date, "yyyy-MM-dd");
    }

    public static String formatDate(Date date, String formate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(formate);
        return sdf.format(date);
    }

    public static String formatDate(Date date, String formate, Locale locale) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(formate, locale);
        return sdf.format(date);
    }

    public static String formatDate(Long time) throws ParseException {
        return formatDate(time, "yyyy-MM-dd");
    }

    public static String formatDate(Long time, String formate) throws ParseException {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat(formate);
        return sdf.format(date);
    }

    public static Date parse(String strDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(strDate);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String md5Hash(String input) {
        byte[] bytesOfMessage;
        try {
            bytesOfMessage = input.getBytes("UTF-8");

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] message = md5.digest(bytesOfMessage);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < message.length; i++) {
                sb.append(Integer.toString((message[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String consumeCodeFormat(String code) {
        if (code != null && code.length() == 12) {
            return code.substring(0, 4) + " " + code.substring(4, 8) + " " + code.substring(8, code.length());
        }
        return code;
    }

    public static String serialNumberFormat(String serialNumber) {
        if (serialNumber != null && serialNumber.length() == 17) {
            return serialNumber.substring(0, 4) + " " + serialNumber.substring(4, 8) + " " + serialNumber.substring(8, 12) + " " + serialNumber.substring(12, serialNumber.length());
        }
        return serialNumber;
    }

    public static String getCodeStatus(int status) {
        String str = null;
        switch (status) {
            case 1:
                str = "未消费";
                break;
            case 2:
                str = "已消费";
                break;
            case 3:
                str = "退款中";
                break;
            case 4:
                str = "已退款";
                break;
        }
        return str;
    }

    public static void setError(final EditText edit, String info) {
        edit.requestFocus();
        edit.setError(Html.fromHtml("<font color='black'>" + info + "</font>"));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                edit.setError(null);
            }
        }, 3000);
    }

    public static void setError(final TextView edit, String info) {
        edit.requestFocus();
        edit.setError(Html.fromHtml("<font color='black'>" + info + "</font>"));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                edit.setError(null);
            }
        }, 3000);
    }

    public static String getPrice(double price) {
        if (price < 0) {
            return "-1";
        } else if (price == 0) {
            return "0";
        } else {
            long plong = Math.round(price);
            if (Math.abs(price - plong) <= 0.005) {
                return plong + "";
            }
            NumberFormat formatter = new DecimalFormat("#0.00");
            return formatter.format(price);
        }
    }

    public static String getPrice(float price) {
        if (price < 0) {
            return "-1";
        } else if (price == 0) {
            return "0";
        } else {
            long plong = Math.round(price);
            if (Math.abs(price - plong) <= 0.005) {
                return plong + "";
            }
            NumberFormat formatter = new DecimalFormat("#0.00");
            return formatter.format(price);
        }
    }

    public static String getPrice(String price) {
        double pDouble = 0;
        try {
            pDouble = Double.parseDouble(price);
        } catch (Exception e) {
            pDouble = -1;
        }
        return getPrice(pDouble);
    }

    public static String getPriceRange(double min, double max) {
        if (max < 0) {
            return "暂无";
        } else if (max == 0) {
            return "免费";
        } else if (min >= max) {
            return "¥" + getPrice(max);
        } else {
            return "¥" + getPrice(min) + "-" + getPrice(max);
        }
    }

    public static String getDistance(double d1, double d2) {
        Double dist;
        dist = Maths.getSelfDistance(d1, d2);

        return getDistance(dist);
    }

    public static String getDistance(double distance) {
        if (distance >= 100) {
            return Math.round(distance) + "";
        }
        if (distance < 0) {
            return "";
        }
        // fix 显示
        if (distance <= 0.005) {
            distance = Math.random() * 5 / 10 + 0.1;
            distance = Math.round(distance * 100) * 1.0 / 100;
        }
        NumberFormat formatter = new DecimalFormat("#0.00");
        return formatter.format(distance);
    }

    public static String getScore(double score) {
        NumberFormat formatter = new DecimalFormat("#0.0");
        return formatter.format(score);
    }
}
