package com.topface.topface.data;

import java.util.ArrayList;

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

    ArrayList<Gift> getGifts();

    Photos getPhotos();

    int getPhotosCount();

    boolean isOnline();
}
