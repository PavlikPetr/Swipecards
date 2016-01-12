package com.topface.topface.ui.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.topface.framework.utils.Debug;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.analytics.TrackedFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.actionbar.ActionBarTitleSetterDelegate;
import com.topface.topface.utils.http.IRequestClient;

import java.lang.reflect.Field;
import java.util.LinkedList;

import butterknife.ButterKnife;

public abstract class BaseFragment extends TrackedFragment implements IRequestClient {

    private static final String STATE_NEED_TITLES = "STATE_NEED_TITLES";
    private LinkedList<ApiRequest> mRequests = new LinkedList<>();

    private ActionBar mSupportActionBar;
    private BroadcastReceiver mProfileLoadReceiver;
    private ActionBarTitleSetterDelegate mTitleSetter;
    private boolean mNeedTitles = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        restoreState();
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
        if (savedInstanceState != null) {
            mNeedTitles = savedInstanceState.getBoolean(STATE_NEED_TITLES, true);
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
        mTitleSetter = new ActionBarTitleSetterDelegate(getSupportActionBar());
        refreshActionBarTitles();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_NEED_TITLES, mNeedTitles);
    }

    private void clearPreviousState() {
        mSupportActionBar = null;
        mTitleSetter = null;
    }

    public void refreshActionBarTitles() {
        setActionBarTitles(getTitle(), getSubtitle());
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
            intent.putExtra(Static.INTENT_REQUEST_KEY, requestCode);
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

    @SuppressWarnings("deprecation")
    protected ActionBar getSupportActionBar() {
        if (mSupportActionBar == null) {
            Activity activity = getActivity();
            if (activity instanceof ActionBarActivity) {
                mSupportActionBar = ((ActionBarActivity) activity).getSupportActionBar();
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
        } else if (activity instanceof ActionBarActivity) {
            // check support of indeterminate progress bar
            ActionBarActivity abActivity = (ActionBarActivity) activity;
            abActivity.setSupportProgressBarIndeterminateVisibility(visible);
        }
    }

    protected void setActionBarTitles(String title, String subtitle) {
        if (mTitleSetter != null && mNeedTitles) {
            mTitleSetter.setActionBarTitles(title, subtitle);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void setActionBarTitles(int title, int subtitle) {
        if (mTitleSetter != null && mNeedTitles) {
            mTitleSetter.setActionBarTitles(title, subtitle);
        }
    }

    protected void setActionBarTitles(String title) {
        if (mTitleSetter != null && mNeedTitles) {
            mTitleSetter.setActionBarTitles(title, null);
        }
    }

    protected void setActionBarTitles(int title) {
        if (mTitleSetter != null && mNeedTitles) {
            mTitleSetter.setActionBarTitles(title, null);
        }
    }

    public ActionBarTitleSetterDelegate getTitleSetter() {
        return mTitleSetter;
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

    public static FragmentSettings VIP_PROFILE = new FragmentSettings(FragmentId.VIP_PROFILE);
    public static FragmentSettings PROFILE = new FragmentSettings(FragmentId.PROFILE);
    public static FragmentSettings DATING = new FragmentSettings(FragmentId.DATING, true);
    public static FragmentSettings TABBED_DIALOGS = new FragmentSettings(FragmentId.TABBED_DIALOGS);
    public static FragmentSettings TABBED_VISITORS = new FragmentSettings(FragmentId.TABBED_VISITORS);
    public static FragmentSettings TABBED_LIKES = new FragmentSettings(FragmentId.TABBED_LIKES);
    public static FragmentSettings PHOTO_BLOG = new FragmentSettings(FragmentId.PHOTO_BLOG);
    public static FragmentSettings GEO = new FragmentSettings(FragmentId.GEO);
    public static FragmentSettings BONUS = new FragmentSettings(FragmentId.BONUS);
    public static FragmentSettings EDITOR = new FragmentSettings(FragmentId.EDITOR);
    public static FragmentSettings SETTINGS = new FragmentSettings(FragmentId.SETTINGS);
    public static FragmentSettings INTEGRATION_PAGE = new FragmentSettings(FragmentId.INTEGRATION_PAGE, 0);
    public static FragmentSettings UNDEFINED = new FragmentSettings(FragmentId.UNDEFINED);

    public static class FragmentSettings implements Parcelable {
        private FragmentId mFragmentId;
        private boolean mIsOverlayed;
        private int mPos;

        public static FragmentSettings getFragmentSettingsById(FragmentId id) {
            switch (id) {
                case VIP_PROFILE:
                    return VIP_PROFILE;
                case PROFILE:
                    return PROFILE;
                case DATING:
                    return DATING;
                case TABBED_DIALOGS:
                    return TABBED_DIALOGS;
                case TABBED_VISITORS:
                    return TABBED_VISITORS;
                case TABBED_LIKES:
                    return TABBED_LIKES;
                case PHOTO_BLOG:
                    return PHOTO_BLOG;
                case GEO:
                    return GEO;
                case BONUS:
                    return BONUS;
                case EDITOR:
                    return EDITOR;
                case SETTINGS:
                    return SETTINGS;
                case INTEGRATION_PAGE:
                    return INTEGRATION_PAGE;
                case UNDEFINED:
                default:
                    return UNDEFINED;
            }
        }

        public FragmentSettings(FragmentId number) {
            this(number, false);
        }

        public FragmentSettings(FragmentId fragmentId, boolean isOverlayed) {
            mFragmentId = fragmentId;
            mIsOverlayed = isOverlayed;
        }

        public FragmentSettings(FragmentId fragmentId, int pos) {
            mFragmentId = fragmentId;
            mPos = pos;
        }

        protected FragmentSettings(Parcel in) {
            try {
                mFragmentId = FragmentId.valueOf(in.readString());
            } catch (IllegalArgumentException x) {
                mFragmentId = null;
            }
            mIsOverlayed = in.readByte() != 0;
            mPos = in.readInt();
        }

        public static final Creator<FragmentSettings> CREATOR = new Creator<FragmentSettings>() {
            @Override
            public FragmentSettings createFromParcel(Parcel in) {
                return new FragmentSettings(in);
            }

            @Override
            public FragmentSettings[] newArray(int size) {
                return new FragmentSettings[size];
            }
        };

        public int getPos() {
            return mPos;
        }

        public FragmentId getFragmentId() {
            return mFragmentId;
        }

        public int getId() {
            return mFragmentId.getId();
        }

        public boolean isOverlayed() {
            return mIsOverlayed;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(mFragmentId == null ? FragmentId.UNDEFINED.name() : mFragmentId.name());
            out.writeInt(mIsOverlayed ? 1 : 0);
            out.writeInt(mPos);
        }
    }

    public enum FragmentId {
        VIP_PROFILE(0),
        PROFILE(1),
        DATING(2),
        TABBED_DIALOGS(3),
        TABBED_VISITORS(4),
        TABBED_LIKES(5),
        PHOTO_BLOG(6),
        GEO(9),
        BONUS(10),
        EDITOR(1000),
        SETTINGS(11),
        INTEGRATION_PAGE(12),
        UNDEFINED(-1);

        private int mNumber;

        /**
         * Constructor for enum type of fragment ids
         * By default fragment is not overlayed by ActionBar
         *
         * @param number integer id
         */
        FragmentId(int number) {
            mNumber = number;
        }

        public int getId() {
            return mNumber;
        }
    }
}
