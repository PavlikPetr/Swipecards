package com.topface.topface.requests.handlers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.topface.requests.IApiResponse;

/**
 * This handler sends broadcasts every time users added to or deleted from bookmarks or blacklist.
 */
public class AttitudeHandler extends VipApiHandler {

    public static final String UPDATE_USER_CATEGORY = "com.topface.topface.action.USER_CATEGORY";
    public static final String FEED_ID = "FEED_ID";
    public static final String FEED_IDS = "FEED_IDS";
    public static final String TYPE = "type";
    public static final String CHANGED = "changed";
    public static final String VALUE = "value";
    private Context mContext;
    private ActionTypes mActionType;
    private int[] mUserId;
    private boolean mIsAddition;

    public AttitudeHandler(Context context, ActionTypes actionType,
                           int[] userId, boolean isAddition) {
        mContext = context;
        mActionType = actionType;
        mUserId = userId;
        mIsAddition = isAddition;
    }

    public static Intent getIntentForActionsUpdate(ActionTypes type, boolean value) {
        Intent intent = new Intent(UPDATE_USER_CATEGORY);
        intent.putExtra(TYPE, type);
        intent.putExtra(CHANGED, value);
        return intent;
    }

    public static Intent getValuedActionsUpdateIntent(ActionTypes type, int userId, boolean value) {
        Intent intent = new Intent(UPDATE_USER_CATEGORY);
        intent.putExtra(TYPE, type);
        intent.putExtra(VALUE, value);
        intent.putExtra(FEED_ID, userId);
        intent.putExtra(FEED_IDS, new int[]{userId});
        return intent;
    }

    public static Intent getValuedActionsUpdateIntent(ActionTypes type, int[] userIds, boolean value) {
        Intent intent = new Intent(UPDATE_USER_CATEGORY);
        intent.putExtra(TYPE, type);
        intent.putExtra(VALUE, value);
        intent.putExtra(FEED_IDS, userIds);
        return intent;
    }

    @Override
    public void success(IApiResponse response) {
        super.success(response);
        Intent intent;
        if (mUserId.length == 1) {
            intent = getValuedActionsUpdateIntent(mActionType, mUserId[0], mIsAddition);
        } else {
            intent = getValuedActionsUpdateIntent(mActionType, mUserId, mIsAddition);
        }
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void fail(int codeError, IApiResponse response) {
        super.fail(codeError, response);
        Intent intent = getIntentForActionsUpdate(mActionType, false);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public enum ActionTypes {BLACK_LIST, BOOKMARK}
}
