package com.topface.topface.data;

import org.json.JSONObject;

public class FeedLike extends FeedItem {
    public boolean highrate;

    public FeedLike(JSONObject data) {
        super(data);
    }

    @Override
    public void fillData(JSONObject item) {
        super.fillData(item);
        highrate = item.optBoolean("highrate");
    }

}
