package com.topface.topface.data;

import org.json.JSONArray;
import org.json.JSONException;

abstract public interface SerializableToJsonArray {
    public JSONArray toJson() throws JSONException;
}
