package com.topface.topface.requests;

import android.content.Context;

import java.util.List;

public class DeleteDialogsRequest extends DeleteAbstractUsersRequest {
    private static final String SERVICE_NAME = "dialog.delete";

    public DeleteDialogsRequest(List<String> ids, Context context) {
        super(ids, context);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

}