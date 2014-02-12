package com.topface.topface.requests;

import android.content.Context;

import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class DeleteGiftsRequest extends DeleteAbstractFeedsRequest {
    private static final String SERVICE = "gift.delete";

    public DeleteGiftsRequest(List<String> ids, Context context) {
        super(ids, context);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

}
