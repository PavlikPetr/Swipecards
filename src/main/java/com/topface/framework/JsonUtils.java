package com.topface.framework;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Класс для работы с Json
 * Следует использовать только его, на случай если мы решим поменять парсер
 */
public class JsonUtils {
    private static Gson mGson;

    public static String toJson(Object obj) {
        return getGson().toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return getGson().fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return getGson().fromJson(json, typeOfT);
    }

    private static Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }

    public static <T> T optFromJson(String json, Class<T> classOfT, T defaultObj) {
        T obj = fromJson(json, classOfT);
        return obj == null ? defaultObj : obj;
    }
}
