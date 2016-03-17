package com.topface.topface.ui.dialogs;

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

import javax.inject.Inject;

/**
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
    protected void initViews(final View root) {
        Debug.log(this, "+onCreate");
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
                Utils.showSoftKeyboard(getContext().getApplicationContext(), mCitySearch);
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
    public void onDestroyView() {
        super.onDestroyView();
        View focus = getActivity().getCurrentFocus();
        if (focus != null) {
            Utils.hideSoftKeyboard(getContext(), focus.getWindowToken());
        }
    }

}
