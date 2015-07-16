package com.topface.topface.utils;

import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ImageView;

import com.topface.topface.R;

/**
 * Updates image inside actionbar's custom view to show notification.
 */
public class CustomViewNotificationController implements IActionbarNotifier {

    private ImageView mIcon;

    public CustomViewNotificationController(ActionBar actionBar) {
        if (actionBar != null) {
            View customView = actionBar.getCustomView();
            if (customView != null) {
                View icon = customView.findViewById(R.id.up_icon);
                if (icon instanceof ImageView) {
                    mIcon = (ImageView) icon;
                }
            }
        }
    }

    @Override
    public void refreshNotificator(int unreadMessages, int unreadMutual) {
        if (mIcon != null) {
            if (unreadMessages > 0 || unreadMutual > 0) {
                mIcon.setImageResource(R.drawable.ic_home_notification);
            } else {
                mIcon.setImageResource(R.drawable.ic_home);
            }
        }
    }
}
