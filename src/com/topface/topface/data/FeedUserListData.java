package com.topface.topface.data;

import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Базоый тип данных для парсинга ответов, состоящих из элемента item, содержащим объекты FeedUser
 */
public class FeedUserListData<T extends FeedUser> extends FeedList<T> {

    private final Class<T> mClass;

    public FeedUserListData(JSONObject data, Class<T> itemClass) {
        mClass = itemClass;
        fillData(data);
        if (data != null) {
            fillData(data);
        }
    }

    protected void fillData(JSONObject data) {
        JSONArray list = data.optJSONArray("items");
        if (list != null) {
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.optJSONObject(i);
                if (item != null) {
                    try {
                        //noinspection unchecked
                        add(mClass.getConstructor(JSONObject.class).newInstance(item));
                    } catch (Exception e) {
                        Debug.error(e);
                    }
                }
            }
        }
    }

}
