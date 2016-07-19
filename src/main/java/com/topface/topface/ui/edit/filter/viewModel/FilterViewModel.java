package com.topface.topface.ui.edit.filter.viewModel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.view.View;
import android.widget.CheckBox;

import com.topface.topface.App;
import com.topface.topface.data.City;
import com.topface.topface.data.DatingFilter;
import com.topface.topface.data.Profile;
import com.topface.topface.databinding.FilterFragmentBinding;
import com.topface.topface.ui.dialogs.CitySearchPopup;
import com.topface.topface.ui.dialogs.IOnCitySelected;
import com.topface.topface.ui.edit.filter.model.FilterData;
import com.topface.topface.ui.views.RangeSeekBar;
import com.topface.topface.utils.IActivityDelegate;
import com.topface.topface.viewModels.BaseViewModel;

import org.jetbrains.annotations.NotNull;

public class FilterViewModel extends BaseViewModel<FilterFragmentBinding> {

    public final static int MIN_AGE = DatingFilter.MIN_AGE;
    public final static int MAX_AGE = DatingFilter.MAX_AGE;
    public final static String MAX_AGE_TITLE = String.format(App.getCurrentLocale(), "%d+", MAX_AGE);
    public final static String MIN_AGE_TITLE = String.valueOf(MIN_AGE);

    public final ObservableBoolean isMaleSelected = new ObservableBoolean(App.get().getProfile().sex == Profile.GIRL);
    public final ObservableField<City> city = new ObservableField<>(App.get().getProfile().city);
    public final ObservableBoolean prettyOnly = new ObservableBoolean();
    public final ObservableBoolean onlineOnly = new ObservableBoolean();
    public final ObservableBoolean isEnabled = new ObservableBoolean(true);
    public final ObservableInt ageStart = new ObservableInt();
    public final ObservableInt ageEnd = new ObservableInt();

    private IActivityDelegate mIActivityDelegate;

    public FilterViewModel(FilterFragmentBinding binding, @NotNull IActivityDelegate delegate, @NotNull FilterData filter) {
        super(binding);
        mIActivityDelegate = delegate;
        setStartingValue(filter);
        initRangeSeekBar();
    }

    private void initRangeSeekBar() {
        getBinding().rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Integer minValue, Integer maxValue, RangeSeekBar.Thumb thumbId) {
                if (thumbId != null) {
                    switch (thumbId) {
                        case MAX:
                            ageEnd.set(maxValue);
                            break;
                        case MIN:
                            ageStart.set(minValue);
                            break;
                    }
                } else {
                    ageEnd.set(maxValue);
                    ageStart.set(minValue);
                }
            }
        });
    }


    private void setStartingValue(FilterData filter) {
        city.set(filter.city);
        isMaleSelected.set(filter.sex == Profile.BOY);
        prettyOnly.set(filter.isPrettyOnly);
        onlineOnly.set(filter.isOnlineOnly);
        ageStart.set(filter.ageStart);
        ageEnd.set(filter.ageEnd);
    }

    public final void onMaleCheckBoxClick(View view) {
        isMaleSelected.set(true);
        isMaleSelected.notifyChange();
    }

    public final void onFemaleCheckBoxClick(View view) {
        isMaleSelected.set(false);
        isMaleSelected.notifyChange();
    }

    public final void onOnlineOnlyClick(View view) {
        onlineOnly.set(((CheckBox) view).isChecked());
    }

    public final void onPreetyOnlyClick(View view) {
        prettyOnly.set(((CheckBox) view).isChecked());
    }

    public final void onSelectCityClick(View view) {
        if (mIActivityDelegate != null) {
            CitySearchPopup popup = CitySearchPopup.newInstance();
            popup.setOnCitySelected(new IOnCitySelected() {
                @Override
                public void onSelected(City city) {
                    FilterViewModel.this.city.set(city);
                }
            });
            popup.show(mIActivityDelegate.getSupportFragmentManager(), CitySearchPopup.TAG);
        }
    }

    @Override
    public void release() {
        super.release();
        mIActivityDelegate = null;
    }
}
