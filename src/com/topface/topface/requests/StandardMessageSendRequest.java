package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class StandardMessageSendRequest extends ConfirmedApiRequest {
    public static final int MESSAGE_FILL_INTERESTS = 14;


    public final static String SERVICE_NAME = "standardMessageSend";
    private int userId;
    private int messageId;

    public StandardMessageSendRequest(Context context, int msgId, int userId) {
        super(context);
        this.userId = userId;
        this.messageId = msgId;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject request = new JSONObject();
        request.put("messageid", messageId);
        request.put("userid", userId);
        return request;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
