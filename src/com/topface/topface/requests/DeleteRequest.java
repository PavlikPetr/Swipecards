package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class DeleteRequest extends AbstractApiRequest {
    public int id;

    public DeleteRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("item",Integer.toString(id));
        return data;
    }

    @Override
    protected String getServiceName() {
        return "delete";
    }
}
