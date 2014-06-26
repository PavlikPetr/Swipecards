package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class FeedGift extends FeedItem implements Parcelable {

    public Gift gift;

    public static final Parcelable.Creator<FeedGift> CREATOR
            = new Parcelable.Creator<FeedGift>() {
        public FeedGift createFromParcel(Parcel in) {
            return new FeedGift(in);
        }

        public FeedGift[] newArray(int size) {
            return new FeedGift[size];
        }
    };

    public FeedGift(ItemType type) {
        super(type);
    }

    public FeedGift() {
        super((JSONObject) null);
    }

    @SuppressWarnings("UnusedDeclaration")
    public FeedGift(JSONObject data) {
        super(data);
    }

    public FeedGift(Parcel in) {
        super(in);
        gift = in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public void fillData(JSONObject item) {
        super.fillData(item);
        gift = new Gift(
                item.optInt("gift"),
                item.optInt("id"),
                Gift.PROFILE,
                item.optString("link")
        );
    }

    public static FeedGift getSendedGiftItem() {
        FeedGift result = new FeedGift();
        result.gift = new Gift(0, Gift.SEND_BTN, null, 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(gift, 0);
    }

}

