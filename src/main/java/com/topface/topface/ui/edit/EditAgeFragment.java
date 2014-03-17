package com.topface.topface.ui.edit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.ui.views.RangeSeekBar;

public class EditAgeFragment extends AbstractEditFragment {
    private static final String ARG_AGE_START = "arg_age_start";
    private static final String ARG_AGE_END = "arg_age_end";
    private static final String ARG_SEX = "arg_sex";
    public static final int absoluteMin = 16;
    public static final int absoluteMax = 80;
    private int age_start;
    private int age_start_before;
    private int age_end;
    private int age_end_before;
    private int sex;
    private String baseSexString;
    private RangeSeekBar<Integer> rsb;

    @Override
    protected void restoreState() {
        super.restoreState();
        Bundle args = getArguments();
        if (args != null) {
            this.age_end = args.getInt(ARG_AGE_END);
            this.age_start = args.getInt(ARG_AGE_START);
            this.sex = args.getInt(ARG_SEX);
        }
        age_end_before = age_start;
        age_end_before = age_end;
    }

    public EditAgeFragment() {
    }

    public static EditAgeFragment newInstance(int age_start, int age_end, int sex) {
        EditAgeFragment fragment = new EditAgeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_AGE_START, age_start);
        args.putInt(ARG_AGE_END, age_end);
        args.putInt(ARG_SEX, sex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.pref_age_picker_hor, null);

        if (sex == 0) {
            baseSexString = getString(R.string.age_filter_girl);
        } else {
            baseSexString = getString(R.string.age_filter_man);
        }

        final TextView tv = (TextView) view.findViewById(R.id.apValue);
        tv.setText(makeString(age_start, age_end));

        rsb = new RangeSeekBar<>(absoluteMin, absoluteMax, getActivity());
        rsb.setMinimalRange(20);
        rsb.setSelectedMaxValue(age_end);
        rsb.setSelectedMinValue(age_start);
        rsb.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue, RangeSeekBar.Thumb thumbId) {
                age_start = minValue;
                age_end = maxValue;
                tv.setText(makeString(age_start, age_end));
            }
        });
        ((LinearLayout) view.findViewById(R.id.apContainer)).addView(rsb);


        return view;
    }

    private void saveAge() {
        if (age_start < absoluteMin) age_start = absoluteMin;
        if (age_end > absoluteMax) age_end = absoluteMax;
        Intent intent = getActivity().getIntent();
        intent.putExtra(EditContainerActivity.INTENT_AGE_START, age_start);
        intent.putExtra(EditContainerActivity.INTENT_AGE_END, age_end);
        getActivity().setResult(Activity.RESULT_OK, intent);
        age_start_before = age_start;
        age_end_before = age_end;
        getActivity().finish();
    }

    private String makeString(int age_start, int age_end) {
        String plus = age_end == absoluteMax ? "+" : "";
        return baseSexString + " " + Integer.toString(age_start) + " - " + Integer.toString(age_end) + plus;
    }


    @Override
    protected void lockUi() {
        rsb.setEnabled(false);
    }

    @Override
    protected void unlockUi() {
        rsb.setEnabled(true);
    }

    @Override
    protected boolean hasChanges() {
        return age_start_before != age_start || age_end_before != age_end;
    }

    @Override
    protected void saveChanges(Handler handler) {
        handler.sendEmptyMessage(0);
        if (hasChanges()) {
            saveAge();
        }
    }

    @Override
    protected String getTitle() {
        return getString(R.string.filter_age);
    }
}
