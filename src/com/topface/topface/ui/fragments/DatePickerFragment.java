package com.topface.topface.ui.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import com.topface.topface.Static;
import com.topface.topface.utils.DateUtils;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private static final int START_SHIFT = 33;

    private static long MAX_DATE;

    public static final String TAG = "datePickerTag";

    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -Static.MAX_AGE);
        MAX_DATE = c.getTimeInMillis();

        c.add(Calendar.YEAR, -(START_SHIFT-Static.MAX_AGE));
        final int year = c.get(Calendar.YEAR);
        final int month = c.get(Calendar.MONTH);
        final int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(),this,year,month,day) {
            int lastYear = year;
            int lastMonth = month;
            int lastDay = day;
            @Override
            public void onDateChanged(DatePicker view, int year, int month, int day) {
                if(!isValidDate(year, month, day, 0, MAX_DATE)){
                    view.init(lastYear,lastMonth,lastDay, this);
                } else {
                    lastYear = year;
                    lastMonth = month;
                    lastDay = day;
                    super.onDateChanged(view, year, month, day);
                }
            }
        };
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

    public static DatePickerFragment newInstance() {
        return new DatePickerFragment();
    }

    public static boolean isValidDate(final int year,final  int month,final  int day,final  long minDate,
                                      final  long maxDate) {
        final long millis = DateUtils.getDateInMilliseconds(year,month,day);
        return (millis >= minDate && millis <= maxDate);
    }
}
