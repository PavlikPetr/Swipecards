package com.topface.topface.data;

import org.json.JSONException;
import org.json.JSONObject;

public interface SerializableToJson {
    public JSONObject toJson() throws JSONException;
}
