package com.topface.topface.data;

import java.util.ArrayList;

/**
 * Universal user from FeedUser
 */
public class UniversalFeedUserWrapper implements IUniversalUser {

    private FeedUser mFeedUser;

    UniversalFeedUserWrapper(FeedUser feedUser) {
        mFeedUser = feedUser;
    }

    @Override
    public boolean isEmpty() {
        return mFeedUser == null || mFeedUser.isEmpty();
    }

    @Override
    public boolean isBanned() {
        return mFeedUser.banned;
    }

    @Override
    public boolean isDeleted() {
        return mFeedUser.deleted;
    }

    @Override
    public boolean isPhotoEmpty() {
        return mFeedUser == null || mFeedUser.photo == null || mFeedUser.photo.isEmpty();
    }

    @Override
    public Photo getPhoto() {
        return mFeedUser == null ? null : mFeedUser.photo;
    }

    @Override
    public int getSex() {
        return mFeedUser == null ? Profile.BOY : mFeedUser.sex;
    }

    @Override
    public String getNameAndAge() {
        return mFeedUser == null ? "" : mFeedUser.getNameAndAge();
    }

    @Override
    public String getCity() {
        if (mFeedUser != null) {
            City city = mFeedUser.city;
            if (city != null) {
                return city.name;
            }
        }
        return "";
    }

    @Override
    public int getId() {
        return mFeedUser == null ? 0 : mFeedUser.id;
    }

    @Override
    public ArrayList<Gift> getGifts() {
        return null;
    }

    @Override
    public Photos getPhotos() {
        return mFeedUser == null ? null : mFeedUser.photos;
    }

    @Override
    public int getPhotosCount() {
        return mFeedUser == null ? 0 : mFeedUser.photosCount;
    }

    @Override
    public boolean isOnline() {
        return mFeedUser != null && mFeedUser.online;
    }
}
