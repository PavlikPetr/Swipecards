package com.topface.topface.requests;

import android.content.Context;

import java.util.List;

public class DeleteVisitorsRequest extends DeleteAbstractUsersRequest {
    private static final String SERVICE = "visitor.delete";

    public DeleteVisitorsRequest(List<String> ids, Context context) {
        super(ids, context);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

    @Override
    protected String getKeyForItems() {
        return "ids";
    }
}
