package com.topface.topface.data;

import com.topface.topface.R;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class Gift extends AbstractDataWithPhotos {

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

    public static LinkedList<Gift> parse(ApiResponse response) {
        LinkedList<Gift> gifts = new LinkedList<Gift>();

        try {
            JSONArray array = response.jsonResult.getJSONArray("gifts");
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                Gift gift = new Gift();
                gift.id = item.optInt("id");
                gift.type = item.optInt("type");
                gift.link = item.optString("link");
                gift.price = item.optInt("price");

                gifts.add(gift);
            }
        } catch (JSONException e) {
            Debug.log("Gift.class", "Wrong response parsing: " + e);
        }

        return gifts;
    }

    // Gets User gifts
    public static LinkedList<Gift> parse(LinkedList<Gift> giftsList) {
        LinkedList<Gift> gifts = new LinkedList<Gift>();
        gifts.add(Gift.getSendedGiftItem());
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

    public static Gift getSendedGiftItem() {
        Gift result = new Gift();
        result.type = Gift.SEND_BTN;
        return result;
    }

}
