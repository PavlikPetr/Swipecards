package com.topface.topface.data;

import com.topface.framework.JsonUtils;

import org.json.JSONObject;

public abstract class AbstractDataWithPhotos extends AbstractData {

    public Photos photos;
    public Photo photo;

    protected static void initPhotos(JSONObject item, AbstractDataWithPhotos data) {
        // Avatar
        if (!item.isNull("photo")) {
            data.photo = JsonUtils.fromJson(item.optJSONObject("photo"), Photo.class);
        }
        // Album
        if (!item.isNull("photos")) {
            data.photos = JsonUtils.fromJson(item.optJSONArray("photos"), Photos.class);
        }
    }
}
