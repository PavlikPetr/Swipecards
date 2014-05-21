package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.requests.handlers.AttitudeHandler;
import com.topface.topface.ui.ContainerActivity;

import java.util.List;

public class DeleteBlackListRequest extends DeleteAbstractUsersRequest {
    private static final String SERVICE = "blacklist.delete";

    public DeleteBlackListRequest(List<String> ids, Context context) {
        super(ids, context);
    }

    public DeleteBlackListRequest(int id, Context context) {
        super(Integer.toString(id), context);
        callback(new AttitudeHandler(getContext(), ContainerActivity.ActionTypes.BLACK_LIST, id, false));
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }
}
