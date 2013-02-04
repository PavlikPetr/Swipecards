package com.topface.topface.utils;

import android.content.Context;
import com.topface.topface.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {

    public static long midnight;
    public static long current_year;
    private final static SimpleDateFormat mDateFormatDayYear = new SimpleDateFormat("dd MMMM yyyy");
    private final static SimpleDateFormat mDateFormatDay = new SimpleDateFormat("dd MMMM");
    private final static SimpleDateFormat mDateFormatDayOfWeek = new SimpleDateFormat("EEEE");
    private final static SimpleDateFormat mDateFormatHours = new SimpleDateFormat("HH:mm");

    public static void syncTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        DateUtils.midnight = cal.getTimeInMillis();

        cal.set(Calendar.DAY_OF_YEAR,1);
        DateUtils.current_year = cal.getTimeInMillis();
    }

    public static String getFormattedDate(Context context, long date) {
        String formattedDate;

        if (date > DateUtils.midnight) { // сегодня
            formattedDate = context.getString(R.string.time_today);
        } else if (date > DateUtils.midnight - Utils.DAY) { //вчера
            formattedDate = context.getString(R.string.time_yesterday);
        } else if (date > DateUtils.midnight - Utils.DAY * 5) { // день недели
            formattedDate = mDateFormatDayOfWeek.format(date);
        } else if (date > DateUtils.current_year) { // неделю назад
            formattedDate = mDateFormatDay.format(date);
        } else {
            formattedDate = mDateFormatDayYear.format(date);
        }

        return formattedDate;
    }

    public static String getFormattedDateHHmm(long date) {
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

}
