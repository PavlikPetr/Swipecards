package com.topface.topface.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.ui.analytics.TrackedFragment;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.NavigationBarController;
import com.topface.topface.utils.http.IRequestClient;

import java.util.LinkedList;

public abstract class BaseFragment extends TrackedFragment implements IRequestClient {

    protected NavigationBarController mNavBarController;

    private LinkedList<ApiRequest> mRequests = new LinkedList<ApiRequest>();

    private BroadcastReceiver updateCountersReceiver;
    public static final int F_UNKNOWN = -1;
    public static final int F_PROFILE = 1001;
    public static final int F_DATING = 1002;
    public static final int F_LIKES = 1003;
    public static final int F_MUTUAL = 1004;
    public static final int F_DIALOGS = 1005;
    public static final int F_TOPS = 1006;
    public static final int F_SETTINGS = 1007;
    public static final int F_VISITORS = 1008;

    protected void onUpdateStart(boolean isFlyUpdating) {
    }

    protected void onUpdateSuccess(boolean isFlyUpdating) {
    }

    protected void onUpdateFail(boolean isFlyUpdating) {
    }

    protected void updateUI(Runnable action) {
        if(isAdded()) {
            getActivity().runOnUiThread(action);
        }
    }

    @Override
    public void onResume() {
        if (mNavBarController != null) mNavBarController.refreshNotificators();
        super.onResume();
        setUpdateCountersReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeAllRequests();
    }

    private void removeAllRequests() {
        if (mRequests != null && mRequests.size() > 0) {
            for (ApiRequest request : mRequests) {
                cancelRequest(request);
            }
            mRequests.clear();
        }
        mRequests.clear();
    }

    @Override
    public void registerRequest(ApiRequest request) {
        if (!mRequests.contains(request)) {
            mRequests.add(request);
        }
    }

    @Override
    public void cancelRequest(ApiRequest request) {
        request.cancel();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        intent.putExtra(Static.INTENT_REQUEST_KEY, requestCode);
        super.startActivityForResult(intent, requestCode);
    }

    private void setUpdateCountersReceiver() {
        if(updateCountersReceiver == null){
            updateCountersReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    if (mNavBarController != null) {
                        mNavBarController.refreshNotificators();
                    }
                }
            };
            if(isAdded()) {
                LocalBroadcastManager.getInstance(getActivity())
                        .registerReceiver(
                                updateCountersReceiver,
                                new IntentFilter(CountersManager.UPDATE_COUNTERS)
                        );
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        View rootView  = getView();
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
        } else if (view instanceof  AdapterView) {
            try {
                //noinspection unchecked
                ((AdapterView) view).setAdapter(null);
            } catch (Exception e) {
                Debug.error(e);
            }
        }
    }

    public void clearContent(){ }
}
