package com.topface.topface.data;

/**
 * Base class for universal user representation
 */
public interface IUniversalUser {

    boolean isEmpty();

    boolean isBanned();

    boolean isDeleted();

    boolean isPhotoEmpty();

    Photo getPhoto();

    int getSex();

    String getNameAndAge();

    String getCity();

    int getId();

    Profile.Gifts getGifts();

    Photos getPhotos();

    int getPhotosCount();

    boolean isOnline();
}
