package com.topface.topface.requests;

import android.content.Context;

import java.util.List;

public class DeleteLikesRequest extends DeleteAbstractFeedsRequest {
    private static final String SERVICE = "like.delete";

    public DeleteLikesRequest(List<String> ids, Context context) {
        super(ids, context);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

}
