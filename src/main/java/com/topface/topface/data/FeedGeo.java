package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class FeedGeo extends FeedLike implements Parcelable {

    public double distance;

    public static final Parcelable.Creator<FeedGeo> CREATOR
            = new Parcelable.Creator<FeedGeo>() {
        public FeedGeo createFromParcel(Parcel in) {
            return new FeedGeo(in);
        }

        public FeedGeo[] newArray(int size) {
            return new FeedGeo[size];
        }
    };

    public FeedGeo(JSONObject data) {
        super(data);
    }

    protected FeedGeo(Parcel in) {
        super(in);
        distance = in.readDouble();
    }

    @Override
    public void fillData(JSONObject item) {
        super.fillData(item);
        distance = item.optDouble("distance");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(distance);
    }
}
