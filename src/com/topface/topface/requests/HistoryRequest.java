package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class HistoryRequest extends ApiRequest {
    // Data
    public static final String service = "history";
    public int userid; // идентификатор пользователя для получения истории сообщений с ним текущего пользвоателя
    //public int offset; // смещение истории сообщений
    public int limit; // количество получаемых элементов истории сообщений
    public int to; // идентификатор сообщения до которого будет осуществляться выборка истории
    public int from; //идентификатор сообщения после которого будет осуществляться выборка истории
    public String debug;

    public HistoryRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject().put("userid", userid).put("limit", limit);
        if (to > 0) {
            data.put("to", to);
        }
        if (from > 0) {
            data.put("from", from);
        }
        if (debug != null) {
            data.put("debug", debug);
        }
        return data;
    }

    @Override
    public String getServiceName() {
        return service;
    }

}
