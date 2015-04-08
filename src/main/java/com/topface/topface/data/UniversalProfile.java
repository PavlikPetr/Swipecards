package com.topface.topface.data;

import com.topface.topface.Static;

/**
 * Uneversal user from profile
 */
public class UniversalProfile implements IUniversalUser {

    private Profile mProfile;

    UniversalProfile(Profile profile) {
        mProfile = profile;
    }

    @Override
    public boolean isEmpty() {
        return mProfile == null || mProfile.isEmpty();
    }

    @Override
    public boolean isBanned() {
        return false;
    }

    @Override
    public boolean isDeleted() {
        return false;
    }

    @Override
    public boolean isPhotoEmpty() {
        return mProfile == null || mProfile.photo == null || mProfile.photo.isEmpty();
    }

    @Override
    public Photo getPhoto() {
        return mProfile == null ? null : mProfile.photo;
    }

    @Override
    public int getSex() {
        return mProfile == null ? Static.BOY : mProfile.sex;
    }

    @Override
    public String getNameAndAge() {
        return mProfile.getNameAndAge();
    }

    @Override
    public String getCity() {
        return mProfile.city.name;
    }

    @Override
    public int getId() {
        return mProfile.uid;
    }

    @Override
    public Profile.Gifts getGifts() {
        return mProfile.gifts;
    }

    @Override
    public Photos getPhotos() {
        return mProfile.photos;
    }

    @Override
    public int getPhotosCount() {
        return mProfile.photosCount;
    }

    @Override
    public boolean isOnline() {
        return mProfile != null;
    }
}
