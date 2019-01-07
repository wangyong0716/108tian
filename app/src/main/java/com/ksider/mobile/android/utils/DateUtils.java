package com.ksider.mobile.android.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by yong on 2015/6/29.
 */
public class DateUtils {
    public static final long SECOND_MILLIS = 1000;
    public static final long MINUTE_MILLIS = 60000;
    public static final long FIVE_MINUTES_MILLIS = 300000;
    public static final long HOUR_MILLIS = 3600000;
    public static final long HALF_HOUR_MILLIS = 1800000;
    public static final long DAY_MILLIS = 86400000;

    /**
     * get accurate date in type of [yyyy-MM-dd HH:mm:ss] from long time
     *
     * @param time
     * @return
     */
    public static String getFormatFullTime(long time) {
        if (time <= 0l) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(time));
    }

    /**
     * get date in type of [yyyy-MM-dd] from long time
     *
     * @param time
     * @return
     */
    public static String getFormatDate(long time) {
        if (time <= 0l) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date(time));
    }

    /**
     * get date in type of [yyyy-MM-dd E] from long time
     *
     * @param time
     * @return
     */
    public static String getFormatDateWithWeek(long time) {
        if (time <= 0l) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd E");
        return sdf.format(new Date(time));
    }

    /**
     * get accurate time in type of [HH:mm:ss] from long time
     *
     * @param time
     * @return
     */
    public static String getFormatTime(long time) {
        if (time <= 0l) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date(time));
    }

    public static String getDurationByNowInAll(long time) {
        String duration = "";
        long now = System.currentTimeMillis();
        Date date = new Date(time);
        Date today = new Date(now);
        int year = today.getYear() - date.getYear();
        int month = today.getMonth() - date.getMonth();
        int day = today.getDay() - date.getDay();
        int hour = today.getHours() - date.getHours();
        int minute = today.getMinutes() - date.getMinutes();
        int second = today.getSeconds() - date.getSeconds();
        if (year >= 1) {
            duration += year + "年";
        }
        if (month >= 1) {
            duration += month + "月";
        }
        if (day >= 1) {
            duration += day + "天";
        }
        if (hour >= 1) {
            duration += hour + "小时";
        }
        if (minute >= 1) {
            duration += minute + "分钟";
        }
        if (second >= 1) {
            duration += second + "秒";
        }
        duration += "以前";
        return duration;
    }

    public static long getDurationByNowInHour(long time) {
        long now = System.currentTimeMillis();
        long hour = (now - time) / HOUR_MILLIS;

        return hour;
    }

    public static long getDurationByNowInDay(long time) {
        long now = System.currentTimeMillis();
        long day = (now - time) / DAY_MILLIS;

        return day;
    }

    public static long getDurationByNowInMinute(long time) {
        long now = System.currentTimeMillis();
        long minute = (now - time) / MINUTE_MILLIS;

        return minute;
    }

    public static boolean inOneMinute(long time) {
        return System.currentTimeMillis() - time < MINUTE_MILLIS;
    }

    public static boolean inTwoMinutes(long time) {
        return System.currentTimeMillis() - time < 2 * MINUTE_MILLIS;
    }

    public static boolean inFiveMinutes(long time) {
        return System.currentTimeMillis() - time < FIVE_MINUTES_MILLIS;
    }

    public static boolean inOneHour(long time) {
        return System.currentTimeMillis() - time < HOUR_MILLIS;
    }

    public static boolean inOneDay(long time) {
        return System.currentTimeMillis() - time < DAY_MILLIS;
    }

    public static boolean inFiveDays(long time) {
        return System.currentTimeMillis() - time < 5 * DAY_MILLIS;
    }

    /**
     * get a string indicates time as simple and accurate as possible
     *
     * @param time
     * @return
     */
    public static String getDefaultDurationByNow(long time) {
        if (inFiveMinutes(time)) {
            return "刚刚";
        } else if (inOneHour(time)) {
            return getDurationByNowInMinute(time) + "分钟以前";
        } else if (inOneDay(time)) {
            return getDurationByNowInHour(time) + "小时以前";
        } else if (inFiveDays(time)) {
            return getDurationByNowInDay(time) + "天以前";
        } else {
            return getFormatDate(time);
        }
    }

    public static String getRecentDate(Long time) {
        if (time <= 0l) {
            return "";
        }
        Calendar today = Calendar.getInstance();
        Calendar theday = Calendar.getInstance();
        theday.setTimeInMillis(time);

        int todayWeek = today.get(Calendar.WEEK_OF_YEAR);
        int todayWeekday = today.get(Calendar.DAY_OF_WEEK);
        int thedayWeek = theday.get(Calendar.WEEK_OF_YEAR);
        int thedayWeekday = theday.get(Calendar.DAY_OF_WEEK);
        if (todayWeekday == 1) {
            if (todayWeek == thedayWeek && thedayWeekday == 1 || thedayWeek == todayWeek - 1 && thedayWeekday != 1) {
                SimpleDateFormat sdf = new SimpleDateFormat("本EHH:mm");
                return sdf.format(new Date(time));
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd日");
                return sdf.format(new Date(time));
            }
        } else {
            if (thedayWeek == todayWeek && thedayWeekday != 1 || thedayWeek == todayWeek + 1 && thedayWeekday == 1) {
                SimpleDateFormat sdf = new SimpleDateFormat("本EHH:mm");
                return sdf.format(new Date(time));
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd日");
                return sdf.format(new Date(time));
            }
        }
    }

    /**
     * get the first mills of the date
     *
     * @param time
     * @return
     */
    public static long getFirstMilSeconds(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * get format date in type of [MM-dd] from long time
     *
     * @param time
     * @return
     */
    public static String getFormatMonthDay(long time) {
        if (time <= 0l) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        return sdf.format(new Date(time));
    }

    //
    public static String getAge(long birthday) {
        Calendar cal = Calendar.getInstance();
        int thisYear = cal.get(Calendar.YEAR);
        cal.setTimeInMillis(birthday);
        int theYear = cal.get(Calendar.YEAR);
        int age = thisYear - theYear;
        return (age > 0 ? age : 0) + "";
    }
}
