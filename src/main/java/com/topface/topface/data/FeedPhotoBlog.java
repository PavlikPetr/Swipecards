package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.framework.JsonUtils;

import org.json.JSONObject;

public class FeedPhotoBlog extends FeedItem implements Parcelable {

    public boolean mutualed;

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

    //Нужен для рефлекшена в FeedListData метод getList()
    @SuppressWarnings("unused")
    public FeedPhotoBlog(JSONObject data) {
        super(data);
    }

    protected FeedPhotoBlog(Parcel in) {
        super(in);
        mutualed = in.readByte() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (mutualed ? 1 : 0));
    }

    @Override
    public void fillData(JSONObject item) {
        this.user = JsonUtils.fromJson(item.toString(), FeedUser.class);
        mutualed = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FeedPhotoBlog)) return false;
        if (!super.equals(o)) return false;
        FeedPhotoBlog that = (FeedPhotoBlog) o;
        return mutualed == that.mutualed;

    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + (mutualed ? 1 : 0);
    }
}
