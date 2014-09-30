package com.topface.topface.utils;

import android.support.v7.app.ActionBar;

import com.topface.topface.R;

/**
 * Updates actionbar logo to show notification.
 */
public class IconNotificationController implements IActionbarNotifier {

    private ActionBar mActionBar;

    public IconNotificationController(ActionBar actionBar) {
        mActionBar = actionBar;
    }

    @Override
    public void refreshNotificator() {
        if (mActionBar != null) {
            if (CacheProfile.unread_messages > 0 || CacheProfile.unread_mutual > 0) {
                mActionBar.setLogo(R.drawable.ic_home_notification);
            } else {
                mActionBar.setLogo(R.drawable.ic_home);
            }
        }
    }


}
