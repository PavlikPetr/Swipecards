package com.topface.topface.requests;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;

import java.util.List;

public class DeleteMutualsRequest extends DeleteFeedsRequest {
    private static final String SERVICE = "mutual.delete";

    public DeleteMutualsRequest(List<String> ids, Context context) {
        super(ids, context);
    }

    public DeleteMutualsRequest(String id, Context context) {
        super(id, context);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

    @Override
    protected String getFeedType() {
        return "Mutuals";
    }
}
