package com.topface.topface.data;

import com.topface.topface.Static;

import java.util.ArrayList;

/**
 * Uneversal user from profile
 */
public class UniversalProfileWrapper implements IUniversalUser {

    private Profile mProfile;

    UniversalProfileWrapper(Profile profile) {
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
    public ArrayList<Gift> getGifts() {
        if (mProfile != null) {
            return mProfile.gifts.getGifts();
        } else {
            return new ArrayList<>();
        }
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
