package com.topface.topface.ui;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.topface.topface.Static;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.ui.fragments.EditorProfileActionsFragment;
import com.topface.topface.ui.fragments.profile.AbstractProfileFragment;
import com.topface.topface.ui.fragments.profile.UserProfileFragment;

public class UserProfileActivity extends CheckAuthActivity<UserProfileFragment> {

    public static final int INTENT_USER_PROFILE = 6;

    public static Intent createIntent(int userId, Context context) {
        return createIntent(userId, null, context, true, true);
    }

    public static Intent createIntent(int userId, Context context, boolean isChatAvailable, boolean isAddToFavoritsAvailable) {
        return createIntent(userId, null, context, isChatAvailable, isAddToFavoritsAvailable);
    }

    public static Intent createIntent(int userId, String itemId, Context context, boolean isChatAvailable, boolean isAddToFavoritsAvailable) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(AbstractProfileFragment.INTENT_UID, userId);
        intent.putExtra(AbstractProfileFragment.INTENT_IS_CHAT_AVAILABLE, isChatAvailable);
        intent.putExtra(AbstractProfileFragment.INTENT_IS_ADD_TO_FAVORITS_AVAILABLE, isAddToFavoritsAvailable);
        if (!TextUtils.isEmpty(itemId)) {
            intent.putExtra(AbstractProfileFragment.INTENT_ITEM_ID, itemId);
        }
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_USER_PROFILE);
        return intent;
    }

    public static Intent createIntent(int userId, String itemId, String bodyStartPageClassName, Context context) {
        return createIntent(userId, itemId, context, true, true)
                .putExtra(AbstractProfileFragment.INTENT_START_BODY_PAGE_NAME, bodyStartPageClassName);
    }

    public static Intent createIntent(ApiResponse response, int userId, String itemId, String bodyStartPageClassName, Context context) {
        Intent intent = createIntent(response, userId, bodyStartPageClassName, context);
        if (!TextUtils.isEmpty(itemId)) {
            intent.putExtra(AbstractProfileFragment.INTENT_ITEM_ID, itemId);
        }
        return intent;
    }

    public static Intent createIntent(ApiResponse response, int userId, String bodyStartPageClassName, Context context) {
        return createIntent(response, userId, context)
                .putExtra(AbstractProfileFragment.INTENT_START_BODY_PAGE_NAME, bodyStartPageClassName);
    }

    public static Intent createIntent(ApiResponse response, int userId, Context context) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(AbstractProfileFragment.INTENT_UID, userId);
        intent.putExtra(EditorProfileActionsFragment.PROFILE_RESPONSE, response.toJson().toString());
        return intent;
    }

    @Override
    protected String getFragmentTag() {
        return UserProfileFragment.class.getSimpleName();
    }

    @Override
    protected UserProfileFragment createFragment() {
        return new UserProfileFragment();
    }

    @Override
    protected void setActionBarView() {
        super.setActionBarView();
    }
}
