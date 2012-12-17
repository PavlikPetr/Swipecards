package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Запрос добавляющий пользователя в черный список
 */
public class BlackListAddRequest extends AbstractApiRequest {
    public static final String SERVICE_NAME = "blacklistAdd";
    /**
     * id пользователя, котогорого нужно добавить в черный список
     */
    private final int mUserId;

    /**
     *
     * @param userId пользователя, которого нужно добавить в черный список
     */
    public BlackListAddRequest(int userId, Context context) {
        super(context);
        mUserId = userId;
    }
    
    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("userid", mUserId);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
