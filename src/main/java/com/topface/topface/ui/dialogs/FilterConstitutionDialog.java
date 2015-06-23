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
import rx.Observable;
import rx.Subscriber;
import rx.observables.ConnectableObservable;

/**
 * Диалог для вибора парамтров роста/веса
 * Created by onikitin on 19.06.15.
 */
public class FilterConstitutionDialog extends DialogFragment {

    private static final String HEIGHT_MARK = "height_mark";
    private static final String TITLE = "title";
    @InjectView(R.id.numberStart)
    NumberPicker mFirstLimit;
    @InjectView(R.id.numberFinish)
    NumberPicker mSecondLimit;
    private boolean mIsHeight;
    private String mTitle;
    private ConstitutionLimits mConstitutionLimits;
    private ConnectableObservable<ConstitutionLimits> mConstitutionLimitsObservable = Observable.create(new Observable.OnSubscribe<ConstitutionLimits>() {
        @Override
        public void call(Subscriber<? super ConstitutionLimits> subscriber) {
            subscriber.onNext(mConstitutionLimits);
        }
    }).publish();

    public static FilterConstitutionDialog newInstance(boolean isHeight, String title) {
        Bundle arg = new Bundle();
        arg.putBoolean(HEIGHT_MARK, isHeight);
        arg.putString(TITLE, title);
        FilterConstitutionDialog dialog = new FilterConstitutionDialog();
        dialog.setArguments(arg);
        return dialog;
    }

    public ConnectableObservable<ConstitutionLimits> getConstitutionLimitsObservable() {
        return mConstitutionLimitsObservable;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsHeight = getArguments().getBoolean(HEIGHT_MARK);
        mTitle = getArguments().getString(TITLE);
        mConstitutionLimits = new ConstitutionLimits();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter_date_picker_dialog_view, null);
        ButterKnife.inject(this, view);
        mFirstLimit.setMaxValue(mIsHeight ? App.getAppOptions().getUserHeightMin() + App.getAppOptions().getUserHeightMax() / 4
                : App.getAppOptions().getUserWeightMax() / 2);
        mFirstLimit.setMinValue(mIsHeight ? App.getAppOptions().getUserHeightMin()
                : App.getAppOptions().getUserWeightMin());
        mSecondLimit.setMaxValue(mIsHeight ? App.getAppOptions().getUserHeightMax()
                : App.getAppOptions().getUserWeightMax());
        mSecondLimit.setMinValue(mIsHeight ? App.getAppOptions().getUserHeightMin() + App.getAppOptions().getUserHeightMax() / 4 + 1
                : App.getAppOptions().getUserWeightMax() / 2 + 1);

        //Начальное положение
        mFirstLimit.setValue(mIsHeight ? App.getAppOptions().getUserHeightMin() + App.getAppOptions().getUserHeightMax() / 4
                : App.getAppOptions().getUserWeightMax() / 2);
        mSecondLimit.setValue(mIsHeight ? App.getAppOptions().getUserHeightMin() + App.getAppOptions().getUserHeightMax() / 4 + 1
                : App.getAppOptions().getUserWeightMax() / 2 + 1);
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
                        getConstitutionLimitsObservable().connect();
                    }
                })
                .setPositiveButton(getActivity().getString(R.string.general_any), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getConstitutionLimitsObservable().connect();
                        dismiss();
                    }
                })
                .create();
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
