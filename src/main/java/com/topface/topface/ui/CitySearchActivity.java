package com.topface.topface.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.ui.views.CitySearchView;
import com.topface.topface.utils.Utils;

public class CitySearchActivity extends BaseFragmentActivity {
    // Constants
    public static final int INTENT_CITY_SEARCH_ACTIVITY = 100;
    public static final int INTENT_CITY_SEARCH_AFTER_REGISTRATION = 102;
    public static final String INTENT_CITY = "city";

    private CitySearchView mCitySearch;
    private View mRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debug.log(this, "+onCreate");

        mRoot = findViewById(R.id.city_root);
        mCitySearch = (CitySearchView) findViewById(R.id.city_search);
        mCitySearch.setOnCityClickListener(new CitySearchView.onCityClickListener() {
            @Override
            public void onClick(City city) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(INTENT_CITY, city);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
        mCitySearch.setOnRootViewListener(new CitySearchView.onRootViewListener() {
            @Override
            public int getHeight() {
                return mRoot.getHeight();
            }
        });
        mCitySearch.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mCitySearch.showDropDown();
            }
        });
    }

    @Override
    protected int getContentLayout() {
        return R.layout.ac_city;
    }

    @Override
    protected void onPause() {
        super.onPause();
        View focus = getCurrentFocus();
        if(focus != null){
            Utils.hideSoftKeyboard(this, focus.getWindowToken());
        }
    }
}
