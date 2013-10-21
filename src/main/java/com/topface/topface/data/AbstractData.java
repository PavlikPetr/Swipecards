package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.IApiResponse;

import org.json.JSONObject;

public abstract class AbstractData {

    public AbstractData() {
        this((JSONObject) null);
    }

    public AbstractData(IApiResponse data) {
        super();
        if (data != null) {
            JSONObject jsonResult = data.getJsonResult();
            if (jsonResult != null) {
                fillData(jsonResult);
            }
        }
    }

    public AbstractData(JSONObject data) {
        super();
        if (data != null) {
            fillData(data);
        }
    }


    protected void fillData(JSONObject data) {
        //Extend me
    }

    /**
     * Устареший метод, использовать не стоит, он бесполезен, там где используется следует заменить на fillData
     */
    @SuppressWarnings("UnusedParameters")
    @Deprecated
    public static Object parse(ApiResponse response) {
        //Extend me
        return null;
    }

}
