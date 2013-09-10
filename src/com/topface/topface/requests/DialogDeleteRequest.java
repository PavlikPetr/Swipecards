package com.topface.topface.requests;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DialogDeleteRequest extends ApiRequest {
    private static final String SERVICE_NAME = "dialogDelete";
    private final List<Integer> mUsersIds;

    public DialogDeleteRequest(List<Integer> usersIds, Context context) {
        super(context);
        mUsersIds = usersIds;
    }

    public DialogDeleteRequest(int userId, Context context) {
        super(context);
        List<Integer> list = new ArrayList<Integer>();
        list.add(userId);
        mUsersIds = list;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("userid", new JSONArray(mUsersIds));
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public void exec() {

        if (mUsersIds != null && mUsersIds.size() > 0) {
            super.exec();
        } else {
            handleFail(ApiResponse.ERRORS_PROCCESED, "User list for delete from black list is empty");
        }
    }


}