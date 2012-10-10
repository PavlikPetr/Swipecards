package com.topface.topface.data;

import org.json.JSONObject;

public abstract class AbstractDataWithPhotos extends AbstractData {

    public Photos photos;
    public Photo photo;

    protected static void initPhotos(JSONObject item, AbstractDataWithPhotos data) {
        // Avatar
        if (!item.isNull("photo")) {
            data.photo = Photo.parse(item.optJSONObject("photo"));
        }
        // Album
        if (!item.isNull("photos")) {
            data.photos = Photos.parse(item.optJSONArray("photos"));
        }
    }
}
