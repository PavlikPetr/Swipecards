package com.topface.topface.ui.adapters;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.databinding.CityItemBinding;
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CityAdapter extends BaseRecyclerViewAdapter<CityItemBinding, City> {

    @Nullable
    private String mCitySearchPrefix;

    @Nullable
    @Override
    protected Bundle getUpdaterEmmitObject() {
        return null;
    }

    @Override
    protected int getItemLayout() {
        return R.layout.city_item;
    }

    @Override
    protected void bindData(CityItemBinding binding, int position) {
        String full = getData().get(position).full;
        SpannableString city = getHighlightCity(full);
        binding.city.setText(city != null ? city : full);
    }

    private SpannableString getHighlightCity(String city) {
        SpannableString spanString = null;
        if (mCitySearchPrefix != null && city.toLowerCase().startsWith(mCitySearchPrefix)) {
            spanString = new SpannableString(city);
            spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, mCitySearchPrefix.length(), 0);
        }
        return spanString;
    }

    @NotNull
    @Override
    protected Class<CityItemBinding> getItemBindingClass() {
        return CityItemBinding.class;
    }

    public void addData(ArrayList<City> cities, String prefix) {
        super.addData(cities);
        mCitySearchPrefix = prefix;
    }


}
