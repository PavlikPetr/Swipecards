package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.topface.topface.App;
import com.topface.topface.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Диалог для вибора парамтров роста/веса
 * Created by onikitin on 19.06.15.
 */
public class FilterNumberPickerDialog extends DialogFragment {

    private static final String HEIGHT_MARK = "height_mark";
    private static final String TITLE = "title";
    @InjectView(R.id.numberStart)
    NumberPicker mFirstLimit;
    @InjectView(R.id.numberFinish)
    NumberPicker mSecondLimit;
    private boolean mIsHeight;
    private String mTitle;

    public static FilterNumberPickerDialog newInstance(boolean isHeight, String title) {
        Bundle arg = new Bundle();
        arg.putBoolean(HEIGHT_MARK, isHeight);
        arg.putString(TITLE, title);
        FilterNumberPickerDialog dialog = new FilterNumberPickerDialog();
        dialog.setArguments(arg);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsHeight = getArguments().getBoolean(HEIGHT_MARK);
        mTitle = getArguments().getString(TITLE);
    }

    private int HeightMax = 100;//App.getAppOptions().getUserHeightMax();
    private int HeightMin = 1;//App.getAppOptions().getUserWeightMin();


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter_date_picker_dialog_view, null);
        ButterKnife.inject(this, view);
        mFirstLimit.setMaxValue(mIsHeight ? HeightMax / 2
                : App.getAppOptions().getUserWeightMax() / 2);
        mFirstLimit.setMinValue(mIsHeight ? HeightMin
                : App.getAppOptions().getUserWeightMin());
        mSecondLimit.setMaxValue(mIsHeight ? HeightMax
                : App.getAppOptions().getUserWeightMax());
        mSecondLimit.setMinValue(mIsHeight ? HeightMax / 2 + 1
                : App.getAppOptions().getUserWeightMax() / 2 + 1);

        //Начальное положение
        mFirstLimit.setValue(mIsHeight ? HeightMax / 2
                : App.getAppOptions().getUserWeightMax() / 2);
        mSecondLimit.setValue(mIsHeight ? HeightMax / 2 + 1
                : App.getAppOptions().getUserWeightMax() / 2);
        mFirstLimit.setWrapSelectorWheel(false);
        mFirstLimit.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mSecondLimit.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mSecondLimit.setWrapSelectorWheel(false);
        mFirstLimit.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mSecondLimit.setMinValue(newVal + 1);
                mSecondLimit.setWrapSelectorWheel(false);
            }
        });
        mSecondLimit.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (mSecondLimit.getValue() - mFirstLimit.getValue() == 1) {
                    mFirstLimit.setMaxValue(mFirstLimit.getValue());
                    mFirstLimit.setWrapSelectorWheel(false);
                    return;
                }
                mFirstLimit.setMaxValue(oldVal);
                mSecondLimit.setWrapSelectorWheel(false);
            }
        });
        return new AlertDialog.Builder(getActivity())
                .setTitle(mTitle)
                .setView(view)
                .setNegativeButton(getActivity().getString(R.string.general_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(getActivity().getString(R.string.general_any), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
    }
}
