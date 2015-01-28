package com.topface.topface.utils.ad.pubnative;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Holds url to let pubnative know that we showed their ad.
 */
public class Beacon implements Parcelable {

    public static final Creator<Beacon> CREATOR = new Creator<Beacon>() {
        @Override
        public Beacon createFromParcel(Parcel source) {
            return new Beacon(source);
        }

        @Override
        public Beacon[] newArray(int size) {
            return new Beacon[size];
        }
    };

    public static final String IMPRESSION = "impression";

    @SerializedName("type")
    private String mType;
    @SerializedName("url")
    private String mUrl;

    @SuppressWarnings("unused")
    public Beacon() {
    }

    protected Beacon(Parcel in) {
        mType = in.readString();
        mUrl = in.readString();
    }

    public String getType() {
        return mType;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mType);
        dest.writeString(mUrl);
    }
}
