package com.topface.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;
import com.topface.topface.utils.Debug;
import android.content.Context;

public class HistoryRequest extends ApiRequest {
    // Data
    private String service = "history";
    public int userid; // идентификатор пользователя для получения истории сообщений с ним текущего пользвоателя
    //public int offset; // смещение истории сообщений
    public int limit; // количество получаемых элементов истории сообщений
    public int to; // идентификатор сообщения до которого будет осуществляться выборка истории
    //---------------------------------------------------------------------------
    public HistoryRequest(Context context) {
        super(context);
    }
    //---------------------------------------------------------------------------
    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject().put("userid", userid).put("limit", limit).put("to", to));
        } catch(JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }
    //---------------------------------------------------------------------------
}
