package com.topface.topface.data;

import org.json.JSONObject;

public class BlackListItem extends FeedItem {
    public BlackListItem(JSONObject data) {
        super(data);
    }

    public void fillData(JSONObject item) {
        //В черном списке нас интересует только юзер, все остальные поля не нужны
        this.user = new FeedUser(item.optJSONObject("user"));
        this.id = this.user.id;
    }
}
