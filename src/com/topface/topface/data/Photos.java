package com.topface.topface.data;


import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Photos extends ArrayList<Photo> {

    public Photos(JSONArray photos) {
        this();
        addAll(parse(photos));
    }

    public Photos() {
        super();
    }

    public static Photos parse(JSONArray photoArray) {
        Photos photos = new Photos();
        if (photoArray != null) {
            for (int i = 0; i < photoArray.length(); i++) {
                try {
                    photos.addFirst(new Photo(photoArray.getJSONObject(i)));
                } catch (JSONException e) {
                    Debug.error("Photo parse error", e);
                }
            }
        }

        return photos;
    }

    /**
     * Есть ли указанное фото в данном списке
     * NOTE: Проверка идет по id фотографии,
     * соответсвенно будет не корректно работать с фотошграфиями от разных пользователей
     *
     * @param photo объект фото
     * @return флаг наличия фотографии в списке
     */
    public boolean contains(Photo photo) {
        return getByPhotoId(photo.getId()) != null;
    }

    /**
     * Есть ли указанный индекс в списке фотографий
     *
     * @param index фотографии
     * @return флаг наличия фотографии в списке
     */
    public boolean contains(int index) {
        return size() >= index + 1;
    }

    public Photo getByPhotoId(int photoId) {
        Photo result = null;
        for (Photo photo : this) {
            if (photo != null && photoId == photo.getId()) {
                result = photo;
                break;
            }
        }
        return result;
    }

    public Photo getFirst() {
        if (!this.isEmpty()) {
            return this.get(0);
        }

        return null;
    }

    public void addFirst(Photo value) {
        this.add(0, value);
    }
}
