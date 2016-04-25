package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Counters data
 * Created by onikitin on 24.06.15.
 */
public class CountersData implements Parcelable {

    private int mBonus = 0;
    private boolean mFromGcm = false;

    /*
     * Все это блистательное великолепие ниже, нужно из-за того, что сервер нам присылает один и
     * тот же объект счетчиков в разных JSON контейнерах, для GCM и реквестов. Такие дела.
     */

    //В запросах
    @SerializedName("likes")
    private int mLikes = 0;
    @SerializedName("mutual")
    private int mMutual = 0;
    @SerializedName("dialogs")
    private int mDialogs = 0;
    @SerializedName("visitors")
    private int mVisitors = 0;
    @SerializedName("fans")
    private int mFans = 0;
    @SerializedName("admirations")
    private int mAdmirations = 0;
    @SerializedName("peopleNearby")
    private int mPeopleNearby = 0;

    //В GCM
    @SerializedName("unread_likes")
    private int mLikesGcm = 0;
    @SerializedName("unread_symphaties")
    private int mMutualGcm = 0;
    @SerializedName("unread_messages")
    private int mDialogsGcm = 0;
    @SerializedName("unread_visitors")
    private int mVisitorsGcm = 0;
    @SerializedName("unread_fans")
    private int mFansGcm = 0;
    @SerializedName("unread_admirations")
    private int mAdmirationsGcm = 0;
    @SerializedName("unread_people_nearby")
    private int mPeopleNearbyGcm = 0;

    public CountersData(CountersData countersData) {
        mFromGcm = countersData.isFromGcm();
        setLikes(countersData.getLikes());
        setMutual(countersData.getMutual());
        setDialogs(countersData.getDialogs());
        setVisitors(countersData.getVisitors());
        setFans(countersData.getFans());
        setAdmirations(countersData.getAdmirations());
        setPeopleNearby(countersData.getPeopleNearby());
    }

    public CountersData() {
    }

    public CountersData(boolean fromGcm) {
        mFromGcm = fromGcm;
    }

    protected CountersData(Parcel in) {
        setLikes(in.readInt());
        setMutual(in.readInt());
        setDialogs(in.readInt());
        setVisitors(in.readInt());
        setFans(in.readInt());
        setAdmirations(in.readInt());
        setPeopleNearby(in.readInt());
        setBonus(in.readInt());
    }

    public void setBonus(int bonus) {
        mBonus = bonus;
    }

    public void setLikes(int likes) {
        if (mFromGcm) {
            mLikesGcm = likes;
        } else {
            mLikes = likes;
        }
    }

    public void setMutual(int mutual) {
        if (mFromGcm) {
            mMutualGcm = mutual;
        } else {
            mMutual = mutual;
        }
    }

    public void setDialogs(int dialogs) {
        if (mFromGcm) {
            mDialogsGcm = dialogs;
        } else {
            mDialogs = dialogs;
        }
    }

    public void setFans(int fans) {
        if (mFromGcm) {
            mFansGcm = fans;
        } else {
            mFans = fans;
        }
    }

    public void setAdmirations(int admirations) {
        if (mFromGcm) {
            mAdmirationsGcm = admirations;
        } else {
            mAdmirations = admirations;
        }
    }

    public void setPeopleNearby(int peopleNearby) {
        if (mFromGcm) {
            mPeopleNearbyGcm = peopleNearby;
        } else {
            mPeopleNearby = peopleNearby;
        }
    }

    public void setVisitors(int visitors) {
        if (mFromGcm) {
            mVisitorsGcm = visitors;
        } else {
            mVisitors = visitors;
        }
    }

    public int getBonus() {
        return mBonus;
    }

    public int getLikes() {
        return mFromGcm ? mLikesGcm : mLikes;
    }

    public int getMutual() {
        return mFromGcm ? mMutualGcm : mMutual;
    }

    public int getDialogs() {
        return mFromGcm ? mDialogsGcm : mDialogs;
    }

    public int getVisitors() {
        return mFromGcm ? mVisitorsGcm : mVisitors;
    }

    public int getFans() {
        return mFromGcm ? mFansGcm : mFans;
    }

    public int getAdmirations() {
        return mFromGcm ? mAdmirationsGcm : mAdmirations;
    }

    public int getPeopleNearby() {
        return mFromGcm ? mPeopleNearbyGcm : mPeopleNearby;
    }

    public boolean isFromGcm() {
        return mFromGcm;
    }

    public static final Creator<CountersData> CREATOR = new Creator<CountersData>() {
        @Override
        public CountersData createFromParcel(Parcel in) {
            return new CountersData(in);
        }

        @Override
        public CountersData[] newArray(int size) {
            return new CountersData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getLikes());
        dest.writeInt(getMutual());
        dest.writeInt(getDialogs());
        dest.writeInt(getVisitors());
        dest.writeInt(getFans());
        dest.writeInt(getAdmirations());
        dest.writeInt(getPeopleNearby());
        dest.writeInt(getBonus());
    }

    public int getCounterByFragmentId(FragmentSettings id) {
        switch (id.getFragmentId()) {
            case TABBED_DIALOGS:
                return getDialogs();
            case TABBED_VISITORS:
                return getVisitors() + getFans();
            case TABBED_LIKES:
                return getLikes() + getMutual() + getAdmirations();
            case GEO:
                return getPeopleNearby();
            case BONUS:
                return getBonus();
        }
        return -1;
    }

    @SuppressWarnings("unused")
    public boolean isNotEmpty() {
        return getLikes() != 0 || getMutual() != 0 || getDialogs() != 0 || getVisitors() != 0
                || getFans() != 0 || getAdmirations() != 0 || getPeopleNearby() != 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CountersData)) {
            return false;
        }
        CountersData data = (CountersData) obj;
        return data.getLikes() == getLikes() &&
                data.getMutual() == getMutual() &&
                data.getDialogs() == getDialogs() &&
                data.getVisitors() == getVisitors() &&
                data.getFans() == getFans() &&
                data.getAdmirations() == getAdmirations() &&
                data.getPeopleNearby() == getPeopleNearby();
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + getLikes();
        hash = hash * 31 + getMutual();
        hash = hash * 31 + getDialogs();
        hash = hash * 31 + getVisitors();
        hash = hash * 31 + getFans();
        hash = hash * 31 + getAdmirations();
        hash = hash * 31 + getPeopleNearby();
        return hash;
    }
}
