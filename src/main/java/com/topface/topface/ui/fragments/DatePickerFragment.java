package com.topface.topface.ui.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.widget.DatePicker;

import com.topface.topface.Static;
import com.topface.topface.ui.analytics.TrackedDialogFragment;
import com.topface.topface.utils.DateUtils;

import java.util.Calendar;

public class DatePickerFragment extends TrackedDialogFragment implements DatePickerDialog.OnDateSetListener {

    private static final int START_SHIFT = 33;
    public static final String YEAR = "year";
    public static final String MONTH = "month";
    public static final String DAY = "day";

    private static long MAX_DATE;
    private static long MIN_DATE;

    public static final String TAG = "datePickerTag";

    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -Static.MIN_AGE);
        MAX_DATE = c.getTimeInMillis();
        c.add(Calendar.YEAR, -(Static.MAX_AGE - Static.MIN_AGE));
        MIN_DATE = c.getTimeInMillis();

        c.add(Calendar.YEAR, -(START_SHIFT-Static.MAX_AGE));
        final int year = getArguments().getInt(YEAR);
        final int month = getArguments().getInt(MONTH);
        final int day = getArguments().getInt(DAY);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(),this,year,month,day) {
            int lastYear = year;
            int lastMonth = month;
            int lastDay = day;
            @Override
            public void onDateChanged(DatePicker view, int year, int month, int day) {
                if(!isValidDate(year, month, day, MIN_DATE, MAX_DATE)){
                    view.init(lastYear,lastMonth,lastDay, this);
                } else {
                    lastYear = year;
                    lastMonth = month;
                    lastDay = day;
                    super.onDateChanged(view, year, month, day);
                }
            }
        };
        if (Build.VERSION.SDK_INT > 11) {
            dialog.getDatePicker().setMinDate(MIN_DATE);
            dialog.getDatePicker().setMaxDate(MAX_DATE);
        }
        return dialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if (mDateSetListener != null) {
            mDateSetListener.onDateSet(view,year,monthOfYear,dayOfMonth);
        }
    }

    public void setOnDateSetListener(DatePickerDialog.OnDateSetListener listener) {
        mDateSetListener = listener;
    }

    public static DatePickerFragment newInstance(int year, int month, int day) {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(YEAR, year);
        arguments.putInt(MONTH, month);
        arguments.putInt(DAY, day);
        datePickerFragment.setArguments(arguments);
        return datePickerFragment;
    }

    public static boolean isValidDate(final int year,final  int month,final  int day,final  long minDate,
                                      final  long maxDate) {
        final long millis = DateUtils.getDateInMilliseconds(year,month,day);
        return (millis >= minDate && millis <= maxDate);
    }

}
