package com.topface.topface.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.topface.ui.fragments.profile.AbstractProfileFragment;
import com.topface.topface.ui.fragments.profile.PhotoSwitcherActivity;
import com.topface.topface.utils.CacheProfile;

/**
 * Fragment with own photo updating photo data on adding/removing photos
 */
public abstract class OwnAvatarFragment extends AbstractProfileFragment {

    public static final String UPDATE_AVATAR_POSITION = "com.topface.topface.updateAvatarPosition";

    private BroadcastReceiver mAvatarPositionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateOwnProfile();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        updateOwnProfile();
        IntentFilter filter = new IntentFilter(UPDATE_AVATAR_POSITION);
        filter.addAction(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT);
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mAvatarPositionReceiver, filter);
    }

    private void updateOwnProfile() {
        setProfile(CacheProfile.getProfile());
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mAvatarPositionReceiver);
    }
}
