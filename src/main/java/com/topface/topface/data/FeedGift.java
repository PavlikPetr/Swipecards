package com.topface.topface.data;

import org.json.JSONObject;

public class FeedGift extends FeedItem {

    public Gift gift;

    public FeedGift(ItemType type) {
        super(type);
    }

    public FeedGift() {
        super((JSONObject) null);
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

}

