package com.topface.topface.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.profile.AbstractProfileFragment;

public class UserProfileActivity extends UserOnlineActivity {

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
        Intent i = new Intent(context, UserProfileActivity.class);
        i.putExtra(AbstractProfileFragment.INTENT_UID, userId);
        if (className != null) {
            i.putExtra(AbstractProfileFragment.INTENT_CALLING_FRAGMENT, className);
        }
        if (itemId != null) {
            i.putExtra(AbstractProfileFragment.INTENT_ITEM_ID, itemId);
        }
        i.putExtra(Static.INTENT_REQUEST_KEY, INTENT_USER_PROFILE);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_user_profile);
    }
}
