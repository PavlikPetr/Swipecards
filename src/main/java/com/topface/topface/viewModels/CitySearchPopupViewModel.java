package com.topface.topface.viewModels;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.topface.framework.JsonUtils;
import com.topface.topface.App;
import com.topface.topface.data.City;
import com.topface.topface.databinding.CitySearchPopupBinding;
import com.topface.topface.requests.CitiesRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.data_models.Cities;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.adapters.CityAdapter;
import com.topface.topface.ui.adapters.ItemEventListener;
import com.topface.topface.ui.dialogs.IOnCitySelected;
import com.topface.topface.utils.rx.RxFieldObservable;
import com.topface.topface.utils.rx.RxUtils;
import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

/**
 * Моделька для попапа выбора города
 * Created by tiberal on 28.06.16.
 */
public class CitySearchPopupViewModel extends BaseViewModel<CitySearchPopupBinding> implements ItemEventListener.OnRecyclerViewItemClickListener<City> {

    public static final String DEFAULT_CITIES = "default_cities";
    public static final String CITY_ON_START = "city_on_start";

    private static final int INPUT_DELAY = 300;
    private CitiesRequest citiesRequest;
    private IOnCitySelected mSelectedListener;
    public RxFieldObservable<String> editTextObservable = new RxFieldObservable<>();
    public ObservableBoolean isRequestInProgress = new ObservableBoolean();
    public ObservableField<String> cityObservableField = new ObservableField<>();
    private CompositeSubscription mViewModelSubscription = new CompositeSubscription();
    private ArrayList<City> defaultCities = new ArrayList<>();

    public CitySearchPopupViewModel(@NotNull final CitySearchPopupBinding binding, @Nullable Bundle bundle, @NotNull IOnCitySelected listener) {
        super(binding, bundle);
        if (bundle != null && bundle.containsKey(DEFAULT_CITIES)) {
            defaultCities = bundle.getParcelableArrayList(DEFAULT_CITIES);
        }
        mSelectedListener = listener;
        cityObservableField.set(bundle != null && bundle.containsKey(CITY_ON_START) ? bundle.getString(CITY_ON_START, Utils.EMPTY) : Utils.EMPTY);
        mViewModelSubscription.add(editTextObservable.getFiledObservable()
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        int length = s.length();
                        return length == 0 || length > 2;
                    }
                })
                .throttleLast(INPUT_DELAY, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        sendRequest(s.isEmpty() ? null : s);
                        isRequestInProgress.set(true);
                    }
                }));
    }

    @Override
    public void itemClick(View view, int itemPosition, City data) {
        cityObservableField.set(data.name);
        if (mSelectedListener != null) {
            mSelectedListener.onSelected(data);
        }
    }

    private void sendRequest(final String prefix) {
        cancelRequestIfNeed();
        citiesRequest = new CitiesRequest(App.getContext(), prefix);
        citiesRequest.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                Cities cities = JsonUtils.fromJson(response.toString(), Cities.class);
                CityAdapter cityAdapter = ((CityAdapter) getBinding().cityList.getAdapter());
                cityAdapter.clearData();
                if (TextUtils.isEmpty(prefix)) {
                    cityAdapter.addData(defaultCities);
                }
                cityAdapter.addData(cities.cities, prefix);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Utils.showErrorMessage();
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                isRequestInProgress.set(false);
            }
        }).exec();
    }

    public int getProgressVisibility(boolean b) {
        return b ? View.VISIBLE : View.GONE;
    }

    private void cancelRequestIfNeed() {
        if (citiesRequest != null && !citiesRequest.isCanceled()) {
            citiesRequest.cancelFromUi();
        }
    }

    @Override
    public void release() {
        super.release();
        cancelRequestIfNeed();
        RxUtils.safeUnsubscribe(mViewModelSubscription);
        mSelectedListener = null;
    }
}
