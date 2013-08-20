package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Запрос удаляет пользователя из черного списка
 */
public class BlackListDeleteRequest extends ApiRequest {
    public static final String SERVICE_NAME = "blacklistDelete";
    /**
     * id пользователя, котогорого нужно добавить в черный список
     */
    private final List<Integer> mUserIds;

    /**
     * @param userIds массив id пользовтелей, которые нужно добавить в черный список
     */
    public BlackListDeleteRequest(List<Integer> userIds, Context context) {
        super(context);
        mUserIds = userIds;
    }

    /**
     * @param userId пользователя, которого нужно добавить в черный список
     */
    public BlackListDeleteRequest(int userId, Context context) {
        super(context);
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(userId);
        mUserIds = list;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("userids", new JSONArray(mUserIds));
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public void exec() {
        if (mUserIds != null && mUserIds.size() > 0) {
            super.exec();
        } else {
            handleFail(ApiResponse.ERRORS_PROCCESED, "User list for delete from black list is empty");
        }
    }
}