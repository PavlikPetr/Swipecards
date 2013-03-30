package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchCitiesRequest extends ApiRequest {
    // Data
    public static final String service = "searchCities";
    public String prefix; // начальный текст наименования города в UTF-8. Минимальный размер текста - 3 символа

    public SearchCitiesRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("prefix", prefix);
    }

    @Override
    public String getServiceName() {
        return service;
    }
}
