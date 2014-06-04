package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.requests.ApiResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class Gift extends AbstractDataWithPhotos implements Parcelable {

    public static final int ROMANTIC = 0;
    public static final int FRIENDS = 2;
    public static final int PRESENT = 1;

    public static final int PROFILE = -1;
    public static final int PROFILE_NEW = -2;
    public static final int SEND_BTN = -3;

    public int id;
    public int type;
    public String link;
    public int price;
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

    public static LinkedList<Gift> parse(ApiResponse response) {
        LinkedList<Gift> gifts = new LinkedList<Gift>();

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
        LinkedList<Gift> gifts = new LinkedList<Gift>();
        gifts.add(
                new Gift(0, Gift.SEND_BTN, null, 0)
        );
        gifts.addAll(giftsList);
        return gifts;
    }

    public static int getTypeNameResId(int type) {
        switch (type) {
            case ROMANTIC:
                return R.string.gifts_romantic;
            case FRIENDS:
                return R.string.gifts_friends;
            case PRESENT:
                return R.string.gifts_present;
            default:
                return R.string.gifts_romantic;
        }
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
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Gift createFromParcel(Parcel in) {
            return new Gift(
                    in.readInt(),
                    in.readInt(),
                    in.readString(),
                    in.readInt()
            );
        }

        @Override
        public Gift[] newArray(int size) {
            return new Gift[size];
        }
    };
}
