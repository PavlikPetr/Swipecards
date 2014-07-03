package com.topface.topface.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.ComplainsFragment;

public class ComplainsActivity extends CheckAuthActivity {

    public static final int INTENT_COMPLAIN = 9;

    public static Intent createIntent(int userId) {
        Intent intent = new Intent(App.getContext(), ComplainsActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_COMPLAIN);
        intent.putExtra(ComplainsFragment.USERID, userId);
        return intent;
    }

    public static Intent createIntent(int userId, String feedId) {
        Intent intent = createIntent(userId);
        intent.putExtra(ComplainsFragment.FEEDID, feedId);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return new ComplainsFragment();
    }
}
