package com.topface.topface.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.fragments.profile.AbstractProfileFragment;
import com.topface.topface.ui.fragments.profile.PhotoSwitcherActivity;

/**
 * Fragment with own photo updating photo data on adding/removing photos
 */
public abstract class OwnAvatarFragment extends AbstractProfileFragment {

    public static final String INCREMENT_AVATAR_POSITION = "incrementAvatarPosition";
    public static final String DECREMENT_AVATAR_POSITION = "decrementAvatarPosition";
    public static final String POSITION = "position";
    public static final String UPDATE_AVATAR_POSITION = "com.topface.topface.updateAvatarPosition";

    private Photo mAvatarVal;

    private BroadcastReceiver mAvatarPositionReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mAvatarVal != null) {
                boolean increment = intent.getBooleanExtra(INCREMENT_AVATAR_POSITION, false);
                int decrement = intent.getIntExtra(DECREMENT_AVATAR_POSITION, 0);
                if (increment) {
                    mAvatarVal.position += 1;
                    return;
                }
                Profile profile = getProfile();
                if (decrement > 0 && profile != null) {
                    mAvatarVal.position -= decrement;
                    profile.photosCount -= decrement;
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(UPDATE_AVATAR_POSITION);
        filter.addAction(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT);
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mAvatarPositionReciver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mAvatarPositionReciver);
    }

    @Override
    protected void setProfile(Profile profile) {
        super.setProfile(profile);
        mAvatarVal = profile.photo;
    }

    @Override
    public void onAvatarClick() {
        Profile profile = getProfile();
        Photos photos = profile.photos;
        if (mAvatarVal == null || photos == null) {
            return;
        }
        int pos;
        if (photos.size() <= mAvatarVal.position) {
            //ава за пределами загруженной пачки
            pos = mAvatarVal.position;
        } else {
            if (photos.get(mAvatarVal.position) == null) {
                return;
            }
            if (photos.get(mAvatarVal.position).getId() != mAvatarVal.getId()) {
                //ид не равны, юзер загрузил новые фотки
                int id = mAvatarVal.getId();
                pos = photos.getPhotoIndexById(id);
            } else {
                pos = mAvatarVal.position;
            }
        }
        startActivity(PhotoSwitcherActivity.
                getPhotoSwitcherIntent(profile.gifts, pos,
                        profile.uid, profile.photosCount,
                        profile.photos));
    }
}
