package com.topface.topface.utils.actionbar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.topface.topface.R;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BlackListAddRequest;
import com.topface.topface.requests.BookmarkAddRequest;
import com.topface.topface.requests.DeleteBlackListRequest;
import com.topface.topface.requests.DeleteBookmarksRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.ComplainsActivity;
import com.topface.topface.ui.EditorProfileActionsActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;

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

    private final static String INTENT_BUY_VIP_FROM = "UserProfileFragment";
    public static final String USER_ID_FOR_REMOVE = "user_id";

    private Menu mBarActions;
    private OverflowMenuType mOverflowMenuType;
    private Activity mActivity;
    private RateController mRateController;
    private ApiResponse mSavedResponse = null;
    private OverflowMenuUser mOverflowMenuFields = null;
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

    public OverflowMenu(Activity activity, Menu barActions) {
        mBarActions = barActions;
        mOverflowMenuType = OverflowMenuType.CHAT_OVERFLOW_MENU;
        mActivity = activity;
        registerBroadcastReceiver();
    }

    public OverflowMenu(Activity activity, Menu barActions, RateController rateController, ApiResponse savedResponse) {
        mBarActions = barActions;
        mOverflowMenuType = OverflowMenuType.PROFILE_OVERFLOW_MENU;
        mActivity = activity;
        mRateController = rateController;
        mSavedResponse = savedResponse;
        registerBroadcastReceiver();
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
            result.add(SEND_ADMIRATION_ACTION);
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
            ArrayList<OverflowMenuItem> overflowMenuItemArray = getProfileOverflowMenu(CacheProfile.isEditor(), isBanned());
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
                if (mActivity != null && isNeedToAddItem(item.getId())) {
                    mBarActions.add(Menu.NONE, item.getId(), Menu.NONE, resourceId != null ? mActivity.getString(resourceId) : "").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                }
            }
            if (overflowMenuItemArray.size() > 1) {
                if (isInBlackList != null) {
                    mBarActions.findItem(ADD_TO_BOOKMARK_ACTION.getId()).setEnabled(!isInBlackList);
                }
                if (isSympathySent != null && isSympathySent) {
                    mBarActions.findItem(SEND_SYMPATHY_ACTION.getId()).setEnabled(false);
                    mBarActions.findItem(SEND_ADMIRATION_ACTION.getId()).setEnabled(false);
                }
            }
        }
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
                if (mActivity != null && isNeedToAddItem(item.getId())) {
                    mBarActions.add(Menu.NONE, item.getId(), Menu.NONE, resourceId != null ? mActivity.getString(resourceId) : "").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                }
            }
            if (isInBlackList != null) {
                mBarActions.findItem(ADD_TO_BOOKMARK_ACTION.getId()).setEnabled(!isInBlackList);
            }
        }
    }

    private boolean isNeedToAddItem(int id) {
        return mBarActions.findItem(id) == null;
    }

    public void onMenuClicked(MenuItem item) {
        int itemId = item.getItemId();
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
                    mActivity.startActivity(ComplainsActivity.createIntent(getProfileId()));
                    break;
                case OPEN_PROFILE_FOR_EDITOR_STUB:
                    if (mSavedResponse != null) {
                        mActivity.startActivity(EditorProfileActionsActivity.createIntent(getProfileId(), mSavedResponse));
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
                        if (mActivity != null) {
                            Utils.showToastNotification(R.string.sympathy_sended, Toast.LENGTH_SHORT);
                        }
                    }

                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onRateFailed(int userId, int mutualId) {
                        setSympathySentState(false, true);
                        if (mActivity != null) {
                            Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_SHORT);
                        }
                        initOverfowMenu();
                    }
                }
        );
        setSympathySentState(true, true);
    }

    private void onClickSendAdmirationAction() {
        Integer userId = getUserId();
        Boolean isMutual = isMutual();
        if (mRateController == null || userId == null || isMutual == null) {
            return;
        }
        boolean isSentAdmiration = mRateController.onAdmiration(
                userId,
                isMutual ?
                        SendLikeRequest.DEFAULT_MUTUAL : SendLikeRequest.DEFAULT_NO_MUTUAL,
                new RateController.OnRateRequestListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onRateCompleted(int mutualId) {
                        setSympathySentState(true, true);
                        if (mActivity != null) {
                            Utils.showToastNotification(R.string.admiration_sended, Toast.LENGTH_SHORT);
                        }
                    }

                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onRateFailed(int userId, int mutualId) {
                        setSympathySentState(false, true);
                        initOverfowMenu();
                    }
                }
        );
        if (isSentAdmiration) {
            setSympathySentState(true, true);
        }
    }

    private void showBuyVipActivity(int resourceId) {
        mActivity.startActivityForResult(
                PurchasesActivity.createVipBuyIntent(mActivity.getString(resourceId), INTENT_BUY_VIP_FROM),
                PurchasesActivity.INTENT_BUY_VIP);
    }

    private void onClickOpenChatAction() {
        if (!isChatAvailable()) {
            showBuyVipActivity(R.string.chat_block_not_mutual);
        } else {
            openChat();
        }
    }

    private void onClickSendGiftAction() {
        if (getOverflowMenuFieldsListener() != null) {
            getOverflowMenuFieldsListener().clickSendGift();
        }
    }

    private void onClickAddToBlackList() {
        Boolean isInBlackList = isInBlackList();
        final Integer userId = getUserId();
        if (isInBlackList == null || userId == null) {
            return;
        }
        ApiRequest request;
        if (isInBlackList) {
            request = new DeleteBlackListRequest(userId, mActivity).
                    callback(new BlackListAndBookmarkHandler(mActivity,
                            BlackListAndBookmarkHandler.ActionTypes.BLACK_LIST,
                            userId,
                            false) {
                        @Override
                        public void success(IApiResponse response) {
                            super.success(response);
                            showBlackListToast(false);
                            LocalBroadcastManager.getInstance(mActivity).
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
        } else {
            request = new BlackListAddRequest(userId, mActivity).
                    callback(new BlackListAndBookmarkHandler(mActivity,
                            BlackListAndBookmarkHandler.ActionTypes.BLACK_LIST,
                            userId,
                            true) {
                        @Override
                        public void success(IApiResponse response) {
                            super.success(response);
                            showBlackListToast(true);
                            LocalBroadcastManager.getInstance(mActivity).
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
        if (isBookmarked == null || userId == null) {
            return;
        }
        ApiRequest request;
        if (isBookmarked) {
            request = new DeleteBookmarksRequest(userId, mActivity).
                    callback(new BlackListAndBookmarkHandler(mActivity,
                            BlackListAndBookmarkHandler.ActionTypes.BOOKMARK,
                            userId,
                            false) {
                        @Override
                        public void success(IApiResponse response) {
                            super.success(response);
                            showBookmarkToast(false);
                            LocalBroadcastManager.getInstance(mActivity).
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
                    });
        }
        setBookmarkedState(null);
        request.exec();
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
        if (getOverflowMenuFieldsListener() == null) {
            return;
        }
        getOverflowMenuFieldsListener().setBookmarkValue(value);
    }

    private void setBlackListState(Boolean value) {
        if (getOverflowMenuFieldsListener() == null) {
            return;
        }
        getOverflowMenuFieldsListener().setBlackListValue(value);
        if (getOverflowMenuFieldsListener().getBlackListValue()) {
            getOverflowMenuFieldsListener().setBookmarkValue(false);
        }
    }

    private void openChat() {
        Intent openChatIntent = getOpenChatIntent();
        if (openChatIntent == null) {
            return;
        }
        mActivity.startActivityForResult(openChatIntent, ChatActivity.REQUEST_CHAT);
    }

    private void setSympathySentState(boolean state, boolean isNeedSentBroadcast) {
        if (getOverflowMenuFieldsListener() != null) {
            getOverflowMenuFieldsListener().setSympathySentValue(state);
            if (isNeedSentBroadcast) {
                LocalBroadcastManager.getInstance(mActivity.getApplicationContext()).
                        sendBroadcast(BlackListAndBookmarkHandler.getIntentForSympathyUpdate(BlackListAndBookmarkHandler.ActionTypes.SYMPATHY, state));
            }
        }
    }

    public void setSavedResponse(ApiResponse apiResponse) {
        mSavedResponse = apiResponse;
    }

    public OverflowMenuUser getOverflowMenuFieldsListener() {
        return mOverflowMenuFields;
    }

    public void setOverflowMenuFieldsListener(OverflowMenuUser overflowMenuFieldsListener) {
        mOverflowMenuFields = overflowMenuFieldsListener;
    }

    private Boolean isBookmarked() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().getBookmarkValue();
    }

    private Boolean isInBlackList() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().getBlackListValue();
    }

    private Boolean isSympathySent() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().getSympathySentValue();
    }

    private Integer getUserId() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().getUserId();
    }

    private Intent getOpenChatIntent() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().getOpenChatIntent();
    }

    private Boolean isMutual() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().isMutual();
    }

    private Boolean isChatAvailable() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().isOpenChatAvailable();
    }

    private Boolean isAddToFavoritsAvailable() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().isAddToFavoritsAvailable();
    }

    private Integer getProfileId() {
        return getOverflowMenuFieldsListener() == null ? null : getOverflowMenuFieldsListener().getProfileId();
    }

    private Boolean isBanned() {
        return getOverflowMenuFieldsListener() == null ? false : getOverflowMenuFieldsListener().isBanned();
    }

    private void registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mUpdateActionsReceiver,
                new IntentFilter(BlackListAndBookmarkHandler.UPDATE_USER_CATEGORY));
    }

    public void onReleaseOverflowMenu() {
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mUpdateActionsReceiver);
        mOverflowMenuFields = null;
        mActivity = null;
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
        private int mFirstResourceId;
        private int mSecondResourceId;

        OverflowMenuItem(int id, int firstResource) {
            this(id, firstResource, firstResource);
        }

        OverflowMenuItem(int id, int firstResource, int secondResource) {
            mId = id;
            mFirstResourceId = firstResource;
            mSecondResourceId = secondResource;
        }

        public int getId() {
            return mId;
        }

        public int getFirstResourceId() {
            return mFirstResourceId;
        }

        public int getSecondResourceId() {
            return mSecondResourceId;
        }
    }
}
