package com.topface.topface.ui.fragments.feed;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.google.gson.reflect.TypeToken;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.History;
import com.topface.topface.promo.dialogs.PromoDialog;
import com.topface.topface.promo.dialogs.PromoExpressMessages;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteDialogsRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.DialogListAdapter;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.config.FeedsCache;
import com.topface.topface.utils.gcmutils.GCMUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

public class DialogsFragment extends FeedFragment<FeedDialog> {

    private static final String PAGE_NAME = "Dialogs";

    private Subscription mDrawerLayoutSubscription;
    private boolean mIsNeedRefresh;

    public DialogsFragment() {
        super();
    }

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof NavigationActivity) {
            Observable<NavigationActivity.DRAWER_LAYOUT_STATE> observable = ((NavigationActivity) getActivity()).getDrawerLayoutStateObservable();
            if (observable != null) {
                mDrawerLayoutSubscription = observable.subscribe(new Action1<NavigationActivity.DRAWER_LAYOUT_STATE>() {
                    @Override
                    public void call(NavigationActivity.DRAWER_LAYOUT_STATE drawer_layout_state) {
                        switch (drawer_layout_state) {
                            case CLOSED:
                                showExpressMessagesPopupIfNeeded();
                                break;
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    private boolean isPromoExpressMessagesDialogAttached() {
        Fragment promoFragment = getFragmentManager().findFragmentByTag(PromoExpressMessages.TAG);
        return promoFragment != null;
    }

    private void showExpressMessagesPopupIfNeeded() {
        boolean isPopupAvailable = isExpressPopupAvailable();
        if (!isPromoExpressMessagesDialogAttached()) {
            if (isPopupAvailable) {
                int paddingTop = 0;
                Fragment fragment = getParentFragment();
                if (fragment != null && fragment instanceof TabbedDialogsFragment) {
                    paddingTop = ((TabbedDialogsFragment) fragment).getTabLayoutHeight();
                }
                PromoExpressMessages popup = new PromoExpressMessages().setExtraPaddingTop(paddingTop);
                popup.setOnCloseListener(new PromoDialog.OnCloseListener() {
                    @Override
                    public void onClose() {
                        mIsNeedRefresh = true;
                    }
                });
                popup.show(getActivity().getSupportFragmentManager(), PromoExpressMessages.TAG);
            }
        } else if (!isPopupAvailable) {
            PromoExpressMessages expressPopup = (PromoExpressMessages) getFragmentManager().findFragmentByTag(PromoExpressMessages.TAG);
            if (expressPopup != null) {
                expressPopup.dismiss();
                updateData(true, false);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mDrawerLayoutSubscription != null && !mDrawerLayoutSubscription.isUnsubscribed()) {
            mDrawerLayoutSubscription.unsubscribe();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsNeedRefresh) {
            updateData(true, false);
            mIsNeedRefresh = false;
        }
        if (getListAdapter() != null) {
            boolean isVip = App.get().getProfile().premium;
            for (FeedDialog feed : getListAdapter().getData()) {
                if (feed.type == FeedDialog.MESSAGE_AUTO_REPLY && isVip) {
                    getListAdapter().getData().clear();
                    updateData(false, false);
                    break;
                }
            }

        }
        if (isPromoExpressMessagesDialogAttached()) {
            showExpressMessagesPopupIfNeeded();
        }
    }

    @Override
    protected Type getFeedListDataType() {
        return new TypeToken<FeedList<FeedDialog>>() {
        }.getType();
    }

    @Override
    protected Class getFeedListItemClass() {
        return FeedDialog.class;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.settings_messages);
    }

    @Override
    protected void makeAllItemsRead() {
    }

    @Override
    protected DialogListAdapter createNewAdapter() {
        return new DialogListAdapter(getActivity().getApplicationContext(), getUpdaterCallback());
    }

    @Override
    protected void makeItemReadWithFeedId(String id) {
        //feed will be marked read in another method
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.DIALOGS;
    }

    @Override
    protected void initLockedFeed(View inflated, int errorCode) {
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
        inflated.findViewById(R.id.btnBuyVip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(PurchasesActivity.createBuyingIntent("EmptyDialogs", App.from(getActivity()).getOptions().topfaceOfferwallRedirect));
            }
        });

        inflated.findViewById(R.id.btnStartRate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuFragment.selectFragment(FragmentId.DATING.getFragmentSettings());
            }
        });
    }

    /**
     * Этот метод используется для получения id элементов ленты при удалении.
     * Но в диалогах у нас работает не так как в остальных лентах
     * и приходится вручную пробрасывать id юзеров вместо id итема
     */
    @Override
    protected List<String> getSelectedFeedIds(FeedAdapter<FeedDialog> adapter) {
        return adapter.getSelectedUsersStringIds();
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_dialogs;
    }

    @Override
    protected int[] getTypesForGCM() {
        return new int[]{GCMUtils.GCM_TYPE_DIALOGS, GCMUtils.GCM_TYPE_MESSAGE, GCMUtils.GCM_TYPE_GIFT};
    }

    @Override
    protected int getFeedType() {
        return CountersManager.DIALOGS;
    }

    @NotNull
    @Override
    protected FeedsCache.FEEDS_TYPE getFeedsType() {
        return FeedsCache.FEEDS_TYPE.DATA_DIALOGS_FEEDS;
    }

    @Override
    protected DeleteAbstractRequest getDeleteRequest(List<String> ids) {
        return new DeleteDialogsRequest(ids, getActivity());
    }

    @Override
    protected int getUnreadCounter() {
        // dialogs are not auto-read
        return mCountersData.getDialogs();
    }

    @Override
    protected String getGcmUpdateAction() {
        return GCMUtils.GCM_DIALOGS_UPDATE;
    }

    @Override
    protected boolean considerDublicates(FeedDialog first, FeedDialog second) {
        return first.user == null ? second.user == null : first.user.id == second.user.id;
    }

    @Override
    protected void onChatActivityResult(int resultCode, Intent data) {
        super.onChatActivityResult(resultCode, data);
        if (data != null) {
            History history = data.getParcelableExtra(ChatActivity.LAST_MESSAGE);
            int userId = data.getIntExtra(ChatActivity.LAST_MESSAGE_USER_ID, -1);
            if (history != null && userId > 0) {
                if (getListAdapter() instanceof DialogListAdapter) {
                    DialogListAdapter adapter = (DialogListAdapter) getListAdapter();
                    FeedDialog dialog;
                    for (int i = 0; i < adapter.getCount(); i++) {
                        dialog = adapter.getItem(i);
                        if (dialog.user != null && dialog.user.id == userId) {
                            adapter.replacePreview(i, history);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected boolean isNeedFirstShowListDelay() {
        return isExpressPopupAvailable();
    }

    private boolean isExpressPopupAvailable() {
        return PromoExpressMessages.isPromoExpressMessagesAvailable();
    }
}
