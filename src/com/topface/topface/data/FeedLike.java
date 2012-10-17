package com.topface.topface.data;

import org.json.JSONObject;

public class FeedLike extends FeedItem {
    public boolean highrate;

    public FeedLike(JSONObject data) {
        super(data);
    }

    public FeedLike(ItemType type) {
        super(type);
    }

    @Override
    public void fillData(JSONObject item) {
        super.fillData(item);
        highrate = item.optBoolean("highrate");
    }
}
