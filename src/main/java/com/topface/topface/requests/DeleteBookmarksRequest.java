package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler;

import java.util.List;

public class DeleteBookmarksRequest extends DeleteAbstractUsersRequest {
    public static String SERVICE_NAME = "bookmark.delete";

    public DeleteBookmarksRequest(List<String> userIds, Context context) {
        super(userIds, context);
        int[] ids = new int[userIds.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = Integer.parseInt(userIds.get(i));
        }
        callback(new BlackListAndBookmarkHandler(getContext(), BlackListAndBookmarkHandler.ActionTypes.BOOKMARK, ids, false));
    }

    public DeleteBookmarksRequest(int id, Context context) {
        super(Integer.toString(id), context);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}