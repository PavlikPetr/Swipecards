package com.topface.topface.ui.dialogs;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.views.CitySearchView;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.debug.FuckingVoodooMagic;

import javax.inject.Inject;

/**
 * Выбираем город
 * Created by tiberal on 14.03.16.
 */
public class CitySearchPopup extends AbstractDialogFragment {

    public static final String TAG = "city_search_popup";

    private CitySearchView mCitySearch;
    @Inject
    TopfaceAppState mAppState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                mAppState.setData(city);
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
    @FuckingVoodooMagic(description = "отключаем принудительны портрет, чтоб дольше все ок работало")
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        View focus = getActivity().getCurrentFocus();
        if (focus != null) {
            Utils.hideSoftKeyboard(getContext(), focus.getWindowToken());
        }
    }

}
