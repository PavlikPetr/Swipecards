package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.topface.topface.data.leftMenu.FragmentIdData;

/**
 * Counters data
 * Created by onikitin on 24.06.15.
 */
public class CountersData implements Parcelable {

    private int mBonus = 0;
    @SerializedName(value = "likes", alternate = "unread_likes")
    private int mLikes = 0;
    @SerializedName(value = "mutual", alternate = "unread_symphaties")
    private int mMutual = 0;
    @SerializedName(value = "dialogs", alternate = "unread_messages")
    private int mDialogs = 0;
    @SerializedName(value = "visitors", alternate = "unread_visitors")
    private int mVisitors = 0;
    @SerializedName(value = "fans", alternate = "unread_fans")
    private int mFans = 0;
    @SerializedName(value = "admirations", alternate = "unread_admirations")
    private int mAdmirations = 0;
    @SerializedName(value = "peopleNearby", alternate = "unread_people_nearby")
    private int mPeopleNearby = 0;

    public CountersData(CountersData countersData) {
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
        mLikes = likes;
    }

    public void setMutual(int mutual) {
        mMutual = mutual;
    }

    public void setDialogs(int dialogs) {
        mDialogs = dialogs;
    }

    public void setFans(int fans) {
        mFans = fans;
    }

    public void setAdmirations(int admirations) {
        mAdmirations = admirations;
    }

    public void setPeopleNearby(int peopleNearby) {
        mPeopleNearby = peopleNearby;
    }

    public void setVisitors(int visitors) {
        mVisitors = visitors;
    }

    public int getBonus() {
        return mBonus;
    }

    public int getLikes() {
        return mLikes;
    }

    public int getMutual() {
        return mMutual;
    }

    public int getDialogs() {
        return mDialogs;
    }

    public int getVisitors() {
        return mVisitors;
    }

    public int getFans() {
        return mFans;
    }

    public int getAdmirations() {
        return mAdmirations;
    }

    public int getPeopleNearby() {
        return mPeopleNearby;
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

    public int getCounterByFragmentId(@FragmentIdData.FragmentId int id) {
        switch (id) {
            case FragmentIdData.TABBED_DIALOGS:
                return getDialogs();
            case FragmentIdData.TABBED_VISITORS:
                return getVisitors() + getFans();
            case FragmentIdData.TABBED_LIKES:
                return getLikes() + getMutual() + getAdmirations();
            case FragmentIdData.GEO:
                return getPeopleNearby();
            case FragmentIdData.BONUS:
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
