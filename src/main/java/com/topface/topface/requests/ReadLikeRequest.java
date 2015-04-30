package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.requests.handlers.ErrorCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReadLikeRequest extends ApiRequest {

    private int mId;
    public final static String SERVICE_NAME = "like.read";

    public ReadLikeRequest(Context context, int id) {
        super(context);
        mId = id;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(mId);
        return new JSONObject().put("ids", jsonArray);
    }

    @Override
    public void exec() {
        if (mId > 0) {
            super.exec();
        } else {
            handleFail(ErrorCodes.ERRORS_PROCCESED, "Invalid id");
        }
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
