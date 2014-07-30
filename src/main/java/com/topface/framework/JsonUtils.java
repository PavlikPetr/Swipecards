package com.topface.framework;

import com.google.gson.Gson;

/**
 * Класс для работы с Json
 * Следует использовать только его, ан случай если мы решим поменять парсер
 */
public class JsonUtils {
    private static Gson mGson;

    public static String toJson(Object obj) {
        return getGson().toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return getGson().fromJson(json, classOfT);
    }

    private static Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }
}
