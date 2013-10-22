package com.topface.topface.data;

import com.topface.topface.requests.IApiResponse;
import com.topface.topface.utils.Debug;

import org.json.JSONObject;

public class Auth extends AbstractData {
    // Data
    public int apiVersion;
    public String ssid;
    public String userId;

    public Auth(IApiResponse response) {
        fillData(response.getJsonResult());
    }

    protected void fillData(JSONObject jsonResult) {
        try {
            ssid = jsonResult.getString("ssid");
            apiVersion = jsonResult.optInt("version");
            userId = jsonResult.optString("userId");
        } catch (Exception e) {
            Debug.error("Auth: Wrong response parsing: ", e);
        }
    }
}
