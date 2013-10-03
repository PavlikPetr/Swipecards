package com.topface.topface.requests;

import android.content.Context;

import java.util.List;

public class DeleteGiftsRequest extends DeleteFeedsRequest {
    private static final String SERVICE = "gift.delete";

    public DeleteGiftsRequest(List<String> ids, Context context) {
        super(ids, context);
    }

    public DeleteGiftsRequest(String id, Context context) {
        super(id, context);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

    @Override
    protected String getFeedType() {
        return "Gifts";
    }
}
