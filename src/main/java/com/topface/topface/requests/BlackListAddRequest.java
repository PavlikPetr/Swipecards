package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.requests.handlers.AttitudeHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.ContainerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Запрос добавляющий пользователя в черный список
 */
public class BlackListAddRequest extends ApiRequest {
    public static final String SERVICE_NAME = "blacklist.add";
    /**
     * id пользователя, котогорого нужно добавить в черный список
     */
    private final List<Integer> mUserIds;

    /**
     * @param userId пользователя, которого нужно добавить в черный список
     */
    public BlackListAddRequest(int userId, Context context) {
        super(context);
        List<Integer> list = new ArrayList<>();
        list.add(userId);
        mUserIds = list;
        callback(new AttitudeHandler(getContext(), ContainerActivity.ActionTypes.BLACK_LIST, userId, true));
    }

    /**
     * @param userIds пользователи, которых нужно добавить в черный список
     */
    public BlackListAddRequest(List<Integer> userIds, Context context) {
        super(context);
        mUserIds = userIds;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("userIds", new JSONArray(mUserIds));
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
            handleFail(ErrorCodes.ERRORS_PROCCESED, "User list for delete from black list is empty");
        }
    }
}