package com.topface.topface.utils;

import com.topface.topface.requests.ComplainRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Constructor;
import java.util.LinkedList;

public class SerializableList extends LinkedList<JsonSerializable> {


    public String toJSON() {
        JSONArray array = new JSONArray();
        for (Object item : this) {
            JsonSerializable jsonItem = (JsonSerializable) item;
            array.put(jsonItem.toJSON());
        }
        return array.toString();
    }

    public void fromJSON(String json,  String className) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            Class<?> serializableClass = Class.forName(className);

            for (int i = 0; i < jsonArray.length(); i++) {
                Constructor<?> constructor = serializableClass.getConstructor();
                Object item = constructor.newInstance();
                JsonSerializable jsonSerializable = (JsonSerializable) item;
                jsonSerializable.fromJSON(jsonArray.getString(i));
                add(jsonSerializable);
            }

        } catch (Exception e) {
            Debug.error(e);
        }
    }
}
