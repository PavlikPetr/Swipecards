package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class BlackListItem extends FeedItem implements Parcelable {

    public static final Parcelable.Creator<BlackListItem> CREATOR
            = new Parcelable.Creator<BlackListItem>() {
        public BlackListItem createFromParcel(Parcel in) {
            return new BlackListItem(in);
        }

        public BlackListItem[] newArray(int size) {
            return new BlackListItem[size];
        }
    };

    public BlackListItem(JSONObject data) {
        super(data);
    }

    protected BlackListItem(Parcel in) {
        super(in);
    }

    public void fillData(JSONObject item) {
        //В черном списке нас интересует только юзер, все остальные поля не нужны
        this.user = new FeedUser(item.optJSONObject("user"));
        this.id = Integer.toString(item.optInt("id"));
    }
}
