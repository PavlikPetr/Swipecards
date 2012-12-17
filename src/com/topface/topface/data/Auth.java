package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class Auth extends AbstractData {
    // Data
    public int api_version;
    public String ssid;

    public static Auth parse(ApiResponse response) {
        Auth auth = new Auth();

        try {
            auth.ssid = response.jsonResult.getString("ssid");
            auth.api_version = response.jsonResult.optInt("version");
        } catch (Exception e) {
            Debug.error("Auth: Wrong response parsing: ", e);
        }

        return auth;
    }
}
