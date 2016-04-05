package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.topface.utils.ad.NativeAd;

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

    public FeedGeo() {
    }

    @SuppressWarnings("UnusedDeclaration")
    public FeedGeo(JSONObject data) {
        super(data);
    }

    protected FeedGeo(Parcel in) {
        super(in);
        distance = in.readDouble();
    }

    public FeedGeo(NativeAd nativeAd) {
        super(nativeAd);
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FeedGeo)) return false;
        if (!super.equals(o)) return false;
        FeedGeo feedGeo = (FeedGeo) o;
        return Double.compare(feedGeo.distance, distance) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp = Double.doubleToLongBits(distance);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
