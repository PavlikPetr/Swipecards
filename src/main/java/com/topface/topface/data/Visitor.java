package com.topface.topface.data;

import org.json.JSONObject;

public class Visitor extends FeedItem {

    public Visitor(JSONObject data) {
        super(data);
    }

    @Override
    public void fillData(JSONObject item) {
        super.fillData(item);
        createdRelative = getRelativeCreatedDate(created);
    }
}
