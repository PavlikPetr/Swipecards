package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageRequest extends ConfirmedApiRequest {
    // Data
    public static final String service = "message";
    private int mUserId; // идентификатор пользователя, кому послали сообщение
    private String mMessage; // текст сообщения в UTF-8. min размер текста - 1 символ, max - 1024

    public MessageRequest(int userId, String message, Context context) {
        super(context);
        mUserId = userId;
        mMessage = message;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("userid", mUserId)
                .put("message", mMessage);
    }

    @Override
    public String getServiceName() {
        return service;
    }

    @Override
    public void exec() {
        if (mUserId < 1) {
            handleFail(ApiResponse.MISSING_REQUIRE_PARAMETER, "Wrong user id");
        } else if (TextUtils.isEmpty(mMessage) || mMessage.length() <= 1) {
            handleFail(ApiResponse.MISSING_REQUIRE_PARAMETER, "Message is too short");
        } else {
            super.exec();
        }
    }
}
