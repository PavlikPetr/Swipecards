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

import com.topface.topface.App;
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
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.RateController;

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

    private MenuItem mBarActions;
    private OverflowMenuType mOverflowMenuType;
    private Activity mActivity;
    private RateController mRateController;
    private int mProfileId;
    private ApiResponse mSavedResponse = null;
    private OverflowMenuUser mOverflowMenuFields = null;
    private Boolean mIsInBlackList;
    private Boolean mIsBookmarked;
    private Boolean mIsSympathySent;
    private Integer mUserId;
    private Intent mOpenChatIntent;
    private Boolean mIsMutual;
    private Boolean mIsChatAvailable;
    private Boolean mIsAddToFavoritsAvailable;
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
                        initFieldIsInBlackList();
                        if (intent.hasExtra(BlackListAndBookmarkHandler.VALUE) && mIsInBlackList != null && !mIsInBlackList) {
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

    public OverflowMenu(Activity activity, MenuItem barActions) {
        mBarActions = barActions;
        mOverflowMenuType = OverflowMenuType.CHAT_OVERFLOW_MENU;
        mActivity = activity;
        registerBroadcastReceiver();
    }

    public OverflowMenu(Activity activity, MenuItem barActions, RateController rateController, ApiResponse savedResponse) {
        mBarActions = barActions;
        mOverflowMenuType = OverflowMenuType.PROFILE_OVERFLOW_MENU;
        mActivity = activity;
        mRateController = rateController;
        mSavedResponse = savedResponse;
        registerBroadcastReceiver();
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

    public ArrayList<OverflowMenuItem> getChatOverflowMenu() {
        ArrayList<OverflowMenuItem> result = new ArrayList<>();
        result.add(ADD_TO_BLACK_LIST_ACTION);
        result.add(ADD_TO_BOOKMARK_ACTION);
        result.add(COMPLAIN_ACTION);
        return result;
    }

    public ArrayList<OverflowMenuItem> getProfileOverflowMenu(boolean isEditor) {
        ArrayList<OverflowMenuItem> result = new ArrayList<>();
        result.add(SEND_SYMPATHY_ACTION);
        result.add(SEND_ADMIRATION_ACTION);
        result.add(OPEN_CHAT_ACTION);
        result.add(SEND_GIFT_ACTION);
        result.add(ADD_TO_BLACK_LIST_ACTION);
        result.add(ADD_TO_BOOKMARK_ACTION);
        result.add(COMPLAIN_ACTION);
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
        if (mBarActions != null && mBarActions.hasSubMenu()) {
            initFieldIsBookmarked();
            initFieldIsInBlackList();
            initFieldIsSympathySent();
            mBarActions.getSubMenu().clear();
            ArrayList<OverflowMenuItem> overflowMenuItemArray = getProfileOverflowMenu(CacheProfile.isEditor());
            for (int i = 0; i < overflowMenuItemArray.size(); i++) {
                OverflowMenuItem item = overflowMenuItemArray.get(i);
                Integer resourceId = null;
                switch (overflowMenuItemArray.get(i)) {
                    case ADD_TO_BLACK_LIST_ACTION:
                        if (mIsInBlackList != null) {
                            resourceId = mIsInBlackList ? item.getSecondResourceId() : item.getFirstResourceId();
                        }
                        break;
                    case ADD_TO_BOOKMARK_ACTION:
                        if (mIsBookmarked != null) {
                            resourceId = mIsBookmarked ? item.getSecondResourceId() : item.getFirstResourceId();
                        }
                        break;
                    default:
                        resourceId = item.getFirstResourceId();
                        break;
                }
                mBarActions.getSubMenu().add(Menu.NONE, item.getId(), Menu.NONE, resourceId != null ? mActivity.getString(resourceId) : "");
            }
            if (mIsInBlackList != null) {
                mBarActions.getSubMenu().findItem(ADD_TO_BOOKMARK_ACTION.getId()).setEnabled(!mIsInBlackList);
            }
            if (mIsSympathySent != null && mIsSympathySent) {
                mBarActions.getSubMenu().findItem(SEND_SYMPATHY_ACTION.getId()).setEnabled(false);
                mBarActions.getSubMenu().findItem(SEND_ADMIRATION_ACTION.getId()).setEnabled(false);
            }
        }
    }

    private void initChatOverflowMenu() {
        if (mBarActions != null && mBarActions.hasSubMenu()) {
            initFieldIsBookmarked();
            initFieldIsInBlackList();
            mBarActions.getSubMenu().clear();
            ArrayList<OverflowMenuItem> overflowMenuItemArray = getChatOverflowMenu();
            for (int i = 0; i < overflowMenuItemArray.size(); i++) {
                OverflowMenuItem item = overflowMenuItemArray.get(i);
                Integer resourceId = null;
                switch (overflowMenuItemArray.get(i)) {
                    case ADD_TO_BLACK_LIST_ACTION:
                        if (mIsInBlackList != null) {
                            resourceId = mIsInBlackList ? item.getSecondResourceId() : item.getFirstResourceId();
                        }
                        break;
                    case ADD_TO_BOOKMARK_ACTION:
                        if (mIsBookmarked != null) {
                            resourceId = mIsBookmarked ? item.getSecondResourceId() : item.getFirstResourceId();
                        }
                        break;
                    default:
                        resourceId = item.getFirstResourceId();
                        break;
                }
                mBarActions.getSubMenu().add(Menu.NONE, item.getId(), Menu.NONE, resourceId != null ? mActivity.getString(resourceId) : "");
            }
            if (mIsInBlackList != null) {
                mBarActions.getSubMenu().findItem(ADD_TO_BOOKMARK_ACTION.getId()).setEnabled(!mIsInBlackList);
            }
        }
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
                    initFieldProfileId();
                    mActivity.startActivity(ComplainsActivity.createIntent(mProfileId));
                    break;
                case OPEN_PROFILE_FOR_EDITOR_STUB:
                    initFieldProfileId();
                    if (mSavedResponse != null) {
                        mActivity.startActivity(EditorProfileActionsActivity.createIntent(mProfileId, mSavedResponse));
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
        initFieldUserId();
        initFieldIsMutual();
        if (mRateController == null || mUserId == null || mIsMutual == null) {
            return;
        }
        mRateController.onLike(
                mUserId,
                mIsMutual ?
                        SendLikeRequest.DEFAULT_MUTUAL : SendLikeRequest.DEFAULT_NO_MUTUAL,
                new RateController.OnRateRequestListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onRateCompleted(int mutualId) {
                        setSympathySentState(true, true);
                        if (mActivity != null) {
                            Toast.makeText(App.getContext(), R.string.sympathy_sended, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onRateFailed(int userId, int mutualId) {
                        setSympathySentState(false, true);
                        if (mActivity != null) {
                            Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                        }
                        initOverfowMenu();
                    }
                }
        );
        setSympathySentState(true, true);
    }

    private void onClickSendAdmirationAction() {
        initFieldUserId();
        initFieldIsMutual();
        if (mRateController == null || mUserId == null || mIsMutual == null) {
            return;
        }
        boolean isSentAdmiration = mRateController.onAdmiration(
                mUserId,
                mIsMutual ?
                        SendLikeRequest.DEFAULT_MUTUAL : SendLikeRequest.DEFAULT_NO_MUTUAL,
                new RateController.OnRateRequestListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onRateCompleted(int mutualId) {
                        setSympathySentState(true, true);
                        if (mActivity != null) {
                            Toast.makeText(App.getContext(), R.string.admiration_sended, Toast.LENGTH_SHORT).show();
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
        initFieldIsChatAvailable();
        if (!mIsChatAvailable) {
            showBuyVipActivity(R.string.chat_block_not_mutual);
        } else {
            openChat();
        }
    }

    private void onClickSendGiftAction() {
        if (mOverflowMenuFields != null) {
            mOverflowMenuFields.clickSendGift();
        }
    }

    private void onClickAddToBlackList() {
        initFieldIsInBlackList();
        initFieldUserId();
        if (mIsInBlackList == null || mUserId == null) {
            return;
        }
        ApiRequest request;
        if (mIsInBlackList) {
            request = new DeleteBlackListRequest(mUserId, mActivity).
                    callback(new BlackListAndBookmarkHandler(mActivity,
                            BlackListAndBookmarkHandler.ActionTypes.BLACK_LIST,
                            mUserId,
                            false) {
                        @Override
                        public void success(IApiResponse response) {
                            super.success(response);
                            showBlackListToast(false);
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                            super.fail(codeError, response);
                            setBlackListState(null);
                            initOverfowMenu();
                        }
                    });
        } else {
            request = new BlackListAddRequest(mUserId, mActivity).
                    callback(new BlackListAndBookmarkHandler(mActivity,
                            BlackListAndBookmarkHandler.ActionTypes.BLACK_LIST,
                            mUserId,
                            true) {
                        @Override
                        public void success(IApiResponse response) {
                            super.success(response);
                            showBlackListToast(true);
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
        initFieldIsBookmarked();
        initFieldUserId();
        if (mIsBookmarked == null || mUserId == null) {
            return;
        }
        ApiRequest request;
        if (mIsBookmarked) {
            request = new DeleteBookmarksRequest(mUserId, mActivity).
                    callback(new BlackListAndBookmarkHandler(mActivity,
                            BlackListAndBookmarkHandler.ActionTypes.BOOKMARK,
                            mUserId,
                            false) {
                        @Override
                        public void success(IApiResponse response) {
                            super.success(response);
                            showBookmarkToast(false);
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                            super.fail(codeError, response);
                            setBookmarkedState(null);
                            initOverfowMenu();
                        }
                    });
        } else {
            request = new BookmarkAddRequest(mUserId, mActivity).
                    callback(new BlackListAndBookmarkHandler(mActivity,
                            BlackListAndBookmarkHandler.ActionTypes.BOOKMARK,
                            mUserId,
                            true) {
                        @Override
                        public void success(IApiResponse response) {
                            super.success(response);
                            showBookmarkToast(true);
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
        initFieldIsAddToFavoritsAvailable();
        if (mIsAddToFavoritsAvailable == null) {
            return;
        }
        if (!mIsAddToFavoritsAvailable) {
            showBuyVipActivity(R.string.add_to_favorite_block_not_vip);
        } else {
            addToFavorite();
        }
    }

    private void showBlackListToast(boolean value) {
        Toast.makeText(mActivity.getApplicationContext(),
                value ?
                        R.string.user_added_to_black_list :
                        R.string.user_deleted_from_black_list,
                Toast.LENGTH_SHORT).show();
    }

    private void showBookmarkToast(boolean value) {
        Toast.makeText(mActivity.getApplicationContext(),
                value ?
                        R.string.user_added_to_bookmark :
                        R.string.user_deleted_from_bookmark,
                Toast.LENGTH_SHORT).show();
    }

    private void setBookmarkedState(Boolean value) {
        if (mOverflowMenuFields == null) {
            return;
        }
        mOverflowMenuFields.setBookmarkValue(value);
    }

    private void setBlackListState(Boolean value) {
        if (mOverflowMenuFields == null) {
            return;
        }
        mOverflowMenuFields.setBlackListValue(value);
        if (mOverflowMenuFields.getBlackListValue()) {
            mOverflowMenuFields.setBookmarkValue(false);
        }
    }

    private void openChat() {
        initFieldOpenChatIntent();
        if (mOpenChatIntent == null) {
            return;
        }
        mActivity.startActivityForResult(mOpenChatIntent, ChatActivity.INTENT_CHAT);
    }

    private void setSympathySentState(boolean state, boolean isNeedSentBroadcast) {
        if (mOverflowMenuFields != null) {
            mOverflowMenuFields.setSympathySentValue(state);
            if (isNeedSentBroadcast) {
                LocalBroadcastManager.getInstance(mActivity.getApplicationContext()).
                        sendBroadcast(BlackListAndBookmarkHandler.getIntentForSympathyUpdate(BlackListAndBookmarkHandler.ActionTypes.SYMPATHY, state));
            }
        }
    }

    public void setSavedResponse(ApiResponse apiResponse) {
        mSavedResponse = apiResponse;
    }

    public void setOverflowMenuFieldsListener(OverflowMenuUser overflowMenuFieldsListener) {
        mOverflowMenuFields = overflowMenuFieldsListener;
    }

    public OverflowMenuUser getOverflowMenuFieldsListener() {
        return mOverflowMenuFields;
    }

    private void initFieldIsBookmarked() {
        if (mOverflowMenuFields != null) {
            mIsBookmarked = mOverflowMenuFields.getBookmarkValue();
        }
    }

    private void initFieldIsInBlackList() {
        if (mOverflowMenuFields != null) {
            mIsInBlackList = mOverflowMenuFields.getBlackListValue();
        }
    }

    private void initFieldIsSympathySent() {
        if (mOverflowMenuFields != null) {
            mIsSympathySent = mOverflowMenuFields.getSympathySentValue();
        }
    }

    private void initFieldUserId() {
        if (mOverflowMenuFields != null) {
            mUserId = mOverflowMenuFields.getUserId();
        }
    }

    private void initFieldOpenChatIntent() {
        if (mOverflowMenuFields != null) {
            mOpenChatIntent = mOverflowMenuFields.getOpenChatIntent();
        }
    }

    private void initFieldIsMutual() {
        if (mOverflowMenuFields != null) {
            mIsMutual = mOverflowMenuFields.isMutual();
        }
    }

    private void initFieldIsChatAvailable() {
        if (mOverflowMenuFields != null) {
            mIsChatAvailable = mOverflowMenuFields.isOpenChatAvailable();
        }
    }

    private void initFieldIsAddToFavoritsAvailable() {
        if (mOverflowMenuFields != null) {
            mIsAddToFavoritsAvailable = mOverflowMenuFields.isAddToFavoritsAvailable();
        }
    }

    private void initFieldProfileId() {
        if (mOverflowMenuFields != null) {
            mProfileId = mOverflowMenuFields.getProfileId();
        }
    }

    private void registerBroadcastReceiver() {
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mUpdateActionsReceiver,
                new IntentFilter(BlackListAndBookmarkHandler.UPDATE_USER_CATEGORY));
    }

    public void onDestroy() {
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mUpdateActionsReceiver);
        mOverflowMenuFields = null;
        mActivity = null;
    }
}
