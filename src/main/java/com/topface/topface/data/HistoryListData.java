package com.topface.topface.data;

import com.topface.framework.JsonUtils;

import org.json.JSONObject;

public class HistoryListData extends FeedListData<History> {

    public FeedUser user;

    public HistoryListData(JSONObject data, Class<History> itemClass) {
        super(data, itemClass);
    }

    @Override
    protected void fillData(JSONObject data) {
        this.user = JsonUtils.fromJson(data.optJSONObject("user"), FeedUser.class);
        super.fillData(data);
    }
}
