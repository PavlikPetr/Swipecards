package com.topface.topface.viewModels;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.view.View;

import com.topface.framework.JsonUtils;
import com.topface.topface.App;
import com.topface.topface.data.City;
import com.topface.topface.databinding.CitySearchPopupBinding;
import com.topface.topface.requests.CitiesRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.data_models.Cities;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.state.EventBus;
import com.topface.topface.ui.adapters.CityAdapter;
import com.topface.topface.ui.adapters.ItemEventListener;
import com.topface.topface.ui.dialogs.ICityPopupCloseListener;
import com.topface.topface.utils.RxFieldObservable;
import com.topface.topface.utils.RxUtils;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.UserConfig;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

/**
 * Моделька для попапа выбора города
 * Created by tiberal on 28.06.16.
 */
public class CitySearchPopupViewModel extends BaseViewModel<CitySearchPopupBinding> implements ItemEventListener.OnRecyclerViewItemClickListener<City> {

    @Inject
    EventBus mEventBus;
    private static final int INPUT_DELAY = 300;
    private CitiesRequest citiesRequest;
    private ICityPopupCloseListener mCloseListener;
    public RxFieldObservable<String> editTextObservable = new RxFieldObservable<>();
    public ObservableBoolean isRequestInProgress = new ObservableBoolean();
    public ObservableField<String> cityObservableField = new ObservableField<>(App.get().getProfile().city.name);
    private CompositeSubscription mViewModelSubscription = new CompositeSubscription();

    public CitySearchPopupViewModel(@NotNull final CitySearchPopupBinding binding, @NotNull ICityPopupCloseListener closeListener) {
        super(binding);
        App.get().inject(this);
        mCloseListener = closeListener;
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
        UserConfig config = App.getUserConfig();
        config.setUserCityChanged(true);
        config.saveConfig();
        mEventBus.setData(data);
        mCloseListener.onClose();
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
        mCloseListener = null;
    }
}
