package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class PhotoAddRequest extends AbstractApiRequest {
    public String big;     // URL фотографии пользователя из социальной сети в большом разрешении
    public String medium;  // URL фотографии пользователя из социальной сети в среднем разрешении
    public String small;   // URL фотографии пользователя из социальной сети в малом разрешении

    //---------------------------------------------------------------------------
    public PhotoAddRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("big", big)
                .put("medium", medium)
                .put("small", small);
    }

    @Override
    protected String getServiceName() {
        return "photoAdd";
    }
}
