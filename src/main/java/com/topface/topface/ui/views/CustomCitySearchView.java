package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ScrollView;

import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.ui.adapters.CustomCitySearchAdapter;
import com.topface.topface.utils.Utils;

/**
 * Created by ppetr on 15.12.14.
 */
public class CustomCitySearchView extends AutoCompleteTextView {

    public static final int CITY_SEARCH_ACTIVITY = 100;
    public static final int CITY_SEARCH_FROM_FILTER_ACTIVITY = 101;
    public static final int CITY_SEARCH_AFTER_REGISTRATION = 102;

    private Context mContext;
    private CustomCitySearchAdapter mAdapter;
    private onCityClickListener cityClickListener;
    private int mRequestKey = CITY_SEARCH_FROM_FILTER_ACTIVITY;

    private ScrollView mScrollView;

    private City mDefaultCity;

    public CustomCitySearchView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CustomCitySearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttrs(context, attrs, 0);
        mContext = context;
        init();
    }

    public CustomCitySearchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getAttrs(context, attrs, defStyle);
        mContext = context;
        init();
    }

    private void getAttrs(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CitySearchAttrs, defStyle, 0);

        int requestKey = a.getInt(R.styleable.CitySearchAttrs_request_key, 1);
        setRequestKey(requestKey);

    }

    private void setRequestKey(int key) {
        switch (key) {
            case 0:
                mRequestKey = CITY_SEARCH_ACTIVITY;
                break;
            case 1:
                mRequestKey = CITY_SEARCH_FROM_FILTER_ACTIVITY;
                break;
            case 2:
                mRequestKey = CITY_SEARCH_AFTER_REGISTRATION;
                break;
        }
    }

    private void init() {
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
//                Utils.hideSoftKeyboard(mContext, CustomCitySearchView.this);
//                CustomCitySearchView.this.clearFocus();
                if (TextUtils.isEmpty(CustomCitySearchView.this.getText())) {
                    mScrollView.requestFocus();
//                    if (mDefaultCity != null) {
//
//                    }
                }
            }
        });
        this.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    scrollToView();
                    checkCurrentCity();
                }
            }
        });
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollToView();
            }
        });
        this.setThreshold(0);
        this.setAdapter(getMyAdapter());
        getMyAdapter().setOnCitySearchProgress(new CustomCitySearchAdapter.onCitySearchProgress() {
            @Override
            public void inProgress(boolean isOnProgress) {
                Log.e("TOP_FACE", "inProgress: " + isOnProgress);
            }

            @Override
            public void onSearchFail(boolean state) {
                Log.e("TOP_FACE", "onSearchFail: " + state);
            }
        });
        this.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Utils.hideSoftKeyboard(mContext, CustomCitySearchView.this);
                if (cityClickListener != null) {
                    cityClickListener.onClick(getMyAdapter().getCityByPosition(position));
                }
            }
        });
    }

    private CustomCitySearchAdapter getMyAdapter() {
        if (mAdapter == null) {
            mAdapter = new CustomCitySearchAdapter(mContext, mRequestKey);
        }
        return mAdapter;
    }

    public interface onCityClickListener {
        public void onClick(City city);
    }

    public void setOnCityClickListener(onCityClickListener listener) {
        cityClickListener = listener;
    }

    public void setDefaultCity(City city) {
        mDefaultCity = city;
        this.setText(city.getName());
        getMyAdapter().setDefaultCity(city);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    private void checkCurrentCity() {
        if (mDefaultCity != null) {
            if (getMyAdapter().getAllCitiesData().id == mDefaultCity.id) {
                this.setText("", false);
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (!CustomCitySearchView.this.isPopupShowing()) {
                            CustomCitySearchView.this.showDropDown();
                        }
                    }
                });
            } else {
                this.setText(mDefaultCity.getName());
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (CustomCitySearchView.this.isPopupShowing()) {
                            CustomCitySearchView.this.dismissDropDown();
                        }
                    }
                });
            }
        }
    }

    public void setScrollableViewToTop(ScrollView scrollView) {
        mScrollView = scrollView;
    }

    private void scrollToView() {
        if (mScrollView == null) {
            return;
        }
        int[] viewLocation = new int[2];
        this.getLocationInWindow(viewLocation);
        int[] scrollLocation = new int[2];
        mScrollView.getLocationInWindow(scrollLocation);
        mScrollView.smoothScrollTo(0, mScrollView.getScrollY() + (viewLocation[1] - scrollLocation[1]));
    }

}
