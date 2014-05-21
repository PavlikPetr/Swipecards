package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.requests.handlers.AttitudeHandler;
import com.topface.topface.ui.ContainerActivity;

import java.util.List;

public class DeleteBookmarksRequest extends DeleteAbstractUsersRequest {
    public static String SERVICE_NAME = "bookmark.delete";

    public DeleteBookmarksRequest(List<String> ids, Context context) {
        super(ids, context);
    }

    public DeleteBookmarksRequest(int id, Context context) {
        super(Integer.toString(id), context);
        callback(new AttitudeHandler(getContext(), ContainerActivity.ActionTypes.BOOKMARK, id, false));
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}