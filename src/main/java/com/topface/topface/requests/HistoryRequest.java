package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.utils.loadcontollers.ChatLoadController;
import com.topface.topface.utils.loadcontollers.LoadController;

import org.json.JSONException;
import org.json.JSONObject;

public class HistoryRequest extends LimitedApiRequest {
    // Data
    public static final String service = "dialog.get";
    public int userid; // идентификатор пользователя для получения истории сообщений с ним текущего пользвоателя
    //public int offset; // смещение истории сообщений
    public String to; // идентификатор сообщения до которого будет осуществляться выборка истории
    public String from; //идентификатор сообщения после которого будет осуществляться выборка истории
    public String debug;

    public HistoryRequest(Context context, int userId) {
        super(context);
        userid = userId;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = super.getRequestData().put("userId", userid);
        if (to != null) {
            data.put("to", to);
        }
        if (from != null) {
            data.put("from", from);
        }
        if (debug != null) {
            data.put("debug", debug);
        }
        return data;
    }

    @Override
    protected LoadController getLoadController() {
        return new ChatLoadController();
    }

    @Override
    public String getServiceName() {
        return service;
    }

}
