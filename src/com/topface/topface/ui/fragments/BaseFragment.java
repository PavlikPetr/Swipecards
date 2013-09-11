package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.widget.AdapterView;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.analytics.TrackedFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.ContactsProvider;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.http.IRequestClient;

import java.util.ArrayList;
import java.util.LinkedList;

public abstract class BaseFragment extends TrackedFragment implements IRequestClient {


    private LinkedList<ApiRequest> mRequests = new LinkedList<ApiRequest>();

    private ActionBar mSupportActionBar;
    private BroadcastReceiver mProfileLoadReceiver;

    private BroadcastReceiver updateCountersReceiver;
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

    public static final int F_EDITOR = 9999;
    public static final int F_UNDEFINED = 9998;

    public static final String INVITE_POPUP = "INVITE_POPUP";
    private boolean mNeedTitles = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(needOptionsMenu());
        super.onCreate(savedInstanceState);
        restoreState();
        (new Thread() {
            @Override
            public void run() {
                super.run();
                inBackroundThread();
            }
        }).start();
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (ApiRequest request : mRequests) {
                        cancelRequest(request);
                    }
                    mRequests.clear();
                }
            }).start();
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
        request.cancel();
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
        } else if (mProfileLoadReceiver == null) {
            mProfileLoadReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    checkProfileLoad();
                }
            };

            LocalBroadcastManager.getInstance(getActivity())
                    .registerReceiver(mProfileLoadReceiver, new IntentFilter(CacheProfile.ACTION_PROFILE_LOAD));
        }
    }

    protected void onLoadProfile() {
        Debug.log(getClass().getSimpleName() + ": onLoadProfile");
    }

    protected void inBackroundThread() {
    }

    protected void showPromoDialog() {
        FragmentActivity activity = getActivity();
        boolean invitePopupShow = false;
        if (CacheProfile.canInvite && activity != null) {
            final SharedPreferences preferences = activity.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);

            long date_start = preferences.getLong(INVITE_POPUP, 1);
            long date_now = new java.util.Date().getTime();

            if (date_now - date_start >= CacheProfile.getOptions().popup_timeout) {
                invitePopupShow = true;
                preferences.edit().putLong(INVITE_POPUP, date_now).commit();
                ContactsProvider provider = new ContactsProvider(activity);
                provider.getContacts(-1, 0, new ContactsProvider.GetContactsListener() {
                    @Override
                    public void onContactsReceived(ArrayList<ContactsProvider.Contact> contacts) {

                        if (isAdded()) {
                            showInvitePopup(contacts);
                        }
                    }
                });
            }
        }

        //Показываем рекламу AirMessages только если не показываем инвайты
        if (!invitePopupShow) {
            AirManager manager = new AirManager(getActivity());
            manager.startFragment(getActivity().getSupportFragmentManager());
//            AirMessagesPopupFragment.showIfNeeded(getFragmentManager(), Options.PremiumAirEntity.AIR_MESSAGES);
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
        if(res != null) {
            inflater.inflate(res, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    protected Integer getOptionsMenuRes() {
        return null;
    }

    protected ActionBar getSupportActionBar() {
        if(mSupportActionBar == null) {
            Activity activity = getActivity();
            if (activity instanceof ActionBarActivity) {
                mSupportActionBar = ((ActionBarActivity)activity).getSupportActionBar();
            }
        }
        return mSupportActionBar;
    }

    protected void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        Activity activity = getActivity();
        if (activity instanceof ActionBarActivity) {
            ((ActionBarActivity)activity).setSupportProgressBarIndeterminateVisibility(visible);
        }
    }

    protected void setActionBarTitles(String title, String subtitle) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
        actionBar.setSubtitle(subtitle);
    }

    protected void setActionBarTitles(int title, int subtitle) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
        actionBar.setSubtitle(subtitle);
    }

    protected void setActionBarTitles(String title) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
        actionBar.setSubtitle(null);
    }

    protected void setActionBarTitles(int title) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
        actionBar.setSubtitle(null);
    }

    protected String getTitle() {
        return null;
    }

    protected String getSubtitle() {
        return null;
    }

    protected void restoreState(){
    }

    protected void setNeedTitles(boolean needTitles) {
        mNeedTitles = needTitles;
    }
}
