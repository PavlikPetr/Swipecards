package com.topface.topface.ui.dialogs;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.databinding.CitySearchPopupBinding;
import com.topface.topface.ui.adapters.CityAdapter;
import com.topface.topface.utils.ListUtils;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.debug.FuckingVoodooMagic;
import com.topface.topface.viewModels.CitySearchPopupViewModel;

import java.util.ArrayList;

/**
 * Попап выбора города
 * Created by tiberal on 28.06.16.
 */
public class CitySearchPopup extends AbstractDialogFragment implements ICityPopupCloseListener {

    public static final String CITY_LIST_DATA = "city_list_data";
    public static final String INPUT_DATA = "input_data";
    public static final String TAG = "city_search_popup";

    private CitySearchPopupViewModel mModel;
    private CityAdapter mAdapter;
    private CitySearchPopupBinding mBinding;

    @Override
    @FuckingVoodooMagic(description = "как только тулбар будет переделан нужно, установку титула и up кнопки переделать на data binding")
    protected void initViews(View root) {
        mAdapter = new CityAdapter();
        mBinding = DataBindingUtil.bind(root);
        mBinding.cityList.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        mBinding.cityList.setAdapter(mAdapter);
        mModel = new CitySearchPopupViewModel(mBinding, this);
        mAdapter.setOnItemClickListener(mModel);
        mBinding.setViewModel(mModel);
        ((TextView) root.findViewById(R.id.title)).setText(R.string.edit_my_city);
        root.findViewById(R.id.title_clickable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });
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
    public void onViewStateRestored(@android.support.annotation.Nullable Bundle savedInstanceState) {
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
    public void onStop() {
        super.onStop();
        mModel.release();
    }

    @Override
    public void onClose() {
        getDialog().cancel();
    }

}
