package com.topface.topface.requests;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;

import java.util.List;

public class DeleteLikesRequest extends DeleteFeedsRequest {
    private static final String SERVICE = "like.delete";

    public DeleteLikesRequest(List<String> ids, Context context) {
        super(ids, context);
    }

    public DeleteLikesRequest(String id, Context context) {
        super(id, context);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

    @Override
    protected String getFeedType() {
        return "Likes";
    }
}
