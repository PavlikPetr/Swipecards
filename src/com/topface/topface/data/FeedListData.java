package com.topface.topface.data;

import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.utils.Debug;

import org.json.JSONArray;
import org.json.JSONObject;

public class FeedListData<T extends FeedItem> extends AbstractData {

    public boolean more;
    public FeedList<T> items;
    private final Class<T> mClass;

    public FeedListData(JSONObject data, Class<T> itemClass) {
        mClass = itemClass;
        if (data != null) {
            fillData(data);
        }
    }

    @Override
    protected void fillData(JSONObject data) {
        super.fillData(data);
        more = data.optBoolean("more");
        items = getList(data.optJSONArray("items"));
    }

    private <T extends FeedItem> FeedList<T> getList(JSONArray list) {
        FeedList<T> result = new FeedList<T>();
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
