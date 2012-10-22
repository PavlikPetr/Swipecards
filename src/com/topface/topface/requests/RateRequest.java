package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

public class RateRequest extends ApiRequest {

    public static final int DEFAULT_MUTUAL = 1;
    public static final int DEFAULT_NO_MUTUAL = 0;

    // Data
    private String service = "rate";
    public int userid; // идентификатор пользователя для оценки
    public int rate; // оценка пользователя. ОДЗ: 1 <= RATE <= 10
    public int mutualid; // идентификатор сообщения из ленты, на который отправляется взаимная симпатия

    public RateRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject().put("userid", userid).put("rate", rate).put("mutualid", mutualid));
        } catch (JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }

}
