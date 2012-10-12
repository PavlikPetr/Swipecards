package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

public class HistoryRequest extends ApiRequest {
    // Data
    private String service = "history";
    public int userid;  // идентификатор пользователя для получения истории сообщений с ним текущего пользвоателя
    public int offset;  // смещение истории сообщений
    public int limit;   // количество получаемых элементов истории сообщений

    public HistoryRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject().put("userid", userid)
                    .put("offset", offset)
                    .put("limit", limit));
        } catch (JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }

}
