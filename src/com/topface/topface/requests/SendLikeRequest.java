package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class SendLikeRequest extends ConfirmedApiRequest {

    public static final int DEFAULT_MUTUAL = 1;
    public static final int DEFAULT_NO_MUTUAL = 0;

    // Data
    public static final String service = "like.send";
    public int userid; // идентификатор пользователя для оценки
    public int mutualid; // идентификатор сообщения из ленты, на который отправляется взаимная симпатия
    public int place; //TODO место отправки лайка

    public SendLikeRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("userid", userid)
                .put("mutualid", mutualid)
                .put("place",place);
    }

    @Override
    public String getServiceName() {
        return service;
    }
}
