package com.topface.topface.ui.dialogs;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.data.Profile;
import com.topface.topface.databinding.CitySearchPopupBinding;
import com.topface.topface.ui.adapters.CityAdapter;
import com.topface.topface.ui.views.toolbar.BackToolbarViewModel;
import com.topface.topface.ui.views.toolbar.IToolbarNavigation;
import com.topface.topface.utils.ListUtils;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.debug.FuckingVoodooMagic;
import com.topface.topface.viewModels.CitySearchPopupViewModel;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static com.topface.topface.viewModels.CitySearchPopupViewModel.CITY_ON_START;
import static com.topface.topface.viewModels.CitySearchPopupViewModel.DEFAULT_CITIES;

/**
 * Попап выбора города
 * Created by tiberal on 28.06.16.
 */
public class CitySearchPopup extends AbstractDialogFragment implements IOnCitySelected {

    public static final String CITY_LIST_DATA = "city_list_data";
    public static final String INPUT_DATA = "input_data";
    public static final String TAG = "city_search_popup";

    private CitySearchPopupViewModel mModel;
    private CityAdapter mAdapter;
    private CitySearchPopupBinding mBinding;
    private IOnCitySelected mOnCitySelected;
    private BackToolbarViewModel mBackToolbarViewModel;

    public static CitySearchPopup newInstance(@Nullable String cityNameOnStart, @Nullable ArrayList<City> defaultCities) {
        CitySearchPopup popup = new CitySearchPopup();
        Bundle bundle = new Bundle();
        Profile profile = App.get().getProfile();
        bundle.putString(CITY_ON_START, cityNameOnStart != null ? cityNameOnStart
                : profile.city != null ? profile.city.getName() : Utils.EMPTY);
        if (ListUtils.isNotEmpty(defaultCities)) {
            bundle.putParcelableArrayList(DEFAULT_CITIES, defaultCities);
        }
        popup.setArguments(bundle);
        return popup;
    }

    public static CitySearchPopup newInstance() {
        return newInstance(null, null);
    }

    @Override
    @FuckingVoodooMagic(description = "как только тулбар будет переделан нужно, установку титула и up кнопки переделать на data binding")
    protected void initViews(View root) {
        mAdapter = new CityAdapter();
        mBinding = DataBindingUtil.bind(root);
        mBinding.cityList.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        mBinding.cityList.setAdapter(mAdapter);
        mModel = new CitySearchPopupViewModel(mBinding, getArguments(), this);
        mAdapter.setOnItemClickListener(mModel);
        mBinding.setViewModel(mModel);
        mBackToolbarViewModel = new BackToolbarViewModel(mBinding.toolbarInclude, getContext().getString(R.string.my_location), new IToolbarNavigation() {
            @Override
            public void onUpButtonClick() {
                getDialog().cancel();
            }
        });
        mBinding.setToolbarViewModel(mBackToolbarViewModel);
        mBackToolbarViewModel.init();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.enterCity.requestFocus();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(CITY_LIST_DATA, mAdapter.getData());
        outState.putString(INPUT_DATA, mBinding.enterCity.getText().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<City> cities = savedInstanceState.getParcelableArrayList(CITY_LIST_DATA);
            String input = savedInstanceState.getString(INPUT_DATA);
            if (ListUtils.isNotEmpty(cities)) {
                mAdapter.addData(cities, input);
                mModel.editTextObservable.setIgnoreEmit(input);
            } else {
                mModel.editTextObservable.set(input);
            }
        } else {
            mModel.editTextObservable.set(Utils.EMPTY);
        }
    }

    @Override
    protected int getDialogLayoutRes() {
        return R.layout.city_search_popup;
    }

    @Override
    protected boolean isModalDialog() {
        return false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mModel.release();
        mBackToolbarViewModel.release();
    }

    public void setOnCitySelected(IOnCitySelected listener) {
        mOnCitySelected = listener;
    }

    @Override
    public void onSelected(City city) {
        if (mOnCitySelected != null) {
            mOnCitySelected.onSelected(city);
        }
        getDialog().cancel();
    }
}
