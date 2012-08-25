package com.topface.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.topface.topface.utils.Debug;

public class MapRequest extends ApiRequest {
	// Data
	//TODO Server API NEEEDED
    private String service = "???";
    public int userid; // идентификатор пользователя, кому послали сообщение
    public String message; // текст сообщения в UTF-8. min размер текста - 1 символ, max - 1024 

    public MapRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject().put("userid", userid).put("message", message));
        } catch(JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }
}
