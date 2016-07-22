package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONObject;

public class FeedLike extends FeedItem implements Parcelable {
    public boolean highrate;
    public boolean mutualed;

    public static final Parcelable.Creator<FeedLike> CREATOR
            = new Parcelable.Creator<FeedLike>() {
        public FeedLike createFromParcel(Parcel in) {
            return new FeedLike(in);
        }

        public FeedLike[] newArray(int size) {
            return new FeedLike[size];
        }
    };

    public FeedLike() {
    }

    public FeedLike(JSONObject data) {
        super(data);
    }

    protected FeedLike(Parcel in) {
        super(in);
        highrate = in.readByte() == 1;
        mutualed = in.readByte() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (highrate ? 1 : 0));
        dest.writeByte((byte) (mutualed ? 1 : 0));
    }

    @Override
    public void fillData(JSONObject item) {
        super.fillData(item);
        highrate = item.optBoolean("highrate");
        mutualed = false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FeedLike)) return false;
        if (!super.equals(o)) return false;
        FeedLike feedLike = (FeedLike) o;
        return highrate == feedLike.highrate && mutualed == feedLike.mutualed;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (highrate ? 1 : 0);
        result = 31 * result + (mutualed ? 1 : 0);
        return result;
    }
}
