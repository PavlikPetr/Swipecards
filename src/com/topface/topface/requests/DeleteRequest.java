package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: Ilya Vorobiev
 * Date: 06.11.12
 * Time: 18:11
 * To change this template use File | Settings | File Templates.
 */
public class DeleteRequest extends AbstractApiRequest {
    public int id;

    public DeleteRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("item",Integer.toString(id));
        return data;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected String getServiceName() {

        return "delete";  //To change body of implemented methods use File | Settings | File Templates.
    }
}
