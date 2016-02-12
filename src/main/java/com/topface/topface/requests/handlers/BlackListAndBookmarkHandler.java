package com.topface.topface.requests.handlers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.topface.requests.IApiResponse;

import java.util.List;


/**
 * This handler sends broadcasts every time users added to or deleted from bookmarks or blacklist.
 */
public class BlackListAndBookmarkHandler extends VipApiHandler {

    public static final String UPDATE_USER_CATEGORY = "com.topface.topface.action.USER_CATEGORY";
    public static final String FEED_ID = "FEED_ID";
    public static final String FEED_IDS = "FEED_IDS";
    public static final String TYPE = "type";
    public static final String CHANGED = "changed";
    public static final String VALUE = "value";
    private Context mContext;
    private ActionTypes mActionType;
    private List<Integer> mUserIdList;
    private Integer mUserId;
    private boolean mIsAddition;
    private ApiHandler mCallback;

    public BlackListAndBookmarkHandler(Context context, ActionTypes actionType,
                                       int userId, boolean isAddition) {
        this(context, actionType, userId, isAddition, null);

    }

    public BlackListAndBookmarkHandler(Context context, ActionTypes actionType,
                                       int userId, boolean isAddition, ApiHandler callback) {
        mContext = context;
        mActionType = actionType;
        mUserId = userId;
        mIsAddition = isAddition;
        mCallback = callback;
    }

    public BlackListAndBookmarkHandler(Context context, ActionTypes actionType,
                                       List<Integer> userIds, boolean isAddition) {
        this(context, actionType, userIds, isAddition, null);
    }

    public BlackListAndBookmarkHandler(Context context, ActionTypes actionType,
                                       List<Integer> userIds, boolean isAddition, ApiHandler callback) {
        mContext = context;
        mActionType = actionType;
        mUserIdList = userIds;
        mIsAddition = isAddition;
        mCallback = callback;
    }

    public static Intent getIntentForActionsUpdate(ActionTypes type, boolean value) {
        Intent intent = new Intent(UPDATE_USER_CATEGORY);
        intent.putExtra(TYPE, type);
        intent.putExtra(CHANGED, value);
        return intent;
    }

    public static Intent getIntentForSympathyUpdate(ActionTypes type, boolean value) {
        Intent intent = new Intent(UPDATE_USER_CATEGORY);
        intent.putExtra(TYPE, type);
        intent.putExtra(VALUE, value);
        return intent;
    }

    public static Intent getValuedActionsUpdateIntent(ActionTypes type, int userId, boolean value) {
        Intent intent = new Intent(UPDATE_USER_CATEGORY);
        intent.putExtra(TYPE, type);
        intent.putExtra(VALUE, value);
        intent.putExtra(FEED_ID, userId);
        return intent;
    }

    public static Intent getValuedActionsUpdateIntent(ActionTypes type, List<Integer> userIds, boolean value) {
        int[] ids = new int[userIds.size()];
        for (int i = 0; i < userIds.size(); i++) {
            ids[i] = userIds.get(i);
        }
        Intent intent = new Intent(UPDATE_USER_CATEGORY);
        intent.putExtra(TYPE, type);
        intent.putExtra(VALUE, value);
        intent.putExtra(FEED_IDS, ids);
        return intent;
    }

    @Override
    public void success(IApiResponse response) {
        super.success(response);
        if (mCallback != null) {
            mCallback.success(response);
        }
        Intent intent = null;
        if (mUserId != null) {
            intent = getValuedActionsUpdateIntent(mActionType, mUserId, mIsAddition);
        } else if (mUserIdList != null) {
            intent = getValuedActionsUpdateIntent(mActionType, mUserIdList, mIsAddition);
        }
        if (intent != null) {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }

    @Override
    public void fail(int codeError, IApiResponse response) {
        super.fail(codeError, response);
        if (mCallback != null) {
            mCallback.fail(codeError, response);
        }
        Intent intent = getIntentForActionsUpdate(mActionType, false);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void always(IApiResponse response) {
        super.always(response);
        if (mCallback != null) {
            mCallback.always(response);
        }
    }

    public enum ActionTypes {BLACK_LIST, BOOKMARK, SYMPATHY}
}
