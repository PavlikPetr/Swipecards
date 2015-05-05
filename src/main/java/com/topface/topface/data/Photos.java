package com.topface.topface.data;


import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Photos extends ArrayList<Photo> implements SerializableToJsonArray {

    public Photos(Photos photos) {
        super();
        for (Photo photo : photos) {
            add(new Photo(photo));
        }
    }

    public Photos() {
        super();
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
    @SuppressWarnings("unused")
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
}
