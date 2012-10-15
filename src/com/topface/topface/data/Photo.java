package com.topface.topface.data;

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
@SuppressWarnings("UnusedDeclaration")
public class Photo {

    public static final String SIZE_ORIGINAL = "original";
    public static final String SIZE_64 = "c64x64";
    public static final String SIZE_128 = "c128x128";
    public static final String SIZE_192 = "c192x192";
    public static final String SIZE_256 = "c256x256";
    public static final String SIZE_960 = "r640x960";

    public static final String PHOTO_KEY_SIZE_PATTERN = "(\\d+)x(\\d+)";
    /**
     * идентификатор фотографии пользователя
     */
    protected int mId;
    private Pattern mPattern;

    public Photo(int id, HashMap<String, String> links) {
        this.mId = id;
        this.links = links;
    }

    /**
     * ассоциативный массив ссылок на фотографии пользователя. Ключами элементов массива являются размеры фотографии пользователя в пикселах.
     * Значениями являются ссылки на фотографии пользователя с заданным размером.
     * Всегда присутствует ключ “original”, представляющий ссылку на исходное загруженное пользователем изображение
     */
    protected HashMap<String, String> links;

    public static Photo parse(JSONObject photoItem) {
        Photo photo = null;
        try {
            if (photoItem.has("id")) {
                int mId = photoItem.getInt("id");
                JSONObject linksJson = photoItem.getJSONObject("links");
                Iterator photoKeys = linksJson.keys();
                HashMap<String, String> links = new HashMap<String, String>();

                while (photoKeys.hasNext()) {
                    String key = photoKeys.next().toString();

                    links.put(key, linksJson.getString(key));
                }
                photo = new Photo(mId, links);
            }

        } catch (JSONException e) {
            Debug.error(e);
        }

        return photo;
    }

    /**
     * Возвращает наиболее подходящий размер фотографии из уже существующих
     *
     * @param size необходимый размер фотографии
     * @return url на выбранную фотографию
     */
    public String getSuitableLink(String size) {
        String url = null;
        if (links.containsKey(size)) {
            url = links.get(size);
        } else {
            getSuitableLink(getSizeFromKey(size));
        }

        if (url == null && links.containsKey(SIZE_ORIGINAL)) {
            url = links.get(SIZE_ORIGINAL);
        }

        return url;
    }

    public String getSuitableLink(int size) {
        int minDifference = Integer.MAX_VALUE;
        String url = null;

        for (HashMap.Entry<String, String> entry : links.entrySet()) {
            int entrySize = getSizeFromKey(entry.getKey());
            int difference = Math.abs(entrySize - size);
            if (difference < minDifference) {
                minDifference = difference;
                url = entry.getValue();
            }
        }

        if (url == null && links.containsKey(SIZE_ORIGINAL)) {
            url = links.get(SIZE_ORIGINAL);
        }

        return url;
    }

    protected int getSizeFromKey(String key) {
        int size = 0;
        if (mPattern == null) {
            mPattern = Pattern.compile(PHOTO_KEY_SIZE_PATTERN);
        }

        Matcher matcher = mPattern.matcher(key);
        if (matcher.find()) {
            size = Math.max(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        }

        return size;
    }

    public int getId() {
        return mId;
    }
}
