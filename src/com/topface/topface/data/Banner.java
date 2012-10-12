package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONObject;

public class Banner extends AbstractData {
    // Data
    public String url;        // - URL адрес изображения баннера
    public String action;     // - идентификатор возможного действия с баннером. Возможные значения: URL, PAGE
    public String parameter;  // - дополнительный параметр действия
    // Constants
    public static final String ACTION_URL = "URL";
    public static final String ACTION_PAGE = "PAGE";
    public static final String INVITE_ACTION = "INVITE";

    public static Banner parse(ApiResponse response) {
        Banner banner = new Banner();

        try {
            JSONObject item = response.mJSONResult;
            banner.url = item.optString("url", "");
            banner.action = item.optString("action", "");
            banner.parameter = item.optString("parameter", "");
        } catch (Exception e) {
            Debug.log("Banner.class", "Wrong response parsing: " + e);
        }

        return banner;
    }

}
