package com.topface.topface.data;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Фотографии пользователей из нашего стораджа фотографий (не напрямую из социальной сети)
 */
@SuppressWarnings("UnusedDeclaration")
public class Photo extends AbstractData {

    public static final String SIZE_ORIGINAL = "original";
    public static final String SIZE_64 = "c64x64";
    public static final String SIZE_150 = "r150x-";
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

    public Photo(JSONObject data) {
        super(data);
    }

    @Override
    protected void fillData(JSONObject photoItem) {
        super.fillData(photoItem);

        if (photoItem.has("id")) {
            mId = photoItem.optInt("id");
            JSONObject linksJson = photoItem.optJSONObject("links");
            if (linksJson != null) {
                Iterator photoKeys = linksJson.keys();
                links = new HashMap<String, String>();

                while (photoKeys.hasNext()) {
                    String key = photoKeys.next().toString();

                    links.put(key, linksJson.optString(key));
                }
            }
        }
    }

    /**
     * ассоциативный массив ссылок на фотографии пользователя. Ключами элементов массива являются размеры фотографии пользователя в пикселах.
     * Значениями являются ссылки на фотографии пользователя с заданным размером.
     * Всегда присутствует ключ “original”, представляющий ссылку на исходное загруженное пользователем изображение
     */
    protected HashMap<String, String> links;

    public static Photo parse(JSONObject photoItem) {
        return new Photo(photoItem);
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
        String url = null;
        if (links != null) {
            int minDifference = Integer.MAX_VALUE;
            for (HashMap.Entry<String, String> entry : links.entrySet()) {
                String entryKey = entry.getKey();
                //Не используем редкие размеры фотографий
                if (!entryKey.equals(SIZE_64) && !entryKey.equals(SIZE_150)) {
                    int entrySize = getSizeFromKey(entryKey);
                    int difference = Math.abs(entrySize - size);
                    if (difference < minDifference) {
                        minDifference = difference;
                        url = entry.getValue();
                    }
                }
            }

            if (url == null && links.containsKey(SIZE_ORIGINAL)) {
                url = links.get(SIZE_ORIGINAL);
            }
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
