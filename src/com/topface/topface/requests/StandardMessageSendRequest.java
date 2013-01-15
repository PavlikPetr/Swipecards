package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class StandardMessageSendRequest extends AbstractApiRequest {
    public static final int MESSAGE_FILL_INTERESTS = 14;
    public static final int MESSAGE_LIKE_GIFT = 15;
    public static final int MESSAGE_FILL_QUESTIONARY = 16;


    public final static String SERVICE_NAME = "StandardMessageSend";
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
        request.put("messageid",messageId);
        request.put("userid",userId);
        return request;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
