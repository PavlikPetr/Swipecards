package com.topface.topface.data.experiments;

import android.content.Context;
import android.content.Intent;

import com.topface.topface.ui.UserProfileActivity;
import com.topface.topface.ui.fragments.profile.PhotoSwitcherActivity;

/**
 * Experiment about autoopening gallery from click on friend avatar
 */
public class AutoOpenGallery {
    private boolean mEnabled = false;
    private String mGroup;

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    public String getGroup() {
        return mGroup;
    }

    public void setGroup(String group) {
        this.mGroup = group;
    }

    public Intent createIntent(int userId, int photosCount, Context context) {
        if (isEnabled() && photosCount > 0) {
            return PhotoSwitcherActivity.getPhotoSwitcherIntent(userId, context);
        } else {
            return UserProfileActivity.createIntent(userId, context);
        }
    }

    public Intent createIntent(int userId, int photosCount, String itemId, Context context) {
        if (isEnabled() && photosCount > 0) {
            return PhotoSwitcherActivity.getPhotoSwitcherIntent(userId, context);
        } else {
            return UserProfileActivity.createIntent(userId, itemId, context);
        }
    }

    public Intent createIntent(int userId, int photosCount, Class callingClass, Context context) {
        if (isEnabled() && photosCount > 0) {
            return PhotoSwitcherActivity.getPhotoSwitcherIntent(userId, callingClass, context);
        } else {
            return UserProfileActivity.createIntent(userId, callingClass, context);
        }
    }

}
