package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Фотографии пользователей из нашего стораджа фотографий (не напрямую из социальной сети)
 */
public class UserPhotos {
    public static final String SIZE_ORIGINAL = "original";
    public static final String SIZE_128 = "128x128";
    @SuppressWarnings("UnusedDeclaration")
    public static final String SIZE_256 = "256x256";
    /**
     * идентификатор фотографии пользователя
     */
    protected int id;

    public UserPhotos(int id, HashMap<String, String> links) {
        this.id = id;
        this.links = links;
    }

    /**
     * ассоциативный массив ссылок на фотографии пользователя. Ключами элементов массива являются размеры фотографии пользователя в пикселах.
     * Значениями являются ссылки на фотографии пользователя с заданным размером.
     * Всегда присутствует ключ “original”, представляющий ссылку на исходное загруженное пользователем изображение
     */
    protected HashMap<String, String> links;

    public static UserPhotos parse(ApiResponse response) {
        return parsePhotos(response.mJSONResult);
    }

    public static UserPhotos parsePhotos(JSONObject photoItem) {
        Iterator photoKeys = photoItem.keys();
        UserPhotos photos = null;
        try {
            int id = photoItem.getInt("id");
            HashMap<String, String> links = new HashMap<String, String>();

            while (photoKeys.hasNext()) {
                String key = photoKeys.next().toString();

                    links.put(key, photoItem.getString(key));
            }

            photos = new UserPhotos(id, links);
        } catch (JSONException e) {
            Debug.error(e);
        }

        return photos;

    }

    /**
     * Возвращает наиболее подходящий размер фотографии из уже существующих
     * @param size необходимый размер фотографии
     * @return url на выбранную фотографию
     */
    public String getSuitableLink(String size) {
        String url = null;
        if (links.containsKey(size)) {
            url = links.get(size);
        }
        else {
            int needSize = getSizeFromKey(size);
            int minDifference = needSize;
            for (HashMap.Entry<String, String> entry : links.entrySet()) {
                int entrySize = getSizeFromKey(entry.getKey());
                int difference = entrySize - needSize;
                if (entrySize >= needSize && difference < minDifference) {
                    minDifference = difference;
                    url = entry.getValue();
                }
            }
        }

        if (url == null && links.containsKey(SIZE_ORIGINAL)) {
            url = links.get(SIZE_ORIGINAL);
        }

        return url;
    }

    protected int getSizeFromKey(String key) {
        int size = 0;
        Pattern pattern = Pattern.compile("(\\d+)x(\\d+)");
        Matcher matcher = pattern.matcher(key);
        matcher.find();
        if (matcher.matches()) {
            size = Math.max(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        }

        return size;
    }
}
