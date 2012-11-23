package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class RateRequest extends AbstractApiRequest {

    public static final int DEFAULT_MUTUAL = 1;
    public static final int DEFAULT_NO_MUTUAL = 0;

    // Data
    public static final String service = "rate";
    public int userid; // идентификатор пользователя для оценки
    public int rate; // оценка пользователя. ОДЗ: 1 <= RATE <= 10
    public int mutualid; // идентификатор сообщения из ленты, на который отправляется взаимная симпатия

    public RateRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("userid", userid)
                .put("rate", rate)
                .put("mutualid", mutualid);
    }

    @Override
    protected String getServiceName() {
        return service;
    }
}
