package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.analytics.TrackedFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.actionbar.ActionBarCustomViewTitleSetterDelegate;
import com.topface.topface.utils.actionbar.ActionBarTitleSetterDelegate;
import com.topface.topface.utils.actionbar.IActionBarTitleSetter;
import com.topface.topface.utils.http.IRequestClient;

import java.util.LinkedList;

public abstract class BaseFragment extends TrackedFragment implements IRequestClient {

    private LinkedList<ApiRequest> mRequests = new LinkedList<>();

    private ActionBar mSupportActionBar;
    private BroadcastReceiver mProfileLoadReceiver;
    private IActionBarTitleSetter mTitleSetter;

    public static enum FragmentId {
        F_VIP_PROFILE(0),
        F_PROFILE(1),
        F_DATING(2, true),
        F_DIALOGS(3),
        F_VISITORS(4),
        F_LIKES(5),
        F_ADMIRATIONS(6),
        F_MUTUAL(7),
        F_LIKES_CLOSINGS(8, true),
        F_MUTUAL_CLOSINGS(9, true),
        F_BOOKMARKS(10),
        F_FANS(11),
        F_GEO(12),
        F_BONUS(13),
        F_EDITOR(1000),
        F_SETTINGS(15),
        F_UNDEFINED(-1);

        private int mNumber;
        private boolean mIsOverlayed;

        /**
         * Constructor for enum type of fragment ids
         * By default fragment is not overlayed by ActionBar
         *
         * @param number integer id
         */
        FragmentId(int number) {
            this(number, false);
        }

        /**
         * Constructor for enum type of fragment ids
         *
         * @param number      integer id
         * @param isOverlayed true if fragment will be overlayed by actionbar
         */
        FragmentId(int number, boolean isOverlayed) {
            mNumber = number;
            mIsOverlayed = isOverlayed;
        }

        public int getId() {
            return mNumber;
        }

        public boolean isOverlayed() {
            return mIsOverlayed;
        }
    }


    private boolean mNeedTitles = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        restoreState();
        setHasOptionsMenu(needOptionsMenu());
        mTitleSetter = createTitleSetter(getSupportActionBar());
        super.onCreate(savedInstanceState);
    }

    protected IActionBarTitleSetter createTitleSetter(ActionBar actionBar) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return new ActionBarCustomViewTitleSetterDelegate(getActivity(),
                    actionBar, R.id.title_clickable, R.id.title, R.id.subtitle);
        } else {
            return new ActionBarTitleSetterDelegate(actionBar);
        }
    }

    protected boolean needOptionsMenu() {
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        clearPreviousState();
        refreshActionBarTitles();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void clearPreviousState() {
        mSupportActionBar = null;
    }

    public void refreshActionBarTitles() {
        if (mNeedTitles) setActionBarTitles(getTitle(), getSubtitle());
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

    @Override
    public void onResume() {
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
            abActivity.setSupportProgressBarIndeterminateVisibility(visible);
        }
    }

    protected void setActionBarTitles(String title, String subtitle) {
        mTitleSetter.setActionBarTitles(title, subtitle);
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void setActionBarTitles(int title, int subtitle) {
        mTitleSetter.setActionBarTitles(title, subtitle);
    }

    protected void setActionBarTitles(String title) {
        mTitleSetter.setActionBarTitles(title, null);
    }

    protected void setActionBarTitles(int title) {
        mTitleSetter.setActionBarTitles(title, null);
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
}
