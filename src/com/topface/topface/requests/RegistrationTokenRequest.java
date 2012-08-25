package com.topface.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;
import com.topface.topface.utils.Debug;
import android.content.Context;

public class RegistrationTokenRequest extends ApiRequest {
    // Data
    private String service = "registrationToken";
    public String token; //Токен регистрации в C2DM

    public RegistrationTokenRequest(Context context) {
        super(context);
    }

    public String toString() {
        JSONObject root = new JSONObject();

        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject().put("token", token));
        } catch(JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }
}
