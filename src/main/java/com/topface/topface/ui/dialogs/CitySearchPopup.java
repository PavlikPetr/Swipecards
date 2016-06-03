package com.topface.topface.ui.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.state.EventBus;
import com.topface.topface.ui.views.CitySearchView;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.debug.FuckingVoodooMagic;

import javax.inject.Inject;

/**
 * Выбираем город
 * Created by tiberal on 14.03.16.
 */
public class CitySearchPopup extends AbstractDialogFragment {

    public static final String TAG = "city_search_popup";

    @Inject
    EventBus mEventBus;
    private CitySearchView mCitySearch;

    public CitySearchPopup() {
        App.get().inject(this);
    }

    @Override
    @FuckingVoodooMagic(description = "если в портрете запрещаем переворот")
    protected void initViews(final View root) {
        Debug.log(this, "+onCreate");
        final Context context = getActivity().getApplicationContext();
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mCitySearch = (CitySearchView) root.findViewById(R.id.city_search);
        mCitySearch.setOnCityClickListener(new CitySearchView.onCityClickListener() {
            @Override
            public void onClick(City city) {
                UserConfig config = App.getUserConfig();
                config.setUserCityChanged(true);
                config.saveConfig();
                mEventBus.setData(city);
                getDialog().cancel();
            }
        });
        mCitySearch.setOnRootViewListener(new CitySearchView.onRootViewListener() {
            @Override
            public int getHeight() {
                return root.getHeight();
            }
        });
        mCitySearch.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mCitySearch.showDropDown();
            }
        });
        mCitySearch.post(new Runnable() {
            @Override
            public void run() {
                if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    Utils.hideSoftKeyboard(context, mCitySearch);
                } else {
                    Utils.showSoftKeyboard(context, mCitySearch);
                }
            }
        });
        mCitySearch.findFocus();
        ((TextView) root.findViewById(R.id.title)).setText(R.string.edit_my_city);
        root.findViewById(R.id.title_clickable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });
    }

    @Override
    protected int getDialogLayoutRes() {
        return R.layout.city_dialog;
    }

    @Override
    protected boolean isModalDialog() {
        return false;
    }

    @Override
    public boolean isUnderActionBar() {
        return false;
    }

    @Override
    @FuckingVoodooMagic(description = "отключаем принудительны портрет, чтоб дальше все ок работало")
    public void onDestroyView() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        ((InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        super.onDestroyView();
    }
}
