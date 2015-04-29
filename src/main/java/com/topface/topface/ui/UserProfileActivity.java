package com.topface.topface.ui;

import android.content.Intent;
import android.text.TextUtils;

import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.fragments.EditorProfileActionsFragment;
import com.topface.topface.ui.fragments.profile.AbstractProfileFragment;
import com.topface.topface.ui.fragments.profile.UserProfileFragment;

public class UserProfileActivity extends CheckAuthActivity<UserProfileFragment> {

    public static final int INTENT_USER_PROFILE = 6;

    public static Intent createIntent(ApiResponse response, int userId, String itemId, boolean isChatAvailable, boolean isAddToFavoritsAvailable, String nameAndAge, String city) {
        Intent intent = new Intent(App.getContext(), UserProfileActivity.class);
        intent.putExtra(ChatFragment.INTENT_USER_NAME_AND_AGE, nameAndAge);
        intent.putExtra(ChatFragment.INTENT_USER_CITY, city);
        if (response != null) {
            intent.putExtra(EditorProfileActionsFragment.PROFILE_RESPONSE, response.toJson().toString());
        }
        intent.putExtra(AbstractProfileFragment.INTENT_UID, userId);
        intent.putExtra(AbstractProfileFragment.INTENT_IS_CHAT_AVAILABLE, isChatAvailable);
        intent.putExtra(AbstractProfileFragment.INTENT_IS_ADD_TO_FAVORITS_AVAILABLE, isAddToFavoritsAvailable);
        if (!TextUtils.isEmpty(itemId)) {
            intent.putExtra(AbstractProfileFragment.INTENT_ITEM_ID, itemId);
        }
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_USER_PROFILE);
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
