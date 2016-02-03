package com.topface.framework;

import android.util.SparseArray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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

    public static <T> T fromJson(String json, TypeToken<T> typeToken) {
        return getGson().fromJson(json, typeToken.getType());
    }

    public static <T> T optFromJson(String json, Class<T> classOfT, T defaultObj) {
        T obj = fromJson(json, classOfT);
        return obj == null ? defaultObj : obj;
    }

    public static String profileToJson(Profile profile) {
        Gson gson = new GsonBuilder().registerTypeAdapter(SparseArray.class
                , new JsonSerializer<SparseArray<Profile.TopfaceNotifications>>() {
            @Override
            public JsonElement serialize(SparseArray<Profile.TopfaceNotifications> sparseArray, Type typeOfSrc, JsonSerializationContext context) {
                ArrayList<Profile.TopfaceNotifications> list = new ArrayList<>();
                for (int i = 0; i < sparseArray.size(); i++) {
                    int key = sparseArray.keyAt(i);
                    Profile.TopfaceNotifications notifications = sparseArray.get(key);
                    list.add(notifications);
                }
                return context.serialize(list);
            }
        }).create();
        return gson.toJson(profile);
    }

    public static String optionsToJson(Options options) {
        Gson gson = new GsonBuilder().registerTypeAdapter(HashMap.class
                , new JsonSerializer<HashMap<String, PageInfo>>() {
            @Override
            public JsonElement serialize(HashMap<String, PageInfo> hashMap, Type typeOfSrc, JsonSerializationContext context) {
                /*
                Немножечко магии, чтоб при сериализации получался такой же json объект,
                который присылает нам сервер.(Чтоб метод fillData в Options отработал как нужно)
                */
                Collection<PageInfo> list = hashMap.values();
                return context.serialize(list);
            }
        }).create();
        return gson.toJson(options);
    }
}
