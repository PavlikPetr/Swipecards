package com.topface.topface.requests;

import android.content.Context;

import java.util.List;

public class DeleteBlackListRequest extends DeleteAbstractUsersRequest {
    private static final String SERVICE = "blacklist.delete";

    public DeleteBlackListRequest(List<String> ids, Context context) {
        super(ids, context);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }
}
