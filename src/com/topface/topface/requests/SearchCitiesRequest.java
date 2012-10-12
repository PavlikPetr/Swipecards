package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchCitiesRequest extends ApiRequest {
    // Data
    private String service = "searchCities";
    public String prefix;   // начальный текст наименования города в UTF-8. Минимальный размер текста - 3 символа

    public SearchCitiesRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject().put("prefix", prefix));
        } catch (JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }

}

