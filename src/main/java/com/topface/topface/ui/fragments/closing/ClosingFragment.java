package com.topface.topface.ui.fragments.closing;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SkipAllClosedRequest;
import com.topface.topface.requests.SkipClosedRequest;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.INavigationFragmentsListener;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.fragments.OnQuickMessageSentListener;
import com.topface.topface.ui.fragments.QuickMessageFragment;
import com.topface.topface.ui.fragments.ViewUsersListFragment;
import com.topface.topface.utils.AnimationHelper;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.cache.UsersListCacheManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Базовый фрагмент экранов запираний
 */
abstract public class ClosingFragment extends ViewUsersListFragment<FeedUser> implements View.OnClickListener {

    public static final int CHAT_CLOSE_DELAY_MILLIS = 1500;
    private UsersListCacheManager mCacheManager;
    private BroadcastReceiver mCountersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onCountersUpdated();
        }
    };
    private boolean mControlViewsHidden = false;
    private INavigationFragmentsListener mFragmentSwitchListener;
    private AnimationHelper mAnimationHelper;

    /**
     * Add items to list of views for hide and show purposes on ImageSwitcher click
     *
     * @param view from ui
     */
    protected void addViewsToHide(View view) {
        mAnimationHelper.addView(view);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof INavigationFragmentsListener) {
            mFragmentSwitchListener = (INavigationFragmentsListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAnimationHelper = new AnimationHelper(getActivity(), R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mCountersReceiver, new IntentFilter(CountersManager.UPDATE_COUNTERS));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mCountersReceiver, new IntentFilter(CountersManager.UPDATE_COUNTERS));
    }

    @Override
    protected void initActionBarControls() {
    }

    @Override
    public Integer getTopPanelLayoutResId() {
        return R.layout.controls_closing_top_panel;
    }

    @Override
    protected void onPageSelected(int position) {
    }

    @Override
    protected UsersList<FeedUser> createUsersList() {
        Class<FeedUser> itemsClass = getItemsClass();
        mCacheManager = new UsersListCacheManager(getCacheKey(), itemsClass);
        @SuppressWarnings("unchecked") UsersList<FeedUser> users = mCacheManager.getCacheAndRemove();
        if (users == null) {
            users = new UsersList<>(itemsClass);
        }
        return users;
    }

    protected abstract String getCacheKey();

    public void showChat() {
        FeedUser user = getCurrentUser();
        if (user != null) {
            QuickMessageFragment fragment = QuickMessageFragment.newInstance(user.id, getChatListener());
            fragment.show(getFragmentManager(), QuickMessageFragment.TAG);
        } else {
            showNextUser();
        }
    }

    protected void skipAllRequest(int type) {
        new SkipAllClosedRequest(type, getActivity())
                .callback(new SimpleApiHandler() {
                    @Override
                    public void always(IApiResponse response) {
                        if (isAdded()) {
                            refreshActionBarTitles();
                        }
                    }
                }).exec();

        onUsersProcessed();
    }

    private OnQuickMessageSentListener getChatListener() {
        return new OnQuickMessageSentListener() {
            @Override
            public void onMessageSent(String message, final QuickMessageFragment fragment) {
                //Закрываем чат с задержкой и переключаем пользователя
                if (isAdded()) {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            closeFragment(fragment);
                            showNextFromUiThread();
                        }
                    }, CHAT_CLOSE_DELAY_MILLIS);
                }
            }

            @Override
            public void onCancel(QuickMessageFragment fragment) {
                closeFragment(fragment);
            }

            private void closeFragment(QuickMessageFragment fragment) {
                if (isAdded()) {
                    FragmentManager fragmentManager = ClosingFragment.this.getFragmentManager();
                    if (fragmentManager != null) {
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.remove(fragment);
                        transaction.commitAllowingStateLoss();
                    }
                }
            }
        };
    }

    private void showNextFromUiThread() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isAdded()) {
                        showNextUser();
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSkipAll:
                EasyTracker.getTracker().sendEvent(getTrackName(), "SkipAll", "", 1L);
                skipAllRequest(getSkipAllRequestType());
                break;
            case R.id.btnSkip:
                if (CacheProfile.premium || alowSkipForNonPremium()) {
                    if (getCurrentUser() != null) {
                        SkipClosedRequest request = new SkipClosedRequest(getActivity());
                        request.callback(new SimpleApiHandler() {
                            @Override
                            public void always(IApiResponse response) {
                                if (isAdded()) {
                                    refreshActionBarTitles();
                                }
                            }
                        });
                        request.item = getCurrentUser().feedItemId;
                        request.exec();
                    }
                    showNextUser();
                } else {
                    Intent intent = PurchasesActivity.getVipBuyIntent(null, ((Object) this).getClass().getSimpleName());
                    startActivityForResult(intent, PurchasesActivity.INTENT_BUY_VIP);
                }
                break;
            case R.id.btnChat:
                EasyTracker.getTracker().sendEvent(getTrackName(), "Chat", "", 1L);
                showChat();
                break;
            case R.id.btnWatchAsList:
                EasyTracker.getTracker().sendEvent(getTrackName(), "WatchAsList", "", 1L);
                Intent intent = PurchasesActivity.getVipBuyIntent(null, ((Object) this).getClass().getSimpleName());
                startActivityForResult(intent, PurchasesActivity.INTENT_BUY_VIP);
                break;
            default:
                break;
        }
    }

    protected boolean alowSkipForNonPremium() {
        return true;
    }

    protected abstract int getSkipAllRequestType();

    @Override
    protected void onUsersProcessed() {
        super.onUsersProcessed();
        clearUsersList();
    }

    @Override
    protected ApiRequest getUsersListRequest() {
        FeedRequest request = new FeedRequest(getFeedType(), getActivity());
        request.limit = LIMIT;
        request.unread = true;
        request.leave = true;
        String lastFeedId = getLastFeedId();
        if (lastFeedId != null)
            request.to = lastFeedId;
        return request;
    }

    abstract protected FeedRequest.FeedService getFeedType();

    @Override
    public Class<FeedUser> getItemsClass() {
        return FeedUser.class;
    }

    protected void onCountersUpdated() {
        if (isAdded()) {
            refreshActionBarTitles();
        }
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected void onNotEmptyDataReturnedOnce() {
        super.onNotEmptyDataReturnedOnce();
        EasyTracker.getTracker().sendView(getTrackName());
    }

    @Override
    protected View.OnClickListener getOnImageSwitcherClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mControlViewsHidden) {
                    mAnimationHelper.animateIn();
                } else {
                    mAnimationHelper.animateOut();
                }

                if (mFragmentSwitchListener != null) {
                    if (mControlViewsHidden) {
                        mFragmentSwitchListener.onShowActionBar();
                    } else {
                        mFragmentSwitchListener.onHideActionBar();
                    }
                }
                mControlViewsHidden = !mControlViewsHidden;
            }
        };
    }
}
