package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.topface.utils.ad.NativeAd;

import org.json.JSONObject;

public class FeedBookmark extends FeedItem implements Parcelable {

    public static final Parcelable.Creator<FeedBookmark> CREATOR
            = new Parcelable.Creator<FeedBookmark>() {
        public FeedBookmark createFromParcel(Parcel in) {
            return new FeedBookmark(in);
        }

        public FeedBookmark[] newArray(int size) {
            return new FeedBookmark[size];
        }
    };

    public FeedBookmark(JSONObject data) {
        super(data);
    }

    protected FeedBookmark(Parcel in) {
        super(in);
    }

    public FeedBookmark(NativeAd nativeAd) {
        super(nativeAd);
    }
}
