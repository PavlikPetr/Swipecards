package com.topface.topface.data;

import com.topface.framework.utils.Debug;
import com.topface.topface.ui.adapters.FeedList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Базоый тип данных для парсинга ответов, состоящих из элемента item, содержащим объекты FeedUser
 */
@SuppressWarnings("serial")
public class FeedUserListData<T extends FeedUser> extends FeedList<T> {

    private final Class<T> mClass;

    public FeedUserListData(JSONObject data, Class<T> itemClass) {
        mClass = itemClass;
        if (data != null) {
            fillData(data);
        }
    }

    protected void fillData(JSONObject data) {
        JSONArray list = data.optJSONArray("users");
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
