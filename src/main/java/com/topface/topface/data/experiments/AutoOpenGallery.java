package com.topface.topface.data.experiments;

import android.content.Context;
import android.content.Intent;

import com.topface.topface.data.Photo;
import com.topface.topface.ui.UserProfileActivity;
import com.topface.topface.ui.fragments.profile.PhotoSwitcherActivity;

/**
 * Experiment about autoopening gallery from click on friend avatar
 */
public class AutoOpenGallery extends BaseExperiment {
    @Override
    protected String getOptionsKey() {
        return "autoOpenGallery";
    }

    public Intent createIntent(int userId, int photosCount, Photo preloadPhoto, Context context) {
        if (isEnabled() && photosCount > 0) {
            return PhotoSwitcherActivity.getPhotoSwitcherIntent(userId, preloadPhoto, context);
        } else {
            return UserProfileActivity.createIntent(userId, context);
        }
    }

    public Intent createIntent(int userId, int photosCount, String itemId, Photo preloadPhoto, Context context) {
        if (isEnabled() && photosCount > 0) {
            return PhotoSwitcherActivity.getPhotoSwitcherIntent(itemId, userId, preloadPhoto, context);
        } else {
            return UserProfileActivity.createIntent(userId, itemId, context);
        }
    }

    public Intent createIntent(int userId, int photosCount, Class callingClass, Photo preloadPhoto, Context context) {
        if (isEnabled() && photosCount > 0) {
            return PhotoSwitcherActivity.getPhotoSwitcherIntent(userId, callingClass, preloadPhoto, context);
        } else {
            return UserProfileActivity.createIntent(userId, callingClass, context);
        }
    }

}
