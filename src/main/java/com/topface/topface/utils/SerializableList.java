package com.topface.topface.utils;

import com.topface.framework.utils.Debug;
import com.topface.topface.data.SerializableToJson;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Constructor;
import java.util.LinkedList;

public class SerializableList extends LinkedList<SerializableToJson> {


    public String toJson() {
        JSONArray array = new JSONArray();
        try {
            for (Object item : this) {
                SerializableToJson jsonItem = (SerializableToJson) item;
                array.put(jsonItem.toJson().toString());
            }
        } catch (JSONException e) {
            Debug.error(e);
        }
        return array.toString();
    }

    public void fromJSON(String json, String className) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            Class<?> serializableClass = Class.forName(className);

            for (int i = 0; i < jsonArray.length(); i++) {
                Constructor<?> constructor = serializableClass.getConstructor();
                Object item = constructor.newInstance();
                SerializableToJson jsonSerializable = (SerializableToJson) item;
                jsonSerializable.fromJSON(jsonArray.getString(i));
                add(jsonSerializable);
            }

        } catch (Exception e) {
            Debug.error(e);
        }
    }
}
