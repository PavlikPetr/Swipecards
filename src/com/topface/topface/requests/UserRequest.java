package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserRequest extends ApiRequest {
    // Data
    private String service = "profiles";
    public ArrayList<Integer> uids = new ArrayList<Integer>(); // массив id пользователя в топфейсе
    public ArrayList<String> fields = new ArrayList<String>(); // массив интересующих полей профиля
    public Boolean visitor; // флаг определения текущего пользователя посетителем профилей запрошенных пользователей

    public UserRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject().put("uids", new JSONArray(uids)).put("visitor", visitor));
        } catch (JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }
}
