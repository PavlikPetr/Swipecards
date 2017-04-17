package com.topface.topface.data;

import com.topface.framework.JsonUtils;
import com.topface.topface.utils.Utils;

import org.json.JSONObject;

public class HistoryListData extends FeedListData<History> {

    public FeedUser user;

    /**
     * Признак "подозрительности" пользователя,
     * если true - надо будет показать плашку "заблокировать/пожаловаться" в чате
     */
    public boolean isSuspiciousUser;

    public HistoryListData(JSONObject data, Class<History> itemClass) {
        super(data, itemClass);
    }

    @Override
    protected void fillData(JSONObject data) {
        this.user = JsonUtils.fromJson(Utils.optString(data, "user"), FeedUser.class);
        this.isSuspiciousUser = data.optBoolean("isSuspiciousUser");
        super.fillData(data);
    }
}
