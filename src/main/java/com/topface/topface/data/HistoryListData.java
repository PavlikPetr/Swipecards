package com.topface.topface.data;

import org.json.JSONObject;

public class HistoryListData extends FeedListData<History> {

    public FeedUser user;

    public HistoryListData(JSONObject data, Class<History> itemClass) {
        super(data, itemClass);
    }

    @Override
    protected void fillData(JSONObject data) {
        super.fillData(data);
        this.user = new FeedUser(data.optJSONObject("user"));
    }
}
