package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.ScrollView;

import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.ui.adapters.CitySearchViewAdapter;
import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Calendar;

// для того, чтобы по первому backPressed скрывалась клавиатура, а по второму - скрывать выпадающий список,
// необходимо установить onRootViewListener. Если интерфейс не задан, то backPressed скроет и клавиатуру и список

public class CitySearchView extends AutoCompleteTextView {

    public static final int CITY_SEARCH_ACTIVITY = 100;
    public static final int CITY_SEARCH_FROM_FILTER_ACTIVITY = 101;
    public static final int CITY_SEARCH_AFTER_REGISTRATION = 102;

    private static final int DELAY_VALUE = 200;

    private Context mContext;
    private CitySearchViewAdapter mAdapter;
    private onCityClickListener cityClickListener;
    private int mRequestKey = CITY_SEARCH_FROM_FILTER_ACTIVITY;

    private long mGetFocusTime;

    private ScrollView mScrollView;

    private City mDefaultCity;
    private City mLastCheckedCity;

    private int mBackPressCounter = 0;
    private int mScreenHeight = 0;

    private onRootViewListener mOnRootViewListener;

    public CitySearchView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CitySearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttrs(context, attrs, 0);
        mContext = context;
        init();
    }

    public CitySearchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getAttrs(context, attrs, defStyle);
        mContext = context;
        init();
    }

    private void getAttrs(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CitySearchAttrs, defStyle, 0);
        int requestKey = a.getInt(R.styleable.CitySearchAttrs_request_key, 1);
        a.recycle();
        setRequestKey(requestKey);
    }

    @Override
    public CitySearchViewAdapter getAdapter() {
        return mAdapter;
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

    private Object getAutoCompleteTextViewPopup() {
        AutoCompleteTextView autoCompeteTextView = CitySearchView.this;
        Field field = null;
        try {
            field = AutoCompleteTextView.class.getDeclaredField("mPopup");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        if (field != null) {
            field.setAccessible(true);
            Object value = null;
            try {
                value = field.get(autoCompeteTextView);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private void setOnDismissListenerHigherThanApi14(ListPopupWindow listPopupWindow, PopupWindow.OnDismissListener dismissListener) {
        if (listPopupWindow != null) {
            Field field = null;
            try {
                field = ListPopupWindow.class.getDeclaredField("mPopup");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            if (field != null) {
                field.setAccessible(true);
                Object value = null;
                try {
                    value = field.get(listPopupWindow);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (value != null) {
                    ((PopupWindow) value).setOnDismissListener(dismissListener);
                }
            }
        }
    }

    private void setOnDismissListenerLowerThanApi14(PopupWindow popupWindow, PopupWindow.OnDismissListener dismissListener) {
        if (popupWindow != null) {
            popupWindow.setOnDismissListener(dismissListener);
        }
    }

    // this is the same method like AutoCompleteTextView.setOnDismissListener but working in all API
    public void setOnDismissListener(PopupWindow.OnDismissListener dismissListener) {
        Object autoCompleteTextViewPopup = getAutoCompleteTextViewPopup();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setOnDismissListenerHigherThanApi14((ListPopupWindow) autoCompleteTextViewPopup, dismissListener);
        } else {
            // fixing WTF? https://rink.hockeyapp.net/manage/apps/26531/app_versions/253/crash_reasons/41706130
            if (autoCompleteTextViewPopup instanceof PopupWindow) {
                setOnDismissListenerLowerThanApi14((PopupWindow) autoCompleteTextViewPopup, dismissListener);
            } else if (autoCompleteTextViewPopup instanceof ListPopupWindow) {
                setOnDismissListenerHigherThanApi14((ListPopupWindow) autoCompleteTextViewPopup, dismissListener);
            }
        }
    }

    private void init() {
        setAdapter(getMyAdapter());
        setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // forcibly show dropdown list for android 2.3
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    getMyAdapter().notifyDataSetChanged();
                }
                if (mBackPressCounter > 1) {
                    mBackPressCounter = 1;
                }
                if (TextUtils.isEmpty(CitySearchView.this.getText()) &
                        // remove incorect calling onDismiss
                        (Calendar.getInstance().getTimeInMillis() - mGetFocusTime) > DELAY_VALUE) {
                    if (mLastCheckedCity != null) {
                        CitySearchView.this.setMyText(mLastCheckedCity.getFullName());
                        CitySearchView.this.setFocusable(false);
                        Utils.hideSoftKeyboard(mContext, CitySearchView.this);
                    }
                }
            }
        });
        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    setScreenHeight();
                    mGetFocusTime = Calendar.getInstance().getTimeInMillis();
                    scrollToView();
                    clearCurrentCity();
                }
            }
        });
        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                CitySearchView.this.setFocusableInTouchMode(true);
                if (isFocused() && !CitySearchView.this.isPopupShowing()) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            CitySearchView.this.showDropDown();
                        }
                    });
                }
                return false;
            }
        });
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollToView();
            }
        });
        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Utils.hideSoftKeyboard(mContext, CitySearchView.this);
                setMyText(getMyAdapter().getItem(position));
                CitySearchView.this.setFocusable(false);
                mLastCheckedCity = getMyAdapter().getCityByPosition(position);
                getMyAdapter().setUserCity(mLastCheckedCity);
                if (cityClickListener != null) {
                    cityClickListener.onClick(getMyAdapter().getCityByPosition(position));
                }
            }
        });
    }

    private CitySearchViewAdapter getMyAdapter() {
        if (mAdapter == null) {
            mAdapter = new CitySearchViewAdapter(mContext, mRequestKey);
        }
        return mAdapter;
    }

    public interface onCityClickListener {
        void onClick(City city);
    }

    public void setOnCityClickListener(onCityClickListener listener) {
        cityClickListener = listener;
    }

    public void setDefaultCity(City city) {
        mDefaultCity = city;
        mLastCheckedCity = city;
        setMyText(city.getFullName());
        getMyAdapter().setUserCity(city);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    // clear edit field and show dropDown with default city list
    private void clearCurrentCity() {
        if (mDefaultCity != null) {
            setMyText("");
            getMyAdapter().fillStartDataList();

            post(new Runnable() {
                @Override
                public void run() {
                    if (!CitySearchView.this.isPopupShowing()) {
                        CitySearchView.this.showDropDown();
                    }
                }
            });
        }
    }

    public void setScrollableViewToTop(ScrollView scrollView) {
        mScrollView = scrollView;
    }

    // moove view on top
    private void scrollToView() {
        if (mScrollView == null) {
            return;
        }
        int[] viewLocation = new int[2];
        getLocationInWindow(viewLocation);
        int[] scrollLocation = new int[2];
        mScrollView.getLocationInWindow(scrollLocation);
        mScrollView.scrollTo(0, mScrollView.getScrollY() + (viewLocation[1] - scrollLocation[1]) -
                (int) mContext.getResources().getDimension(R.dimen.custom_city_search_view_padding_top));
    }

    // set text to view without calling filterable
    private void setMyText(CharSequence text) {
        CitySearchViewAdapter adapter = getMyAdapter();
        mAdapter = null;
        setText(text);
        mAdapter = adapter;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, @NotNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            mBackPressCounter++;
            if (mBackPressCounter > 1) {
                return super.onKeyPreIme(keyCode, event);
            } else {
                Utils.hideSoftKeyboard(mContext, this);
                return true;
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    private void setScreenHeight() {
        if (mOnRootViewListener != null) {
            int screenHeight = mOnRootViewListener.getHeight();
            mScreenHeight = mScreenHeight < screenHeight ? screenHeight : mScreenHeight;
        }
    }

    public void setOnRootViewListener(onRootViewListener rootViewListener) {
        mOnRootViewListener = rootViewListener;
        setScreenHeight();
    }

    // get root height for determine softKeyboard visibility
    public interface onRootViewListener {
        int getHeight();
    }
}
