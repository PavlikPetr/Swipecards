package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.topface.utils.ad.NativeAd;

import org.json.JSONObject;

public class FeedPhotoBlog extends FeedItem implements Parcelable {

    public static final Creator<FeedPhotoBlog> CREATOR
            = new Creator<FeedPhotoBlog>() {
        public FeedPhotoBlog createFromParcel(Parcel in) {
            return new FeedPhotoBlog(in);
        }

        public FeedPhotoBlog[] newArray(int size) {
            return new FeedPhotoBlog[size];
        }
    };

    public FeedPhotoBlog() {
    }

    public FeedPhotoBlog(JSONObject data) {
        super(data);
    }

    protected FeedPhotoBlog(Parcel in) {
        super(in);
    }

    public FeedPhotoBlog(NativeAd nativeAd) {
        super(nativeAd);
    }

    @Override
    public void fillData(JSONObject item) {
        user = new FeedUser(item, this);
    }

}
