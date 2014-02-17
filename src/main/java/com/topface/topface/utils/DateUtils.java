package com.topface.topface.utils;

import com.topface.topface.App;
import com.topface.topface.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static long midnight;
    public static long current_year;
    private final static SimpleDateFormat mDateFormatDayYear = new SimpleDateFormat("dd MMMM yyyy");
    private final static SimpleDateFormat mDateFormatDay = new SimpleDateFormat("dd MMMM");
    private final static SimpleDateFormat mDateFormatDayOfWeek = new SimpleDateFormat("EEEE");
    private final static SimpleDateFormat mDateFormatHours = new SimpleDateFormat("HH:mm");

    public static final long DAY_IN_MILLISECONDS = 86400000; // 24 * 60 * 60 * 1000;
    public static final long HOUR_IN_MILLISECONDS = 3600000; // 60 * 60 * 1000;
    public static final long MINUTE_IN_MILLISECONDS = 60000; // 60 * 1000;

    public static void syncTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        DateUtils.midnight = cal.getTimeInMillis();

        cal.set(Calendar.DAY_OF_YEAR, 1);
        DateUtils.current_year = cal.getTimeInMillis();
    }

    public static String getRelativeDate(long date, boolean withTime) {
        String formattedDate;

        if (date > DateUtils.midnight) { // сегодня
            if (withTime) {
                formattedDate = mDateFormatHours.format(date);
            } else {
                formattedDate = App.getContext().getString(R.string.time_today);
            }
        } else if (date > DateUtils.midnight - Utils.DAY) { //вчера
            formattedDate = App.getContext().getString(R.string.time_yesterday);
        } else if (date > DateUtils.midnight - Utils.DAY * 5) { // день недели
            formattedDate = mDateFormatDayOfWeek.format(date);
        } else if (date > DateUtils.current_year) { // неделю назад
            formattedDate = mDateFormatDay.format(date);
        } else {
            formattedDate = mDateFormatDayYear.format(date);
        }

        return formattedDate;
    }

    public static String getFormattedTime(long date) {
        return mDateFormatHours.format(date);
    }

    public static boolean isWithinADay(long prev, long current) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(current);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (prev < cal.getTimeInMillis());
    }

    public static Date getDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static long getDateInMilliseconds(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getSeconds(Date date) {
        return date.getTime() / 1000;
    }

}
