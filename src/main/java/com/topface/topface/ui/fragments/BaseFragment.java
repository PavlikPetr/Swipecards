package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.CustomTitlesBaseFragmentActivity;
import com.topface.topface.ui.analytics.TrackedFragment;
import com.topface.topface.utils.AirManager;
import com.topface.topface.utils.BackgroundThread;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.ContactsProvider;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.actionbar.ActionBarTitleSetterDelegate;
import com.topface.topface.utils.actionbar.IActionBarTitleSetter;
import com.topface.topface.utils.http.IRequestClient;

import java.util.ArrayList;
import java.util.LinkedList;

public abstract class BaseFragment extends TrackedFragment implements IRequestClient {


    private LinkedList<ApiRequest> mRequests = new LinkedList<ApiRequest>();

    private IActionBarTitleSetter mTitleSetter;
    private ActionBar mSupportActionBar;
    private BroadcastReceiver mProfileLoadReceiver;

    private BroadcastReceiver updateCountersReceiver;

    public static enum FragmentId {
        F_VIP_PROFILE, F_PROFILE, F_DATING,
        F_LIKES, F_ADMIRATIONS, F_MUTUAL,
        F_LIKES_CLOSINGS, F_MUTUAL_CLOSINGS,
        F_DIALOGS,
        F_VISITORS, F_BOOKMARKS, F_FANS,
        F_BONUS,
        F_EDITOR, F_SETTINGS,
        F_UNDEFINED
    }

    public static final String INVITE_POPUP = "INVITE_POPUP";
    private boolean mNeedTitles = true;
    private boolean invitePopupShow;

    private static boolean needShowPopup = true;

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
    }

    protected void showPromoDialog() {
        FragmentActivity activity = getActivity();
        if (needShowPopup) {
            showInvitePopup(activity);

            //Показываем рекламу AirMessages только если не показываем инвайты
            if (!invitePopupShow && !CacheProfile.premium && activity != null) {
                AirManager manager = new AirManager(activity);
                if (manager.startFragment(activity.getSupportFragmentManager())) {
                    needShowPopup = false;
                }
            }
        }
    }

    private void showInvitePopup(final FragmentActivity activity) {
        if (CacheProfile.canInvite && activity != null) {
            final SharedPreferences preferences = activity.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
            needShowPopup = false;
            final ContactsProvider.GetContactsHandler handler = new ContactsProvider.GetContactsHandler() {
                @Override
                public void onContactsReceived(ArrayList<ContactsProvider.Contact> contacts) {
                    if (isAdded()) {
                        showInvitePopup(contacts);
                        needShowPopup = false;
                    }
                }
            };
            new BackgroundThread() {

                @Override
                public void execute() {
                    doInvitePopupActions(preferences, activity, handler);
                }
            };

        }
    }

    private void doInvitePopupActions(SharedPreferences preferences, FragmentActivity activity,
                                      ContactsProvider.GetContactsHandler handler) {
        long date_start = preferences.getLong(INVITE_POPUP, 1);
        long date_now = new java.util.Date().getTime();

        if (date_now - date_start >= CacheProfile.getOptions().popup_timeout) {
            invitePopupShow = true;
            preferences.edit().putLong(INVITE_POPUP, date_now).commit();
            if (activity != null) {
                ContactsProvider provider = new ContactsProvider(activity);
                provider.getContacts(-1, 0, handler);
            }
        }
    }

    public void showInvitePopup(ArrayList<ContactsProvider.Contact> data) {
        EasyTracker.getTracker().sendEvent("InvitesPopup", "Show", "", 0L);
        InvitesPopup popup = InvitesPopup.newInstance(data);
        ((BaseFragmentActivity) getActivity()).startFragment(popup);
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
}
