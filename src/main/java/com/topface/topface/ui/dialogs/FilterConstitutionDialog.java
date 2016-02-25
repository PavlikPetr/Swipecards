package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.topface.topface.R;
import com.topface.topface.ui.edit.FilterFragment;

import org.jetbrains.annotations.NotNull;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Диалог для выбора параметров роста/веса
 * Created by onikitin on 19.06.15.
 */
public class FilterConstitutionDialog extends DialogFragment {

    public static final String TAG = "com.topface.topface.ui.dialogs.FilterConstitutionDialog_TAG";

    private static final String TITLE = "title";
    private static final String FIRST_PICKER = "first_picker";
    private static final String SECOND_PICKER = "second_picker";
    private static final String CONFIG_MIN = "config_min";
    private static final String CONFIG_MAX = "config_max";

    @Bind(R.id.pickerFirstLimit)
    NumberPicker mFirstPicker;
    @Bind(R.id.pickerSecondLimit)
    NumberPicker mSecondPicker;
    private String mTitle;
    private ConstitutionLimits mConstitutionLimits;
    private OnConstitutionDialogListener mListener;
    private int mPickersMinValue;
    private int mPickersMaxValue;

    /**
     * Создать новый инстанс диалога
     *
     * @param configMin минимальное значение параметра из AppOptions
     * @param configMax максимальное значение параметра из AppOptions
     * @param title     титул диалога
     * @param filterMin текущее минимальное значение параметра из DatingFilter
     * @param filterMax текущее максимальное значение параметра из DatingFilter
     * @return FilterConstitutionDialog
     */
    public static FilterConstitutionDialog newInstance(int configMin, int configMax, String title, int filterMin, int filterMax) {
        Bundle arg = new Bundle();
        arg.putString(TITLE, title);
        arg.putInt(FIRST_PICKER, filterMin);
        arg.putInt(SECOND_PICKER, filterMax);
        arg.putInt(CONFIG_MIN, configMin);
        arg.putInt(CONFIG_MAX, configMax);
        FilterConstitutionDialog dialog = new FilterConstitutionDialog();
        dialog.setArguments(arg);
        return dialog;
    }

    public void setConstitutionDialogListener(OnConstitutionDialogListener listener) {
        this.mListener = listener;
    }

    private void setPickersValue(int firstValue, int secondValue) {
        mFirstPicker.setValue(firstValue);
        mSecondPicker.setValue(secondValue);
    }

    private void setPickerMinAndMaxValue(NumberPicker numberPicker, int min, int max) {
        numberPicker.setMinValue(min);
        numberPicker.setMaxValue(max);
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter_date_picker_dialog_view, null);
        ButterKnife.bind(this, view);
        Fragment fragment = getParentFragment();
        if (fragment != null && fragment instanceof FilterFragment) {
            mListener = ((FilterFragment) fragment).getConstitutionDialogListener();
        }
        Bundle bundle;
        //Начальное положение
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
        } else {
            bundle = getArguments();
        }
        mTitle = bundle.getString(TITLE);
        mPickersMinValue = bundle.getInt(CONFIG_MIN);
        mPickersMaxValue = bundle.getInt(CONFIG_MAX);
        setPickerMinAndMaxValue(mFirstPicker, mPickersMinValue, mPickersMaxValue - 1);
        setPickerMinAndMaxValue(mSecondPicker, mPickersMinValue + 1, mPickersMaxValue);
        if (bundle.getInt(FIRST_PICKER) != 0 && bundle.getInt(SECOND_PICKER) != 0) {
            setPickersValue(bundle.getInt(FIRST_PICKER), bundle.getInt(SECOND_PICKER));
        } else {
            setPickersValue(mPickersMinValue, mPickersMaxValue);
        }
        mFirstPicker.setWrapSelectorWheel(false);
        mSecondPicker.setWrapSelectorWheel(false);
        mFirstPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mSecondPicker.setMinValue(newVal + 1);
                mSecondPicker.setWrapSelectorWheel(false);
            }
        });
        mSecondPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (mSecondPicker.getValue() - mFirstPicker.getValue() == 1) {
                    mFirstPicker.setMaxValue(mFirstPicker.getValue());
                    mFirstPicker.setWrapSelectorWheel(false);
                    return;
                }
                mFirstPicker.setMaxValue(oldVal);
                mFirstPicker.setWrapSelectorWheel(false);
            }
        });
        return new AlertDialog.Builder(getActivity())
                .setTitle(mTitle)
                .setView(view)
                .setNegativeButton(getString(R.string.general_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFirstPicker.clearFocus();
                        mSecondPicker.clearFocus();
                        mConstitutionLimits = new ConstitutionLimits();
                        //Если в окне установлены максимальные пределы то ищем по всем пользователям, не учитывая рост/вес
                        if (!(mPickersMaxValue == mSecondPicker.getValue() &&
                                mPickersMinValue == mFirstPicker.getValue())) {
                            mConstitutionLimits.setLimits(mFirstPicker.getValue(), mSecondPicker.getValue());
                        }
                        if (mListener != null) {
                            mListener.handleValues(mConstitutionLimits);
                        }
                    }
                })
                .setPositiveButton(getString(R.string.general_any), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mListener != null) {
                            mListener.handleValues(new ConstitutionLimits());
                        }
                        dismiss();
                    }
                })
                .create();
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        mListener = null;
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(FIRST_PICKER, mFirstPicker.getValue());
        outState.putInt(SECOND_PICKER, mSecondPicker.getValue());
        outState.putInt(CONFIG_MIN, mPickersMinValue);
        outState.putInt(CONFIG_MAX, mPickersMaxValue);
        outState.putString(TITLE, mTitle);
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
