package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.topface.framework.utils.Debug;
import com.topface.topface.requests.ApiResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class Gift extends AbstractDataWithPhotos implements Parcelable {

    public static final int PROFILE = -1;
    public static final int PROFILE_NEW = -2;
    public static final int SEND_BTN = -3;

    @SerializedName("gift")
    public int id;
    public int type;
    public String link;
    public int price;
    @SerializedName("id")
    public int feedId;

    public Gift(int id, int type, String link, int price) {
        super();
        this.id = id;
        this.type = type;
        this.link = link;
        this.price = price;
    }

    public Gift(int id, int feedId, int type, String link) {
        super();
        this.id = id;
        this.type = type;
        this.link = link;
        this.feedId = feedId;
    }

    @SuppressWarnings("deprecation")
    public static LinkedList<Gift> parse(ApiResponse response) {
        LinkedList<Gift> gifts = new LinkedList<>();

        try {
            JSONArray array = response.jsonResult.getJSONArray("gifts");
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);

                Gift gift = new Gift(
                        item.optInt("id"),
                        item.optInt("type"),
                        item.optString("link"),
                        item.optInt("price")
                );

                gifts.add(gift);
            }
        } catch (JSONException e) {
            Debug.error("Gift.class: Wrong response parsing", e);
        }

        return gifts;
    }

    // Gets User gifts
    public static LinkedList<Gift> parse(LinkedList<Gift> giftsList) {
        LinkedList<Gift> gifts = new LinkedList<>();
        gifts.add(
                new Gift(0, Gift.SEND_BTN, null, 0)
        );
        gifts.addAll(giftsList);
        return gifts;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(type);
        dest.writeString(link);
        dest.writeInt(price);
        dest.writeInt(feedId);
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator<Gift> CREATOR = new Parcelable.Creator<Gift>() {
        public Gift createFromParcel(Parcel in) {
            Gift gift = new Gift(
                    in.readInt(),
                    in.readInt(),
                    in.readString(),
                    in.readInt()
            );
            gift.feedId = in.readInt();
            return gift;
        }

        @Override
        public Gift[] newArray(int size) {
            return new Gift[size];
        }
    };
}
