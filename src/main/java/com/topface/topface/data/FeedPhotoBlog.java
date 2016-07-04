package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.framework.JsonUtils;

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

    //Нужен для рефлекшена в FeedListData метод getList()
    @SuppressWarnings("unused")
    public FeedPhotoBlog(JSONObject data) {
        super(data);
    }

    protected FeedPhotoBlog(Parcel in) {
        super(in);
    }

    @Override
    public void fillData(JSONObject item) {
        this.user = JsonUtils.fromJson(item.toString(), FeedUser.class);
    }

}
