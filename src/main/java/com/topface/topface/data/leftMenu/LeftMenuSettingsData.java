package com.topface.topface.data.leftMenu;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ppavlik on 06.05.16.
 * Main settings for left menu fragments
 */
public class LeftMenuSettingsData implements Parcelable {

    @FragmentIdData.FragmentId
    private int mFragmentId;
    private boolean mIsOverlayed;

    /**
     * create new left menu fragment settings
     *
     * @param fragmentId  unique fragment id
     * @param isOverlayed overlay flag
     */
    public LeftMenuSettingsData(@FragmentIdData.FragmentId int fragmentId, boolean isOverlayed) {
        mFragmentId = fragmentId;
        mIsOverlayed = isOverlayed;
    }

    /**
     * create new left menu fragment settings
     *
     * @param fragmentId unique fragment id
     */
    public LeftMenuSettingsData(@FragmentIdData.FragmentId int fragmentId) {
        this(fragmentId, false);
    }

    /**
     * get fragmnet id
     *
     * @return fragment id, unique key
     */
    @FragmentIdData.FragmentId
    public int getFragmentId() {
        return mFragmentId;
    }

    /**
     * get overlay flag
     *
     * @return overlayed if true
     */
    public boolean isOverlayed() {
        return mIsOverlayed;
    }

    public int getUniqueKey() {
        return mFragmentId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LeftMenuSettingsData)) return false;
        LeftMenuSettingsData data = (LeftMenuSettingsData) o;
        return mFragmentId == data.getFragmentId() && mIsOverlayed == data.isOverlayed();
    }

    @Override
    public int hashCode() {
        return 31 * mFragmentId + (mIsOverlayed ? 1 : 0);
    }

    protected LeftMenuSettingsData(Parcel in) {
        mFragmentId = FragmentIdData.getFragmentId(in.readInt());
        mIsOverlayed = in.readInt() != 0;
    }

    public static final Parcelable.Creator<LeftMenuSettingsData> CREATOR = new Parcelable.Creator<LeftMenuSettingsData>() {
        @Override
        public LeftMenuSettingsData createFromParcel(Parcel in) {
            return new LeftMenuSettingsData(in);
        }

        @Override
        public LeftMenuSettingsData[] newArray(int size) {
            return new LeftMenuSettingsData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mFragmentId);
        out.writeInt(mIsOverlayed ? 1 : 0);
    }
}
