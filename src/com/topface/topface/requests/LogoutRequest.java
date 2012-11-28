package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: Ilya Vorobiev
 * Date: 13.11.12
 * Time: 11:19
 * To change this template use File | Settings | File Templates.
 */
public class LogoutRequest extends AbstractApiRequest {
    public static final String service = "logout";

    public LogoutRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getServiceName() {
        return service;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
