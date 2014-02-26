package com.topface.topface.requests;

import android.content.Context;
import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsRequest extends ApiRequest {
    // Data
    public static final String service = "user.setProfile";
    public String name; // новое имя пользователя в UTF-8
    public int age = -1; // возраст пользователя. Минимальное значение - 12,
    // максимальное - 99. Если указано меньше
    // минимального или болше максимального значение
    // кропятся по ОДЗ
    public int sex = -1; // новый пол пользователя
    public Location location; //координаты пользователя
    public int cityid = -1; // идентификатор города пользователя
    public String status; // статус
    public int background = -1;
    public Boolean invisible;
    public int xstatus = -1; //цель знакомства

    public SettingsRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        if (name != null) {
            data.put("name", name);
        }
        if (status != null) {
            data.put("status", status);
        }
        if (age != -1) {
            data.put("age", age);
        }
        if (sex != -1) {
            data.put("sex", sex);
        }
        if (location != null) {
            data.put("lat", location.getLatitude());
            data.put("lng", location.getLongitude());
        }
        //if (lat != -1) data.put("lat", lat);
        //if (lng != -1) data.put("lng", lng);
        if (cityid != -1) {
            data.put("cityId", cityid);
        }
        if (background != -1) {
            data.put("background", background);
        }
        if (invisible != null) {
            data.put("invisible", invisible);
        }
        if (xstatus != -1) {
            data.put("xstatus", xstatus);
        }

        return data;
    }

    @Override
    public String getServiceName() {
        return service;
    }

}
