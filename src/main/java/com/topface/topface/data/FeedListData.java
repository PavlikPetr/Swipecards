package com.topface.topface.data;

import com.topface.framework.utils.Debug;
import com.topface.topface.ui.adapters.FeedList;

import org.json.JSONArray;
import org.json.JSONObject;

public class FeedListData<T extends FeedItem> extends AbstractData {

    public boolean more;
    public int mutualTime;
    public FeedList<T> items;
    private final Class mClass;

    public FeedListData(JSONObject data, Class itemClass) {
        mClass = itemClass;
        if (data != null) {
            fillData(data);
        }
    }

    public FeedListData(FeedList<T> data, boolean more, Class classType) {
        items = new FeedList<>();
        items.addAll(data);
        this.more = more;
        mClass = classType;
    }

    public FeedListData(FeedList<T> data, boolean more, Class classType, int mutualTime) {
        items = new FeedList<>();
        items.addAll(data);
        this.more = more;
        mClass = classType;
        this.mutualTime = mutualTime;
    }

    protected void fillData(JSONObject data) {
        more = data.optBoolean("more");
        items = getList(data.optJSONArray("items"));
        mutualTime = data.optInt("mutualTime");
    }

    public <T extends FeedItem> FeedList<T> getList(JSONArray list) {
        FeedList<T> result = new FeedList<>();
        if (list != null) {
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.optJSONObject(i);
                if (item != null) {
                    try {
                        //noinspection unchecked
                        result.add((T) mClass.getConstructor(JSONObject.class).newInstance(item));
                    } catch (Exception e) {
                        Debug.error(e);
                    }
                }
            }
        }
        return result;
    }

}
