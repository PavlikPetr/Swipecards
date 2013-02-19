package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageRequest extends ApiRequest {
    // Data
    public static final String service = "message";
    public int userid; // идентификатор пользователя, кому послали сообщение
    public String message; // текст сообщения в UTF-8. min размер текста - 1 символ, max - 1024 

    public MessageRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("userid", userid)
                .put("message", message);
    }

    @Override
    public String getServiceName() {
        return service;
    }
}
