package com.topface.topface.ui.fragments.closing;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.fragments.OnQuickMessageSentListener;
import com.topface.topface.ui.fragments.QuickMessageFragment;
import com.topface.topface.ui.fragments.ViewUsersListFragment;
import com.topface.topface.utils.CacheProfile;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Базовый фрагмент экранов запираний
 */
abstract public class ClosingFragment extends ViewUsersListFragment<FeedUser> implements View.OnClickListener {

    public static final int CHAT_CLOSE_DELAY_MILLIS = 1500;

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
        return new UsersList<FeedUser>(FeedUser.class);
    }

    public void showChat() {
        FeedUser user = getCurrentUser();
        if (user != null) {
            QuickMessageFragment fragment = QuickMessageFragment.newInstance(user.id, getChatListener());
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(BaseFragmentActivity.getContentViewCompat(), fragment, ((Object) fragment).getClass().getName());
            transaction.addToBackStack(null);
            transaction.commit();
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
                if (ClosingFragment.this != null) {
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
                if (ClosingFragment.this != null) {
                    FragmentManager fragmentManager = ClosingFragment.this.getFragmentManager();
                    if (ClosingFragment.this != null && fragmentManager != null) {
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.remove(fragment);
                        transaction.commitAllowingStateLoss();
                    }
                }
            }
        };
    }

    private void showNextFromUiThread() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showNextUser();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSkipAll:
                EasyTracker.getTracker().sendEvent(getTrackName(), "SkipAll", "", 1L);
                skipAllRequest(getSkipAllRequestType());
                break;
            case R.id.btnSkip:
                EasyTracker.getTracker().sendEvent(getTrackName(), "Skip", "", 1L);
                if (CacheProfile.premium || alowSkipForNonPremium()) {
                    if (getCurrentUser() != null && getCurrentUser().feedItem != null) {
                        SkipClosedRequest request = new SkipClosedRequest(getActivity());
                        request.callback(new SimpleApiHandler() {
                            @Override
                            public void always(IApiResponse response) {
                                if(isAdded()) {
                                    refreshActionBarTitles();
                                }
                            }
                        });
                        request.item = getCurrentUser().feedItem.id;
                        request.exec();
                    }
                    showNextUser();
                } else {
                    Intent intent = ContainerActivity.getVipBuyIntent(null, ((Object) this).getClass().getSimpleName());
                    startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                }
                break;
            case R.id.btnChat:
                EasyTracker.getTracker().sendEvent(getTrackName(), "Chat", "", 1L);
                showChat();
                break;
            case R.id.btnWatchAsList:
                EasyTracker.getTracker().sendEvent(getTrackName(), "WatchAsList", "", 1L);
                Intent intent = ContainerActivity.getVipBuyIntent(null, ((Object) this).getClass().getSimpleName());
                startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
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
        String lastFeedId = getLastFeedId();
        if (lastFeedId != null)
            request.to = lastFeedId;
        return request;
    }

    abstract protected FeedRequest.FeedService getFeedType();

    @Override
    public Class getItemsClass() {
        return FeedUser.class;
    }

    @Override
    protected void onCountersUpdated() {
        super.onCountersUpdated();
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
}
