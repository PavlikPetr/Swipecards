package com.topface.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;
import com.topface.topface.utils.Debug;
import android.content.Context;

public class SettingsRequest extends ApiRequest {
    // Data
    private String service = "settings";
    public String name; // новое имя пользователя в UTF-8
    public int age = -1; // возраст пользователя. Минимальное значение - 12, максимальное - 99. Если указано меньше минимального или болше максимального значение кропятся по ОДЗ
    public int sex = -1; // новый пол пользователя
    public double lat = -1; // долгота местонахождения пользователя
    public double lng = -1; // широта местонахождения пользователя
    public int cityid = -1; // идентификатор города пользователя
    public String status; // статус

    public SettingsRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            JSONObject data = new JSONObject();
            if (name != null)
                data.put("name", name);
            if (status != null)
                data.put("status", status);
            if (age != -1)
                data.put("age", age);
            if (sex != -1)
                data.put("sex", sex);
            if (lat != -1)
                data.put("lat", lat);
            if (lng != -1)
                data.put("lng", lng);
            if (cityid != -1)
                data.put("cityid", cityid);
            root.put("data", data);
        } catch(JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }
}
