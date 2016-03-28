package com.topface.topface.utils.actionbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.StringRes;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.BalanceData;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BlackListAddRequest;
import com.topface.topface.requests.BookmarkAddRequest;
import com.topface.topface.requests.DeleteBlackListRequest;
import com.topface.topface.requests.DeleteBookmarksRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.ComplainsActivity;
import com.topface.topface.ui.EditorProfileActionsActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;

import static com.topface.topface.utils.actionbar.OverflowMenu.OverflowMenuItem.ADD_TO_BLACK_LIST_ACTION;
import static com.topface.topface.utils.actionbar.OverflowMenu.OverflowMenuItem.ADD_TO_BOOKMARK_ACTION;
import static com.topface.topface.utils.actionbar.OverflowMenu.OverflowMenuItem.COMPLAIN_ACTION;
import static com.topface.topface.utils.actionbar.OverflowMenu.OverflowMenuItem.OPEN_CHAT_ACTION;
import static com.topface.topface.utils.actionbar.OverflowMenu.OverflowMenuItem.OPEN_PROFILE_FOR_EDITOR_STUB;
import static com.topface.topface.utils.actionbar.OverflowMenu.OverflowMenuItem.SEND_ADMIRATION_ACTION;
import static com.topface.topface.utils.actionbar.OverflowMenu.OverflowMenuItem.SEND_GIFT_ACTION;
import static com.topface.topface.utils.actionbar.OverflowMenu.OverflowMenuItem.SEND_SYMPATHY_ACTION;

/**
 * you need to call OverflowMenu.onDestroy inside onDestroy method your native class for unregister
 * broadcast receiver and remove interface OverflowMenuUser
 */
public class OverflowMenu {

    @Inject
    TopfaceAppState mAppState;
    private final static String INTENT_BUY_VIP_FROM = "UserProfileFragment";
    public static final String USER_ID_FOR_REMOVE = "user_id";

    private Menu mBarActions;
    private OverflowMenuType mOverflowMenuType;
    private Context mContext;
    private RateController mRateController;
    private ApiResponse mSavedResponse = null;
    private OverflowMenuUser mOverflowMenuFields = null;
    private BalanceData mBalanceData;
    private Subscription mBalanceSubscription;
    private BroadcastReceiver mUpdateActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BlackListAndBookmarkHandler.ActionTypes type = (BlackListAndBookmarkHandler.ActionTypes) intent.getSerializableExtra(BlackListAndBookmarkHandler.TYPE);
            boolean value = intent.getBooleanExtra(BlackListAndBookmarkHandler.VALUE, false);
            if (type != null) {
                switch (type) {
                    case BLACK_LIST:
                        setBlackListState(value);
                        initOverfowMenu();
                        break;
                    case BOOKMARK:
                        Boolean isInBlackList = isInBlackList();
                        if (intent.hasExtra(BlackListAndBookmarkHandler.VALUE) && isInBlackList != null && !isInBlackList) {
                            setBookmarkedState(value);
                            initOverfowMenu();
                        }
                        break;
                    case SYMPATHY:
                        setSympathySentState(value, false);
                        initOverfowMenu();
                        break;
                }
            }
        }
    };

    private IActivityDelegate mActivityDelegate;

    public OverflowMenu(IActivityDelegate iActivityDelegate, Menu barActions) {
        App.from(App.getContext()).inject(this);
        mBarActions = barActions;
        mOverflowMenuType = OverflowMenuType.CHAT_OVERFLOW_MENU;
        mContext = iActivityDelegate.getApplicationContext();
        registerBroadcastReceiver();
        mActivityDelegate = iActivityDelegate;
    }

    public OverflowMenu(IActivityDelegate iActivityDelegate, Menu barActions, RateController rateController, ApiResponse savedResponse) {
        this(iActivityDelegate, barActions);
        mBalanceSubscription = mAppState.getObservable(BalanceData.class).subscribe(new Action1<BalanceData>() {
            @Override
            public void call(BalanceData balanceData) {
                mBalanceData = balanceData;
            }
        });
        mOverflowMenuType = OverflowMenuType.PROFILE_OVERFLOW_MENU;
        mRateController = rateController;
        mSavedResponse = savedResponse;
    }

    public ArrayList<OverflowMenuItem> getChatOverflowMenu() {
        ArrayList<OverflowMenuItem> result = new ArrayList<>();
        result.add(ADD_TO_BLACK_LIST_ACTION);
        result.add(ADD_TO_BOOKMARK_ACTION);
        result.add(COMPLAIN_ACTION);
        return result;
    }

    public ArrayList<OverflowMenuItem> getProfileOverflowMenu(boolean isEditor, boolean isBanned) {
        ArrayList<OverflowMenuItem> result = new ArrayList<>();
        if (!isBanned) {
            result.add(SEND_SYMPATHY_ACTION);
            if (!App.from(mActivity).getOptions().isHideAdmirations) {
                result.add(SEND_ADMIRATION_ACTION);
            }
            result.add(OPEN_CHAT_ACTION);
            result.add(SEND_GIFT_ACTION);
            result.add(ADD_TO_BLACK_LIST_ACTION);
            result.add(ADD_TO_BOOKMARK_ACTION);
            result.add(COMPLAIN_ACTION);
        }
        if (isEditor) {
            result.add(OPEN_PROFILE_FOR_EDITOR_STUB);
        }
        return result;
    }

    public OverflowMenuItem findOverflowMenuItemById(int id) {
        for (OverflowMenuItem item : OverflowMenuItem.values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    public boolean isCurrentIdOverflowMenuItem(int itemId) {
        OverflowMenuItem currentOverflowMenuItem = findOverflowMenuItemById(itemId);
        return currentOverflowMenuItem != null;
    }

    private void initProfileOverflowMenu() {
        if (mBarActions != null) {
            mBarActions.removeItem(R.id.tempItem);
            Boolean isBookmarked = isBookmarked();
            Boolean isInBlackList = isInBlackList();
            Boolean isSympathySent = isSympathySent();
            ArrayList<OverflowMenuItem> overflowMenuItemArray = getProfileOverflowMenu(App.from(mActivity).getProfile().isEditor(), isBanned());
            for (int i = 0; i < overflowMenuItemArray.size(); i++) {
                OverflowMenuItem item = overflowMenuItemArray.get(i);
                Integer resourceId = null;
                switch (overflowMenuItemArray.get(i)) {
                    case ADD_TO_BLACK_LIST_ACTION:
                        if (isInBlackList != null) {
                            resourceId = isInBlackList ? item.getSecondResourceId() : item.getFirstResourceId();
                        }
                        break;
                    case ADD_TO_BOOKMARK_ACTION:
                        if (isBookmarked != null) {
                            resourceId = isBookmarked ? item.getSecondResourceId() : item.getFirstResourceId();
                        }
                        break;
                    default:
                        resourceId = item.getFirstResourceId();
                        break;
                }
                if (isNeedToAddItem(item.getId())) {
                    mBarActions.add(Menu.NONE, item.getId(), i, resourceId == null || mContext == null ? Utils.EMPTY : mContext.getString(resourceId)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                } else {
                    replaceItem(item, resourceId);
                }
            }
            if (overflowMenuItemArray.size() > 1) {
                if (isInBlackList != null) {
                    findItem(ADD_TO_BOOKMARK_ACTION.getId()).setEnabled(!isInBlackList);
                }
                if (isSympathySent != null && isSympathySent) {
                    findItem(SEND_SYMPATHY_ACTION.getId()).setEnabled(false);
                    findItem(SEND_ADMIRATION_ACTION.getId()).setEnabled(false);
                }
            }
        }
    }

    private MenuItem findItem(int id) {
        MenuItem item = mBarActions.findItem(id);
        return item == null ? EMPTY_MENU_ITEM : item;
    }

    private void initChatOverflowMenu() {
        if (mBarActions != null) {
            mBarActions.removeItem(R.id.tempItem);
            Boolean isBookmarked = isBookmarked();
            Boolean isInBlackList = isInBlackList();
            ArrayList<OverflowMenuItem> overflowMenuItemArray = getChatOverflowMenu();
            for (int i = 0; i < overflowMenuItemArray.size(); i++) {
                OverflowMenuItem item = overflowMenuItemArray.get(i);
                Integer resourceId = null;
                switch (overflowMenuItemArray.get(i)) {
                    case ADD_TO_BLACK_LIST_ACTION:
                        if (isInBlackList != null) {
                            resourceId = isInBlackList ? item.getSecondResourceId() : item.getFirstResourceId();
                        }
                        break;
                    case ADD_TO_BOOKMARK_ACTION:
                        if (isBookmarked != null) {
                            resourceId = isBookmarked ? item.getSecondResourceId() : item.getFirstResourceId();
                        }
                        break;
                    default:
                        resourceId = item.getFirstResourceId();
                        break;
                }
                if (isNeedToAddItem(item.getId())) {
                    mBarActions.add(Menu.NONE, item.getId(), i, resourceId == null || mContext == null ? Utils.EMPTY : mContext.getString(resourceId)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                } else {
                    replaceItem(item, resourceId);
                }
            }
            if (isInBlackList != null) {
                findItem(ADD_TO_BOOKMARK_ACTION.getId()).setEnabled(!isInBlackList);
            }
        }
    }

    private void replaceItem(OverflowMenuItem item, Integer resourceId) {
        if (item.getId() == ADD_TO_BLACK_LIST_ACTION.getId() ||
                item.getId() == ADD_TO_BOOKMARK_ACTION.getId()) {
            int order = findItem(item.getId()).getOrder();
            mBarActions.removeItem(item.getId());
            mBarActions.add(Menu.NONE, item.getId(), order, resourceId == null || mContext == null ? Utils.EMPTY : mContext.getString(resourceId)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }

    private boolean isNeedToAddItem(int id) {
        return findItem(id) == EMPTY_MENU_ITEM;
    }

    public void onMenuClicked(MenuItem item) {
        int itemId = item.getItemId();
        Integer profileId = getProfileId();
        if (isCurrentIdOverflowMenuItem(itemId)) {
            OverflowMenuItem overflowMenuItem = findOverflowMenuItemById(itemId);
            switch (overflowMenuItem) {
                case SEND_SYMPATHY_ACTION:
                    onClickSendSymphatyAction();
                    break;
                case SEND_ADMIRATION_ACTION:
                    onClickSendAdmirationAction();
                    break;
                case OPEN_CHAT_ACTION:
                    onClickOpenChatAction();
                    break;
                case SEND_GIFT_ACTION:
                    onClickSendGiftAction();
                    break;
                case COMPLAIN_ACTION:
                    if (profileId != null && mActivityDelegate != null) {
                        mActivityDelegate.startActivity(ComplainsActivity.createIntent(profileId));
                    }
                    break;
                case OPEN_PROFILE_FOR_EDITOR_STUB:
                    if (mSavedResponse != null && profileId != null && mActivityDelegate != null) {
                        mActivityDelegate.startActivity(EditorProfileActionsActivity.createIntent(profileId, mSavedResponse));
                    }
                    break;
                case ADD_TO_BLACK_LIST_ACTION:
                    onClickAddToBlackList();
                    break;
                case ADD_TO_BOOKMARK_ACTION:
                    onClickAddToBookmarkAction();
                    break;
                default:
                    break;
            }
            initOverfowMenu();
        }
    }

    public void initOverfowMenu() {
        switch (mOverflowMenuType) {
            case CHAT_OVERFLOW_MENU:
                initChatOverflowMenu();
                break;
            case PROFILE_OVERFLOW_MENU:
                initProfileOverflowMenu();
                break;
        }
    }

    private void onClickSendSymphatyAction() {
        Integer userId = getUserId();
        Boolean isMutual = isMutual();
        if (mRateController == null || userId == null || isMutual == null) {
            return;
        }
        mRateController.onLike(
                userId,
                isMutual ?
                        SendLikeRequest.DEFAULT_MUTUAL : SendLikeRequest.DEFAULT_NO_MUTUAL,
                new RateController.OnRateRequestListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onRateCompleted(int mutualId) {
                        setSympathySentState(true, true);
                        Utils.showToastNotification(R.string.sympathy_sended, Toast.LENGTH_SHORT);
                    }

                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onRateFailed(int userId, int mutualId) {
                        setSympathySentState(false, true);
                        Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_SHORT);
                        initOverfowMenu();
                    }
                }, App.from(mActivity).getOptions().blockUnconfirmed
        );
        setSympathySentState(true, true);
    }

    private void onClickSendAdmirationAction() {
        Integer userId = getUserId();
        Boolean isMutual = isMutual();
        if (mRateController == null || userId == null || isMutual == null) {
            return;
        }
        boolean isSentAdmiration = mRateController.onAdmiration(mBalanceData,
                userId,
                isMutual ?
                        SendLikeRequest.DEFAULT_MUTUAL : SendLikeRequest.DEFAULT_NO_MUTUAL,
                new RateController.OnRateRequestListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onRateCompleted(int mutualId) {
                        setSympathySentState(true, true);
                        Utils.showToastNotification(R.string.admiration_sended, Toast.LENGTH_SHORT);
                    }

                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onRateFailed(int userId, int mutualId) {
                        setSympathySentState(false, true);
                        initOverfowMenu();
                    }
                }, App.from(mActivity).getOptions()
        );
        if (isSentAdmiration) {
            setSympathySentState(true, true);
        }
    }

    private void showBuyVipActivity(int resourceId) {
        if (mActivityDelegate != null) {
            mActivityDelegate.startActivityForResult(
                    PurchasesActivity.createVipBuyIntent(mContext != null ? mContext.getString(resourceId) : Utils.EMPTY, INTENT_BUY_VIP_FROM),
                    PurchasesActivity.INTENT_BUY_VIP);
        }
    }

    private void onClickOpenChatAction() {
        Boolean isChatAvailable = isChatAvailable();
        if (isChatAvailable == null) {
            return;
        }
        if (!isChatAvailable) {
            showBuyVipActivity(R.string.chat_block_not_mutual);
        } else {
            openChat();
        }
    }

    private void onClickSendGiftAction() {
        OverflowMenuUser overflowMenuFieldsListener = getOverflowMenuFieldsListener();
        if (overflowMenuFieldsListener != null) {
            overflowMenuFieldsListener.clickSendGift();
        }
    }

    private void onClickAddToBlackList() {
        Boolean isInBlackList = isInBlackList();
        final Integer userId = getUserId();
        if (isInBlackList == null || userId == null || mContext == null) {
            return;
        }
        ApiRequest request;
        if (isInBlackList) {
            request = new DeleteBlackListRequest(userId, mContext).
                    callback(new BlackListAndBookmarkHandler(mContext,
                            BlackListAndBookmarkHandler.ActionTypes.BLACK_LIST,
                            userId,
                            false) {
                        @Override
                        public void success(IApiResponse response) {
                            super.success(response);
                            showBlackListToast(false);
                            LocalBroadcastManager.getInstance(mContext).
                                    sendBroadcast(new Intent(DialogsFragment.REFRESH_DIALOGS)
                                            .putExtra(USER_ID_FOR_REMOVE, -1));
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                            super.fail(codeError, response);
                            setBlackListState(null);
                            initOverfowMenu();
                        }
                    });
        } else {
            request = new BlackListAddRequest(userId, mContext).
                    callback(new BlackListAndBookmarkHandler(mContext,
                            BlackListAndBookmarkHandler.ActionTypes.BLACK_LIST,
                            userId,
                            true) {
                        @Override
                        public void success(IApiResponse response) {
                            super.success(response);
                            showBlackListToast(true);
                            LocalBroadcastManager.getInstance(mContext).
                                    sendBroadcast(new Intent(DialogsFragment.REFRESH_DIALOGS)
                                            .putExtra(USER_ID_FOR_REMOVE, userId));
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                            super.fail(codeError, response);
                            setBlackListState(null);
                            initOverfowMenu();
                        }
                    });
        }
        setBlackListState(null);
        request.exec();
    }

    private void addToFavorite() {
        Boolean isBookmarked = isBookmarked();
        Integer userId = getUserId();
        if (isBookmarked == null || userId == null || mContext == null) {
            return;
        }
        (isBookmarked ? new DeleteBookmarksRequest(userId, mContext).
                callback(new BlackListAndBookmarkHandler(mContext,
                        BlackListAndBookmarkHandler.ActionTypes.BOOKMARK,
                        userId,
                        false) {
                    @Override
                    public void success(IApiResponse response) {
                        super.success(response);
                        showBookmarkToast(false);
                        LocalBroadcastManager.getInstance(mContext).
                                sendBroadcast(new Intent(BlackListAndBookmarkHandler.UPDATE_USER_CATEGORY));
                    }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                            super.fail(codeError, response);
                            setBookmarkedState(null);
                            initOverfowMenu();
                        }
                    });
        } else {
        request = new BookmarkAddRequest(userId, mActivity).
                    callback(new BlackListAndBookmarkHandler(mActivity,
                            BlackListAndBookmarkHandler.ActionTypes.BOOKMARK,
                            userId,
                            true) {
                        @Override
                        public void success(IApiResponse response) {
                            super.success(response);
                            showBookmarkToast(true);
                            LocalBroadcastManager.getInstance(mActivity).
                                    sendBroadcast(new Intent(BlackListAndBookmarkHandler.UPDATE_USER_CATEGORY));
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                            super.fail(codeError, response);
                            setBookmarkedState(null);
                            initOverfowMenu();
                        }
                    })).exec();
        setBookmarkedState(null);
    }

    private void onClickAddToBookmarkAction() {
        Boolean isAddToFavoritsAvailable = isAddToFavoritsAvailable();
        if (isAddToFavoritsAvailable == null) {
            return;
        }
        if (!isAddToFavoritsAvailable) {
            showBuyVipActivity(R.string.add_to_favorite_block_not_vip);
        } else {
            addToFavorite();
        }
    }

    private void showBlackListToast(boolean value) {
        if (value) {
            Utils.showToastNotification(R.string.user_added_to_black_list, Toast.LENGTH_SHORT);
        } else {
            Utils.showToastNotification(R.string.user_deleted_from_black_list, Toast.LENGTH_SHORT);
        }
    }

    private void showBookmarkToast(boolean value) {
        if (value) {
            Utils.showToastNotification(R.string.user_added_to_bookmark, Toast.LENGTH_SHORT);
        } else {
            Utils.showToastNotification(R.string.user_deleted_from_bookmark, Toast.LENGTH_SHORT);
        }
    }

    private void setBookmarkedState(Boolean value) {
        OverflowMenuUser overflowMenuFieldsListener = getOverflowMenuFieldsListener();
        if (overflowMenuFieldsListener != null) {
            overflowMenuFieldsListener.setBookmarkValue(value);
        }
    }

    private void setBlackListState(Boolean value) {
        OverflowMenuUser overflowMenuFieldsListener = getOverflowMenuFieldsListener();
        if (overflowMenuFieldsListener != null) {
            overflowMenuFieldsListener.setBlackListValue(value);
            if (overflowMenuFieldsListener.getBlackListValue()) {
                overflowMenuFieldsListener.setBookmarkValue(false);
            }
        }
    }

    private void openChat() {
        Intent openChatIntent = getOpenChatIntent();
        if (openChatIntent == null || mActivityDelegate == null) {
            return;
        }
        mActivityDelegate.startActivityForResult(openChatIntent, ChatActivity.REQUEST_CHAT);
    }

    private void setSympathySentState(boolean state, boolean isNeedSentBroadcast) {
        OverflowMenuUser overflowMenuFieldsListener = getOverflowMenuFieldsListener();
        if (overflowMenuFieldsListener != null) {
            overflowMenuFieldsListener.setSympathySentValue(state);
            if (isNeedSentBroadcast && mContext != null) {
                LocalBroadcastManager.getInstance(mContext).
                        sendBroadcast(BlackListAndBookmarkHandler.getIntentForSympathyUpdate(BlackListAndBookmarkHandler.ActionTypes.SYMPATHY, state));
            }
        }
    }

    public void setSavedResponse(ApiResponse apiResponse) {
        mSavedResponse = apiResponse;
    }

    @Nullable
    public OverflowMenuUser getOverflowMenuFieldsListener() {
        return mOverflowMenuFields;
    }

    public void setOverflowMenuFieldsListener(OverflowMenuUser overflowMenuFieldsListener) {
        mOverflowMenuFields = overflowMenuFieldsListener;
    }

    @Nullable
    private Boolean isBookmarked() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().getBookmarkValue();
    }

    @Nullable
    private Boolean isInBlackList() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().getBlackListValue();
    }

    @Nullable
    private Boolean isSympathySent() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().getSympathySentValue();
    }

    @Nullable
    private Integer getUserId() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().getUserId();
    }

    @Nullable
    private Intent getOpenChatIntent() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().getOpenChatIntent();
    }

    @Nullable
    private Boolean isMutual() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().isMutual();
    }

    @Nullable
    private Boolean isChatAvailable() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().isOpenChatAvailable();
    }

    @Nullable
    private Boolean isAddToFavoritsAvailable() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().isAddToFavoritsAvailable();
    }

    @Nullable
    private Integer getProfileId() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().getProfileId();
    }

    @NotNull
    private Boolean isBanned() {
        return getOverflowMenuFieldsListener() == null ? false : getOverflowMenuFieldsListener().isBanned();
    }

    private void registerBroadcastReceiver() {
        if (mContext != null) {
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mUpdateActionsReceiver,
                    new IntentFilter(BlackListAndBookmarkHandler.UPDATE_USER_CATEGORY));
        }
    }

    public void onReleaseOverflowMenu() {
        if (mBalanceSubscription != null) {
            mBalanceSubscription.unsubscribe();
        }
        if (mContext != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mUpdateActionsReceiver);
        }
        mActivityDelegate = null;
        mOverflowMenuFields = null;
        mContext = null;
    }

    private enum OverflowMenuType {CHAT_OVERFLOW_MENU, PROFILE_OVERFLOW_MENU}

    public enum OverflowMenuItem {
        SEND_GIFT_ACTION(1, R.string.general_gift),
        SEND_SYMPATHY_ACTION(2, R.string.general_sympathy),
        SEND_ADMIRATION_ACTION(3, R.string.general_delight),
        OPEN_CHAT_ACTION(4, R.string.user_actions_chat),
        ADD_TO_BLACK_LIST_ACTION(5, R.string.black_list_add_short, R.string.black_list_delete),
        COMPLAIN_ACTION(6, R.string.general_complain),
        ADD_TO_BOOKMARK_ACTION(7, R.string.general_bookmarks_add, R.string.general_bookmarks_delete),
        OPEN_PROFILE_FOR_EDITOR_STUB(8, R.string.editor_profile_admin);
        private int mId;
        @StringRes
        private int mFirstResourceId;
        @StringRes
        private int mSecondResourceId;

        OverflowMenuItem(int id, @StringRes int firstResource) {
            this(id, firstResource, firstResource);
        }

        OverflowMenuItem(int id, @StringRes int firstResource, @StringRes int secondResource) {
            mId = id;
            mFirstResourceId = firstResource;
            mSecondResourceId = secondResource;
        }

        public int getId() {
            return mId;
        }

        @StringRes
        public int getFirstResourceId() {
            return mFirstResourceId;
        }

        @StringRes
        public int getSecondResourceId() {
            return mSecondResourceId;
        }
    }

    private static final MenuItem EMPTY_MENU_ITEM = new MenuItem() {
        @Override
        public int getItemId() {
            return 0;
        }

        @Override
        public int getGroupId() {
            return 0;
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public MenuItem setTitle(CharSequence title) {
            return null;
        }

        @Override
        public MenuItem setTitle(int title) {
            return null;
        }

        @Override
        public CharSequence getTitle() {
            return null;
        }

        @Override
        public MenuItem setTitleCondensed(CharSequence title) {
            return null;
        }

        @Override
        public CharSequence getTitleCondensed() {
            return null;
        }

        @Override
        public MenuItem setIcon(Drawable icon) {
            return null;
        }

        @Override
        public MenuItem setIcon(int iconRes) {
            return null;
        }

        @Override
        public Drawable getIcon() {
            return null;
        }

        @Override
        public MenuItem setIntent(Intent intent) {
            return null;
        }

        @Override
        public Intent getIntent() {
            return null;
        }

        @Override
        public MenuItem setShortcut(char numericChar, char alphaChar) {
            return null;
        }

        @Override
        public MenuItem setNumericShortcut(char numericChar) {
            return null;
        }

        @Override
        public char getNumericShortcut() {
            return 0;
        }

        @Override
        public MenuItem setAlphabeticShortcut(char alphaChar) {
            return null;
        }

        @Override
        public char getAlphabeticShortcut() {
            return 0;
        }

        @Override
        public MenuItem setCheckable(boolean checkable) {
            return null;
        }

        @Override
        public boolean isCheckable() {
            return false;
        }

        @Override
        public MenuItem setChecked(boolean checked) {
            return null;
        }

        @Override
        public boolean isChecked() {
            return false;
        }

        @Override
        public MenuItem setVisible(boolean visible) {
            return null;
        }

        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public MenuItem setEnabled(boolean enabled) {
            return null;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public boolean hasSubMenu() {
            return false;
        }

        @Override
        public SubMenu getSubMenu() {
            return null;
        }

        @Override
        public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
            return null;
        }

        @Override
        public ContextMenu.ContextMenuInfo getMenuInfo() {
            return null;
        }

        @Override
        public void setShowAsAction(int actionEnum) {

        }

        @Override
        public MenuItem setShowAsActionFlags(int actionEnum) {
            return null;
        }

        @Override
        public MenuItem setActionView(View view) {
            return null;
        }

        @Override
        public MenuItem setActionView(int resId) {
            return null;
        }

        @Override
        public View getActionView() {
            return null;
        }

        @Override
        public MenuItem setActionProvider(ActionProvider actionProvider) {
            return null;
        }

        @Override
        public ActionProvider getActionProvider() {
            return null;
        }

        @Override
        public boolean expandActionView() {
            return false;
        }

        @Override
        public boolean collapseActionView() {
            return false;
        }

        @Override
        public boolean isActionViewExpanded() {
            return false;
        }

        @Override
        public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
            return null;
        }
    };
}
