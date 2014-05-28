package com.topface.topface.data;

import com.topface.framework.utils.Debug;
import com.topface.topface.requests.ApiResponse;

import org.json.JSONObject;

public class Banner extends AbstractData {
    // Data
    public String name; // - наименование баннера
    public String url; // - URL адрес изображения баннера
    public String action; // - идентификатор возможного действия с баннером. Возможные значения: URL, PAGE
    public String parameter; // - дополнительный параметр действия
    // Constants
    public static final String ACTION_URL = "URL";
    public static final String ACTION_PAGE = "PAGE";
    public static final String ACTION_METHOD = "METHOD";
    public static final String ACTION_OFFERWALL = "OFFERWALL";

    public Banner(ApiResponse response) {
        fillData(response);
    }

    private void fillData(ApiResponse response) {
        try {
            JSONObject item = response.jsonResult;
            name = item.optString("name");
            url = item.optString("url");
            action = item.optString("action");
            parameter = item.optString("parameter");
        } catch (Exception e) {
            Debug.error("Banner.class: Wrong response parsing", e);
        }
    }
}
