package com.topface.topface.data;

import org.json.JSONObject;

public class FeedGift extends FeedItem {

    public Gift gift;

    public FeedGift(ItemType type) {
        super(type);
    }

    public FeedGift(JSONObject data) {
        super(data);
    }

    public FeedGift() {
        super((JSONObject) null);
    }

    @Override
    public void fillData(JSONObject item) {
        super.fillData(item);
        gift = new Gift();
        gift.id = item.optInt("gift");
        gift.link = item.optString("link");
        gift.type = Gift.PROFILE;
        gift.feedId = item.optInt("id");
    }

    public static FeedGift getSendedGiftItem() {
        FeedGift result = new FeedGift();
        result.gift = new Gift();
        result.gift.type = Gift.SEND_BTN;
        return result;
    }

}

