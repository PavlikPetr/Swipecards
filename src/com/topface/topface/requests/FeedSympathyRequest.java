package com.topface.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;
import com.topface.topface.utils.Debug;
import android.content.Context;

public class FeedSympathyRequest extends ApiRequest {
    // Data
    private String service = "feedSymphaty";
    public int limit; // максимальный размер выборки входящих симпатий
    public int from; // начальный идентификатор симпатии для выборки
    public boolean only_new; // осуществлять выборку только по непрочитанным симпатиям
    public boolean leave = false; // не отмечать полученные сообщения прочитанными

    public FeedSympathyRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            JSONObject data = new JSONObject().put("limit", limit).put("leave", leave);
            if (from > 0)
                data.put("from", from);
            if (only_new)
                data.put("new", only_new);
            root.put("data", data);
        } catch(JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }
}
