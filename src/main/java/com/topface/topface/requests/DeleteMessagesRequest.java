package com.topface.topface.requests;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;

import java.util.List;

public class DeleteMessagesRequest extends DeleteFeedsRequest {
    private static final String SERVICE = "message.delete";

    public DeleteMessagesRequest(List<String> ids, Context context) {
        super(ids, context);
    }

    public DeleteMessagesRequest(String id, Context context) {
        super(id, context);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

    @Override
    protected String getFeedType() {
        return "Messages";
    }
}
