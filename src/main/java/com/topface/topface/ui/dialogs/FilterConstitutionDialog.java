package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.topface.topface.App;
import com.topface.topface.R;

import org.jetbrains.annotations.NotNull;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Диалог для выбора парамтров роста/веса
 * Created by onikitin on 19.06.15.
 */
public class FilterConstitutionDialog extends DialogFragment {

    public static final String TAG = "com.topface.topface.ui.dialogs.FilterConstitutionDialog_TAG";

    private static final String HEIGHT_MARK = "height_mark";
    private static final String TITLE = "title";
    private static final String FIRST_LIMIT = "first_limit";
    private static final String SECOND_LIMIT = "second_limit";

    @InjectView(R.id.pickerFirstLimit)
    NumberPicker mFirstLimit;
    @InjectView(R.id.pickerSecondLimit)
    NumberPicker mSecondLimit;
    private boolean mIsHeight;
    private String mTitle;
    private ConstitutionLimits mConstitutionLimits;
    private OnConstitutionDialogListener mListener;

    public static FilterConstitutionDialog newInstance(boolean isHeight, String title, int firstValue, int secondValue) {
        Bundle arg = new Bundle();
        arg.putBoolean(HEIGHT_MARK, isHeight);
        arg.putString(TITLE, title);
        arg.putInt(FIRST_LIMIT, firstValue);
        arg.putInt(SECOND_LIMIT, secondValue);
        FilterConstitutionDialog dialog = new FilterConstitutionDialog();
        dialog.setArguments(arg);
        return dialog;
    }

    public void setConstitutionDialogListener(OnConstitutionDialogListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsHeight = getArguments().getBoolean(HEIGHT_MARK);
        mTitle = getArguments().getString(TITLE);
        mConstitutionLimits = new ConstitutionLimits();
    }

    private void setPickersValue(int firstValue, int secondValue) {
        mFirstLimit.setValue(firstValue);
        mSecondLimit.setValue(secondValue);
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter_date_picker_dialog_view, null);
        ButterKnife.inject(this, view);
        mFirstLimit.setMaxValue(mIsHeight ? App.getAppOptions().getUserHeightMax()
                : App.getAppOptions().getUserWeightMax());
        mFirstLimit.setMinValue(mIsHeight ? App.getAppOptions().getUserHeightMin()
                : App.getAppOptions().getUserWeightMin());
        mSecondLimit.setMaxValue(mIsHeight ? App.getAppOptions().getUserHeightMax()
                : App.getAppOptions().getUserWeightMax());
        mSecondLimit.setMinValue(mIsHeight ? App.getAppOptions().getUserHeightMin()
                : App.getAppOptions().getUserWeightMin());

        Bundle bundle;
        //Начальное положение
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
        } else {
            bundle = getArguments();
        }
        if (bundle.getInt(FIRST_LIMIT) != 0 && bundle.getInt(SECOND_LIMIT) != 0) {
            setPickersValue(bundle.getInt(FIRST_LIMIT), bundle.getInt(SECOND_LIMIT));
        } else {
            setPickersValue(mIsHeight ? App.getAppOptions().getUserHeightMin()
                            : App.getAppOptions().getUserWeightMin(),
                    mIsHeight ? App.getAppOptions().getUserHeightMax()
                            : App.getAppOptions().getUserWeightMax());
        }
        mFirstLimit.setWrapSelectorWheel(false);
        mSecondLimit.setWrapSelectorWheel(false);
        mFirstLimit.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mSecondLimit.setMinValue(newVal + 1);
                mFirstLimit.setWrapSelectorWheel(false);
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
                        mFirstLimit.clearFocus();
                        mSecondLimit.clearFocus();
                        //Если в окне установлены максимальные пределы то ищем по всем пользователям, не учитывая рост/вес
                        if (!(App.getAppOptions().getUserHeightMax() == mSecondLimit.getValue() &&
                                App.getAppOptions().getUserHeightMin() == mFirstLimit.getValue())) {
                            mConstitutionLimits.setLimits(mFirstLimit.getValue(), mSecondLimit.getValue());
                        }
                        mListener.handleValues(mConstitutionLimits);
                    }
                })
                .setPositiveButton(getActivity().getString(R.string.general_any), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.handleValues(mConstitutionLimits);
                        dismiss();
                    }
                })
                .create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(FIRST_LIMIT, mFirstLimit.getValue());
        outState.putInt(SECOND_LIMIT, mSecondLimit.getValue());
        super.onSaveInstanceState(outState);
    }

    public interface OnConstitutionDialogListener {
        void handleValues(ConstitutionLimits limits);
    }

    public class ConstitutionLimits {

        public int min = 0;
        public int max = 0;

        public void setLimits(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }
}
