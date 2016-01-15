package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Counters data
 * Created by onikitin on 24.06.15.
 */
public class CountersData implements Parcelable {

    public int likes = 0;
    public int mutual = 0;
    public int dialogs = 0;
    public int visitors = 0;
    public int fans = 0;
    public int admirations = 0;
    public int peopleNearby = 0;
    public int bonus = 0;

    public CountersData(CountersData countersData) {
        this.likes = countersData.likes;
        this.mutual = countersData.mutual;
        this.dialogs = countersData.dialogs;
        this.visitors = countersData.visitors;
        this.fans = countersData.fans;
        this.admirations = countersData.admirations;
        this.peopleNearby = countersData.peopleNearby;
    }

    public CountersData() {
    }

    protected CountersData(Parcel in) {
        likes = in.readInt();
        mutual = in.readInt();
        dialogs = in.readInt();
        visitors = in.readInt();
        fans = in.readInt();
        admirations = in.readInt();
        peopleNearby = in.readInt();
        bonus = in.readInt();
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
        dest.writeInt(likes);
        dest.writeInt(mutual);
        dest.writeInt(dialogs);
        dest.writeInt(visitors);
        dest.writeInt(fans);
        dest.writeInt(admirations);
        dest.writeInt(peopleNearby);
        dest.writeInt(bonus);
    }

    public int getCounterByFragmentId(FragmentSettings id) {
        switch (id.getFragmentId()) {
            case TABBED_DIALOGS:
                return dialogs;
            case TABBED_VISITORS:
                return visitors + fans;
            case TABBED_LIKES:
                return likes + mutual + admirations;
            case GEO:
                return peopleNearby;
            case BONUS:
                return bonus;
        }
        return -1;
    }

    @SuppressWarnings("unused")
    public boolean isNotEmpty() {
        return likes != 0 || mutual != 0 || dialogs != 0 || visitors != 0
                || fans != 0 || admirations != 0 || peopleNearby != 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        CountersData data = (CountersData) obj;
        return data.likes == likes &&
                data.mutual == mutual &&
                data.dialogs == dialogs &&
                data.visitors == visitors &&
                data.fans == fans &&
                data.admirations == admirations &&
                data.peopleNearby == peopleNearby;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + likes;
        hash = hash * 31 + mutual;
        hash = hash * 31 + dialogs;
        hash = hash * 31 + visitors;
        hash = hash * 31 + fans;
        hash = hash * 31 + admirations;
        hash = hash * 31 + peopleNearby;
        return hash;
    }
}
