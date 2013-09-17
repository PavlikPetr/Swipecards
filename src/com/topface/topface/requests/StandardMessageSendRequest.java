package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class StandardMessageSendRequest extends ConfirmedApiRequest {
    public static final int MESSAGE_FILL_INTERESTS = 14;


    public final static String SERVICE = "message.sendStandard";
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
        request.put("messageId", messageId);
        request.put("userId", userId);
        return request;
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }
}
