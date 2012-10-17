package com.topface.topface.data;

import com.topface.topface.ui.adapters.FeedList;
import org.json.JSONObject;

public class FeedListData<T extends FeedItem> extends AbstractData {

    public int unread;
    public boolean more;
    public FeedList<T> list;

    public FeedListData(JSONObject data) {
        super(data);
    }

    @Override
    protected void fillData(JSONObject data) {
        super.fillData(data);
        unread = data.optInt("unread");
        more = data.optBoolean("more");
        list = T.getList(data.optJSONArray("list"));
    }
}
