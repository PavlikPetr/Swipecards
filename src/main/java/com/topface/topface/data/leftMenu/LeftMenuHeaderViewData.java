package com.topface.topface.data.leftMenu;

import com.topface.framework.imageloader.IPhoto;

/**
 * Created by ppavlik on 10.05.16.
 * Data object for left menu header
 */
public class LeftMenuHeaderViewData {

    private IPhoto mPhoto;
    private String mName;
    private String mAge;
    private String mCity;

    /**
     * Create new data object for left menu header
     *
     * @param photo interface for ImageLoader interaction with photos
     * @param name  users name
     * @param city  users city
     */
    public LeftMenuHeaderViewData(IPhoto photo, String name, String age, String city) {
        mPhoto = photo;
        mName = name;
        mAge = age;
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
     * Get users age
     *
     * @return users age
     */
    public String getAge() {
        return mAge;
    }

    /**
     * Set users age
     *
     * @param age users age
     */
    public void setAge(String age) {
        mAge = age;
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

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LeftMenuHeaderViewData)) return false;
        LeftMenuHeaderViewData data = (LeftMenuHeaderViewData) o;
        if (mPhoto == null || mPhoto.equals(data.getPhoto())) return false;
        if (mName == null || mName.equals(data.getName())) return false;
        return mCity == null || mCity.equals(data.getCity());
    }

    @Override
    public int hashCode() {
        int res = mPhoto != null ? mPhoto.hashCode() : 0;
        res = (res * 31) + (mName != null ? mName.hashCode() : 0);
        return (res * 31) + (mCity != null ? mCity.hashCode() : 0);
    }
}
