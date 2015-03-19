package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.requests.handlers.ErrorCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class InviteFacebookFriendsRequest extends ApiRequest {

    public final static String SERVICE_NAME = "virus.inviteFriends";
    private ArrayList<String> mUserIds;


    public InviteFacebookFriendsRequest(Context context, ArrayList<String> userIds) {
        super(context);
        mUserIds = userIds;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("Ids", new JSONArray(mUserIds));    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public void exec() {
        if (mUserIds != null && mUserIds.size() > 0) {
            super.exec();
        } else {
            handleFail(ErrorCodes.ERRORS_PROCCESED, "Invited friends list is empty");
        }
    }

}
