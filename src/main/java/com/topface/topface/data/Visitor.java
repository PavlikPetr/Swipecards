package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.topface.utils.ad.NativeAd;

import org.json.JSONObject;

public class Visitor extends FeedItem implements Parcelable {

    public static final Parcelable.Creator<Visitor> CREATOR
            = new Parcelable.Creator<Visitor>() {
        public Visitor createFromParcel(Parcel in) {
            return new Visitor(in);
        }

        public Visitor[] newArray(int size) {
            return new Visitor[size];
        }
    };

    public Visitor(JSONObject data) {
        super(data);
    }

    protected Visitor(Parcel in) {
        super(in);
    }

    public Visitor(NativeAd nativeAd) {
        super(nativeAd);
    }

    @Override
    public void fillData(JSONObject item) {
        super.fillData(item);
        createdRelative = getRelativeCreatedDate(created);
    }
}
