package com.topface.topface.data;

import org.json.JSONObject;

public class FeedPhotoBlogListData extends FeedListData<FeedPhotoBlog> {

    public FeedPhotoBlogListData(JSONObject data, Class itemClass) {
        super(data, itemClass);
    }

    protected void fillData(JSONObject data) {
        items = getList(data.optJSONArray("users"));
    }
}
