package com.topface.topface.requests;

import android.content.Context;

import java.util.List;

public class DeleteAdmirationsRequest extends DeleteAbstractFeedsRequest {
    private static final String SERVICE = "admiration.delete";

    public DeleteAdmirationsRequest(List<String> ids, Context context) {
        super(ids, context);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

}
