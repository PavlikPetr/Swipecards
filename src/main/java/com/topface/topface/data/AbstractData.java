package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;

import org.json.JSONObject;

public abstract class AbstractData {

    public AbstractData() {
        this(null);
    }

    public AbstractData(JSONObject data) {
        if (data != null) {
            fillData(data);
        }
    }

    protected void fillData(JSONObject data) {
        //Extend me
    }

    public static Object parse(ApiResponse response) {
        return null;
    }

}
