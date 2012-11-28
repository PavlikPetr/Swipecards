package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class SettingsRequest extends AbstractApiRequest {
    // Data
    public static final String service = "settings";
    public String name; // новое имя пользователя в UTF-8
    public int age = -1; // возраст пользователя. Минимальное значение - 12,
    // максимальное - 99. Если указано меньше
    // минимального или болше максимального значение
    // кропятся по ОДЗ
    public int sex = -1; // новый пол пользователя
    //public double lat = ; // долгота местонахождения пользователя
    //public double lng = ; // широта местонахождения пользователя
    public int cityid = -1; // идентификатор города пользователя
    public String status; // статус
    public int background = -1;

    public SettingsRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        if (name != null) data.put("name", name);
        if (status != null) data.put("status", status);
        if (age != -1) data.put("age", age);
        if (sex != -1) data.put("sex", sex);
        //if (lat != -1) data.put("lat", lat);
        //if (lng != -1) data.put("lng", lng);
        if (cityid != -1) data.put("cityid", cityid);
        if (background != -1) data.put("background", background);

        return data;
    }

    @Override
    public String getServiceName() {
        return service;
    }

}
