package com.topface.topface.data;

import com.topface.topface.requests.IApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONObject;

public class Auth extends AbstractData {
    // Data
    public int api_version;
    public String ssid;

    public Auth(IApiResponse response) {
        fillData(response.getJsonResult());
    }

    @Override
    protected void fillData(JSONObject jsonResult) {
        try {
            ssid = jsonResult.getString("ssid");
            api_version = jsonResult.optInt("version");
        } catch (Exception e) {
            Debug.error("Auth: Wrong response parsing: ", e);
        }
    }
}
