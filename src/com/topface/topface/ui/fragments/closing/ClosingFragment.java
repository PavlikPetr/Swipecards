package com.topface.topface.ui.fragments.closing;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.fragments.OnQuickMessageSentListener;
import com.topface.topface.ui.fragments.QuickMessageFragment;
import com.topface.topface.ui.fragments.ViewUsersListFragment;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.CacheProfile;

/**
 * Базовый фрагмент экранов запираний
 */
abstract public class ClosingFragment extends ViewUsersListFragment<FeedUser> implements View.OnClickListener{

    public static final int CHAT_CLOSE_DELAY_MILLIS = 1500;

    @Override
    protected void initActionBarControls(ActionBar actionbar) {
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
        if(user != null) {
            QuickMessageFragment fragment = QuickMessageFragment.newInstance(user.id, getChatListener());
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(android.R.id.content, fragment, fragment.getClass().getName());
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            showNextUser();
        }
    }

    protected void skipAllRequest(int type) {
        SkipAllClosedRequest skipAllRequest = new SkipAllClosedRequest(type, getActivity());
        skipAllRequest.callback(new SimpleApiHandler() {
            @Override
            public void always(ApiResponse response) {
                if(isAdded()) {
                    refreshActionBarTitles(getView());
                }
            }
        });
        skipAllRequest.exec();
        onUsersProcessed();
    }

    private OnQuickMessageSentListener getChatListener() {
        return new OnQuickMessageSentListener() {
            @Override
            public void onMessageSent(String message, final QuickMessageFragment fragment) {
                //Закрываем чат с задержкой и переключаем пользователя
                if (ClosingFragment.this != null) {
                    ClosingFragment.this.getView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            closeFragment(fragment);
                            showNextUser();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSkipAll:
                EasyTracker.getTracker().trackEvent(getTrackName(), "SkipAll", "", 1L);
                skipAllRequest(getSkipAllRequestType());
                break;
            case R.id.btnSkip:
                EasyTracker.getTracker().trackEvent(getTrackName(), "Skip", "", 1L);
                if (CacheProfile.premium || alowSkipForNonPremium()) {
                    if(getCurrentUser() != null  && getCurrentUser().feedItem != null) {
                        SkipClosedRequest request = new SkipClosedRequest(getActivity());
                        request.callback(new SimpleApiHandler(){
                            @Override
                            public void always(ApiResponse response) {
                                if(isAdded()) {
                                    refreshActionBarTitles(getView());
                                }
                            }
                        });
                        request.item = getCurrentUser().feedItem.id;
                        request.exec();
                    }
                    showNextUser();
                } else {
                    Intent intent = ContainerActivity.getVipBuyIntent(null, getClass().getSimpleName());
                    startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                }
                break;
            case R.id.btnChat:
                EasyTracker.getTracker().trackEvent(getTrackName(), "Chat", "", 1L);
                showChat();
                break;
            case R.id.btnWatchAsList:
                EasyTracker.getTracker().trackEvent(getTrackName(), "WatchAsList", "", 1L);
                Intent intent = ContainerActivity.getVipBuyIntent(null, getClass().getSimpleName());
                startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                break;
            default:
                break;
        }
    }

    protected boolean alowSkipForNonPremium(){
        return true;
    }

    protected abstract int getSkipAllRequestType();

    @Override
    protected void onUsersProcessed() {
        super.onUsersProcessed();
        clearUsersList();
        if (getActivity() instanceof NavigationActivity) {
            ((NavigationActivity)getActivity()).onClosings();
        }
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
            refreshActionBarTitles(getView());
        }
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected void onNotEmptyDataReturnedOnce() {
        super.onNotEmptyDataReturnedOnce();
        EasyTracker.getTracker().trackView(getTrackName());
    }
}
