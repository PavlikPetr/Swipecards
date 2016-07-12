package com.topface.topface.ui.edit.filter.viewModel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.data.City;
import com.topface.topface.data.Profile;
import com.topface.topface.databinding.FilterFragmentBinding;
import com.topface.topface.state.EventBus;
import com.topface.topface.ui.dialogs.CitySearchPopup;
import com.topface.topface.ui.edit.filter.model.FilterData;
import com.topface.topface.utils.IActivityDelegate;
import com.topface.topface.utils.RxUtils;
import com.topface.topface.viewModels.BaseViewModel;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import rx.Subscription;

public class FilterViewModel extends BaseViewModel<FilterFragmentBinding> {

    public final ObservableBoolean isMaleSelected = new ObservableBoolean(App.get().getProfile().sex == Profile.GIRL);
    public final ObservableField<City> city = new ObservableField<>(App.get().getProfile().city);
    public final ObservableBoolean preetyOnly = new ObservableBoolean();
    public final ObservableBoolean onlineOnly = new ObservableBoolean();
    public final ObservableBoolean isEnabled = new ObservableBoolean(true);
    public final ObservableInt ageStart = new ObservableInt();
    public final ObservableInt ageEnd = new ObservableInt();

    @Inject
    EventBus mEventBus;
    private IActivityDelegate mIActivityDelegate;
    private Subscription mSubscription;

    public FilterViewModel(FilterFragmentBinding binding, @NotNull IActivityDelegate delegate, @NotNull FilterData filter) {
        super(binding);
        App.get().inject(this);
        mIActivityDelegate = delegate;
        setStartingValue(filter);
        mSubscription = mEventBus.getObservable(City.class).subscribe(new RxUtils.ShortSubscription<City>() {
            @Override
            public void onNext(City city) {
                FilterViewModel.this.city.set(city);
            }
        });
    }

    private void setStartingValue(FilterData filter) {
        city.set(filter.city);
        isMaleSelected.set(filter.sex == Profile.BOY);
        preetyOnly.set(filter.isPreetyOnly);
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

    public final void onSelectCityClick(View view) {
        if (mIActivityDelegate != null) {
            CitySearchPopup.getInstance(city.get().name).show(mIActivityDelegate.getSupportFragmentManager(), CitySearchPopup.TAG);
        }
    }

    @Override
    public void release() {
        super.release();
        RxUtils.safeUnsubscribe(mSubscription);
        mIActivityDelegate = null;
    }
}
