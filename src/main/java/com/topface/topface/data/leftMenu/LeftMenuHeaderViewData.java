package com.topface.topface.data.leftMenu;

import com.topface.framework.imageloader.IPhoto;

/**
 * Created by ppavlik on 10.05.16.
 * Data object for left menu header
 */
public class LeftMenuHeaderViewData {

    private IPhoto mPhoto;
    private String mName;
    private String mCity;

    /**
     * Create new data object for left menu header
     *
     * @param photo         interface for ImageLoader interaction with photos
     * @param name          users name
     * @param city          users city
     */
    public LeftMenuHeaderViewData(IPhoto photo, String name, String city) {
        mPhoto = photo;
        mName = name;
        mCity = city;
    }

    /**
     * Get users IPhoto interface
     *
     * @return interface for ImageLoader interaction with photos
     */
    public IPhoto getPhoto() {
        return mPhoto;
    }

    /**
     * Set users IPhoto interface
     *
     * @param photo interface for ImageLoader interaction with photos
     */
    public void setPhoto(IPhoto photo) {
        mPhoto = photo;
    }

    /**
     * Get users name
     *
     * @return users name
     */
    public String getName() {
        return mName;
    }

    /**
     * Set users name
     *
     * @param name users name
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Get users city name
     *
     * @return users city
     */
    public String getCity() {
        return mCity;
    }

    /**
     * Set users city name
     *
     * @param city users city
     */
    public void setCity(String city) {
        mCity = city;
    }
}
