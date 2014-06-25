package com.topface.topface.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.profile.AbstractProfileFragment;

public class UserProfileActivity extends UserOnlineActivity {

    public static final int INTENT_USER_PROFILE = 6;

    public static Intent getUserProfileIntent(int userId, Context context) {
        return getUserProfileIntent(userId, null, Static.EMPTY, context);
    }

    public static Intent getUserProfileIntent(int userId, String itemId, Context context) {
        return getUserProfileIntent(userId, itemId, null, context);
    }

    public static Intent getUserProfileIntent(int userId, Class callingClass, Context context) {
        return getUserProfileIntent(userId, null, callingClass.getName(), context);
    }

    public static Intent getUserProfileIntent(int userId, String itemId, String className, Context context) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isTaskRoot()) {
                    Intent i = new Intent(this, NavigationActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    onBackPressed();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
