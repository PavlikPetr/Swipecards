package com.topface.topface.requests;

import android.content.Context;

public class DeleteMessagesRequest extends DeleteAbstractFeedsRequest {
    private static final String SERVICE = "message.delete";

    public DeleteMessagesRequest(String id, Context context) {
        super(id, context);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

}
