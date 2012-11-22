package com.topface.topface.ui.edit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.ui.views.RangeSeekBar;

public class EditAgeFragment extends AbstractEditFragment {
    private int age_start;
    private int age_end;
    private int sex;
    private String baseSexString;

    private static final int absoluteMin = 16;
    private static final int absoluteMax = 99;

    private RangeSeekBar<Integer> rsb;

    public EditAgeFragment(int age_start, int age_end, int sex) {
        this.age_end = age_end;
        this.age_start = age_start;
        this.sex = sex;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pref_age_picker_hor, null);

        if (sex == 0) {
            baseSexString = getString(R.string.age_filter_girl);
        } else {
            baseSexString = getString(R.string.age_filter_man);
        }

        ((TextView) getActivity().findViewById(R.id.tvNavigationTitle)).setText(R.string.edit_age);

        getActivity().findViewById(R.id.btnNavigationHome).setVisibility(View.GONE);
        mBackButton = (Button) getActivity().findViewById(R.id.btnNavigationBackWithText);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setText(R.string.general_edit_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });


        final TextView tv = (TextView) view.findViewById(R.id.apValue);
        tv.setText(makeString(age_start, age_end));

        rsb = new RangeSeekBar<Integer>(absoluteMin, absoluteMax, getActivity());
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

        mSaveButton = (Button) getActivity().findViewById(R.id.btnNavigationRightWithText);
        mSaveButton.setVisibility(View.VISIBLE);
        mSaveButton.setText(R.string.general_save_button);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getActivity().getIntent();

                intent.putExtra(EditContainerActivity.INTENT_AGE_START, age_start);
                intent.putExtra(EditContainerActivity.INTENT_AGE_END, age_end);

                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
            }
        });

        return view;
    }

    private String makeString(int age_start, int age_end) {
        return baseSexString + " " + Integer.toString(age_start) + " - " + Integer.toString(age_end);
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
        return false;
    }

    @Override
    protected void saveChanges(Handler handler) {
        handler.sendEmptyMessage(0);
    }
}
