package com.topface.topface.data;


import com.topface.framework.utils.Debug;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Photos extends ArrayList<Photo> implements SerializableToJsonArray {

    public Photos(JSONArray photos) {
        super();
        addAll(parse(photos));
    }

    public Photos(Photos photos) {
        super();
        for (Photo photo : photos) {
            add(new Photo(photo));
        }
    }

    public Photos() {
        super();
    }

    public static Photos parse(JSONArray photoArray) {
        Photos photos = new Photos();
        if (photoArray != null) {
            for (int i = 0; i < photoArray.length(); i++) {
                try {
                    photos.add(new Photo(photoArray.getJSONObject(i)));
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
        return photo != null && getPhotoById(photo.getId()) != null;
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

    public Photo getPhotoById(int photoId) {
        Photo result = null;
        for (Photo photo : this) {
            if (photo != null && photoId == photo.getId()) {
                result = photo;
                break;
            }
        }
        return result;
    }

    /**
     * Возвращает позицию аватары в массиве по id. Если id не соответствует ни одна фотография
     * будет возвращения 0 позиция
     *
     * @param photoId id фоторгафии
     */
    public int getPhotoIndexById(int photoId) {
        for (int i = 0; i < size(); i++) {
            if (this.get(i) != null && photoId == this.get(i).getId()) {
                return i;
            }
        }
        return 0;
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


    public int getRealPhotosCount() {
        int realPhotosCount = 0;
        for (Photo photo : this) {
            if (photo != null && !photo.isFake()) {
                realPhotosCount++;
            }
        }
        return realPhotosCount;
    }

    public boolean removeById(int photoId) {
        for (Photo photo : this) {
            if (photo.getId() == photoId) {
                this.remove(photo);
                return true;
            }
        }
        return false;
    }

    @Override
    public JSONArray toJson() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (Photo photo : this) {
            if (photo != null) {
                jsonArray.put(photo.toJson());
            }
        }
        return jsonArray;
    }

    public int[] getIdsArray() {
        int[] result = new int[this.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.get(i).getId();
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Photos) {
            if (this.size() != ((Photos) o).size()) {
                return false;
            }
            for (int i = 0; i < this.size(); i++) {
                if (!(this.get(i).equals(((Photos) o).get(i)))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
