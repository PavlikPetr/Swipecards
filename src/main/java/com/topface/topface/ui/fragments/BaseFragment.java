package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;

import com.topface.topface.Static;
import com.topface.topface.promo.PromoPopupManager;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.CustomTitlesBaseFragmentActivity;
import com.topface.topface.ui.analytics.TrackedFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.actionbar.ActionBarTitleSetterDelegate;
import com.topface.topface.utils.actionbar.IActionBarTitleSetter;
import com.topface.topface.utils.http.IRequestClient;

import java.util.LinkedList;

public abstract class BaseFragment extends TrackedFragment implements IRequestClient {


    private LinkedList<ApiRequest> mRequests = new LinkedList<ApiRequest>();

    private IActionBarTitleSetter mTitleSetter;
    private ActionBar mSupportActionBar;
    private BroadcastReceiver mProfileLoadReceiver;

    private BroadcastReceiver updateCountersReceiver;
    public static final int F_UNDEFINED = -1;
    public static final int F_VIP_PROFILE = 1000;
    public static final int F_PROFILE = 1001;
    public static final int F_DATING = 1002;
    public static final int F_LIKES = 1003;
    public static final int F_MUTUAL = 1004;
    public static final int F_DIALOGS = 1005;
    //Страницы топов у нас больше нет
    //public static final int F_TOPS = 1006;
    public static final int F_SETTINGS = 1007;
    public static final int F_VISITORS = 1008;
    public static final int F_BOOKMARKS = 1009;
    public static final int F_FANS = 1010;
    public static final int F_ADMIRATIONS = 1011;
    public static final int F_BONUS = 1012;

    public static final int F_EDITOR = 9999;

    public static final String INVITE_POPUP_PREF_KEY = "INVITE_POPUP";
    private boolean mNeedTitles = true;
    private PromoPopupManager mPromoPopupManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        restoreState();
        setHasOptionsMenu(needOptionsMenu());
        super.onCreate(savedInstanceState);
    }

    protected boolean needOptionsMenu() {
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        refreshActionBarTitles();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void refreshActionBarTitles() {
        if (mNeedTitles) setActionBarTitles(getTitle(), getSubtitle());
    }

    protected void onUpdateStart(boolean isFlyUpdating) {
    }

    protected void onUpdateSuccess(boolean isFlyUpdating) {
    }

    protected void onUpdateFail(boolean isFlyUpdating) {
    }

    protected void updateUI(Runnable action) {
        if (isAdded()) {
            getActivity().runOnUiThread(action);
        }
    }

    @Override
    public void onResume() {
        setUpdateCountersReceiver();
        super.onResume();
        if (mProfileLoadReceiver == null) {
            mProfileLoadReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (isAdded()) {
                        checkProfileLoad();
                    }
                }
            };
        }
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mProfileLoadReceiver, new IntentFilter(CacheProfile.ACTION_PROFILE_LOAD));
        checkProfileLoad();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeAllRequests();
        if (updateCountersReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(updateCountersReceiver);
            updateCountersReceiver = null;
        }
        if (mProfileLoadReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mProfileLoadReceiver);
            mProfileLoadReceiver = null;
        }
    }

    private void removeAllRequests() {
        if (mRequests != null && mRequests.size() > 0) {
            for (ApiRequest request : mRequests) {
                cancelRequest(request);
            }
            mRequests.clear();
        }
    }

    @Override
    public void registerRequest(ApiRequest request) {
        if (!mRequests.contains(request)) {
            mRequests.add(request);
        }
    }

    @Override
    public void cancelRequest(ApiRequest request) {
        request.cancelFromUi();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode != -1) {
            intent.putExtra(Static.INTENT_REQUEST_KEY, requestCode);
        }
        super.startActivityForResult(intent, requestCode);
    }

    private void setUpdateCountersReceiver() {
        if (updateCountersReceiver == null) {
            updateCountersReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    onCountersUpdated();
                }
            };
            if (isAdded()) {
                LocalBroadcastManager.getInstance(getActivity())
                        .registerReceiver(
                                updateCountersReceiver,
                                new IntentFilter(CountersManager.UPDATE_COUNTERS)
                        );
            }
        }
    }

    protected void onCountersUpdated() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeAllRequests();
        View rootView = getView();
        if (rootView != null) {
            unbindDrawables(getView());
            System.gc();
        }
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        } else if (view instanceof AdapterView) {
            try {
                //noinspection unchecked
                ((AdapterView) view).setAdapter(null);
            } catch (Exception e) {
                Debug.error(e);
            }
        }
    }

    public void clearContent() {
    }

    private void checkProfileLoad() {
        if (CacheProfile.isLoaded()) {
            onLoadProfile();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    protected void onLoadProfile() {
        Debug.log(((Object) this).getClass().getSimpleName() + ": onLoadProfile");

        if (isNeedShowPromoPopup()) {
            showPromoDialog();
        }
    }

    /**
     * Показывает различные варианты попапов на старте
     *
     * @return удалось ли показать попап
     */
    protected boolean showPromoDialog() {
        FragmentActivity activity = getActivity();
        Debug.log("Promo: showPromoDialog");
        boolean promoPopupResult = false;
        if (PromoPopupManager.needShowPopup && !CacheProfile.premium && activity != null) {
            if (mPromoPopupManager == null) {
                mPromoPopupManager = new PromoPopupManager(activity);
            }
            promoPopupResult = mPromoPopupManager.startFragment();
            Debug.log("Promo: startFragment result: " + promoPopupResult);
        }

        return promoPopupResult;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Integer res = getOptionsMenuRes();
        if (res != null && menu != null) {
            menu.clear();
            inflater.inflate(res, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    protected Integer getOptionsMenuRes() {
        return null;
    }

    protected ActionBar getSupportActionBar() {
        if (mSupportActionBar == null) {
            Activity activity = getActivity();
            if (activity instanceof ActionBarActivity) {
                mSupportActionBar = ((ActionBarActivity) activity).getSupportActionBar();
            }
        }
        return mSupportActionBar;
    }

    protected void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        Activity activity = getActivity();

        if (activity instanceof BaseFragmentActivity) {
            // use overriden setSupportProgressBarIndeterminateVisibility from BaseFragmentActivity
            ((BaseFragmentActivity) activity).setSupportProgressBarIndeterminateVisibility(visible);
        } else if (activity instanceof ActionBarActivity) {
            // check support of indeterminate progress bar
            ActionBarActivity abActivity = (ActionBarActivity) activity;
            if (abActivity.supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)) {
                abActivity.setSupportProgressBarIndeterminate(visible);
            }
        }
    }

    protected void setActionBarTitles(String title, String subtitle) {
        getActionBarTitleSetter(getSupportActionBar()).setActionBarTitles(title, subtitle);
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void setActionBarTitles(int title, int subtitle) {
        getActionBarTitleSetter(getSupportActionBar()).setActionBarTitles(title, subtitle);
    }

    protected void setActionBarTitles(String title) {
        getActionBarTitleSetter(getSupportActionBar()).setActionBarTitles(title, null);
    }

    protected void setActionBarTitles(int title) {
        getActionBarTitleSetter(getSupportActionBar()).setActionBarTitles(title, null);
    }

    protected String getTitle() {
        return null;
    }

    protected String getSubtitle() {

        return null;
    }

    protected void restoreState() {
    }

    protected void setNeedTitles(boolean needTitles) {
        mNeedTitles = needTitles;
    }

    protected IActionBarTitleSetter getActionBarTitleSetter(ActionBar actionBar) {
        if (mTitleSetter == null) {
            Activity activity = getActivity();
            if (activity instanceof CustomTitlesBaseFragmentActivity) {
                mTitleSetter = ((CustomTitlesBaseFragmentActivity) activity).getActionBarTitleSetterDelegate();
            } else {
                mTitleSetter = new ActionBarTitleSetterDelegate(actionBar);
            }
        }

        return mTitleSetter;
    }

    protected boolean isNeedShowPromoPopup() {
        return false;
    }
}
