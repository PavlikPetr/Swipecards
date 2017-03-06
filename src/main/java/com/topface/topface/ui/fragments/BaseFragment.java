package com.topface.topface.ui.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.analytics.TrackedFragment;
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.IFragmentDelegate;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.http.IRequestClient;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.LinkedList;

import butterknife.ButterKnife;
import butterknife.Unbinder;


public abstract class BaseFragment extends TrackedFragment implements IRequestClient, IOnBackPressed, IFragmentDelegate {

    private LinkedList<ApiRequest> mRequests = new LinkedList<>();

    private ActionBar mSupportActionBar;
    private BroadcastReceiver mProfileLoadReceiver;
    private Unbinder mUnbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        restoreState(savedInstanceState != null ? savedInstanceState : getArguments());
        setHasOptionsMenu(needOptionsMenu());
        super.onCreate(savedInstanceState);
        try {
            ViewConfiguration config = ViewConfiguration.get(getActivity());
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            Debug.error(ex);
        }
    }

    protected int getStatusBarColor() {
        return Utils.getColorPrimaryDark(getActivity());
    }

    protected boolean needOptionsMenu() {
        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clearPreviousState();
        refreshActionBarTitles();
        if (view != null) {
            AppConfig appConfig = App.getAppConfig();
            appConfig.setHardwareAcceleratedState(view.isHardwareAccelerated());
            appConfig.saveConfig();
        }
    }

    public void setToolbarSettings(ToolbarSettingsData settings) {
        if (getActivity() instanceof ToolbarActivity) {
            //TODO SETTOOLBARSETTINGS
//            ((ToolbarActivity) getActivity()).setToolbarSettings(settings);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isButterKnifeAvailable() && mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    protected void bindView(View view) {
        mUnbinder = ButterKnife.bind(this, view);
    }

    protected boolean isButterKnifeAvailable() {
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void clearPreviousState() {
        mSupportActionBar = null;
    }

    public void refreshActionBarTitles() {
    }

    @SuppressWarnings("unused")
    protected void onUpdateStart(boolean isFlyUpdating) {
    }

    @SuppressWarnings("unused")
    protected void onUpdateSuccess(boolean isFlyUpdating) {
    }

    @SuppressWarnings("unused")
    protected void onUpdateFail(boolean isFlyUpdating) {
    }

    protected void updateUI(Runnable action) {
        if (isAdded()) {
            getActivity().runOnUiThread(action);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(getResources().getColor(getStatusBarColor()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setStatusBarColor();
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
        refreshActionBarTitles();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeAllRequests();
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
            intent.putExtra(App.INTENT_REQUEST_KEY, requestCode);
            super.startActivityForResult(intent, requestCode);
        }
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

    protected void onLoadProfile() {
        Debug.log(((Object) this).getClass().getSimpleName() + ": onLoadProfile");
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

    @Nullable
    protected ActionBar getSupportActionBar() {
        if (mSupportActionBar == null) {
            Activity activity = getActivity();
            if (activity instanceof AppCompatActivity) {
                mSupportActionBar = ((AppCompatActivity) activity).getSupportActionBar();
            }
        }
        return mSupportActionBar;
    }

    @SuppressWarnings("deprecation")
    protected void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        Activity activity = getActivity();

        if (activity instanceof BaseFragmentActivity) {
            // use overriden setSupportProgressBarIndeterminateVisibility from BaseFragmentActivity
            ((BaseFragmentActivity) activity).setSupportProgressBarIndeterminateVisibility(visible);
        } else if (activity instanceof AppCompatActivity) {
            // check support of indeterminate progress bar
            AppCompatActivity abActivity = (AppCompatActivity) activity;
            abActivity.setSupportProgressBarIndeterminateVisibility(visible);
        }
    }

    protected void restoreState(Bundle savedInstanceState) {
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
