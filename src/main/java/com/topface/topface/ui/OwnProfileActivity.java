package com.topface.topface.ui;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.topface.topface.Static;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.ui.fragments.EditorProfileActionsFragment;
import com.topface.topface.ui.fragments.profile.AbstractProfileFragment;
import com.topface.topface.ui.fragments.profile.OwnProfileFragment;
import com.topface.topface.ui.fragments.profile.UserProfileFragment;

public class OwnProfileActivity extends CheckAuthActivity<OwnProfileFragment> {

    public static final int INTENT_USER_PROFILE = 6;

    public static Intent createIntent(int userId, Context context) {
        return createIntent(userId, null, Static.EMPTY, context);
    }

    public static Intent createIntent(int userId, String itemId, Context context) {
        return createIntent(userId, itemId, null, context);
    }

    public static Intent createIntent(int userId, Class callingClass, Context context) {
        return createIntent(userId, null, callingClass.getName(), context);
    }

    public static Intent createIntent(int userId, String itemId, String className, Context context) {
        Intent intent = new Intent(context, OwnProfileActivity.class);
        intent.putExtra(AbstractProfileFragment.INTENT_UID, userId);
        if (!TextUtils.isEmpty(className)) {
            intent.putExtra(AbstractProfileFragment.INTENT_CALLING_FRAGMENT, className);
        }
        if (!TextUtils.isEmpty(itemId)) {
            intent.putExtra(AbstractProfileFragment.INTENT_ITEM_ID, itemId);
        }
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_USER_PROFILE);
        return intent;
    }

    public static Intent createIntent(int userId, String itemId, String className, String bodyStartPageClassName, Context context) {
        return createIntent(userId, itemId, className, context)
                .putExtra(AbstractProfileFragment.INTENT_START_BODY_PAGE_NAME, bodyStartPageClassName);
    }

    public static Intent createIntent(ApiResponse response, int userId, String itemId, String className, String bodyStartPageClassName, Context context) {
        Intent intent = createIntent(response, userId, className, bodyStartPageClassName, context);
        if (!TextUtils.isEmpty(itemId)) {
            intent.putExtra(AbstractProfileFragment.INTENT_ITEM_ID, itemId);
        }
        return intent;
    }

    public static Intent createIntent(ApiResponse response, int userId, String className, String bodyStartPageClassName, Context context) {
        return createIntent(response, userId, className, context)
                .putExtra(AbstractProfileFragment.INTENT_START_BODY_PAGE_NAME, bodyStartPageClassName);
    }

    public static Intent createIntent(ApiResponse response, int userId, String className, Context context) {
        Intent intent = new Intent(context, OwnProfileActivity.class);
        intent.putExtra(AbstractProfileFragment.INTENT_UID, userId);
        if (!TextUtils.isEmpty(className)) {
            intent.putExtra(AbstractProfileFragment.INTENT_CALLING_FRAGMENT, className);
        }
        intent.putExtra(EditorProfileActionsFragment.PROFILE_RESPONSE, response.toJson().toString());
        return intent;
    }

    @Override
    protected String getFragmentTag() {
        return UserProfileFragment.class.getSimpleName();
    }

    @Override
    protected OwnProfileFragment createFragment() {
        return new OwnProfileFragment();
    }

    @Override
    protected void setActionBarView() {
        super.setActionBarView();
    }
}
