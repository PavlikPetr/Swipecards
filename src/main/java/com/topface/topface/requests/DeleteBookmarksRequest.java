package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler;

import java.util.ArrayList;
import java.util.List;

public class DeleteBookmarksRequest extends DeleteAbstractUsersRequest {
    public static String SERVICE_NAME = "bookmark.delete";

    public DeleteBookmarksRequest(List<String> userIds, Context context) {
        super(userIds, context);
        ArrayList<Integer> ids = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            ids.add(Integer.parseInt(userIds.get(i)));
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